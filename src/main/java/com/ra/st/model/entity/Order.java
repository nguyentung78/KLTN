package com.ra.st.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "serial_number", nullable = false, length = 100, unique = true)
    private String serialNumber; // UUID tự sinh

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // Người đặt hàng

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "note", length = 100)
    private String note;

    @Column(name = "receive_name", nullable = false, length = 100)
    private String receiveName;

    @Column(name = "receive_address", nullable = false, length = 255)
    private String receiveAddress;

    @Column(name = "receive_phone", nullable = false, length = 15)
    private String receivePhone;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "received_at")
    private Date receivedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderDetail> orderDetails;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    public enum OrderStatus {
        WAITING,    // Đơn hàng mới chờ xác nhận
        CONFIRMED,  // Đã xác nhận
        DELIVERING, // Đang giao hàng
        DELIVERED,  // Đã giao hàng
        CANCELLED   // Đã hủy
    }
}
