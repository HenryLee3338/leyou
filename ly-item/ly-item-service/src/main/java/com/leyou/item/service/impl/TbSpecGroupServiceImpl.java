package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.mapper.TbSpecGroupMapper;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 规格参数的分组表，每个商品分类下有多个规格参数组 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbSpecGroupServiceImpl extends ServiceImpl<TbSpecGroupMapper, TbSpecGroup> implements TbSpecGroupService {

    @Autowired
    private TbSpecParamService specParamService;

    /**
     * 根据分类id查询规格组
     * SQL: select * from tb_spec_group where cid = ?
     * @param id 分类id
     * @return ResponseEntity<List<SpecGroupDTO>>
     */
    @Override
    public List<SpecGroupDTO> findSpecGroupByCategoryId(Long id) {
        //1.查询条件
        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecGroup::getCid,id);
        List<TbSpecGroup> tbSpecGroupList = this.list(queryWrapper);
        //2.判断集合是否为空
        if (CollectionUtils.isEmpty(tbSpecGroupList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        //3.将tbSpecGroupList转换成specGroupDTOList
        List<SpecGroupDTO> specGroupDTOList = BeanHelper.copyWithCollection(tbSpecGroupList, SpecGroupDTO.class);
        return specGroupDTOList;
    }

    /**
     * 新增规格组
     * @param specGroupDTO 前台传来的数据
     */
    @Override
    @Transactional
    public void insertSpecGroup(SpecGroupDTO specGroupDTO) {
        //1.将specGroupDTO转换成tbSpecGroup
        TbSpecGroup tbSpecGroup = BeanHelper.copyProperties(specGroupDTO, TbSpecGroup.class);
        //2.新增规格组
        this.save(tbSpecGroup);
    }

    /**
     * 根据分类id查询规格组参数数据
     * @param id 分类id
     * @return  List<SpecGroupDTO>
     */
    @Override
    public List<SpecGroupDTO> findSpecGroupWithParamsByCategoryId(Long id) {
        //1.查询对应的SpecGroupDTO集合
        List<SpecGroupDTO> specGroupDTOS = this.findSpecGroupByCategoryId(id);
        //2.给以上每个specGroupDTO的对象中的params赋值
        //如果规格组集合长度为10，就需要查询10此规格组参数，这样的话查询数据库次数过多
//        for (SpecGroupDTO specGroupDTO : specGroupDTOS) {
//            List<SpecParamDTO> specParams = specParamService.findSpecParamByCategoryIdOrGroupId(specGroupDTO.getId(), null, null);
//            specGroupDTO.setParams(specParams);
//        }
        //根据cid查的话只需要查一次数据库，查询后根据groupId分组
        List<SpecParamDTO> specParams = specParamService.findSpecParamByCategoryIdOrGroupId(null, id, null);//查询
        Map<Long, List<SpecParamDTO>> paramMapByGroup = specParams.stream().collect(Collectors.groupingBy(SpecParamDTO::getGroupId));//分组
//        for (SpecGroupDTO specGroupDTO : specGroupDTOS) {//也可采用下面的流式编程
//            List<SpecParamDTO> specParamDTOList = paramMapByGroup.get(specGroupDTO.getId());
//            specGroupDTO.setParams(specParamDTOList);
//        }
        specGroupDTOS = specGroupDTOS.stream().map(group -> {
            List<SpecParamDTO> specParamDTOS = paramMapByGroup.get(group.getId());
            group.setParams(specParamDTOS);
            return group;
        }).collect(Collectors.toList());
        return specGroupDTOS;
    }
}
