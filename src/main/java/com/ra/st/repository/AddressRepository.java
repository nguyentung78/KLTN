package com.ra.st.repository;

import com.ra.st.model.entity.Address;
import com.ra.st.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // ✅ Lấy danh sách địa chỉ của một user
    List<Address> findByUser(Users user);
}
