package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.leyou.user.entity.TbUserAddress;
import com.leyou.user.mapper.TbUserAddressMapper;
import com.leyou.user.service.TbUserAddressService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户收货地址表 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbUserAddressServiceImpl extends ServiceImpl<TbUserAddressMapper, TbUserAddress> implements TbUserAddressService {

}
