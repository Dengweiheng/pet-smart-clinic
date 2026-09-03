package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("drug")
public class Drug {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String drugCode;

    private String name;

    private String category;

    private Integer isRx;

    private String indication;

    private String dosageInstruction;

    private String contraindication;

    private String status;

    private java.math.BigDecimal price;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
