package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.dto.LoginRequest;
import com.server.server.dto.LoginResponse;
import com.server.server.dto.RegisterRequest;
import com.server.server.entity.SysUser;
import com.server.server.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户认证与鉴权业务逻辑层
 * <p>
 * 处理用户登录、注册、退出登录、在线人数统计以及用户状态管理。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;

    /**
     * 内存中存储活跃用户 Token 与用户名映射（模拟会话 Session）
     */
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    /**
     * 当前在线用户计数器
     */
    private final AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 用户登录接口
     *
     * @param request 登录请求参数（包含用户名和密码）
     * @return 登录成功后的响应对象（包含 token、用户ID、角色等）
     */
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getStatus, "ACTIVE");

        // 查询数据库中处于正常激活状态的用户
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 密码校验逻辑：支持明文比对与演示初始密码比对
        String rawHash = user.getPasswordHash();
        boolean pass = rawHash != null && rawHash.equals(request.getPassword());
        if (!pass && rawHash != null && rawHash.startsWith("$2a$10$examplehash")) {
            pass = "123456".equals(request.getPassword());
        }
        if (!pass) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 生成全局唯一登录 Token 并存入内存
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user.getUsername());
        onlineCount.incrementAndGet();

        // 组装登录结果
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        return response;
    }

    /**
     * 新用户注册
     *
     * @param request 注册参数（包含用户名、密码、手机号、角色等）
     */
    public void register(RegisterRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (sysUserMapper.selectOne(wrapper) != null) {
            throw new IllegalArgumentException("该用户名已被占用");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword()); 
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() == null ? "USER" : request.getRole());
        user.setStatus("ACTIVE");
        sysUserMapper.insert(user);
    }

    /**
     * 用户注销/登出
     *
     * @param token 客户端传递的认证 Token
     * @return 是否成功移除登录态
     */
    public boolean logout(String token) {
        boolean removed = tokenStore.remove(token) != null;
        if (removed && onlineCount.get() > 0) {
            onlineCount.decrementAndGet();
        }
        return removed;
    }

    /**
     * 获取当前系统在线用户数量
     *
     * @return 在线用户人数
     */
    public int getOnlineUserCount() {
        return onlineCount.get();
    }

    /**
     * 获取全量系统用户列表（供管理后台使用）
     *
     * @return 用户实体列表
     */
    public List<SysUser> listUsers() {
        return sysUserMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 更新指定用户的启用/禁用状态
     *
     * @param userId 用户 ID
     * @param status 目标状态（如 ACTIVE/DISABLED）
     * @return 更新是否成功
     */
    public boolean updateUserStatus(Long userId, String status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return false;
        user.setStatus(status);
        return sysUserMapper.updateById(user) > 0;
    }
}

