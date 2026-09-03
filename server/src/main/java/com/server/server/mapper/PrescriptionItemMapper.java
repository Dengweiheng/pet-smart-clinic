package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方药品明细项 数据访问层接口 (Mapper)
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，提供对电子处方药品明细表 (prescription_item) 的 CRUD 操作。
 */
@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {
}

