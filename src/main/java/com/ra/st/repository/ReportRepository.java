package com.ra.st.repository;

import com.ra.st.model.entity.Order;
import com.ra.st.model.entity.Product;
import com.ra.st.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface ReportRepository extends JpaRepository<Order, Long> {

    // ✅ Doanh thu theo thời gian
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.createdAt BETWEEN :fromDate AND :toDate")
    BigDecimal getSalesRevenueOverTime(Date fromDate, Date toDate);

    // ✅ Số lượng đơn hàng theo thời gian
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :fromDate AND :toDate")
    Long getTotalOrdersOverTime(Date fromDate, Date toDate);

    // ✅ Sản phẩm bán chạy nhất
    @Query("SELECT p.productName FROM Product p ORDER BY p.soldQuantity DESC LIMIT 10")
    List<String> getBestSellerProducts();

    // ✅ Sản phẩm yêu thích nhất
    @Query("SELECT p.productName FROM WishList w JOIN w.product p GROUP BY p ORDER BY COUNT(w) DESC LIMIT 10")
    List<String> getMostLikedProducts();

    // ✅ Doanh thu theo danh mục
    @Query("SELECT c.categoryName, SUM(o.totalPrice) FROM Order o JOIN o.orderDetails od JOIN od.product p JOIN p.category c GROUP BY c.categoryName")
    List<Object[]> getRevenueByCategory();

    // ✅ Khách hàng chi tiêu nhiều nhất
    @Query("SELECT u.username, SUM(o.totalPrice) FROM Order o JOIN o.user u GROUP BY u ORDER BY SUM(o.totalPrice) DESC LIMIT 10")
    List<Object[]> getTopSpendingCustomers();

    // ✅ Tài khoản mới trong tháng
    @Query("SELECT COUNT(u) FROM Users u WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE) AND YEAR(u.createdAt) = YEAR(CURRENT_DATE)")
    Long getNewAccountsThisMonth();

    @Query("SELECT u.username FROM Users u WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE) AND YEAR(u.createdAt) = YEAR(CURRENT_DATE)")
    List<String> getNewAccountListThisMonth();
}
