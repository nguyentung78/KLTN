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
    boolean existsByCategoryAndFeatured(Category category, boolean featured);

    @Query("SELECT p FROM Product p ORDER BY p.soldQuantity DESC")
    List<Product> findTopBestSellingProducts(Pageable pageable);

    // Lấy danh sách sản phẩm có phân trang & sắp xếp
    Page<Product> findAll(Pageable pageable);

    // Tìm kiếm sản phẩm theo tên hoặc mô tả (phân trang)
    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);
    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    // Lấy danh sách sản phẩm nổi bật
    List<Product> findByFeaturedTrue();

    // Lấy danh sách sản phẩm theo danh mục
    List<Product> findByCategoryId(Long categoryId);

    // Lấy danh sách sản phẩm mới nhất (sắp xếp theo createdAt giảm dần)
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findNewProducts(Pageable pageable);
}
