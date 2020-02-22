package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.TbBrandService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private TbBrandService brandService;

    /**
     * 分页查询品牌
     * 需求: GET /brand/page?key=&page=1&rows=5&sortBy=id&desc=false
     * 通过feign调用的时候需要@RequestParam
     * required = false表示这个参数不是必须的 ， 默认时true
     * defaultValue表示默认值
     * @param key 关键字
     * @param page 当前页码
     * @param rows 每页条数
     * @param sortBy 排序字段
     * @param desc 是否降序
     * @return ResponseEntity<PageResult<BrandDTO>>
     */
    @GetMapping(value = "/page",name = "分页查询品牌")
    public ResponseEntity<PageResult<BrandDTO>> findByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "10") Integer rows,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "desc",required = true,defaultValue = "false") Boolean desc
            ){
        PageResult<BrandDTO> pageResult = brandService.findByPage(key,page,rows,sortBy,desc);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 新增品牌
     * @param brand 品牌信息
     * @param cids 品牌所属的种类的集合
     * @return ResponseEntity<Void> 无返回值
     */
    @PostMapping(name = "新增品牌")
    public ResponseEntity<Void> insertBrand(BrandDTO brand,@RequestParam("cids") List<Long> cids){
        brandService.insertBrand(brand,cids);
        return ResponseEntity.ok().build();//无返回而结果
    }

    /**
     * 修改品牌
     * @param brand 品牌信息
     * @param cids 品牌所属的种类的集合
     * @return ResponseEntity<Void> 无返回值
     */
    @PutMapping(name = "修改品牌")
    public ResponseEntity<Void> updateBrand(BrandDTO brand,@RequestParam("cids") List<Long> cids){
        brandService.updateBrand(brand,cids);
        return ResponseEntity.ok().build();//无返回而结果
    }


    /**
     * 根据分类id查询品牌数据
     * @param id 分类id
     * @return ResponseEntity<List<BrandDTO>>
     */
    @GetMapping(value = "/of/category",name = "根据分类id查询品牌数据")
    public ResponseEntity<List<BrandDTO>> findBrandListByCategoryId(@RequestParam("id") Long id){
        List<BrandDTO> brandDTOList = brandService.findBrandListByCategoryId(id);
        return ResponseEntity.ok(brandDTOList);
    }

    /**
     * 通过品牌id集合查询品牌集合
     * @param ids 这是一个品牌id的集合
     * @return ResponseEntity<List<BrandDTO>> 返回一个品牌集合
     */
    @GetMapping(value = "/list",name = "通过品牌id集合查询品牌集合")
    public ResponseEntity<List<BrandDTO>> findBrandListByBrandIdList(@RequestParam("ids") List<Long> ids){
        List<BrandDTO> brandList = brandService.findBrandListByBrandIdList(ids);
        return ResponseEntity.ok(brandList);
    }

    /**
     * 通过品牌id查询品牌
     * @param id 这是一个品牌id  从url路径占位符中取参数必须要用一个@PathVariable注解才能取到
     * @return ResponseEntity<BrandDTO> 返回一个品牌集合
     */
    @GetMapping(value = "/{id}",name = "通过品牌id查询品牌")
    public ResponseEntity<BrandDTO> findBrandByBrandId(@PathVariable("id") Long id){
        BrandDTO brandDTO = brandService.findBrandByBrandId(id);
        return ResponseEntity.ok(brandDTO);
    }

}
