package com.fundoonotes.repository;

import com.fundoonotes.entity.Label;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Label Management - Repository
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Provides database operations for labels.
 * Supports label lookup and duplicate-name validation per user.
 */

public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findByUser_IdOrderByNameAsc(Long userId);

    Optional<Label> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);

    boolean existsByUser_IdAndNameIgnoreCaseAndIdNot(
            Long userId, String name, Long id);

    boolean existsByIdAndUser_Id(Long id, Long userId);
}