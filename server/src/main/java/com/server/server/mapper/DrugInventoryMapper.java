package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.DrugInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品库存数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对药品库存表 (drug_inventory) 的增删改查及库存扣减/入库操作。
 */
@Mapper
public interface DrugInventoryMapper extends BaseMapper<DrugInventory> {
}

