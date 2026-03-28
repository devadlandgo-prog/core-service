package com.landgo.coreservice.security;
import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import jakarta.servlet.http.HttpServletRequest; import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component; import org.springframework.util.StringUtils; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.UUID;
@Slf4j @Component @RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try { String jwt = getJwt(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                UUID userId = tokenProvider.getUserIdFromToken(jwt); String role = tokenProvider.getRoleFromToken(jwt);
                UserPrincipal up = UserPrincipal.create(userId, role);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(up, null, up.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ex) { log.error("Could not set user authentication", ex); }
        filterChain.doFilter(request, response);
    }
    private String getJwt(HttpServletRequest request) { String b = request.getHeader("Authorization"); return (StringUtils.hasText(b) && b.startsWith("Bearer ")) ? b.substring(7) : null; }
}
