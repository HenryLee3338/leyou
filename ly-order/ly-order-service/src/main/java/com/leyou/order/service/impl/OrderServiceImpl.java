package com.leyou.order.service.impl;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.service.OrderService;
import com.leyou.order.service.TbOrderDetailService;
import com.leyou.order.service.TbOrderLogisticsService;
import com.leyou.order.service.TbOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderService orderService;

    @Autowired
    private TbOrderDetailService orderDetailService;

    @Autowired
    private TbOrderLogisticsService orderLogisticsService;

    /**
     * 新增订单，减库存
     * @param orderDTO 订单实体类
     * @param userId 用户id
     * @return 订单id
     * 订单表-----------------------------------------------------------------------------------------------------------
     * 暂不考虑的字段: post_fee(邮费),invoice_type(发票类型),source_type(订单来源app,pc,微信),time(各种时间)
     * 页面传递的字段: payment_type(付费类型)
     * 后台赋值的字段: orderId(订单id),total_fee(总费用),userId(用户id),status(订单状态付款,未付款),b_type
     * 订单详细表-------------------------------------------------------------------------------------------------------
     * 暂不考虑的字段: id(订单详细id),time
     * 页面传递的字段: skuId,num(数量)
     * 后台赋值的字段: orderId(订单id),title(商品标题),own_spec,price,image
     * 物流信息表-------------------------------------------------------------------------------------------------------
     * 暂时不做
     */
    @Override
    public Long addOrder(OrderDTO orderDTO, String userId) {
        //1.保存订单表

        //2.保存订单详情表

        //3.保存物流信息

        return null;
    }
}
