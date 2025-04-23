package com.ra.st.repository;

import com.ra.st.model.entity.Product;
import com.ra.st.model.entity.Review;
import com.ra.st.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProduct(Product product, Pageable pageable);
    Optional<Review> findByUserAndProduct(Users user, Product product);
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product")
    Long countByProduct(Product product);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double findAverageRatingByProduct(Product product);
    boolean existsByUserAndProduct(Users user, Product product);
}