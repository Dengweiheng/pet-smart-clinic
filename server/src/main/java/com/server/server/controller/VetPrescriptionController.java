package com.server.server.controller;

import com.server.server.dto.*;
import com.server.server.service.VetPrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 执业兽医在线问诊与电子处方管理控制器
 * <p>
 * 提供在线问诊会话管理、医生开具电子处方、处方药合规购药核验、兽医认证查询等接口。
 */
@RestController
@RequestMapping("/api/vet")
@RequiredArgsConstructor
public class VetPrescriptionController {

    private final VetPrescriptionService vetPrescriptionService;

    /**
     * 用户发起在线问诊
     *
     * @param request 包含用户ID、宠物ID、病情描述等信息的请求体
     * @return 问诊信息响应 DTO
     */
    @PostMapping("/consultations")
    public ConsultationResponse createConsultation(@RequestBody ConsultationCreateRequest request) {
        return vetPrescriptionService.createConsultation(request);
    }

    /**
     * 查询指定问诊单的详细信息
     *
     * @param consultationId 问诊单 ID
     * @return 问诊详情
     */
    @GetMapping("/consultations/{consultationId}")
    public ConsultationResponse getConsultation(@PathVariable String consultationId) {
        return vetPrescriptionService.getConsultation(consultationId);
    }

    /**
     * 执业兽医开具电子处方
     *
     * @param request 开方请求体（包含关联问诊ID、兽医ID、宠物ID、诊断结论、处方药品列表）
     * @return 开具成功后的电子处方完整信息
     */
    @PostMapping("/prescriptions")
    public PrescriptionResponse createPrescription(@RequestBody PrescriptionCreateRequest request) {
        return vetPrescriptionService.createPrescription(request);
    }

    /**
     * 根据处方 ID 获取电子处方详情及药品用量
     *
     * @param prescriptionId 处方 ID
     * @return 处方详情
     */
    @GetMapping("/prescriptions/{prescriptionId}")
    public PrescriptionResponse getPrescription(@PathVariable String prescriptionId) {
        return vetPrescriptionService.getPrescription(prescriptionId);
    }

    /**
     * 购买处方药前的合规前置校验（核对“药、方、宠”一致性）
     *
     * @param request 购药合规核验请求
     * @return 合规核验结果
     */
    @PostMapping("/prescriptions/validate-purchase")
    public PurchaseValidationResponse validatePurchase(@RequestBody PurchaseValidationRequest request) {
        return vetPrescriptionService.validatePurchase(request);
    }

    /**
     * 获取问诊单列表（可按用户 ID 筛选）
     *
     * @param userId 可选用户 ID
     * @return 问诊单列表
     */
    @GetMapping("/consultations")
    public List<ConsultationResponse> listConsultations(@RequestParam(required = false) String userId) {
        return vetPrescriptionService.listConsultations(userId);
    }

    /**
     * 获取已通过资质认证的执业兽医列表
     *
     * @return 兽医师列表
     */
    @GetMapping("/vets")
    public List<Map<String, Object>> listVets() {
        return vetPrescriptionService.listVets();
    }

    /**
     * 获取系统普通用户列表（用于问诊/建档选择）
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        return vetPrescriptionService.listUsers();
    }

    /**
     * 获取宠物列表（支持按主人用户 ID 过滤）
     *
     * @param userId 主人用户 ID
     * @return 宠物列表
     */
    @GetMapping("/pets")
    public List<Map<String, Object>> listPets(@RequestParam(required = false) String userId) {
        return vetPrescriptionService.listPets(userId);
    }

    /**
     * 获取电子处方列表（支持按用户 ID 过滤其名下宠物的处方）
     *
     * @param userId 主人用户 ID
     * @return 处方列表
     */
    @GetMapping("/prescriptions")
    public List<PrescriptionResponse> listPrescriptions(@RequestParam(required = false) String userId) {
        return vetPrescriptionService.listPrescriptions(userId);
    }
}

