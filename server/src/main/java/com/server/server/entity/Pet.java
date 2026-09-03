package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerUserId;

    private String name;

    private String species;

    private String breed;

    private String gender;

    private LocalDate birthDate;

    private BigDecimal weightKg;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
