package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.service.TbSpecGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//包含规格组和规格参数

@RestController
@RequestMapping("/spec")
public class SpecController {

    @Autowired
    private TbSpecGroupService SpecGroupService;

    /**
     * 根据分类id查询规格组
     * @param id 分类id
     * @return ResponseEntity<List<SpecGroupDTO>>
     */
    @GetMapping(value = "/groups/of/category",name = "根据分类id查询规格组")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCategoryId(@RequestParam("id") Long id){
        List<SpecGroupDTO> specGroupDTOList = SpecGroupService.findSpecGroupByCategoryId(id);
        return ResponseEntity.ok(specGroupDTOList);
    }
}
