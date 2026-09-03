package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.OnlineConsultation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 在线问诊/咨询记录 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对在线咨询会话表 (online_consultation) 的 CRUD 操作。
 */
@Mapper
public interface OnlineConsultationMapper extends BaseMapper<OnlineConsultation> {
}

