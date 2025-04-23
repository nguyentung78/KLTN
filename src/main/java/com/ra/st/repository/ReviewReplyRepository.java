package com.ra.st.repository;

import com.ra.st.model.entity.Review;
import com.ra.st.model.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    List<ReviewReply> findByReview(Review review);
}