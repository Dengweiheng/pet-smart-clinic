package com.server.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.dto.DrugInventoryUpdateRequest;
import com.server.server.dto.PrescriptionResponse;
import com.server.server.dto.VetQualificationRequest;
import com.server.server.entity.Drug;
import com.server.server.entity.OrderInfo;
import com.server.server.entity.SysUser;
import com.server.server.mapper.DrugMapper;
import com.server.server.mapper.OrderInfoMapper;
import com.server.server.service.AuthService;
import com.server.server.service.VetPrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台运营与系统管理控制台
 * <p>
 * 提供兽医执业资质审核、全平台用户状态管控、药品 SKU 与库存维护、
 * 处方全流程合规监管、以及平台运营大盘多维数据看板。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VetPrescriptionService vetPrescriptionService;
    private final AuthService authService;
    private final DrugMapper drugMapper;
    private final OrderInfoMapper orderInfoMapper;

    /**
     * 审核兽医师执业资质
     *
     * @param request 审核入参（包含兽医ID和目标状态，如 APPROVED / REJECTED）
     * @return 审核操作状态
     */
    @PutMapping("/vets/qualification")
    public Map<String, Object> updateVetQualification(@RequestBody VetQualificationRequest request) {
        boolean success = vetPrescriptionService.updateVetQualification(request.getVetId(), request.getStatus());
        // 若执业资质审核被拒绝，则自动禁用该账号登录权限以确保平台安全
        if ("REJECTED".equals(request.getStatus())) {
            authService.updateUserStatus(Long.valueOf(request.getVetId()), "DISABLED");
        }
        return Map.of("success", success);
    }

    /**
     * 修改用户账户状态（启用/禁用）
     *
     * @param id 用户 ID
     * @param body 请求体中的状态参数（status: ACTIVE / DISABLED）
     * @return 操作结果
     */
    @PutMapping("/users/{id}/status")
    public Map<String, Object> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        authService.updateUserStatus(id, body.get("status"));
        return Map.of("success", true);
    }

    /**
     * 获取全部药品库存分布数据
     *
     * @return 药品编码与对应当前库存的 Map
     */
    @GetMapping("/drugs/inventory")
    public Map<String, Integer> getDrugInventory() {
        return vetPrescriptionService.getDrugInventory();
    }

    /**
     * 更新指定药品的库存数量
     *
     * @param request 药品库存更新参数（药品编码、最新库存）
     * @return 是否更新成功
     */
    @PutMapping("/drugs/inventory")
    public Map<String, Object> updateDrugInventory(@RequestBody DrugInventoryUpdateRequest request) {
        boolean success = vetPrescriptionService.updateDrugInventory(request.getDrugCode(), request.getStock());
        return Map.of("success", success);
    }

    /**
     * 监管大盘：全局处方开具与核销监控
     *
     * @return 全量电子处方列表
     */
    @GetMapping("/prescriptions/monitor")
    public List<PrescriptionResponse> monitorPrescriptions() {
        return vetPrescriptionService.listPrescriptions(null);
    }

    /**
     * 管理员数据统计大盘看板
     * <p>
     * 聚合实时在线人数、全平台处方总量、药品在售 SKU 数、近 7 日订单交易趋势折线数据。
     *
     * @return 看板统计数据
     */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<OrderInfo> orders = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .ge(OrderInfo::getCreatedAt, sevenDaysAgo));

        // 生成最近 7 天连续日期的初始订单计数 Map（保证日期连续不中断）
        Map<String, Long> trendMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            trendMap.put(date, 0L);
        }
        
        // 统计各日期实际成交订单数量
        orders.forEach(o -> {
            String date = o.getCreatedAt().toLocalDate().toString();
            if (trendMap.containsKey(date)) {
                trendMap.put(date, trendMap.get(date) + 1);
            }
        });

        return Map.of(
                "onlineUsers", authService.getOnlineUserCount(),
                "prescriptionCount", vetPrescriptionService.listPrescriptions(null).size(),
                "drugSkuCount", drugMapper.selectCount(null),
                "orderTrend", trendMap,
                "totalOrders", orderTrendCount(trendMap)
        );
    }

    /**
     * 辅助统计趋势中的订单总和
     */
    private long orderTrendCount(Map<String, Long> trend) {
        return trend.values().stream().mapToLong(l -> l).sum();
    }

    /**
     * 获取全量系统用户列表
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    public List<SysUser> listUsers() {
        return authService.listUsers();
    }

    /**
     * 获取药品库全量药品信息并附带实时库存
     *
     * @return 包含药品属性与实时库存的列表
     */
    @GetMapping("/drugs")
    public List<Map<String, Object>> listDrugs() {
        List<Drug> drugs = drugMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, Integer> inventory = vetPrescriptionService.getDrugInventory();
        return drugs.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("drugCode", d.getDrugCode());
            m.put("name", d.getName());
            m.put("category", d.getCategory());
            m.put("isRx", d.getIsRx());
            m.put("indication", d.getIndication());
            m.put("dosageInstruction", d.getDosageInstruction());
            m.put("contraindication", d.getContraindication());
            m.put("price", d.getPrice());
            m.put("status", d.getStatus());
            m.put("stock", inventory.getOrDefault(d.getDrugCode(), 0));
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * 管理员录入新药品 SKU
     *
     * @param drug 药品实体对象
     * @return 录入成功的药品对象
     */
    @PostMapping("/drugs")
    public Drug createDrug(@RequestBody Drug drug) {
        drug.setCreatedAt(LocalDateTime.now());
        drug.setUpdatedAt(LocalDateTime.now());
        drugMapper.insert(drug);
        // 初始化默认库存为 0
        vetPrescriptionService.updateDrugInventory(drug.getDrugCode(), 0); 
        return drug;
    }

    /**
     * 编辑/修改现有药品信息
     *
     * @param id 药品 ID
     * @param drug 包含最新信息的药品实体
     * @return 更新后的药品对象
     */
    @PutMapping("/drugs/{id}")
    public Drug updateDrug(@PathVariable Long id, @RequestBody Drug drug) {
        drug.setId(id);
        drug.setUpdatedAt(LocalDateTime.now());
        drugMapper.updateById(drug);
        return drug;
    }

    /**
     * 删除指定药品
     *
     * @param id 药品 ID
     * @return 删除状态
     */
    @DeleteMapping("/drugs/{id}")
    public Map<String, Object> deleteDrug(@PathVariable Long id) {
        drugMapper.deleteById(id);
        return Map.of("success", true);
    }
}

