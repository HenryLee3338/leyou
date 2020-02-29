package com.leyou.order.controller;

import com.leyou.common.auth.entity.UserHolder;
import com.leyou.common.utils.IdWorker;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.service.OrderService;
import com.leyou.order.service.TbOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderService orderService;

    /**
     * 新增订单
     * @param orderDTO 订单实体类，json，需要@RequestBody才能取得到
     * @return 订单id
     */
    @PostMapping(value = "/order",name = "新增订单")
    public ResponseEntity<Long> addOrder(@RequestBody OrderDTO orderDTO){
        String userId = UserHolder.getUserId();
        Long orderId = orderService.addOrder(orderDTO,userId);
        return ResponseEntity.ok(orderId);
    }
}
