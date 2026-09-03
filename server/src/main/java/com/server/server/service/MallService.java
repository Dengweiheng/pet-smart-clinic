package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.dto.CartAddRequest;
import com.server.server.dto.CartItemDTO;
import com.server.server.dto.CreateOrderRequest;
import com.server.server.entity.*;
import com.server.server.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 宠物医药商城与订单服务层
 * <p>
 * 处理药品目录检索、购物车管理、处方药合规核验、下单扣库存、订单支付/发货/签收及物流跟踪。
 */
@Service
@RequiredArgsConstructor
public class MallService {

    private final DrugMapper drugMapper;
    private final DrugInventoryMapper drugInventoryMapper;
    private final ShoppingCartItemMapper shoppingCartItemMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final LogisticsRecordMapper logisticsRecordMapper;

    /**
     * 药品列表查询（支持关键字模糊匹配与处方药/非处方药筛选）
     *
     * @param keyword 药品名称关键字
     * @param isRx 是否为处方药（1: 处方药，0: 非处方药/OTC）
     * @return 药品信息列表
     */
    public List<Drug> listDrugs(String keyword, Integer isRx) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Drug::getName, keyword);
        }
        if (isRx != null) {
            wrapper.eq(Drug::getIsRx, isRx);
        }
        // 仅展示在售 (ON_SALE) 或未设置状态的药品
        wrapper.and(w -> w.eq(Drug::getStatus, "ON_SALE").or().isNull(Drug::getStatus).or().eq(Drug::getStatus, ""));
        return drugMapper.selectList(wrapper);
    }

    /**
     * 加入购物车（若购物车已有该药品则累加数量，否则新建条目）
     *
     * @param request 加入购物车请求参数（用户ID、宠物ID、药品ID、购买数量）
     * @return 购物车记录
     */
    public ShoppingCartItem addToCart(CartAddRequest request) {
        LambdaQueryWrapper<ShoppingCartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCartItem::getUserId, request.getUserId())
                .eq(ShoppingCartItem::getPetId, request.getPetId())
                .eq(ShoppingCartItem::getDrugId, request.getDrugId());

        ShoppingCartItem existing = shoppingCartItemMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            shoppingCartItemMapper.updateById(existing);
            return existing;
        }

        ShoppingCartItem item = new ShoppingCartItem();
        item.setUserId(request.getUserId());
        item.setPetId(request.getPetId());
        item.setDrugId(request.getDrugId());
        item.setQuantity(request.getQuantity());
        shoppingCartItemMapper.insert(item);
        return item;
    }

    /**
     * 查询指定用户及宠物的购物车列表，并关联药品详细信息
     *
     * @param userId 用户 ID
     * @param petId 宠物 ID
     * @return 购物车 DTO 列表
     */
    public List<CartItemDTO> listCart(Long userId, Long petId) {
        LambdaQueryWrapper<ShoppingCartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCartItem::getUserId, userId).eq(ShoppingCartItem::getPetId, petId);
        List<ShoppingCartItem> items = shoppingCartItemMapper.selectList(wrapper);
        
        List<CartItemDTO> dtos = new ArrayList<>();
        for (ShoppingCartItem item : items) {
            Drug d = drugMapper.selectById(item.getDrugId());
            CartItemDTO dto = new CartItemDTO();
            dto.setId(item.getId());
            dto.setDrugId(item.getDrugId());
            dto.setQuantity(item.getQuantity());
            if (d != null) {
                dto.setDrugName(d.getName());
                dto.setDrugCode(d.getDrugCode());
                dto.setUnitPrice(d.getPrice());
                dto.setIsRx(d.getIsRx());
            }
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 从购物车中移除指定药品项
     *
     * @param userId 用户 ID
     * @param petId 宠物 ID
     * @param drugId 药品 ID
     */
    public void removeFromCart(Long userId, Long petId, Long drugId) {
        LambdaQueryWrapper<ShoppingCartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCartItem::getUserId, userId)
                .eq(ShoppingCartItem::getPetId, petId)
                .eq(ShoppingCartItem::getDrugId, drugId);
        shoppingCartItemMapper.delete(wrapper);
    }

    /**
     * 创建订单并执行处方药合规核验与库存扣减
     *
     * @param request 下单请求（包含用户ID、宠物ID、关联处方ID等）
     * @return 创建成功的订单主信息
     */
    public OrderInfo createOrder(CreateOrderRequest request) {
        // 1. 获取购物车内容
        List<CartItemDTO> cartItems = listCart(request.getUserId(), request.getPetId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("购物车为空");
        }

        // 2. 检索关联的处方单（若存在）
        Prescription prescription = null;
        if (request.getPrescriptionId() != null) {
            prescription = prescriptionMapper.selectById(request.getPrescriptionId());
        }

        // 3. 计算总金额并核验处方药合规性
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemDTO cartItem : cartItems) {
            Drug drug = drugMapper.selectById(cartItem.getDrugId());
            if (drug == null) {
                throw new IllegalArgumentException("药品不存在");
            }
            // 处方药严格核验：必须提供对应宠物、有效期内且包含该药品的有效处方
            if (drug.getIsRx() == 1) {
                validatePrescription(request.getPetId(), drug.getId(), prescription);
            }
            BigDecimal price = drug.getPrice() != null ? drug.getPrice() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 4. 生成订单主记录
        OrderInfo order = new OrderInfo();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setUserId(request.getUserId());
        order.setPetId(request.getPetId());
        order.setPrescriptionId(request.getPrescriptionId());
        order.setTotalAmount(totalAmount);
        order.setOrderStatus("PENDING_PAY");
        order.setPaymentStatus("UNPAID");
        orderInfoMapper.insert(order);

        // 5. 生成订单明细、扣减库存并清空已购购物车项
        for (CartItemDTO cartItem : cartItems) {
            Drug drug = drugMapper.selectById(cartItem.getDrugId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setDrugId(cartItem.getDrugId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(drug.getPrice() != null ? drug.getPrice() : BigDecimal.ZERO);
            orderItem.setIsRx(drug.getIsRx());
            orderItemMapper.insert(orderItem);

            // 扣减药品库存
            LambdaQueryWrapper<DrugInventory> inventoryWrapper = new LambdaQueryWrapper<>();
            inventoryWrapper.eq(DrugInventory::getDrugId, cartItem.getDrugId());
            DrugInventory inventory = drugInventoryMapper.selectOne(inventoryWrapper);
            if (inventory != null) {
                inventory.setStock(Math.max(0, inventory.getStock() - cartItem.getQuantity()));
                drugInventoryMapper.updateById(inventory);
            }

            // 清理购物车条目
            shoppingCartItemMapper.deleteById(cartItem.getId());
        }

        return order;
    }

    /**
     * 订单支付确认
     *
     * @param orderNo 订单编号
     * @return 更新后的订单信息
     */
    public OrderInfo payOrder(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        order.setPaymentStatus("PAID");
        order.setOrderStatus("PAID");
        orderInfoMapper.updateById(order);
        return order;
    }

    /**
     * 订单发货处理
     *
     * @param orderNo 订单编号
     */
    public void shipOrder(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        order.setOrderStatus("SHIPPED");
        orderInfoMapper.updateById(order);
    }

    /**
     * 用户确认收货
     *
     * @param orderNo 订单编号
     */
    public void confirmReceipt(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        order.setOrderStatus("COMPLETED");
        orderInfoMapper.updateById(order);
    }

    /**
     * 查询全平台订单列表（管理端使用）
     *
     * @return 按创建时间倒序排列的订单列表
     */
    public List<OrderInfo> listAllOrders() {
        return orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>().orderByDesc(OrderInfo::getCreatedAt));
    }

    /**
     * 查询当前用户的历史订单列表
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    public List<OrderInfo> listOrders(Long userId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId, userId).orderByDesc(OrderInfo::getCreatedAt);
        return orderInfoMapper.selectList(wrapper);
    }

    /**
     * 根据订单编号查询物流流转信息
     *
     * @param orderNo 订单号
     * @return 物流跟踪记录
     */
    public LogisticsRecord getLogistics(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo, orderNo);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);
        if (order == null) {
            return null;
        }

        LambdaQueryWrapper<LogisticsRecord> logisticsWrapper = new LambdaQueryWrapper<>();
        logisticsWrapper.eq(LogisticsRecord::getOrderId, order.getId());
        return logisticsRecordMapper.selectOne(logisticsWrapper);
    }

    /**
     * 处方药合规校验核心方法
     *
     * @param petId 购药的目标宠物 ID
     * @param drugId 待核验的处方药 ID
     * @param prescription 关联的电子处方对象
     */
    private void validatePrescription(Long petId, Long drugId, Prescription prescription) {
        if (prescription == null) {
            throw new IllegalArgumentException("处方药必须提交有效处方");
        }
        if (!petId.equals(prescription.getPetId())) {
            throw new IllegalArgumentException("处方与宠物不一致");
        }
        if (prescription.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("处方已过期");
        }

        LambdaQueryWrapper<PrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionItem::getPrescriptionId, prescription.getId())
                .eq(PrescriptionItem::getDrugId, drugId);
        PrescriptionItem item = prescriptionItemMapper.selectOne(wrapper);
        if (item == null) {
            throw new IllegalArgumentException("药品不在处方内");
        }
    }
}

