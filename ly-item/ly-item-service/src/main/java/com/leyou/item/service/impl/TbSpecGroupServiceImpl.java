package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.mapper.TbSpecGroupMapper;
import com.leyou.item.service.TbSpecGroupService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
}
