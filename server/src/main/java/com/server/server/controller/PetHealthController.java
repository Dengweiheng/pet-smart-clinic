package com.server.server.controller;

import com.server.server.dto.PetCreateRequest;
import com.server.server.dto.PetHealthRecordRequest;
import com.server.server.dto.PetHealthRecordResponse;
import com.server.server.dto.PetResponse;
import com.server.server.service.PetHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物基础档案与健康记录控制器
 * <p>
 * 提供宠物基本信息的录入、修改、详情查询，以及宠物过敏史、慢性病和接种用药历史的管理接口。
 */
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetHealthController {

    private final PetHealthService petHealthService;

    /**
     * 新建宠物基础档案
     *
     * @param request 包含宠物姓名、物种、品种、性别、体重、生日等信息的请求体
     * @return 创建成功后的宠物信息
     */
    @PostMapping
    public PetResponse createPet(@RequestBody PetCreateRequest request) {
        return petHealthService.createPet(request);
    }

    /**
     * 根据宠物 ID 获取宠物基础档案详情
     *
     * @param petId 宠物 ID
     * @return 宠物详细信息
     */
    @GetMapping("/{petId}")
    public PetResponse getPet(@PathVariable String petId) {
        return petHealthService.getPet(petId);
    }

    /**
     * 更新已有宠物的基础信息
     *
     * @param petId 宠物 ID
     * @param request 宠物更新参数
     * @return 更新后的宠物信息
     */
    @PutMapping("/{petId}")
    public PetResponse updatePet(@PathVariable String petId, @RequestBody PetCreateRequest request) {
        return petHealthService.updatePet(petId, request);
    }

    /**
     * 保存或更新宠物的健康诊疗档案（过敏史、既往病史、疫苗接种与用药备注）
     *
     * @param petId 宠物 ID
     * @param request 宠物健康档案内容
     * @return 更新后的健康档案数据
     */
    @PutMapping("/{petId}/health-record")
    public PetHealthRecordResponse saveOrUpdateHealthRecord(@PathVariable String petId,
                                                            @RequestBody PetHealthRecordRequest request) {
        return petHealthService.saveOrUpdateHealthRecord(petId, request);
    }

    /**
     * 查询指定宠物的健康诊疗档案
     *
     * @param petId 宠物 ID
     * @return 宠物健康档案数据
     */
    @GetMapping("/{petId}/health-record")
    public PetHealthRecordResponse getHealthRecord(@PathVariable String petId) {
        return petHealthService.getHealthRecord(petId);
    }
}

