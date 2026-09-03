package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.VetProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执业兽医师资质档案 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对兽医执业资质表 (vet_profile) 的数据操作。
 */
@Mapper
public interface VetProfileMapper extends BaseMapper<VetProfile> {
}

