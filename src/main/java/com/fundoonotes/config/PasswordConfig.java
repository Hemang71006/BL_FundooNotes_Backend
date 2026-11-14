
package com.fundoonotes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Password Configuration
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Configures the PasswordEncoder bean used by the application.
 * Uses BCrypt to securely hash user passwords before storing them.
 */

@Configuration
public class PasswordConfig {

    // Registers BCryptPasswordEncoder as a Spring bean.
    @Bean
    public PasswordEncoder passwordEncoder() {

        // Returns the BCrypt implementation of PasswordEncoder.
        return new BCryptPasswordEncoder();
    }
}
