package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电子处方主表 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对电子处方主表 (prescription) 的 CRUD 操作。
 */
@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
}

