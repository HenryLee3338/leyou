package com.leyou.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.TbSpecGroup;

import java.util.List;

/**
 * <p>
 * 规格参数的分组表，每个商品分类下有多个规格参数组 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbSpecGroupService extends IService<TbSpecGroup> {

    List<SpecGroupDTO> findSpecGroupByCategoryId(Long id);
}
