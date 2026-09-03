package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.LogisticsRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单物流追踪记录 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对物流信息表 (logistics_record) 的 CRUD 操作。
 */
@Mapper
public interface LogisticsRecordMapper extends BaseMapper<LogisticsRecord> {
}

