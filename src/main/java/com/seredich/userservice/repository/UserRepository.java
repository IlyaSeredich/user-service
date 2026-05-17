package com.seredich.userservice.repository;

import com.seredich.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

    @Query(value = """
                SELECT * FROM users
                WHERE id = :id
            """, nativeQuery = true)
    User getUserById(@Param("id") Long id);

    @Query("SELECT u FROM User u")
    List<User> getAllUsers();
}
