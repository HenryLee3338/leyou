package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    SearchService searchService;

    /**
     * 根据查询的需求分页查询对应的商品
     * @param searchRequest 查询需求
     * @return <PageResult<GoodsDTO>>
     */
    @PostMapping(value = "/page",name = "关键字搜索")
    public ResponseEntity<PageResult<GoodsDTO>> findGoodsByPage(@RequestBody SearchRequest searchRequest){
        PageResult<GoodsDTO> pageResult = searchService.findGoodsByPage(searchRequest);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询搜索对应的过滤条件
     * @param searchRequest 查询需求
     * @return <PageResult<GoodsDTO>>
     */
    @PostMapping(value = "/filter",name = "过滤搜索")
    public ResponseEntity<Map<String, List<?>>> filter(@RequestBody SearchRequest searchRequest){
        Map<String, List<?>> filterMap = searchService.filter(searchRequest);
        return ResponseEntity.ok(filterMap);
    }
}
