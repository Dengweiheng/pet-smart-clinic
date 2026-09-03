package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.dto.*;
import com.server.server.entity.*;
import com.server.server.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 兽医在线问诊与电子处方业务逻辑服务
 * <p>
 * 核心功能：
 * 1. 在线问诊单创建与接诊状态维护
 * 2. 执业兽医开具电子处方（包含处方药项、用法用量、有效期）
 * 3. 处方药合规核验（药、方、宠三方校验）
 * 4. 兽医资质认证审核流转
 * 5. 药品库存管理与各业务实体联动
 */
@Service
public class VetPrescriptionService {

    private final OnlineConsultationMapper onlineConsultationMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final DrugMapper drugMapper;
    private final DrugInventoryMapper drugInventoryMapper;
    private final VetProfileMapper vetProfileMapper;
    private final SysUserMapper sysUserMapper;
    private final PetMapper petMapper;

    /**
     * 构造器注入所需的 Mapper 依赖
     */
    public VetPrescriptionService(OnlineConsultationMapper onlineConsultationMapper,
                                  PrescriptionMapper prescriptionMapper,
                                  PrescriptionItemMapper prescriptionItemMapper,
                                  DrugMapper drugMapper,
                                  DrugInventoryMapper drugInventoryMapper,
                                  VetProfileMapper vetProfileMapper,
                                  SysUserMapper sysUserMapper,
                                  PetMapper petMapper) {
        this.onlineConsultationMapper = onlineConsultationMapper;
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
        this.drugMapper = drugMapper;
        this.drugInventoryMapper = drugInventoryMapper;
        this.vetProfileMapper = vetProfileMapper;
        this.sysUserMapper = sysUserMapper;
        this.petMapper = petMapper;
    }

    /**
     * 用户发起在线医生问诊申请
     *
     * @param request 问诊创建请求（包含用户ID、宠物ID、病情主诉等）
     * @return 问诊信息响应 DTO
     */
    public ConsultationResponse createConsultation(ConsultationCreateRequest request) {
        OnlineConsultation c = new OnlineConsultation();
        c.setConsultationNo("CONS" + System.currentTimeMillis());
        c.setUserId(Long.valueOf(request.getUserId()));
        c.setPetId(Long.valueOf(request.getPetId()));
        c.setChiefComplaint(request.getProblemDescription());
        c.setStatus("WAITING_VET"); // 初始状态：待接诊
        onlineConsultationMapper.insert(c);

        ConsultationResponse r = new ConsultationResponse();
        r.setConsultationId(String.valueOf(c.getId()));
        r.setUserId(request.getUserId());
        r.setPetId(request.getPetId());
        r.setPetName(request.getPetName());
        r.setProblemDescription(request.getProblemDescription());
        r.setStatus(c.getStatus());
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    /**
     * 获取指定问诊单的详细信息
     *
     * @param consultationId 问诊单 ID
     * @return 问诊信息响应 DTO
     */
    public ConsultationResponse getConsultation(String consultationId) {
        OnlineConsultation c = onlineConsultationMapper.selectById(Long.valueOf(consultationId));
        if (c == null) return null;
        ConsultationResponse r = new ConsultationResponse();
        r.setConsultationId(String.valueOf(c.getId()));
        r.setUserId(String.valueOf(c.getUserId()));
        r.setPetId(String.valueOf(c.getPetId()));
        r.setProblemDescription(c.getChiefComplaint());
        r.setStatus(c.getStatus());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }

    /**
     * 兽医开具电子处方
     *
     * @param request 处方开具请求（诊断结论、处方药品列表、有效天数等）
     * @return 开具成功的处方详情
     */
    public PrescriptionResponse createPrescription(PrescriptionCreateRequest request) {
        // 1. 校验问诊单合法性与宠物一致性
        OnlineConsultation c = onlineConsultationMapper.selectById(Long.valueOf(request.getConsultationId()));
        if (c == null) throw new IllegalArgumentException("问诊记录不存在");
        if (!c.getPetId().equals(Long.valueOf(request.getPetId()))) throw new IllegalArgumentException("处方宠物与问诊宠物不一致");

        // 2. 插入处方主记录
        Prescription p = new Prescription();
        p.setPrescriptionNo("PRE" + UUID.randomUUID().toString().substring(0, 8));
        p.setConsultationId(c.getId());
        p.setVetUserId(Long.valueOf(request.getVetId()));
        p.setPetId(Long.valueOf(request.getPetId()));
        p.setDiagnosis(request.getDiagnosis());
        p.setStatus("ISSUED"); // 状态：已开具
        p.setValidFrom(LocalDateTime.now());
        p.setValidUntil(LocalDateTime.now().plusDays(request.getValidDays() == null ? 3 : request.getValidDays()));
        prescriptionMapper.insert(p);

        // 3. 逐条写入处方药品清单
        List<PrescriptionResponse.PrescriptionDrug> rows = new ArrayList<>();
        if (request.getItems() != null) {
            for (PrescriptionCreateRequest.PrescriptionDrugItem i : request.getItems()) {
                Drug d = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugCode, i.getDrugCode()));
                if (d == null) continue;
                PrescriptionItem pi = new PrescriptionItem();
                pi.setPrescriptionId(p.getId());
                pi.setDrugId(d.getId());
                pi.setDosage(i.getDosage());
                pi.setQuantity(i.getQuantity());
                prescriptionItemMapper.insert(pi);

                PrescriptionResponse.PrescriptionDrug row = new PrescriptionResponse.PrescriptionDrug();
                row.setDrugCode(i.getDrugCode());
                row.setDrugName(i.getDrugName());
                row.setDosage(i.getDosage());
                row.setQuantity(i.getQuantity());
                rows.add(row);
            }
        }

