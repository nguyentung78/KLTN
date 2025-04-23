package com.ra.st.service;

import com.ra.st.model.dto.CategoryResponseDTO;
import com.ra.st.model.dto.ProductResponseDTO;
import com.ra.st.model.dto.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface PublicService {
    ResponseEntity<?> getAllCategories();
    Page<ProductResponseDTO> getAllProducts(int page, int size, String sortBy, String order);
    Page<ProductResponseDTO> searchProducts(String keyword, int page, int size);
    ResponseEntity<?> getFeaturedProducts();
    ResponseEntity<?> getNewProducts();
    ResponseEntity<?> getBestSellerProducts();
    ResponseEntity<?> getProductsByCategory(Long categoryId);
    ResponseEntity<?> getProductById(Long productId);
    Page<ReviewResponseDTO> getReviewsByProduct(Long productId, int page, int size);
    ResponseEntity<?> getAverageRatingByProduct(Long productId);
}
