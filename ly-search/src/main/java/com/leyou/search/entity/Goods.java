package com.leyou.search.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;
import java.util.Set;

/**
 * 一个SPU对应一个Goods
 */
@Data
@Document(indexName = "leyou", type = "goods", shards = 1, replicas = 1)
public class Goods {
    @Id
    @Field(type = FieldType.Keyword)
    // spuId
    private Long id;
    @Field(type = FieldType.Keyword, index = false)
    // 副标题，促销信息
    private String subTitle;
    // sku信息的json结构
    @Field(type = FieldType.Keyword, index = false)
    private String skus;
    // 所有需要被搜索的信息，包含标题，分类，甚至品牌
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String all;
    // 品牌id
    private Long brandId;
    // 商品第3级分类id
    private Long categoryId;
    // spu创建时间
    private Long createTime;
    // 价格
    private Set<Long> price;
    // 可搜索的规格参数，key是参数名，value是参数值
    private Map<String, Object> specs;
}