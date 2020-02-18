package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("item-service")
public interface ItemClient {

    /**
     * 根据分类id查询品牌数据
     * @param id 分类id
     * @return ResponseEntity<List<BrandDTO>>
     */
    @GetMapping(value = "/brand/of/category",name = "根据分类id查询品牌数据")
    public List<BrandDTO> findBrandListByCategoryId(@RequestParam("id") Long id);

    /**
     * 分页查询spu
     * @param page 当前页码
     * @param rows 每页条数
     * @param key 关键字(搜索)
     * @param saleable 是否上架
     * @return ResponseEntity<PageResult<SpuDTO>>
     */
    @RequestMapping(value = "/spu/page",name = "分页查询spu数据")
    public PageResult<SpuDTO> findSpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable
    );

    /**
     *  根据spuId查询SpuDetail
     * @param id spu的id
     * @return ResponseEntity<SpuDetailDTO>
     */
    @GetMapping(value = "/spu/detail",name = "根据spuId查询SpuDEtail")
    public SpuDetailDTO findSpuDetailBySpuId(@RequestParam("id") Long id);

    /**
     * 根据spuId查询Sku集合
     * @param id spuId
     * @return ResponseEntity<List<SkuDTO>>
     */
    @GetMapping(value = "/sku/of/spu",name = "根据spuId查询Sku集合")
    public List<SkuDTO> findSkuDTOListBySpuId(@RequestParam("id") Long id);

    /**
     * 根据分类id或组id查询规格参数
     * @param gid 规格组id
     * @param cid 分类id
     * @param searching 是否查询
     * @return ResponseEntity<List<SpecParamDTO>>
     */
    @GetMapping(value = "/spec/params",name = "根据分类id或组id查询规格参数")
    public List<SpecParamDTO> findSpecParamByCategoryIdOrGroupId(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching
    );
}
