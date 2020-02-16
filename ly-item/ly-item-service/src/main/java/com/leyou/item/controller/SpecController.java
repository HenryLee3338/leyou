package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//包含规格组和规格参数

@RestController
@RequestMapping("/spec")
public class SpecController {

    @Autowired
    private TbSpecGroupService specGroupService;
    @Autowired
    private TbSpecParamService specParamService;

    /**
     * 根据分类id查询规格组
     * @param id 分类id
     * @return ResponseEntity<List<SpecGroupDTO>>
     */
    @GetMapping(value = "/groups/of/category",name = "根据分类id查询规格组")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCategoryId(@RequestParam("id") Long id){
        List<SpecGroupDTO> specGroupDTOList = specGroupService.findSpecGroupByCategoryId(id);
        return ResponseEntity.ok(specGroupDTOList);
    }

    /**
     * 根据分类id或组id查询规格参数
     * @param gid 规格组id
     * @param cid 分类id
     * @param searching 是否查询
     * @return ResponseEntity<List<SpecParamDTO>>
     */
    @GetMapping(value = "/params",name = "根据分类id或组id查询规格参数")
    public ResponseEntity<List<SpecParamDTO>> findSpecParamByCategoryIdOrGroupId(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching
    ){
        List<SpecParamDTO> specParamDTOList = specParamService.findSpecParamByCategoryIdOrGroupId(gid,cid,searching);
        return ResponseEntity.ok(specParamDTOList);
    }

    /**
     * 新增规格组
     * @param specGroupDTO 因为前台传来的是一个json数据，所以加@RequestBody才能接收到数据
     */
    @PostMapping(value = "/group",name = "新增规格组")
    public ResponseEntity<Void> insertSpecGroup(@RequestBody SpecGroupDTO specGroupDTO){
        specGroupService.insertSpecGroup(specGroupDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 新增规格参数
     * @param specParamDTO  因为前台传来的是一个json数据，所以加@RequestBody才能接收到数据
     */
    @PostMapping(value = "/param",name = "新增规格参数")
    public ResponseEntity<Void> insertSpecParam(@RequestBody SpecParamDTO specParamDTO){
        System.out.println(specParamDTO + "----------------------------");
        specParamService.insertSpecParam(specParamDTO);
        return ResponseEntity.ok().build();//无返回而结果
    }

}
