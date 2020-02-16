package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.mapper.TbSpecParamMapper;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 规格参数组下的参数名 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbSpecParamServiceImpl extends ServiceImpl<TbSpecParamMapper, TbSpecParam> implements TbSpecParamService {

    /**
     * 根据分类id或组id查询规格参数
     * @param gid 规格组id
     * @param cid 分类id
     * @param searching 是否查询
     * @return ResponseEntity<List<SpecParamDTO>>
     */
    @Override
    public List<SpecParamDTO> findSpecParamByCategoryIdOrGroupId(Long gid, Long cid, Boolean searching) {
        QueryWrapper<TbSpecParam> queryWrapper = new QueryWrapper<>();
        //1.判断gid和cid是否都是空
        if (gid == null && cid == null){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        //2.判断gid是否为空
        if (gid != null){
            queryWrapper.lambda().eq(TbSpecParam::getGroupId,gid);
        }
        //3.判断cid是否为空
        if (cid != null){
            queryWrapper.lambda().eq(TbSpecParam::getCid,cid);
        }
        //4.判断是否搜索
        if (searching != null){
            queryWrapper.lambda().eq(TbSpecParam::getSearching,searching);
        }
        //5.tbSpecParamList转换成specParamTDOList
        List<TbSpecParam> tbSpecParamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(tbSpecParamList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        List<SpecParamDTO> specParamDTOList = BeanHelper.copyWithCollection(tbSpecParamList, SpecParamDTO.class);
        return specParamDTOList;
    }

    /**
     * 新增规格参数
     * @param specParamDTO 前台传来的参数数据
     */
    @Override
    @Transactional
    public void insertSpecParam(SpecParamDTO specParamDTO) {
        //1.将specParamDTO转换成tbSpecParam
        TbSpecParam tbSpecParam = BeanHelper.copyProperties(specParamDTO, TbSpecParam.class);
        //2.将参数信息存储到参数表
        this.save(tbSpecParam);
    }
}
