package com.ra.st.repository;

import com.ra.st.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findRoleByRoleName(Role.RoleName roleName);
}
