package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue",durable = "true"), //声明队列
            exchange = @Exchange(name = "pay.direct",type = ExchangeTypes.DIRECT), //声明交换机
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId){
        //1，查询订单
        Order order = orderService.getById(orderId);
        //2.判断订单状态
        if(order == null || order.getStatus() != 1){
            return;
        }
        //3.更新订单状态
        orderService.markOrderPaySuccess(orderId);
    }
}
