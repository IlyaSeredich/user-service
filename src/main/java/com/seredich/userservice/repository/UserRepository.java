package com.seredich.userservice.repository;

import com.seredich.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query(value = """
                SELECT * FROM users
                WHERE id = :id
            """, nativeQuery = true)
    Optional<User> findUserById(@Param("id") Long id);

    @Query("SELECT u FROM User u")
    List<User> findAllUsers();
}
