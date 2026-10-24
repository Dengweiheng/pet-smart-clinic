package com.server.server.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.config.RabbitMqConfig;
import com.server.server.entity.DrugInventory;
import com.server.server.entity.OrderInfo;
import com.server.server.entity.OrderItem;
import com.server.server.mapper.DrugInventoryMapper;
import com.server.server.mapper.OrderInfoMapper;
import com.server.server.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单超时取消消费者
 * <p>
 * 监听死信队列 {@link RabbitMqConfig#ORDER_CANCEL_QUEUE}，
 * 对 30 分钟内仍未支付的订单执行取消，并把下单时预扣的库存回补。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutListener {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final DrugInventoryMapper drugInventoryMapper;

    /**
     * 处理订单超时消息
     *
     * @param orderNo 下单时投递的订单编号
     */
    @RabbitListener(queues = RabbitMqConfig.ORDER_CANCEL_QUEUE)
    public void onOrderTimeout(String orderNo) {
        OrderInfo order = orderInfoMapper.selectOne(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));

        // 幂等保护：订单不存在，或已被支付/取消，直接丢弃消息，避免重复回补库存
        if (order == null || !"PENDING_PAY".equals(order.getOrderStatus())) {
            log.info("订单 {} 无需取消，当前状态：{}", orderNo, order == null ? "不存在" : order.getOrderStatus());
            return;
        }

        // 1. 订单状态置为已取消
        order.setOrderStatus("CANCELLED");
        orderInfoMapper.updateById(order);

        // 2. 按下单明细回补库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            DrugInventory inventory = drugInventoryMapper.selectOne(
                    new LambdaQueryWrapper<DrugInventory>().eq(DrugInventory::getDrugId, item.getDrugId()));
            if (inventory != null) {
                inventory.setStock(inventory.getStock() + item.getQuantity());
                drugInventoryMapper.updateById(inventory);
            }
        }

        log.info("订单 {} 超时未支付，已自动取消，回补 {} 个商品的库存", orderNo, items.size());
    }
}
