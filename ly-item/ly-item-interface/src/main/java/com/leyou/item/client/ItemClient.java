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

    /**
     * 通过品牌id集合查询品牌集合
     * @param ids 这是一个品牌id的集合
     * @return ResponseEntity<List<BrandDTO>> 返回一个品牌集合
     */
    @GetMapping(value = "/brand/list",name = "通过品牌id集合查询品牌集合")
    public List<BrandDTO> findBrandListByBrandIdList(@RequestParam("ids") List<Long> ids);

    /**
     * 根据分类id集合查询分类集合
     * @param ids 分类id的集合
     * @return ResponseEntity<List<CategoryDTO>> 分类集合
     */
    @GetMapping(value = "/category/list",name = "根据分类id集合查询分类集合")
    public List<CategoryDTO> findCategoryListByCategoryIdList(@RequestParam(name = "ids") List<Long> ids);

    /**
     * 根据spuId查询Spu对象
     * @param id 从url路径占位符中取参数必须要用一个@PathVariable注解才能取到
     * @return ResponseEntity<SpuDTO>
     */
    @GetMapping(value = "/spu/{id}",name = "根据spuId查询Spu对象")
    public SpuDTO findSpuBySpuId(@PathVariable("id") Long id);

    /**
     * 通过品牌id查询品牌
     * @param id 这是一个品牌id  从url路径占位符中取参数必须要用一个@PathVariable注解才能取到
     * @return ResponseEntity<BrandDTO> 返回一个品牌集合
     */
    @GetMapping(value = "/brand/{id}",name = "通过品牌id查询品牌")
    public BrandDTO findBrandByBrandId(@PathVariable("id") Long id);

    /**
     * 根据分类id查询规格组参数数据
     * @param id 分类id
     * @return  List<SpecGroupDTO>
     */
    @GetMapping(value = "/spec/of/category",name = "根据分类id查询规格组参数数据")
    public List<SpecGroupDTO> findSpecGroupWithParamsByCategoryId(@RequestParam(value = "id") Long id);
}
