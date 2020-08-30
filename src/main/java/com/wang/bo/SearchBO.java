package com.wang.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @Author: wangshujun
 */
@Data
@Accessors(chain = true)
public class SearchBO implements Serializable {

    private static final long serialVersionUID = 726584121470276470L;

    /**
     * 一级分类id
     */
    private List<Long> firstClassifyIds;

    /**
     * 二级分类id
     */
    private List<Long> secondClassifyIds;

    /**
     * 三级分类id
     */
    private List<Long> thirdClassifyIds;

    /**
     * 品牌id
     */
    private List<Long> brandIds;


    /**
     * 模糊搜索的条件
     */
    private String condition;

    /**
     * 排序  0 综合   1 新品    2 销量    3 价格
     */
    private Integer sort;

    /**
     * 排序方式 1 asc   2 desc
     */
    private Integer sortType;

    /**
     * 最低价
     */
    private Double minPrice;

    /**
     * 最高价
     */
    private Double maxPrice;

}