        // 4. 更新问诊记录为已完成
        c.setStatus("COMPLETED");
        c.setVetUserId(Long.valueOf(request.getVetId()));
        onlineConsultationMapper.updateById(c);

        // 5. 组装响应对象
        PrescriptionResponse r = new PrescriptionResponse();
        r.setPrescriptionId(String.valueOf(p.getId()));
        r.setConsultationId(request.getConsultationId());
        r.setVetId(request.getVetId());
        r.setVetName(request.getVetName());
        r.setPetId(request.getPetId());
        r.setDiagnosis(p.getDiagnosis());
        r.setStatus(p.getStatus());
        r.setIssuedAt(p.getValidFrom());
        r.setExpiresAt(p.getValidUntil());
        r.setItems(rows);
        return r;
    }

    /**
     * 根据处方 ID 查询处方详细信息及药品条目
     *
     * @param prescriptionId 处方 ID
     * @return 处方完整信息 DTO
     */
    public PrescriptionResponse getPrescription(String prescriptionId) {
        Prescription p = prescriptionMapper.selectById(Long.valueOf(prescriptionId));
        if (p == null) return null;
        PrescriptionResponse r = new PrescriptionResponse();
        r.setPrescriptionId(String.valueOf(p.getId()));
        r.setConsultationId(String.valueOf(p.getConsultationId()));
        r.setVetId(String.valueOf(p.getVetUserId()));
        r.setPetId(String.valueOf(p.getPetId()));
        r.setDiagnosis(p.getDiagnosis());
        r.setStatus(p.getStatus());
        r.setIssuedAt(p.getValidFrom());
        r.setExpiresAt(p.getValidUntil());

        List<PrescriptionItem> items = prescriptionItemMapper.selectList(new LambdaQueryWrapper<PrescriptionItem>().eq(PrescriptionItem::getPrescriptionId, p.getId()));
        List<PrescriptionResponse.PrescriptionDrug> data = new ArrayList<>();
        for (PrescriptionItem it : items) {
            Drug d = drugMapper.selectById(it.getDrugId());
            PrescriptionResponse.PrescriptionDrug row = new PrescriptionResponse.PrescriptionDrug();
            row.setDrugCode(d == null ? null : d.getDrugCode());
            row.setDrugName(d == null ? null : d.getName());
            row.setDosage(it.getDosage());
            row.setQuantity(it.getQuantity());
            data.add(row);
        }
        r.setItems(data);
        return r;
    }

    /**
     * 购买前处方药合规性前置校验（核验“药、方、宠”是否一致）
     *
     * @param request 购药合规核验请求
     * @return 核验结果响应（是否通过、被拒药品编码、提示消息）
     */
    public PurchaseValidationResponse validatePurchase(PurchaseValidationRequest request) {
        PurchaseValidationResponse r = new PurchaseValidationResponse();
        List<String> rejected = new ArrayList<>();
        boolean hasRx = false;
        Prescription p = (request.getPrescriptionId() == null || request.getPrescriptionId().isBlank()) ? null : prescriptionMapper.selectById(Long.valueOf(request.getPrescriptionId()));

        for (PurchaseValidationRequest.OrderDrugItem i : request.getItems()) {
            Drug d = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugCode, i.getDrugCode()));
            if (d == null) { rejected.add(i.getDrugCode()); continue; }
            if (d.getIsRx() == 1) {
                hasRx = true;
                // 校验：处方非空、处方所属宠物匹配、处方处于有效期内
                if (p == null || !Long.valueOf(request.getPetId()).equals(p.getPetId()) || p.getValidUntil().isBefore(LocalDateTime.now())) {
                    rejected.add(i.getDrugCode());
                    continue;
                }
                // 校验：处方明细中确实包含该药品
                PrescriptionItem pi = prescriptionItemMapper.selectOne(new LambdaQueryWrapper<PrescriptionItem>().eq(PrescriptionItem::getPrescriptionId, p.getId()).eq(PrescriptionItem::getDrugId, d.getId()));
                if (pi == null) rejected.add(i.getDrugCode());
            }
        }

        r.setPrescriptionRequired(hasRx);
        r.setRejectedDrugCodes(rejected);
        r.setPass(rejected.isEmpty());
        r.setMessage(rejected.isEmpty() ? "审核通过，可购买" : "审核不通过：药、方、宠信息不一致或缺少有效处方");
        return r;
    }

    /**
     * 审核/更新兽医资质状态
     *
     * @param vetId 兽医用户 ID
     * @param status 资质状态（APPROVED / REJECTED / PENDING）
     * @return 是否更新成功
     */
    public boolean updateVetQualification(String vetId, String status) {
        VetProfile v = vetProfileMapper.selectOne(new LambdaQueryWrapper<VetProfile>().eq(VetProfile::getUserId, Long.valueOf(vetId)));
        if (v == null) return false;
        v.setQualificationStatus(status);
        if ("APPROVED".equals(status)) v.setApprovedAt(LocalDateTime.now());
        return vetProfileMapper.updateById(v) > 0;
    }

    /**
     * 获取全部药品的当前库存映射表（药品编码 -> 库存量）
     *
     * @return 库存映射 Map
     */
    public Map<String, Integer> getDrugInventory() {
        List<DrugInventory> list = drugInventoryMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, Integer> data = new HashMap<>();
        for (DrugInventory i : list) {
            Drug d = drugMapper.selectById(i.getDrugId());
            if (d != null) data.put(d.getDrugCode(), i.getStock());
        }
        return data;
    }

    /**
     * 更新指定药品的库存数量
     *
     * @param drugCode 药品编码
     * @param stock 目标库存数值
     * @return 是否操作成功
     */
    public boolean updateDrugInventory(String drugCode, Integer stock) {
        Drug d = drugMapper.selectOne(new LambdaQueryWrapper<Drug>().eq(Drug::getDrugCode, drugCode));
        if (d == null) return false;
        DrugInventory i = drugInventoryMapper.selectOne(new LambdaQueryWrapper<DrugInventory>().eq(DrugInventory::getDrugId, d.getId()));
        if (i == null) {
            i = new DrugInventory();
            i.setDrugId(d.getId());
            i.setStock(stock);
            i.setSafetyStock(10);
            return drugInventoryMapper.insert(i) > 0;
        }
        i.setStock(stock);
        return drugInventoryMapper.updateById(i) > 0;
    }

    /**
     * 查询电子处方列表（支持按用户 ID 过滤其名下宠物的处方）
     *
     * @param userId 可选用户 ID
     * @return 处方列表
     */
    public List<PrescriptionResponse> listPrescriptions(String userId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && !userId.trim().isEmpty()) {
            LambdaQueryWrapper<Pet> petWrapper = new LambdaQueryWrapper<Pet>()
                    .eq(Pet::getOwnerUserId, Long.valueOf(userId));
            List<Pet> userPets = petMapper.selectList(petWrapper);
            if (userPets.isEmpty()) return new ArrayList<>();
            List<Long> petIds = new ArrayList<>();
            for (Pet p : userPets) petIds.add(p.getId());
            wrapper.in(Prescription::getPetId, petIds);
        }
        List<Prescription> list = prescriptionMapper.selectList(wrapper);
        List<PrescriptionResponse> out = new ArrayList<>();
        for (Prescription p : list) {
            PrescriptionResponse r = new PrescriptionResponse();
            r.setPrescriptionId(String.valueOf(p.getId()));
            r.setConsultationId(String.valueOf(p.getConsultationId()));
            r.setVetId(String.valueOf(p.getVetUserId()));
            r.setPetId(String.valueOf(p.getPetId()));
            
            SysUser u = sysUserMapper.selectById(p.getVetUserId());
            if (u != null) r.setVetName(u.getUsername());

            r.setDiagnosis(p.getDiagnosis());
            r.setStatus(p.getStatus());
            r.setIssuedAt(p.getValidFrom());
            r.setExpiresAt(p.getValidUntil());
            out.add(r);
        }
        return out;
    }

    /**
     * 查询在线问诊记录列表
     *
     * @param userId 可选用户 ID 过滤
     * @return 问诊记录列表
     */
    public List<ConsultationResponse> listConsultations(String userId) {
        LambdaQueryWrapper<OnlineConsultation> wrapper = new LambdaQueryWrapper<OnlineConsultation>().orderByDesc(OnlineConsultation::getCreatedAt);
        if (userId != null && !userId.trim().isEmpty()) {
            wrapper.eq(OnlineConsultation::getUserId, Long.valueOf(userId));
        }
        List<OnlineConsultation> list = onlineConsultationMapper.selectList(wrapper);
        List<ConsultationResponse> out = new ArrayList<>();
        for (OnlineConsultation c : list) {
            ConsultationResponse r = new ConsultationResponse();
            r.setConsultationId(String.valueOf(c.getId()));
            r.setUserId(String.valueOf(c.getUserId()));
            r.setPetId(String.valueOf(c.getPetId()));
            
            Pet p = petMapper.selectById(c.getPetId());
            if (p != null) r.setPetName(p.getName());

            r.setProblemDescription(c.getChiefComplaint());
            r.setStatus(c.getStatus());
            r.setCreatedAt(c.getCreatedAt());
            out.add(r);
        }
        return out;
    }

    /**
     * 获取已审核认证通过的执业兽医师列表
     *
     * @return 兽医 ID 与姓名列表
     */
    public List<Map<String, Object>> listVets() {
        List<VetProfile> list = vetProfileMapper.selectList(new LambdaQueryWrapper<VetProfile>().eq(VetProfile::getQualificationStatus, "APPROVED"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (VetProfile v : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", String.valueOf(v.getUserId()));
            m.put("name", v.getRealName());
            out.add(m);
        }
        return out;
    }

    /**
     * 获取普通宠物主人用户列表
     *
     * @return 用户基本信息列表
     */
    public List<Map<String, Object>> listUsers() {
        List<SysUser> list = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "USER"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (SysUser u : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", String.valueOf(u.getId()));
            m.put("name", u.getUsername());
            out.add(m);
        }
        return out;
    }

    /**
     * 获取宠物列表（支持按主人用户 ID 过滤）
     *
     * @param userId 主人用户 ID
     * @return 宠物信息简表
     */
    public List<Map<String, Object>> listPets(String userId) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && !userId.trim().isEmpty()) {
            wrapper.eq(Pet::getOwnerUserId, Long.valueOf(userId));
        }
        List<Pet> list = petMapper.selectList(wrapper);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Pet p : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", String.valueOf(p.getId()));
            m.put("name", p.getName());
            m.put("ownerUserId", String.valueOf(p.getOwnerUserId()));
            out.add(m);
        }
        return out;
    }
}

