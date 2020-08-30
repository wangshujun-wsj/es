package com.wang.controller;

import com.wang.bo.SearchBO;
import com.wang.domain.Product;
import com.wang.service.SearchService;
import com.wang.vo.BrandVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description:
 * @Author: wangshujun
 */
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SearchController {

    @Autowired
    private SearchService searchService;


    @PostMapping("/add")
    public void add(@RequestBody Product product) {
        searchService.add(product);
    }

    @DeleteMapping("/deleteIndex")
    public void deleteIndex() {
        searchService.deleteIndex();
    }

    /**
     * 删除数据
     *
     * @param id
     * @return void
     * @author wangshujun
     */
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        searchService.delete(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return void
     * @author wangshujun
     */
    @DeleteMapping("/batchDelete")
    public void batchDelete(@RequestBody List<Long> ids) {
        searchService.batchDelete(ids);
    }

    /**
     * 全文检索商品中的品牌列表(只有根据品类进行查询)
     *
     * @param bo
     * @return List<Goods>
     * @author wangshujun
     */
    @GetMapping("/findBrands")
    public List<BrandVO> findBrands(@RequestBody SearchBO bo) {
        return searchService.findBrands(bo);
    }

    /**
     *
     * @param bo
     * @param page
     * @param size
     * @return com.rocwo.rwshop.commons.web.ServerResponse
     * @author wangshujun
     */
    @GetMapping("/findProduct/{page}/{size}")
    public AggregatedPage<Product> findGoods(@RequestBody SearchBO bo,
                                             @PathVariable int page,
                                             @PathVariable int size) {

        return searchService.findProduct(bo, page, size);
    }

}
