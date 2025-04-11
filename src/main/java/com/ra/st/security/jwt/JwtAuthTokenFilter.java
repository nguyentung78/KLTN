package com.ra.st.security.jwt;

import com.ra.st.security.UserDetailService;
import com.ra.st.service.LogoutService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private LogoutService logoutService;

    private static final String[] PERMIT_ALL_ENDPOINTS = {
            "/api/v1/user/cart/checkout/success",
            "/api/v1/user/cart/checkout/cancel",
            "/api/v1/public/",
            "/api/v1/auth/",
            "/api/paypal/"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.info("Processing request to URI: {}", requestURI);

        // Kiểm tra nếu yêu cầu đến endpoint trong danh sách permitAll
        boolean isPermitAllEndpoint = false;
        for (String endpoint : PERMIT_ALL_ENDPOINTS) {
            if (requestURI.startsWith(endpoint)) {
                isPermitAllEndpoint = true;
                logger.info("PermitAll endpoint matched: {}", endpoint);
                break;
            }
        }

        if (isPermitAllEndpoint) {
            logger.info("Skipping token validation for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);
        if (token != null) {
            logger.info("Token found: {}", token);
        } else {
            logger.info("No token found in request");
        }

        try {
            if (token != null && logoutService.isTokenBlacklisted(token)) {
                logger.warn("Token is blacklisted: {}", token);
                response.setContentType("text/plain; charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token đã bị thu hồi, vui lòng đăng nhập lại.");
                return;
            }

            if (token != null && jwtProvider.validateToken(token)) {
                String userName = jwtProvider.getUserNameFromToken(token);
                logger.info("Token validated, username: {}", userName);
                UserDetails userDetails = userDetailService.loadUserByUsername(userName);
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    logger.info("Authentication set for user: {}", userName);
                }
            }
        } catch (Exception exception) {
            logger.error("Error processing token: {}", exception.getMessage(), exception);
        }

        filterChain.doFilter(request, response);
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}