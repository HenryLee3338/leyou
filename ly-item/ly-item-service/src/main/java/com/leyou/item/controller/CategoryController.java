package com.leyou.item.controller;


import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.TbCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private TbCategoryService categoryService;

    /**
     * 根据pid获取分类数据
     * //@CrossOrigin({"http://manage.leyou.com","http://www.leyou.com"}) //允许从这个ip过来的请求
     * 通过feign调用的时候需要@RequestParam
     * @param pid 品牌id
     * @return ResponseEntity<List<CategoryDTO>>
     */
    @GetMapping(value = "/of/parent",name = "获取属于这个父id所有分类数据")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByParent(@RequestParam("pid") Long pid){
        List<CategoryDTO> categoryDTOList = categoryService.findCategoryListByParent(pid);
        return ResponseEntity.ok(categoryDTOList);
    }

    /**
     * 根据品牌id查询所对应的分类数据
     * 应用场景: 当点击修改按钮时，回显品牌对应的分类数据
     * @param id 品牌id
     * @return ResponseEntity<List<CategoryDTO>>
     */
    @GetMapping(value = "/of/brand",name = "根据品牌id查询所对应的分类数据")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByBrandId(@RequestParam(name = "id") Long id){
        List<CategoryDTO> categoryDTOList = categoryService.findCategoryListByBrandId(id);
        return ResponseEntity.ok(categoryDTOList);
    }

    /**
     * 根据分类id集合查询分类集合
     * @param ids 分类id的集合
     * @return ResponseEntity<List<CategoryDTO>> 分类集合
     */
    @GetMapping(value = "/list",name = "根据分类id集合查询分类集合")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByCategoryIdList(@RequestParam(name = "ids") List<Long> ids){
        List<CategoryDTO> categoryDTOList = categoryService.findCategoryListByCategoryIdList(ids);
        return ResponseEntity.ok(categoryDTOList);
    }
}
