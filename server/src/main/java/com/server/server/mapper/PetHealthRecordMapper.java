package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.PetHealthRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物健康诊疗档案 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对宠物健康/就诊档案表 (pet_health_record) 的 CRUD 操作。
 */
@Mapper
public interface PetHealthRecordMapper extends BaseMapper<PetHealthRecord> {
}

