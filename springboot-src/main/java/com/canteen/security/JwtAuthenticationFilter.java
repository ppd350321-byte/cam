package com.canteen.security;

import com.canteen.entity.Admin;
import com.canteen.entity.User;
import com.canteen.repository.AdminRepository;
import com.canteen.repository.PermissionRepository;
import com.canteen.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // Check JWT blacklist
            String blacklistKey = "jwt:blacklist:" + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"登录已失效，请重新登录\",\"data\":null}");
                return;
            }

            // 检查 Redis 中是否缓存了该 token（登录时写入）
            String tokenCacheKey = "jwt:token:" + token;
            boolean tokenCached = Boolean.TRUE.equals(redisTemplate.hasKey(tokenCacheKey));
            if (!tokenCached) {
                // token 不在 Redis 中，视为已失效（被登出或过期清理）
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"登录已失效，请重新登录\",\"data\":null}");
                return;
            }

            String tokenType = jwtTokenProvider.getTokenType(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            SecurityUser securityUser = null;

            if ("admin".equals(tokenType)) {
                Admin admin = adminRepository.findByUsername(username).orElse(null);
                if (admin != null && "active".equals(admin.getStatus())
                        && !Boolean.TRUE.equals(admin.getIsDeleted())) {
                    Set<String> permCodes = permissionRepository.findPermissionCodesByAdminId(admin.getId());
                    securityUser = new SecurityUser(admin, permCodes);
                }
            } else {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null && "active".equals(user.getStatus())
                        && !Boolean.TRUE.equals(user.getIsDeleted())) {
                    securityUser = new SecurityUser(user);
                }
            }

            if (securityUser != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
