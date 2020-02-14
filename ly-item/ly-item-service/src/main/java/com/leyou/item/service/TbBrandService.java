package com.leyou.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.TbBrand;

import java.util.List;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbBrandService extends IService<TbBrand> {

    PageResult<BrandDTO> findByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc);

    void insertBrand(BrandDTO brand, List<Long> cids);

    void updateBrand(BrandDTO brand, List<Long> cids);
}
