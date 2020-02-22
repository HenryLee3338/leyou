package com.leyou.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecParam;

import java.util.List;

/**
 * <p>
 * 规格参数组下的参数名 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbSpecParamService extends IService<TbSpecParam> {

    List<SpecParamDTO> findSpecParamByCategoryIdOrGroupId(Long gid, Long cid, Boolean searching);

    void insertSpecParam(SpecParamDTO specParamDTO);


}
