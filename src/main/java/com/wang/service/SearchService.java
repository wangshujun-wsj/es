package com.wang.service;


import com.wang.bo.SearchBO;
import com.wang.domain.Product;
import com.wang.vo.BrandVO;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;

import java.util.List;

/**
 * @Description:
 * @Author: wangshujun
 */
public interface SearchService {

    /**
     * 添加数据
     * @author wangshujun
     * @return void
     */
    void add(Product product);

    /**
     * 从es 中删除数据
     * @author wangshujun
     * @param  id
     * @return void
     */
    void delete(Long id);

    /**
     *  从es 中批量删除数据
     * @author wangshujun
     * @param ids
     * @return void
     */
    void batchDelete(List<Long> ids);

    /**
     * 从es 中删除索引()所有数据都删除
     * @author wangshujun
     * @return void
     */
    void deleteIndex();

    /**
     * 全文检索商品中的品牌列表
     * @author wangshujun
     * @param bo
     */
    List<BrandVO> findBrands(SearchBO bo);

    /**
     * 全文检索
     * @author wangshujun
     * @param bo
     * @param page
     * @param size
     * @return PageVO<SearchVO>
     */
    AggregatedPage<Product> findProduct(SearchBO bo, int page, int size);
}
