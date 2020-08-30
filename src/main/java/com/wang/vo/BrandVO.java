package com.wang.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Description:
 * @Author: wangshujun
 */
@Data
@Accessors(chain = true)
public class BrandVO implements Serializable {

    private static final long serialVersionUID = -696251606279377411L;
    /**
     * 品牌id
     */
    private Long id;

    /**
     * 品牌名称
     */
    private String brandName;
}
