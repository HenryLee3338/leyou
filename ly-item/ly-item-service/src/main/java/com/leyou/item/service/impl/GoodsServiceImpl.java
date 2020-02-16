package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.*;
import com.leyou.item.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbSpuService spuService;

    @Autowired
    private TbCategoryService categoryService;

    @Autowired
    private TbBrandService brandService;

    @Autowired
    private TbSpuDetailService tbSpuDetailService;

    @Autowired
    private TbSkuService tbSkuService;

    /**
     * 分页查询spu
     * @param page 当前页码
     * @param rows 每页条数
     * @param key 关键字(搜索)
     * @param saleable 是否上架
     * @return ResponseEntity<PageResult<SpuDTO>>
     */
    public PageResult<SpuDTO> findSpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
        QueryWrapper<TbSpu> queryWrapper = new QueryWrapper<>();
        Page<TbSpu> page1 = new Page(page,rows);//构建分页

        //1.判断key是否为空
        if (StringUtils.isNotBlank(key)){
            queryWrapper.lambda().like(TbSpu::getName,key);
        }
        //2.判断saleable是否为空
        if (saleable != null){
            queryWrapper.lambda().eq(TbSpu::getSaleable,saleable);
        }
        //3.将tbSpuList转换成spuDTOList
        IPage<TbSpu> spuIPage = spuService.page(page1, queryWrapper);
        long total = spuIPage.getTotal();
        List<TbSpu> tbSpuList = spuIPage.getRecords();
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(tbSpuList, SpuDTO.class);
        //4.给每一个对象添加品牌名称和分类名称属性
        for (SpuDTO spuDTO : spuDTOList) {
            handlerCategoryNameAndBrandName(spuDTO);
        }
        return new PageResult(total,spuDTOList);
    }

    /**
     * 新增保存商品信息
     * 需要保存三张表的数据 tb_spu, tb_spu_detail, tb_sku
     * tbSpuDetail和tbSku的数据都在spuDTO当中了
     * @param spuDTO 前台传来的数据
     */
    @Override
    public void saveGoods(SpuDTO spuDTO) {
        //1.保存spu数据
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        spuService.save(tbSpu);
        //2.保存tbSpuDetail数据
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
        tbSpuDetail.setSpuId(tbSpu.getId());//tbSpuDetail中没有tbSpu的id
        tbSpuDetailService.save(tbSpuDetail);
        //3.保存tbSku数据
        List<TbSku> tbSkus = BeanHelper.copyWithCollection(spuDTO.getSkus(), TbSku.class);
        for (TbSku sku : tbSkus) {
            sku.setSpuId(tbSpu.getId());//sku中没有tbSpu的id
        }
        tbSkuService.saveBatch(tbSkus);//批量保存，执行的是一个sql语句
    }

    /**
     * 根据品牌id和分类id查询品牌名称和分类名称
     * @param spuDTO 带有品牌id和分类id属性
     */
    private void handlerCategoryNameAndBrandName(SpuDTO spuDTO){
        //1.通过品牌id查询品牌名称
        Long brandId = spuDTO.getBrandId();//品牌id
        TbBrand brand = brandService.getById(brandId);
        spuDTO.setBrandName(brand.getName());

        //2.通过分类id查询分类名称
        List<Long> categoryIdList = spuDTO.getCategoryIds();//三级分类id的集合
        Collection<TbCategory> categoryCollection = categoryService.listByIds(categoryIdList);//mybatisplus提供的根据id集合查询的方法
        //分类与分类之间用/分割
        String categoryNames = categoryCollection.stream(). //变成流
                map(TbCategory::getName). //获取每个对象的名称
                collect(Collectors.joining("/")); //收集数据并分割
        spuDTO.setCategoryName(categoryNames);
    }
}
