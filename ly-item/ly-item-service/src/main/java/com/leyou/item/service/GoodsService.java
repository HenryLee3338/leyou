package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;

import java.util.List;

public interface GoodsService {

    PageResult<SpuDTO> findSpuByPage(Integer page, Integer rows, String key, Boolean saleable);

    void saveGoods(SpuDTO spuDTO);

    void updateSaleable(Long id, Boolean saleable);

    SpuDetailDTO findSpuDetailBySpuId(Long id);

    List<SkuDTO> findSkuListBySpuId(Long id);

    void updateGoods(SpuDTO spuDTO);

    SpuDTO findSpuBySpuid(Long id);
}
