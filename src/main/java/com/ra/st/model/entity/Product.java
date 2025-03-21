package com.ra.st.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "sku", nullable = false, length = 100, unique = true)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 100, unique = true)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "sold_quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer soldQuantity;

    @Column(name = "featured", nullable = false)
    private Boolean featured;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (featured == null) {
            featured = false;
        }
        if (soldQuantity == null) {
            soldQuantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
        this.featured = (this.soldQuantity >= 100);
    }
}
