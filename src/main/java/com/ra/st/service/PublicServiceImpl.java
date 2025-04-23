package com.ra.st.service;

import com.ra.st.model.dto.CategoryResponseDTO;
import com.ra.st.model.dto.ProductResponseDTO;
import com.ra.st.model.dto.ReviewReplyDTO;
import com.ra.st.model.dto.ReviewResponseDTO;
import com.ra.st.model.entity.Category;
import com.ra.st.model.entity.Product;
import com.ra.st.model.entity.Review;
import com.ra.st.repository.CategoryRepository;
import com.ra.st.repository.ProductRepository;
import com.ra.st.repository.ReviewReplyRepository;
import com.ra.st.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicServiceImpl implements PublicService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReplyRepository reviewReplyRepository;

    // Phương thức tiện ích để chuyển đổi Product sang ProductResponseDTO
    private ProductResponseDTO convertToProductResponseDTO(Product product) {
        CategoryResponseDTO categoryDTO = null;
        if (product.getCategory() != null) {
            Category category = product.getCategory();
            categoryDTO = new CategoryResponseDTO(
                    category.getCategoryId(),
                    category.getCategoryName(),
                    category.getDescription(),
                    category.getStatus()
            );
        }

        // Tính số sao trung bình và số lượng đánh giá
        Double averageRating = reviewRepository.findAverageRatingByProduct(product);
        Long reviewCount = reviewRepository.countByProduct(product);

        return ProductResponseDTO.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .unitPrice(product.getUnitPrice())
                .stockQuantity(product.getStockQuantity())
                .image(product.getImage())
                .category(categoryDTO)
                .featured(product.getFeatured())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .build();
    }

    @Override
    public ResponseEntity<?> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponseDTO(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getDescription(),
                        category.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @Override
    public Page<ProductResponseDTO> getAllProducts(int page, int size, String sortBy, String order) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(this::convertToProductResponseDTO);
    }

    @Override
    public Page<ProductResponseDTO> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

        return products.map(this::convertToProductResponseDTO);
    }

    @Override
    public ResponseEntity<?> getFeaturedProducts() {
        List<ProductResponseDTO> products = productRepository.findByFeaturedTrue()
                .stream()
                .map(this::convertToProductResponseDTO)
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            products = productRepository.findNewProducts(pageable)
                    .stream()
                    .map(this::convertToProductResponseDTO)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(products);
    }

    @Override
    public ResponseEntity<?> getNewProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductResponseDTO> products = productRepository.findNewProducts(pageable)
                .stream()
                .map(this::convertToProductResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @Override
    public ResponseEntity<?> getBestSellerProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> results = productRepository.findBestSellingProducts(pageable);

        List<ProductResponseDTO> products = results.stream()
                .map(result -> {
                    Product product = (Product) result[0];
                    return convertToProductResponseDTO(product);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @Override
    public ResponseEntity<?> getProductsByCategory(Long categoryId) {
        List<ProductResponseDTO> products = productRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(this::convertToProductResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @Override
    public ResponseEntity<?> getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
        return ResponseEntity.ok(convertToProductResponseDTO(product));
    }

    @Override
    public Page<ReviewResponseDTO> getReviewsByProduct(Long productId, int page, int size) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByProduct(product, pageable);

        return reviews.map(review -> {
            List<ReviewReplyDTO> replies = reviewReplyRepository.findByReview(review)
                    .stream()
                    .map(reply -> ReviewReplyDTO.builder()
                            .id(reply.getId())
                            .reply(reply.getReply())
                            .createdAt(reply.getCreatedAt())
                            .adminUsername(reply.getAdmin().getUsername())
                            .adminAvatar(reply.getAdmin().getAvatar())
                            .build())
                    .collect(Collectors.toList());

            return ReviewResponseDTO.builder()
                    .id(review.getId())
                    .username(review.getUser().getUsername())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .createdAt(review.getCreatedAt())
                    .replies(replies)
                    .build();
        });
    }

    @Override
    public ResponseEntity<?> getAverageRatingByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
        Double averageRating = reviewRepository.findAverageRatingByProduct(product);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }
}
