package com.wang.enums;

/**
 * @Description: 排序 Enum
 * @Author: wangshujun
 * @Date: 2020/7/27 16:06
 */
public enum SortEnum {
    /**
     * 0 综合
     */
    COMPOSITE(0,"createTime"),
    /**
     * 1 新品
     */
    NEW_PRODUCT(1,"createTime"),
    /**
     * 2 销量
     */
    SALES_VOLUME(2,"totalSale"),
    /**
     * 3 价格
     */
    PRICE(3,"price");

    /**
     * code码
     */
    private int code;

    /**
     * 对应默认排序字段
     */
    private String value;

    SortEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
