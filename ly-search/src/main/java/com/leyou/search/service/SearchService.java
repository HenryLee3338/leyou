package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.entity.Goods;

import java.util.List;
import java.util.Map;

public interface SearchService {

    Goods buildGoods(SpuDTO spu);

    PageResult<GoodsDTO> findGoodsByPage(SearchRequest searchRequest);

    Map<String, List<?>> filter(SearchRequest searchRequest);
}
