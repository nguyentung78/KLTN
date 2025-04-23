package com.ra.st.repository;

import com.ra.st.model.entity.ShoppingCart;
import com.ra.st.model.entity.Users;
import com.ra.st.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    // Lấy danh sách sản phẩm trong giỏ hàng của một user
    List<ShoppingCart> findByUser(Users user);

    // Kiểm tra xem user đã thêm sản phẩm này vào giỏ hàng chưa
    Optional<ShoppingCart> findByUserAndProduct(Users user, Product product);

    // Xóa toàn bộ giỏ hàng của user
    void deleteByUser(Users user);

    // Xóa tất cả các mục trong giỏ hàng liên quan đến một sản phẩm
    void deleteByProduct(Product product);
}