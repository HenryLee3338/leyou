package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.TbBrand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 Mapper 接口
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbBrandMapper extends BaseMapper<TbBrand> {

    @Select("select b.* from tb_brand b, tb_category_brand cb where b.id = cb.brand_id and cb.category_id = #{id}")
    List<TbBrand> findBrandListByCategoryId(@Param("id") Long id);
}
