package com.ra.st.repository;

import com.ra.st.model.entity.Order;
import com.ra.st.model.entity.OrderDetail;
import com.ra.st.model.entity.OrderDetailKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailKey> {

    // ✅ Lấy danh sách chi tiết đơn hàng theo Order
    List<OrderDetail> findByOrder(Order order);

    // ✅ Lấy danh sách chi tiết đơn hàng theo orderId

}
