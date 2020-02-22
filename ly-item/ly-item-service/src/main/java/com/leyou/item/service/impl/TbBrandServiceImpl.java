package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.entity.TbCategoryBrand;
import com.leyou.item.mapper.TbBrandMapper;
import com.leyou.item.service.TbBrandService;
import com.leyou.item.service.TbCategoryBrandService;
import com.leyou.item.service.TbCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbBrandServiceImpl extends ServiceImpl<TbBrandMapper, TbBrand> implements TbBrandService {

    @Autowired
    private TbCategoryBrandService categoryBrandService;

    /**
     * 分页查询品牌
     * SQL: select * from where name like 'key' or letter = 'key' order by sortBy desc limit (page-1)*rows,rows
     * SQL: select * from where name like '关键字' or letter = '关键字' order by 排序字段 是否降序 limit (当前页码-1)*每页条数,每页条数
     *
     * @param key    关键字
     * @param page   当前页码
     * @param rows   每页条数
     * @param sortBy 排序字段
     * @param desc   是否降序
     * @return ResponseEntity<PageResult < BrandDTO>>
     */
    @Override
    public PageResult<BrandDTO> findByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        QueryWrapper<TbBrand> queryWrapper = new QueryWrapper<>();
        Page<TbBrand> page1 = new Page<>(page, rows); //构建了分页

        //1.判断key是否有值,如果有值，模糊查询
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.lambda().or().like(TbBrand::getName, key).or().eq(TbBrand::getLetter, key);
        }

        //2.判断降序升序
        if (StringUtils.isNotBlank(sortBy)) {
            if (desc) {
                page1.setDesc(sortBy);
//                queryWrapper.orderByDesc(sortBy);
            } else {
                page1.setAsc(sortBy);
//                queryWrapper.orderByAsc(sortBy);
            }
        }
        IPage<TbBrand> iPage = this.page(page1, queryWrapper);
        //iPage.getTotal(); //总条数
        //iPage.getRecords(); //当前页的数据
        return new PageResult(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * 新增品牌
     * 两张表里要新增数据
     *
     * @param brand 品牌信息
     * @param cids  品牌所属的种类的集合
     */
    @Override
    @Transactional
    public void insertBrand(BrandDTO brand, List<Long> cids) {
        //1.将BrandDTO转成TbBrand
        TbBrand tbBrand = BeanHelper.copyProperties(brand, TbBrand.class);
        //2.将品牌信息存储到品牌表
        this.save(tbBrand);
        //3.将品牌与分类的关联遍历存储到中间表
        for (Long cid : cids) {
            TbCategoryBrand categoryBrand = new TbCategoryBrand();
            categoryBrand.setBrandId(tbBrand.getId());
            categoryBrand.setCategoryId(cid);
            categoryBrandService.save(categoryBrand);
        }
    }

    /**
     * 修改品牌
     *
     * @param brand 品牌信息
     * @param cids  品牌所属的种类的集合
     */
    @Override
    @Transactional
    public void updateBrand(BrandDTO brand, List<Long> cids) {
        Long brandId = brand.getId();
        //1.将BrandDTO转换成TbBrand
        TbBrand tbBrand = BeanHelper.copyProperties(brand, TbBrand.class);
        //2.更新品牌相关数据
        this.updateById(tbBrand);
        //3.删除此品牌之前的分类数据
        QueryWrapper<TbCategoryBrand> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbCategoryBrand::getBrandId, brandId);
        categoryBrandService.remove(queryWrapper);
        //4.将此品牌新的分类数据添加
        for (Long cid : cids) {
            TbCategoryBrand categoryBrand = new TbCategoryBrand();
            categoryBrand.setBrandId(tbBrand.getId());
            categoryBrand.setCategoryId(cid);
            categoryBrandService.save(categoryBrand);
        }
    }

    /**
     * 根据分类id查询品牌数据
     * SQL: select b.* from tb_brand b, tb_category_brand cb where b.id = cb.brand_id and cb.category_id = 分类id
     *
     * @param id 分类id
     * @return List<BrandDTO>
     */
    @Override
    public List<BrandDTO> findBrandListByCategoryId(Long id) {
        //1.通过自定义sql查询，getBaseMapper()是获取相对应的mapper
        List<TbBrand> tbBrandList = this.getBaseMapper().findBrandListByCategoryId(id);
        //2.判断tbBrandList是否为空
        if (CollectionUtils.isEmpty(tbBrandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //3.将tbBrandList转换成brandDTOList
        List<BrandDTO> brandDTOList = BeanHelper.copyWithCollection(tbBrandList, BrandDTO.class);
        return brandDTOList;
    }

    /**
     * 通过品牌id集合查询品牌集合
     *
     * @param ids 这是一个品牌id的集合
     * @return ResponseEntity<List < BrandDTO>> 返回一个品牌集合
     */
    @Override
    public List<BrandDTO> findBrandListByBrandIdList(List<Long> ids) {
        //1.查询
        Collection<TbBrand> tbBrandList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(tbBrandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //2.转换,因为是一个Collection集合，所以无法直接使用BeanHelper
        //因为这里copyProperties需要参数才行，所以没法用::的形式
        //tbBrandList.stream().map(BeanHelper::copyProperties).collect(Collectors.toList());
        List<BrandDTO> brandDTOList = tbBrandList.stream().map(tbBrand -> {
            return BeanHelper.copyProperties(tbBrand, BrandDTO.class);
        }).collect(Collectors.toList());

        return brandDTOList;
    }

    /**
     * 通过品牌id查询品牌
     * @param id 这是一个品牌id
     * @return ResponseEntity<BrandDTO> 返回一个品牌集合
     */
    @Override
    public BrandDTO findBrandByBrandId(Long id) {
        //1.查询
        TbBrand tbBrand = this.getById(id);
        //2.转换
        BrandDTO brandDTO = BeanHelper.copyProperties(tbBrand, BrandDTO.class);
        return brandDTO;
    }
}
