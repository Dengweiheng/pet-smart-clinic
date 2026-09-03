package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.Drug;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物药品信息 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对药品基础信息表 (drug) 的 CRUD 操作。
 */
@Mapper
public interface DrugMapper extends BaseMapper<Drug> {
}

