package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("logistics_record")
public class LogisticsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String companyName;

    private String trackingNo;

    private String logisticsStatus;

    private LocalDateTime updatedAt;
}
