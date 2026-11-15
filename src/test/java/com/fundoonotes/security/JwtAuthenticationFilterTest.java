
package com.fundoonotes.security;

import com.fundoonotes.entity.User;
import com.fundoonotes.redis.RedisService;
import com.fundoonotes.repository.UserRepository;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - JWT Filter Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests JWT authentication filter behavior.
 * Verifies missing tokens, valid tokens, invalid tokens,
 * and malformed tokens.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private RedisService redisService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsMissing() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            // Confirms that the request continues through the filter chain.
        };

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtService, userRepository, redisService);
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {

        String token = "valid-test-token";
        String email = "hemang@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded-password");

        when(redisService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(token, email)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            // Confirms that the request continues after token validation.
        };

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        verify(redisService).isTokenBlacklisted(token);
        verify(jwtService).extractEmail(token);
        verify(userRepository).findByEmail(email);
        verify(jwtService).isTokenValid(token, email);
    }

    @Test
    void shouldNotAuthenticateUserWhenTokenIsInvalid() throws Exception {

        String token = "invalid-test-token";
        String email = "hemang@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded-password");

        when(redisService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(token, email)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            // Confirms that the filter chain continues without authentication.
        };

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(redisService).isTokenBlacklisted(token);
    }

    @Test
    void shouldNotAuthenticateWhenTokenCannotBeParsed() throws Exception {

        String token = "malformed-test-token";

        when(redisService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtService.extractEmail(token))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            // Confirms that the filter chain continues after the invalid token is ignored.
        };

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(redisService).isTokenBlacklisted(token);
        verify(jwtService).extractEmail(token);
    }
}
