package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.entity.*;
import com.leyou.item.service.*;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_DOWN_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_UP_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbSpuService spuService;

    @Autowired
    private TbCategoryService categoryService;

    @Autowired
    private TbBrandService brandService;

    @Autowired
    private TbSpuDetailService spuDetailService;

    @Autowired
    private TbSkuService skuService;

    @Autowired
    private TbSkuService tbSkuService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

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
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
        //1.保存spu数据
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        spuService.save(tbSpu);
        //2.保存tbSpuDetail数据
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
        tbSpuDetail.setSpuId(tbSpu.getId());//tbSpuDetail中没有tbSpu的id
        spuDetailService.save(tbSpuDetail);
        //3.保存tbSku数据
        List<TbSku> tbSkus = BeanHelper.copyWithCollection(spuDTO.getSkus(), TbSku.class);
        for (TbSku sku : tbSkus) {
            sku.setSpuId(tbSpu.getId());//sku中没有tbSpu的id
        }
        skuService.saveBatch(tbSkus);//批量保存，执行的是一个sql语句
    }

    /**
     * 修改商品信息
     * 需要涉及三张表的数据 tb_spu, tb_spu_detail, tb_sku
     * spu和spuDetail直接更新，sku先删除后新增
     * @param spuDTO 前台传来的数据
     */
    @Override
    @Transactional
    public void updateGoods(SpuDTO spuDTO) {
        //1.修改spu数据
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        spuService.updateById(tbSpu);
        //2.修改tbSpuDetail数据
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
        spuDetailService.updateById(tbSpuDetail);
        //3.修改tbSku数据，先删除后新增
        //删除
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,tbSpu.getId());
        skuService.remove(queryWrapper);
        //新增
        List<TbSku> tbSkus = BeanHelper.copyWithCollection(spuDTO.getSkus(), TbSku.class);
        for (TbSku sku : tbSkus) {
            sku.setSpuId(tbSpu.getId());//sku中没有tbSpu的id
        }
        skuService.saveBatch(tbSkus);//批量保存，执行的是一个sql语句
    }

    /**
     * 根据spuId查询Spu对象
     * @param id
     * @return ResponseEntity<SpuDTO>
     */
    @Override
    public SpuDTO findSpuBySpuid(Long id) {
        //1.查询
        TbSpu tbSpu = spuService.getById(id);
        //2.转换
        SpuDTO spuDTO = BeanHelper.copyProperties(tbSpu, SpuDTO.class);
        return spuDTO;
    }

    /**
     * 根据skuId集合查询sku集合
     * @param ids skuId集合
     * @return sku集合
     */
    @Override
    public List<SkuDTO> findSkuListBySkuIds(List<Long> ids) {
        //1.查询
        Collection<TbSku> tbSkus = skuService.listByIds(ids);
        //2.转换
        List<SkuDTO> skuDTOS = tbSkus.stream().map(tbSku -> {
            return BeanHelper.copyProperties(tbSku, SkuDTO.class);
        }).collect(Collectors.toList());
        return skuDTOS;
    }

    /**
     * 修改商品上下架
     * @param id 商品id
     * @param saleable 上架还是下架
     */
    @Override
    @Transactional
    public void updateSaleable(Long id, Boolean saleable) {
        //1.修改saleable状态
        UpdateWrapper<TbSpu> updateWrapper = new UpdateWrapper();
        updateWrapper.lambda().eq(TbSpu::getId,id);
        updateWrapper.lambda().set(TbSpu::getSaleable,saleable);
        boolean isUpdate = spuService.update(updateWrapper);
        if (!isUpdate){//如果失败
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //2.修改spu下的sku的enable状态
        UpdateWrapper<TbSku> updateWrapper1 = new UpdateWrapper<>();
        updateWrapper1.lambda().eq(TbSku::getSpuId,id);
        updateWrapper1.lambda().set(TbSku::getEnable,saleable);
        boolean isUpdate1 = skuService.update(updateWrapper1);
        if (!isUpdate1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //3.向MQ发送消息
        String tag = saleable?ITEM_UP_TAGS:ITEM_DOWN_TAGS; //如果saleable是true，上架，反之下架
        rocketMQTemplate.convertAndSend(ITEM_TOPIC_NAME + ":" + tag,id);//topic:tag  一级分类:二级分类
        System.out.println("上下架成功,MQ消息以发送" + tag);
    }

    /**
     *  根据spuId查询SpuDetail
     * @param id spu的id
     * @return SpuDetailDTO
     */
    @Override
    public SpuDetailDTO findSpuDetailBySpuId(Long id) {
        //1.查询
        //注意：根据id查询的时候，需要在对应的表中对应的属性上加一个@TableId注解
        TbSpuDetail tbSpuDetail = spuDetailService.getById(id);
        if (tbSpuDetail == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //2.将tbSpuDetail转换成spuDetailDTO
        SpuDetailDTO spuDetailDTO = BeanHelper.copyProperties(tbSpuDetail, SpuDetailDTO.class);
        return spuDetailDTO;
    }

    /**
     * 根据spuId查询Sku集合
     * @param id spuId
     * @return List<SkuDTO>
     */
    @Override
    public List<SkuDTO> findSkuListBySpuId(Long id) {
        //1.查询
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,id);
        List<TbSku> tbSkuList = tbSkuService.list(queryWrapper);
        if (CollectionUtils.isEmpty(tbSkuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //2.转换
        List<SkuDTO> skuDTOList = BeanHelper.copyWithCollection(tbSkuList, SkuDTO.class);
        return skuDTOList;
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
