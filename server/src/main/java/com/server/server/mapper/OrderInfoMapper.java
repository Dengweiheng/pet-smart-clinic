package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品交易订单主表 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对订单主表 (order_info) 的数据操作。
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
}

