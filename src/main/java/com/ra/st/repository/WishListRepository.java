package com.ra.st.repository;

import com.ra.st.model.entity.Users;
import com.ra.st.model.entity.WishList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    @EntityGraph(attributePaths = {"product"})
    Page<WishList> findByUser(Users user, Pageable pageable);
    // Lấy danh sách sản phẩm yêu thích của người dùng
    List<WishList> findByUser(Users user);

    // Kiểm tra sản phẩm đã có trong danh sách yêu thích chưa
    Optional<WishList> findByUserAndProductId(Users user, Long productId);

    // Xóa sản phẩm khỏi danh sách yêu thích
    void deleteByUserAndProductId(Users user, Long productId);
}
