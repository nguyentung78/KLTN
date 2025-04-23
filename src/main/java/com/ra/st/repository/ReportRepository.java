package com.ra.st.repository;

import com.ra.st.model.entity.Order;
import com.ra.st.model.entity.Product;
import com.ra.st.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface ReportRepository extends JpaRepository<Order, Long> {

    // Doanh thu theo thời gian
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.createdAt BETWEEN :fromDate AND :toDate")
    BigDecimal getSalesRevenueOverTime(Date fromDate, Date toDate);

    // Số lượng đơn hàng theo thời gian
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :fromDate AND :toDate")
    Long getTotalOrdersOverTime(Date fromDate, Date toDate);

    @Query("SELECT DATE(o.createdAt), SUM(o.totalPrice) " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :fromDate AND :toDate " +
            "GROUP BY DATE(o.createdAt) " +
            "ORDER BY DATE(o.createdAt)")
    List<Object[]> getSalesRevenueOverTimeByDay(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query("SELECT DATE(o.createdAt), COUNT(o) " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :fromDate AND :toDate " +
            "GROUP BY DATE(o.createdAt) " +
            "ORDER BY DATE(o.createdAt)")
    List<Object[]> getInvoicesOverTimeByDay(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    // Sản phẩm bán chạy nhất
    @Query("SELECT p.productName, SUM(od.orderQuantity) as totalSold " +
            "FROM OrderDetail od " +
            "JOIN Product p ON p.id = od.id.productId " +
            "WHERE od.order.status = 'DELIVERED' " +
            "GROUP BY p.productName " +
            "ORDER BY totalSold DESC")
    List<Object[]> getBestSellerProducts(Pageable pageable);

    // Sản phẩm yêu thích nhất
    @Query("SELECT p.productName, COUNT(w) as likeCount " +
            "FROM WishList w JOIN w.product p " +
            "GROUP BY p.productName " +
            "ORDER BY likeCount DESC")
    List<Object[]> getMostLikedProducts(Pageable pageable);

    // Doanh thu theo danh mục
    @Query("SELECT c.categoryName, SUM(o.totalPrice) as totalRevenue " +
            "FROM Order o JOIN o.orderDetails od " +
            "JOIN Product p ON p.id = od.id.productId " +
            "JOIN p.category c " +
            "GROUP BY c.categoryName")
    List<Object[]> getRevenueByCategory();

    // Khách hàng chi tiêu nhiều nhất
    @Query("SELECT u.username, SUM(o.totalPrice) as totalSpent " +
            "FROM Order o JOIN o.user u " +
            "GROUP BY u.username " +
            "ORDER BY totalSpent DESC")
    List<Object[]> getTopSpendingCustomers(Pageable pageable);

    // Tài khoản mới trong tháng
    @Query("SELECT COUNT(u) FROM Users u WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE) AND YEAR(u.createdAt) = YEAR(CURRENT_DATE)")
    Long getNewAccountsThisMonth();

    @Query("SELECT u.username, u.email, u.createdAt " +
            "FROM Users u " +
            "WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE) AND YEAR(u.createdAt) = YEAR(CURRENT_DATE)")
    List<Object[]> getNewAccountListThisMonth(Pageable pageable);

    @Query("SELECT p.id, p.productName, AVG(r.rating), COUNT(r.id) " +
            "FROM Product p " +
            "LEFT JOIN Review r ON r.product.id = p.id " +
            "GROUP BY p.id, p.productName")
    Page<Object[]> getProductsWithAverageRating(Pageable pageable);

    @Query("SELECT p.id, p.productName, AVG(r.rating), COUNT(r.id) " +
            "FROM Product p " +
            "LEFT JOIN Review r ON r.product.id = p.id " +
            "WHERE p.productName LIKE %:keyword% " +
            "GROUP BY p.id, p.productName")
    Page<Object[]> searchProductsWithAverageRating(@Param("keyword") String keyword, Pageable pageable);
}