package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物基础信息 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对宠物信息表 (pet) 的 CRUD 操作。
 */
@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}

