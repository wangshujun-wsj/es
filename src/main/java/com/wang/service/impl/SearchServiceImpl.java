package com.wang.service.impl;


import com.alibaba.fastjson.JSON;
import com.wang.bo.SearchBO;
import com.wang.domain.Product;
import com.wang.enums.SortEnum;
import com.wang.service.SearchService;
import com.wang.vo.BrandVO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @Description:
 * @Author: wangshujun
 */
@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    /**
     * brandId
     */
    private static final String BRAND_ID = "brandId";

    /**
     * 高亮字段
     */
    private static final  String HIGHLIGHT_FILED = "title";

    @Override
    public void add(Product product) {
        product.setCreateTime(LocalDateTime.now());
        IndexQuery indexQuery = new IndexQueryBuilder().withObject(product).build();
        esTemplate.index(indexQuery);
    }

    /**
     * 从es 中删除数据
     * @author wangshujun
     * @param  id es中的id, 对应 tb_Product_sale 主键
     * @return void
     */
    @Override
    public void delete(Long id) {
        esTemplate.delete(Product.class, id.toString());
    }

    /**
     *  从es 中批量删除数据
     * @author wangshujun
     * @param  ids es中的id, 对应 tb_Product_sale 主键
     * @return void
     */
    @Override
    public void batchDelete(List<Long> ids) {
        ids.forEach(this::delete);
    }

    /**
     * 根据es 主键更新es数据
     * @author wangshujun
     * @param params
     * @param product
     * @return void
     */
    private void updateById(Map<String, Object> params, Product product) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.doc(params);
        UpdateQueryBuilder updateQueryBuilder = new UpdateQueryBuilder();
        updateQueryBuilder.withId(product.getId() + "");
        updateQueryBuilder.withUpdateRequest(updateRequest);
        updateQueryBuilder.withClass(Product.class);
        UpdateQuery updateQuery = updateQueryBuilder.build();
        esTemplate.update(updateQuery);
    }

    /**
     * 从es 中删除索引()所有数据都删除
     * @author wangshujun
     * @return void
     */
    @Override
    public void deleteIndex() {
        esTemplate.deleteIndex("product");
    }

    /**
     * 全文检索商品中的品牌列表(只有根据品类进行查询)
     * @author wangshujun
     * @param bo
     * @return List<BrandVO>
     */
    @Override
    public List<BrandVO> findBrands(SearchBO bo) {


        BoolQueryBuilder builder = QueryBuilders.boolQuery();

        // 设置要查询的索引库的名称和类型
        NativeSearchQueryBuilder query = setIndexAndType();


        queryClassify(bo, builder);

        // 这两句用于去重, 只返回去重后的brandId字段数据
        TermsAggregationBuilder field = AggregationBuilders.terms("agg").field(BRAND_ID).size(20);
        // 设置值返回 brandId
        SourceFilter sourceFilter = new FetchSourceFilter(new String[]{BRAND_ID}, new String[]{});

        NativeSearchQuery searchQuery = query.withQuery(builder)
                .withSourceFilter(sourceFilter)
                .addAggregation(field)
                .build();

        Aggregations aggregations = esTemplate.query(searchQuery, SearchResponse::getAggregations);

        //转换成map集合
        Map<String, Aggregation> aggregationMap = aggregations.asMap();

        //获得对应的聚合函数的聚合子类，该聚合子类也是个map集合,里面的value就是桶Bucket，我们要获得Bucket
        LongTerms longTerms = (LongTerms) aggregationMap.get("agg");
        //获得所有的桶
        List<LongTerms.Bucket> buckets = longTerms.getBuckets();
        //将集合转换成迭代器遍历桶,当然如果你不删除buckets中的元素，直接foreach遍历就可以了
        Iterator<LongTerms.Bucket> iterator = buckets.iterator();
        List<Long> ids = new ArrayList<>();
        List<BrandVO> brandVOList = new ArrayList<> ();
        while(iterator.hasNext()) {
            //bucket桶也是一个map对象，我们取它的key值就可以了
            String brandId = iterator.next().getKeyAsString();
            // 添加存储数据的集合
            ids.add(Long.parseLong(brandId));
            BrandVO vo = new BrandVO();
            vo.setId(Long.parseLong(brandId));
            brandVOList.add(vo);
        }
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return brandVOList;
    }

    /**
     * 全文检索
     * @author wangshujun
     * @param bo
     * @param page
     * @param size
     * @return AggregatedPage<Product>
     */
    @Override
    public AggregatedPage<Product> findProduct(SearchBO bo, int page, int size) {


        // PageRequest 分页从 0 开始
        page--;
        Pageable pageable = PageRequest.of(page, size);

        // 处理排序
        SortBuilder<FieldSortBuilder> sortBuilder = setProductSort(bo);


        NativeSearchQuery query = query(pageable, sortBuilder, bo);
        AggregatedPage<Product> highLightList = getHighLightList(query);
        // 列表数据
        System.out.println("列表数据  " + highLightList.getContent());
        // 总页数
        System.out.println("总页数  " + highLightList.getTotalPages());
        // 总数
        System.out.println("总数  " + highLightList.getTotalElements());
        return getHighLightList(query);
    }

    /**
     * 设置要查询的索引库的名称和类型
     * @author wangshujun
     * @return PageInfo<Product>
     */
    private NativeSearchQueryBuilder setIndexAndType() {
        NativeSearchQueryBuilder query = new NativeSearchQueryBuilder();
        // 指定要查询的索引库的名称和类型，其实就是我们文档@Document中设置的indedName和type
        query.withIndices("product").withTypes("_doc");
        return query;
    }

    /**
     * 查询条件
     * @author wangshujun
     * @param pageable, sortBuilder, bo
     * @return org.springframework.data.elasticsearch.core.query.NativeSearchQuery
     */
    private NativeSearchQuery query(Pageable pageable, SortBuilder<FieldSortBuilder> sortBuilder, SearchBO bo) {
        // 前端传的模糊查询条件
        String keywords = bo.getCondition() != null ? bo.getCondition() : "";

        // 高亮标签, class 前端用来设置样式
        String preTag = "<span class='highlight'>";
        String postTag = "</span>";

        // 多字段查询
        String[] keys = new String[]{"title", "brandName"};

        BoolQueryBuilder builder = QueryBuilders.boolQuery();


        // 查询全部
        MatchAllQueryBuilder matchAll = QueryBuilders.matchAllQuery();
        // 多字段查询
        MultiMatchQueryBuilder multiMatch = QueryBuilders.multiMatchQuery(keywords, keys);

        // 设置要查询的索引库的名称和类型
        NativeSearchQueryBuilder query = setIndexAndType();

        // 查询, 如果条件为空, 则查询全部, 如果条件有值, 则多字段进行查询
        builder.must(StringUtils.isEmpty(keywords) ? matchAll : multiMatch);

        queryClassify(bo, builder);
        queryProduct(bo, builder);

        return query
                .withQuery(builder)
                // 高亮
                .withHighlightFields(new HighlightBuilder.Field(HIGHLIGHT_FILED)
                        .preTags(preTag)
                        .postTags(postTag)
                )
                // 排序
                .withSort(sortBuilder)
                // 分页
                .withPageable(pageable)
                .build();

    }

    /**
     * 高亮、分页
     * @author wangshujun
     * @param query  查询条件
     * @return org.springframework.data.elasticsearch.core.aggregation.AggregatedPage<com.rocwo.rwshop.bo.Product>
     */
    private AggregatedPage<Product> getHighLightList(NativeSearchQuery query) {
        return esTemplate.queryForPage(query, Product.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {

                List<Product> itemHighLightList = new ArrayList<>();
                SearchHits hits = response.getHits();
                for (SearchHit h : hits) {
                    Map<String, Object> map = h.getSourceAsMap();
                    HighlightField highlightField = h.getHighlightFields().get(HIGHLIGHT_FILED);
                    String title;
                    if (highlightField != null) {
                        // 高亮内容
                        title = highlightField.getFragments()[0].toString();
                    } else {
                        title = map.get(HIGHLIGHT_FILED).toString();
                    }

                    Product Product = JSON.parseObject(JSON.toJSONString(map), Product.class);
                    Product.setTitle(title);
                    itemHighLightList.add(Product);
                }

                return new AggregatedPageImpl<>((List<T>)itemHighLightList,
                        pageable,
                        response.getHits().totalHits);
            }
        });
    }

    /**
     * 处理排序
     * @author wangshujun
     * @param bo
     * @return org.elasticsearch.search.sort.SortBuilder
     */
    private SortBuilder<FieldSortBuilder> setProductSort(SearchBO bo) {

        SortOrder order = getSortOrder(bo.getSortType());
        int sort = getSort(bo.getSort());
        return getSortBuilder(sort, order);
    }

    /**
     * 从枚举中获取排序方式
     * @author wangshujun
     * @param sort
     * @param order
     * @return org.elasticsearch.search.sort.SortBuilder<org.elasticsearch.search.sort.FieldSortBuilder>
     */
    private SortBuilder<FieldSortBuilder> getSortBuilder(int sort, SortOrder order) {
        SortBuilder<FieldSortBuilder> sortBuilder = null;
        //循环枚举类
        for(SortEnum sortEnum: SortEnum.values()){
            if(sort == sortEnum.getCode()){
                sortBuilder = new FieldSortBuilder(sortEnum.getValue()).order(order);
                break;
            }
        }
        return sortBuilder;
    }

    /**
     * 获取 sort 方式, 默认为  0 综合排序
     * @author wangshujun
     * @param sort
     * @return org.elasticsearch.search.sort.SortBuilder
     */
    private Integer getSort(Integer sort) {
        return sort != null ? sort : SortEnum.COMPOSITE.getCode();
    }

    /**
     * 获取 sortType  排序方式 1 asc   2 desc, 默认为 2
     * @author wangshujun
     * @param sortType 排序方式 1 asc   2 desc
     * @return org.elasticsearch.search.sort.SortBuilder
     */
    private SortOrder getSortOrder(Integer sortType) {
        // 排序方式 1 asc   2 desc
        sortType = sortType != null ? sortType : 2;
        return sortType == 2 ? SortOrder.DESC : SortOrder.ASC;
    }

    /**
     * 拼接查询条件
     * @author wangshujun
     * @param builder
     * @param ids
     * @param field 
     * @return void 
     */
    private void queryShould(BoolQueryBuilder builder, List<Long> ids, String field) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 多条使用   should (or)
        ids.forEach(id -> boolQueryBuilder.should(QueryBuilders.termQuery(field, id)));
        // 精准匹配此项 must (and)
        builder.must(boolQueryBuilder);
    }

    /**
     *  一级分类 二级分类 三级分类  的查询
     * @author wangshujun
     * @param bo
     * @param builder  BoolQueryBuilder查询对象
     * @return void
     */
    private void queryClassify(SearchBO bo, BoolQueryBuilder builder) {
        // 精准匹配一级分类id 多个一级之间使用 or
        if (bo.getFirstClassifyIds() != null) {
            queryShould(builder, bo.getFirstClassifyIds(), "firstClassifyId");
        }

        // 精准匹配二级分类id 多个二级之间使用 or
        if (bo.getSecondClassifyIds() != null) {
            queryShould(builder, bo.getSecondClassifyIds(), "secondClassifyId");
        }

        // 精准匹配三级级分类id 多个三级之间使用 or
        if (bo.getThirdClassifyIds() != null) {
            queryShould(builder, bo.getThirdClassifyIds(), "thirdClassifyId");
        }
    }

    /**
     *  商品的查询条件
     * @author wangshujun
     * @param bo
     * @param builder  BoolQueryBuilder查询对象
     * @return void
     */
    private void queryProduct(SearchBO bo, BoolQueryBuilder builder) {
        // 精准匹配品牌id  多个品牌之间使用 or
        if (bo.getBrandIds() != null) {
            queryShould(builder, bo.getBrandIds(), BRAND_ID);
        }

        if (bo.getMaxPrice() != null || bo.getMinPrice() != null) {
            // 价格区间
            builder.must(QueryBuilders.rangeQuery("price").gte(bo.getMinPrice()).lte(bo.getMaxPrice()));
        }
    }
}
