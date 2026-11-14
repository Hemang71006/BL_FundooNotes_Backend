
package com.fundoonotes.repository;

import com.fundoonotes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Repository
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Provides database operations for the User entity.
 * Supports saving users and checking whether an email already exists.
 */

public interface UserRepository extends JpaRepository<User, Long> {

    // Checks whether a user already exists with the given email.
    boolean existsByEmail(String email);
}
