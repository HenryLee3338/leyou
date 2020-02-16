package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;

public interface GoodsService {

    public PageResult<SpuDTO> findSpuByPage(Integer page, Integer rows, String key, Boolean saleable);


    void saveGoods(SpuDTO spuDTO);
}
