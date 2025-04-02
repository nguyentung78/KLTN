package com.ra.st.repository;

import com.ra.st.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<Users,Long> {
    Users findUserByUsername(String username);
    Users findUsersByEmail(String email);
    @Query("SELECT u FROM Users u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Users> searchUsersByUsername(String username, Pageable pageable);

    boolean existsByPhone(String phone);
}
