package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.dto.LoginRequest;
import com.server.server.dto.LoginResponse;
import com.server.server.dto.RegisterRequest;
import com.server.server.entity.SysUser;
import com.server.server.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * 用户认证与鉴权业务逻辑层
 * <p>
 * 处理用户登录、注册、退出登录、在线人数统计以及用户状态管理。
 * 登录态由 Redis 统一承载：Token 使用带过期时间的 Key 存储，
 * 在线用户使用 ZSet 统计（member 为用户 ID，score 为该用户 Token 的过期时间戳），
 * 因此服务重启不会丢失登录态，也支持多实例部署。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * 登录 Token 键前缀，完整键为 auth:token:{token}，值为用户 ID
     */
    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    /**
     * 在线用户 ZSet 键，member 为用户 ID，score 为 Token 过期时间戳（毫秒）
     */
    private static final String ONLINE_USERS_KEY = "auth:online:users";

    /**
     * Token 有效期，与 ZSet 中记录的过期时刻保持一致
     */
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final SysUserMapper sysUserMapper;

    private final StringRedisTemplate redisTemplate;

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

        // 生成全局唯一登录 Token 并写入 Redis，30 分钟后自动过期
        String token = UUID.randomUUID().toString();
        String userId = String.valueOf(user.getId());
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, userId, TOKEN_TTL);

        // 记录到在线用户集合：先清理已过期成员，再写入本次登录的过期时刻
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().removeRangeByScore(ONLINE_USERS_KEY, 0, now);
        redisTemplate.opsForZSet().add(ONLINE_USERS_KEY, userId, now + TOKEN_TTL.toMillis());

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
        String tokenKey = TOKEN_KEY_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(tokenKey);
        if (userId == null) {
            return false;
        }
        redisTemplate.delete(tokenKey);
        redisTemplate.opsForZSet().remove(ONLINE_USERS_KEY, userId);
        return true;
    }

    /**
     * 获取当前系统在线用户数量
     * <p>
     * 只统计 Token 尚未过期的成员，过期成员会被区间条件自动排除，无需额外清理。
     *
     * @return 在线用户人数
     */
    public int getOnlineUserCount() {
        Long count = redisTemplate.opsForZSet()
                .count(ONLINE_USERS_KEY, System.currentTimeMillis(), Double.MAX_VALUE);
        return count == null ? 0 : count.intValue();
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
