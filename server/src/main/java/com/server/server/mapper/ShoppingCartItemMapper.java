package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.ShoppingCartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车条目 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对购物车项表 (shopping_cart_item) 的增删改查操作。
 */
@Mapper
public interface ShoppingCartItemMapper extends BaseMapper<ShoppingCartItem> {
}

