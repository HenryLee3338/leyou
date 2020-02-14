package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.item.entity.TbSpu;
import com.leyou.item.mapper.TbSpuMapper;
import com.leyou.item.service.TbSpuService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * spu表，该表描述的是一个抽象性的商品，比如 iphone8 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbSpuServiceImpl extends ServiceImpl<TbSpuMapper, TbSpu> implements TbSpuService {

}
