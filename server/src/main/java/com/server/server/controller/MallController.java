package com.server.server.controller;

import com.server.server.dto.*;
import com.server.server.entity.Drug;
import com.server.server.entity.LogisticsRecord;
import com.server.server.entity.OrderInfo;
import com.server.server.entity.ShoppingCartItem;
import com.server.server.service.MallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物医药商城与订单交易控制器
 * <p>
 * 提供药品搜索展示、购物车增删查、订单创建/支付/发货/收货、物流信息追踪等接口。
 */
@RestController
@RequestMapping("/api/mall")
@RequiredArgsConstructor
public class MallController {

    private final MallService mallService;

    /**
     * 查询药品列表
     *
     * @param keyword 搜索关键词（支持根据药品名称模糊匹配）
     * @param isRx 药品类型筛选（1: 处方药，0: 非处方药/OTC）
     * @return 符合条件的药品列表
     */
    @GetMapping("/drugs")
    public List<Drug> listDrugs(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer isRx) {
        return mallService.listDrugs(keyword, isRx);
    }

    /**
     * 添加药品到购物车
     *
     * @param request 包含用户ID、宠物ID、药品ID和数量的请求体
     * @return 购物车条目实体
     */
    @PostMapping("/cart/items")
    public ShoppingCartItem addToCart(@RequestBody CartAddRequest request) {
        return mallService.addToCart(request);
    }

    /**
     * 查询指定用户及宠物的购物车列表
     *
     * @param userId 用户 ID
     * @param petId 宠物 ID
     * @return 包含药品详细信息（名称、单价、类型等）的购物车 DTO 列表
     */
    @GetMapping("/cart/items")
    public List<CartItemDTO> listCart(@RequestParam Long userId, @RequestParam Long petId) {
        return mallService.listCart(userId, petId);
    }

    /**
     * 从购物车中删除指定商品
     *
     * @param userId 用户 ID
     * @param petId 宠物 ID
     * @param drugId 待移除的药品 ID
     */
    @DeleteMapping("/cart/items")
    public void removeFromCart(@RequestParam Long userId, @RequestParam Long petId, @RequestParam Long drugId) {
        mallService.removeFromCart(userId, petId, drugId);
    }

    /**
     * 结算购物车并创建交易订单（含处方合规前置拦截）
     *
     * @param request 下单请求体（包含用户ID、宠物ID、关联处方ID等）
     * @return 创建成功的订单主信息
     */
    @PostMapping("/orders")
    public OrderInfo createOrder(@RequestBody CreateOrderRequest request) {
        return mallService.createOrder(request);
    }

    /**
     * 模拟订单线上支付
     *
     * @param request 支付请求体（包含订单号）
     * @return 支付成功后的订单实体
     */
    @PostMapping("/orders/pay")
    public OrderInfo payOrder(@RequestBody PayOrderRequest request) {
        return mallService.payOrder(request.getOrderNo());
    }

    /**
     * 订单发货（商家/管理端操作）
     *
     * @param request 发货请求体（包含订单号）
     */
    @PutMapping("/orders/ship")
    public void shipOrder(@RequestBody ShipOrderRequest request) {
        mallService.shipOrder(request.getOrderNo());
    }

    /**
     * 用户确认收货
     *
     * @param request 确认收货请求体（包含订单号）
     */
    @PostMapping("/orders/confirm-receipt")
    public void confirmReceipt(@RequestBody PayOrderRequest request) {
        mallService.confirmReceipt(request.getOrderNo());
    }

    /**
     * 查询全平台所有订单（管理端专用）
     *
     * @return 全量订单列表
     */
    @GetMapping("/orders/admin/all")
    public List<OrderInfo> listAllOrders() {
        return mallService.listAllOrders();
    }

    /**
     * 根据订单号查询该订单的物流轨迹信息
     *
     * @param orderNo 订单号
     * @return 物流追踪记录
     */
    @GetMapping("/orders/{orderNo}/logistics")
    public LogisticsRecord getLogistics(@PathVariable String orderNo) {
        return mallService.getLogistics(orderNo);
    }

    /**
     * 查询指定用户的所有历史订单列表
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    @GetMapping("/orders")
    public List<OrderInfo> listOrders(@RequestParam Long userId) {
        return mallService.listOrders(userId);
    }
}

