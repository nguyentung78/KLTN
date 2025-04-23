package com.ra.st.controller;

import com.ra.st.model.dto.CategoryResponseDTO;
import com.ra.st.model.dto.ProductResponseDTO;
import com.ra.st.model.dto.ReviewResponseDTO;
import com.ra.st.service.PublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @Autowired
    private PublicService publicService;

    @GetMapping("/products/{productId}/reviews")
    public Page<ReviewResponseDTO> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return publicService.getReviewsByProduct(productId, page, size);
    }

    @GetMapping("/products/{productId}/average-rating")
    public ResponseEntity<?> getAverageRatingByProduct(@PathVariable Long productId) {
        return publicService.getAverageRatingByProduct(productId);
    }
    // Danh sách danh mục được bán
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return publicService.getAllCategories();
    }

    // Danh sách sản phẩm được bán (có phân trang và sắp xếp)
    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        return ResponseEntity.ok(publicService.getAllProducts(page, size, sortBy, order));
    }

    // Tìm kiếm sản phẩm theo tên hoặc mô tả
    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(publicService.searchProducts(keyword, page, size));
    }

    // Danh sách sản phẩm nổi bật
    @GetMapping("/products/featured-products")
    public ResponseEntity<?> getFeaturedProducts() {
        return publicService.getFeaturedProducts();
    }

    // Danh sách sản phẩm mới
    @GetMapping("/products/new-products")
    public ResponseEntity<?> getNewProducts() {
        return publicService.getNewProducts();
    }

    // Dsách sản phẩm bán chạy
    @GetMapping("/products/best-seller-products")
    public ResponseEntity<?> getBestSellerProducts() {
        return publicService.getBestSellerProducts();
    }

    // Danh sách sản phẩm theo danh mục
    @GetMapping("/products/categories/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable Long categoryId) {
        return publicService.getProductsByCategory(categoryId);
    }

    // Chi tiết thông tin sản phẩm theo ID
    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        return publicService.getProductById(productId);
    }
}
