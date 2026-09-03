package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("online_consultation")
public class OnlineConsultation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String consultationNo;

    private Long userId;

    private Long petId;

    private Long vetUserId;

    private Long aiSessionId;

    private String chiefComplaint;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
