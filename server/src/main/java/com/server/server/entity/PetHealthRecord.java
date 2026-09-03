package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_health_record")
public class PetHealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;

    private String allergies;

    private String chronicDiseases;

    private String vaccineNotes;

    private String medicationNotes;

    private Long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
