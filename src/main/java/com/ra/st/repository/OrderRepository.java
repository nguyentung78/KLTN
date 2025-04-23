package com.ra.st.repository;

import com.ra.st.model.entity.Order;
import com.ra.st.model.entity.Product;
import com.ra.st.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy danh sách đơn hàng theo trạng thái (phân trang)
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    // Lấy danh sách đơn hàng của người dùng
    List<Order> findByUser(Users user);

    List<Order> findByStatus(Order.OrderStatus status);

    // Tìm đơn hàng theo số serial
    Optional<Order> findBySerialNumberAndUser(String serialNumber, Users user);

    // Lấy danh sách đơn hàng của một người dùng theo trạng thái
    List<Order> findByUserAndStatus(Users user, Order.OrderStatus status);

    // Kiểm tra người dùng đã mua và nhận sản phẩm ở trạng thái cụ thể
    @Query("SELECT COUNT(o) > 0 " +
            "FROM Order o JOIN o.orderDetails od " +
            "WHERE o.user = :user " +
            "AND od.id.productId = :productId " +
            "AND o.status = :status")
    boolean existsByUserAndProductIdAndStatus(
            @Param("user") Users user,
            @Param("productId") Long productId,
            @Param("status") Order.OrderStatus status);
}