package com.leyou.order.service;

import com.leyou.order.dto.OrderDTO;

public interface OrderService {
    Long addOrder(OrderDTO orderDTO, String userId);
}
