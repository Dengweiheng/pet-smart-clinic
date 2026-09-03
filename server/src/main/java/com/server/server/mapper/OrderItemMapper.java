package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细项 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对订单药品条目表 (order_item) 的数据操作。
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}

