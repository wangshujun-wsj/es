package com.wang.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * elasticsearch 7 type 去掉了, 不能随意设置了
 * @Description:
 * @Author: wangshujun
 * @Date: 2020/8/7 19:48
 */
@Data
@Document(indexName = "product", type = "_doc")
@Accessors(chain = true)
public class Product implements Serializable {

    private static final long serialVersionUID = 448645216356884525L;
    /**
     * 商品id
     */
    @Id
    @Field(type = FieldType.Long, index = false)
    private Long id;

    /**
     * 一级分类id
     */
    @Field(type = FieldType.Long)
    private Long firstClassifyId;

    /**
     * 二级分类id
     */
    @Field(type = FieldType.Long)
    private Long secondClassifyId;

    /**
     * 三级分类id
     */
    @Field(type = FieldType.Keyword)
    private Long thirdClassifyId;

    /**
     * 品牌id
     */
    @Field(type = FieldType.Keyword)
    private Long brandId;

    /**
     * 品牌name
     */
    @Field(type = FieldType.Text, fielddata = true)
    private String brandName;

    /**
     * 商品标题, 用来分词查询
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 价格
     */
    @Field(type = FieldType.Double)
    private BigDecimal price;

    /**
     * 总销量
     */
    @Field(type = FieldType.Long)
    private Long salesVolume;

    /**
     *  创建时间
     */
    @Field(type = FieldType.Date, index = false)
    @JsonFormat(pattern = "yyyy-MM-dd\'T\'HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd\'T\'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
}
