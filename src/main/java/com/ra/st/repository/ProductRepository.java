package com.ra.st.repository;

import com.ra.st.model.entity.Category;
import com.ra.st.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByProductName(String productName);

    @Query("SELECT p, SUM(od.orderQuantity) as totalSold " +
            "FROM OrderDetail od " +
            "JOIN Product p ON p.id = od.id.productId " +
            "WHERE od.order.status = 'DELIVERED' " +
            "GROUP BY p " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts(Pageable pageable);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

    List<Product> findByFeaturedTrue();

    List<Product> findByCategoryCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findNewProducts(Pageable pageable);

    boolean existsByCategoryCategoryId(Long categoryId);
}