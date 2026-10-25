package org.gagan.florasense.Repositary;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Enum.Role;
//import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepositary extends JpaRepository<User, Integer> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // 2. Get all normal users (exclude admins)
    List<User> findByRole(Role role);

    // 3. Get a user by ID and role
    Optional<User> findByUserIdAndRole(Integer userId, Role role);

    @Query("""
        SELECT u FROM User u
        WHERE u.role = :role
             AND(
                 :search IS NULL
                 OR LOWER(u.username) LIKE
                    LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.email) LIKE
                    LOWER(CONCAT('%', :search, '%'))
             )
             AND(
                 :active IS NULL
                 OR u.isActive = :active
                     )
        """)
    Page<User> searchUsers(
            @Param("role") Role role,
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );

    // 4. Count total registered users
    long countByRole(Role role);

    // 5. Count active and inactive users
    long countByRoleAndIsActive(Role role, boolean isActice);

    // 6. Count registrations within a date range
    // Used for last 7 days and previous 7 days
    long countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Role role, LocalDateTime start, LocalDateTime end);
}
