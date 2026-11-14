
package com.fundoonotes.security;

import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - JWT Authentication Filter
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Intercepts incoming HTTP requests and checks for JWT tokens.
 * Validates tokens and sets the authenticated user's security context.
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Read the Authorization header from the incoming request.
        String authorizationHeader = request.getHeader("Authorization");

        // Continue normally if the header does not contain a Bearer token.
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token after the "Bearer " prefix.
        String token = authorizationHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            // Authenticate only if the current request has no authentication yet.
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null && jwtService.isTokenValid(token, user.getEmail())) {

                    // Convert the database user into Spring Security's UserDetails object.
                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getEmail())
                            .password(user.getPassword())
                            .authorities(List.of())
                            .build();

                    // Create an authenticated security token for this user.
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Store the authenticated user for the current request.
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (JwtException | IllegalArgumentException exception) {
            // Clear authentication when the JWT is invalid or expired.
            SecurityContextHolder.clearContext();
        }

        // Pass the request to the next filter.
        filterChain.doFilter(request, response);
    }
}
