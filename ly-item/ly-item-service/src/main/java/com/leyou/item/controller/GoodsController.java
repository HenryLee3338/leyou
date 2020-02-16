package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.service.GoodsService;
import com.leyou.item.service.impl.GoodsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//这里不加@RquestMapping,因为前缀各不相同
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询spu
     * @param page 当前页码
     * @param rows 每页条数
     * @param key 关键字(搜索)
     * @param saleable 是否上架
     * @return ResponseEntity<PageResult<SpuDTO>>
     */
    @RequestMapping(value = "/spu/page",name = "分页查询spu数据")
    public ResponseEntity<PageResult<SpuDTO>> findSpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable
    ){
        PageResult<SpuDTO> pageResult = goodsService.findSpuByPage(page,rows,key,saleable);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 新增保存商品信息
     * @param spuDTO 前台传来的信息是json格式，需要加@RequestBody才能接收到数据
     */
    @PostMapping(value = "/goods",name = "保存商品信息")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO){
        goodsService.saveGoods(spuDTO);
        return ResponseEntity.ok().build();
    }

}
