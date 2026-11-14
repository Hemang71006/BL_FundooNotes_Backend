
package com.fundoonotes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Entity
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Represents a user in the Fundoo Notes application.
 * Stores user details such as name, email, encrypted password,
 * and account creation time in the database.
 */

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    // Primary key that uniquely identifies each user.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stores the user's first name.
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    // Stores the user's last name.
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    // Stores a unique email address for each user.
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Stores the BCrypt-encrypted password.
    @Column(nullable = false)
    private String password;

    // Stores the date and time when the account was created.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatically sets the creation time before inserting a new user.
    @PrePersist
    public void setCreationTime() {
        this.createdAt = LocalDateTime.now();
    }
}
