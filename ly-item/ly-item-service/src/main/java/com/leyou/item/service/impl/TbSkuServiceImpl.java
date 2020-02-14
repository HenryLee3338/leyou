package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.item.entity.TbSku;
import com.leyou.item.mapper.TbSkuMapper;
import com.leyou.item.service.TbSkuService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * sku表,该表表示具体的商品实体,如黑色的 64g的iphone 8 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbSkuServiceImpl extends ServiceImpl<TbSkuMapper, TbSku> implements TbSkuService {

}
