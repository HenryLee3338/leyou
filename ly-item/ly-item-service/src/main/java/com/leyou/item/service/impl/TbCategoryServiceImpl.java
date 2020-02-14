package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.mapper.TbCategoryMapper;
import com.leyou.item.service.TbCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbCategoryServiceImpl extends ServiceImpl<TbCategoryMapper, TbCategory> implements TbCategoryService {

    /**
     * 根据pid获取分类数据
     * @param pid 品牌id
     * @return ResponseEntity<List<CategoryDTO>>
     */
    @Override
    public List<CategoryDTO> findCategoryListByParent(Long pid) {
        //1.根据pid查询
        QueryWrapper<TbCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbCategory::getParentId,pid);
//        queryWrapper.eq("parentId",pid);
        List<TbCategory> tbCategoryList = this.list(queryWrapper);
        //2.判断是否有数据
        if (CollectionUtils.isEmpty(tbCategoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //3.将TbCategoryList转换成CategoryDTOList
        List<CategoryDTO> categoryDTOList = BeanHelper.copyWithCollection(tbCategoryList, CategoryDTO.class);
        return categoryDTOList;
    }

    /**
     * 根据品牌id查询所对应的分类数据
     * SQL: select c.* from tb_category_brand cb , tb_category c where cb.category_id=c.id and cb.brand_id=品牌id
     * @param id 品牌id
     * @return ResponseEntity<List<CategoryDTO>>
     */
    @Override
    public List<CategoryDTO> findCategoryListByBrandId(Long id) {
        //1.通过自定义sql查询，getBaseMapper()是获取相对应的mapper
        List<TbCategory> tbCategoryList = this.getBaseMapper().findCategoryListByBrandId(id);
        //2.判断是否为空
        if (CollectionUtils.isEmpty(tbCategoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //3.将tbCategory转换成CategoryDTO
        List<CategoryDTO> categoryDTOList = BeanHelper.copyWithCollection(tbCategoryList, CategoryDTO.class);
        return categoryDTOList;
    }
}
