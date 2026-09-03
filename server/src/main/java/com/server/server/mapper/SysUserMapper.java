package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对系统用户表 (sys_user) 的数据操作（用户、执业兽医、管理员）。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

