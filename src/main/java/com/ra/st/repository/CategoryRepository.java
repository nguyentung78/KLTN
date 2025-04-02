package com.ra.st.repository;

import com.ra.st.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ✅ Kiểm tra xem danh mục có tồn tại theo tên hay không
    boolean existsByCategoryName(String categoryName);
    boolean existsByCategoryNameAndCategoryIdNot(String categoryName, Long categoryId);

    // ✅ Tìm danh mục theo tên (phân trang)
    Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);
}
