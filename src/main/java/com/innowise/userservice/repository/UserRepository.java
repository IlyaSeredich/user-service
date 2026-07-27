package com.innowise.userservice.repository;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    @Query(value = """
                SELECT * FROM users
                WHERE id = :id
            """, nativeQuery = true)
    Optional<User> findUserById(@Param("id") UUID id);

    boolean existsUserByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
