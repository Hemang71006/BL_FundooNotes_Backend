
package com.fundoonotes.repository;

import com.fundoonotes.entity.User;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Repository Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests database operations provided by UserRepository.
 * Checks user persistence and email lookup functionality.
 */

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    // Injects the actual repository to test database operations.
    @Autowired
    private UserRepository userRepository;

    // Tests whether a new user can be saved successfully.
    @Test
    void saveUserSuccessfully() {

        User user = new User();
        user.setFirstName("Hemang");
        user.setLastName("Shah");
        user.setEmail("hemang@example.com");
        user.setPassword("encoded-password");

        // Save the user into the configured database.
        User savedUser = userRepository.save(user);

        // Verify that the database generated the ID and creation timestamp.
        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertEquals("hemang@example.com", savedUser.getEmail());
    }

    // Tests whether the repository can find an existing email.
    @Test
    void existsByEmailReturnsTrue() {

        User user = new User();
        user.setFirstName("Hemang");
        user.setLastName("Shah");
        user.setEmail("hemang@example.com");
        user.setPassword("encoded-password");

        // Save the user before checking the email.
        userRepository.save(user);

        // Verify that the repository finds the saved email.
        assertTrue(userRepository.existsByEmail("hemang@example.com"));
    }

    // Tests that an email not present in the database returns false.
    @Test
    void existsByEmailReturnsFalse() {

        // Verify that an unknown email does not exist.
        assertFalse(userRepository.existsByEmail("unknown@example.com"));
    }
}
