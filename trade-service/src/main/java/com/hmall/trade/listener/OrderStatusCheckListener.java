package com.hmall.trade.listener;

import com.hmall.trade.constants.MqConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStatusCheckListener {
    private final IOrderService orderService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.DELAY_ORDER_QUEUE_NAME,durable = "true"),
            exchange = @Exchange(name = MqConstants.DELAY_EXCHANGE_NAME,delayed = "true"),
            key = MqConstants.DELAY_ROUTING_KEY
    ))
    public void checkOrderStatus(Long orderId){
        //1. 查询本地订单状态
        Order order = orderService.getById(orderId);
        //2. 如果订单为空或者订单状态为已支付，return
        if(order == null || order.getStatus() != 1){
            return;
        }
        // TODO 3.去支付服务查询真正的支付状态
        boolean isPay = true;
        if(isPay){
            //3.1如果为已支付，就修改订单状态
            orderService.markOrderPaySuccess(orderId);
        }else {
            //3.2如果为未支付，取消订单，恢复库存
            orderService.cancelOrder(orderId);
        }

    }
}
