package com.ra.st.service;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.ra.st.model.dto.*;
import com.ra.st.model.entity.*;
import com.ra.st.repository.*;
import com.ra.st.security.UserPrinciple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private WishListRepository wishListRepository;
    @Autowired
    private PayPalService payPalService;

    //=======================CART=========================

    @Override
    public List<CartItemResponseDTO> getCartItems() {
        Users currentUser = getCurrentUser();
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUser(currentUser);

        return cartItems.stream().map(item -> new CartItemResponseDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getProduct().getImage(),
                item.getProduct().getUnitPrice(),
                item.getOrderQuantity()
        )).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> addToCart(CartItemRequestDTO request) {
        Users currentUser = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (request.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Số lượng phải lớn hơn 0!");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            return ResponseEntity.badRequest().body("Sản phẩm " + product.getProductName() + " không đủ hàng! Còn lại: " + product.getStockQuantity());
        }

        ShoppingCart cartItem = shoppingCartRepository.findByUserAndProduct(currentUser, product)
                .orElse(new ShoppingCart(null, currentUser, product, 0));

        int newQuantity = cartItem.getOrderQuantity() + request.getQuantity();
        if (product.getStockQuantity() < newQuantity) {
            return ResponseEntity.badRequest().body("Sản phẩm " + product.getProductName() + " không đủ hàng! Còn lại: " + product.getStockQuantity());
        }

        cartItem.setOrderQuantity(newQuantity);
        shoppingCartRepository.save(cartItem);

        return ResponseEntity.ok("Thêm sản phẩm vào giỏ hàng thành công!");
    }

    @Override
    public ResponseEntity<?> updateCartItem(Long cartItemId, Integer quantity) {
        ShoppingCart cartItem = shoppingCartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm trong giỏ hàng không tồn tại!"));

        Users currentUser = getCurrentUser();

        if (!cartItem.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Bạn không có quyền chỉnh sửa mục giỏ hàng này!");
        }

        if (quantity <= 0) {
            shoppingCartRepository.delete(cartItem);
            return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng.");
        }

        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < quantity) {
            return ResponseEntity.badRequest()
                    .body("Sản phẩm " + product.getProductName() + " không đủ hàng! Còn lại: " + product.getStockQuantity());
        }

        cartItem.setOrderQuantity(quantity);
        shoppingCartRepository.save(cartItem);
        return ResponseEntity.ok("Cập nhật số lượng thành công!");
    }

    @Override
    public ResponseEntity<?> removeCartItem(Long cartItemId) {
        ShoppingCart cartItem = shoppingCartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm trong giỏ hàng không tồn tại!"));

        Users currentUser = getCurrentUser();

        if (!cartItem.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Bạn không có quyền xóa mục giỏ hàng này!");
        }

        shoppingCartRepository.deleteById(cartItemId);
        return ResponseEntity.ok("Xóa sản phẩm khỏi giỏ hàng thành công!");
    }

    @Override
    @Transactional
    public ResponseEntity<?> clearCart() {
        Users currentUser = getCurrentUser();
        shoppingCartRepository.deleteByUser(currentUser);
        return ResponseEntity.ok("Xóa toàn bộ giỏ hàng thành công!");
    }

    @Override
    @Transactional
    public ResponseEntity<?> checkout(CheckoutRequestDTO request) {
        Users currentUser = getCurrentUser();
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUser(currentUser);

        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống!");
        }

        for (ShoppingCart item : cartItems) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getOrderQuantity()) {
                return ResponseEntity.badRequest()
                        .body("Sản phẩm " + product.getProductName() + " không đủ hàng! Còn lại: " + product.getStockQuantity());
            }
        }

        Address selectedAddress = null;

        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));
            if (!address.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest()
                        .body("Địa chỉ này không tồn tại hoặc không phải của bạn!");
            }
            selectedAddress = address;
        } else if (request.getReceiveAddress() != null && !request.getReceiveAddress().trim().isEmpty()
                && request.getReceivePhone() != null && !request.getReceivePhone().trim().isEmpty()
                && request.getReceiveName() != null && !request.getReceiveName().trim().isEmpty()) {
            if (!request.getReceivePhone().matches("^\\d{10}$")) {
                return ResponseEntity.badRequest()
                        .body("Số điện thoại nhận hàng phải có 10 chữ số!");
            }
            selectedAddress = new Address(null, currentUser, request.getReceiveAddress(),
                    request.getReceivePhone(), request.getReceiveName());
            addressRepository.save(selectedAddress);
        }

        if (selectedAddress == null) {
            List<Address> userAddresses = addressRepository.findByUser(currentUser);
            if (userAddresses.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Người dùng chưa có địa chỉ giao hàng, vui lòng thêm địa chỉ giao hàng!");
            }
            return ResponseEntity.badRequest()
                    .body("Vui lòng chọn địa chỉ giao hàng!");
        }

        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setSerialNumber(UUID.randomUUID().toString());
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getUnitPrice().multiply(BigDecimal.valueOf(item.getOrderQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        newOrder.setTotalPrice(totalPrice);
        newOrder.setStatus(Order.OrderStatus.WAITING);
        newOrder.setCreatedAt(new Date());
        newOrder.setReceiveAddress(selectedAddress.getFullAddress());
        newOrder.setReceivePhone(selectedAddress.getPhone());
        newOrder.setReceiveName(selectedAddress.getReceiveName());
        newOrder.setNote(request.getNote() != null ? request.getNote() : "");

        orderRepository.save(newOrder);

        for (ShoppingCart item : cartItems) {
            Product product = item.getProduct();
            OrderDetailKey orderDetailKey = new OrderDetailKey(newOrder.getId(), product.getId());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setId(orderDetailKey);
            orderDetail.setOrder(newOrder);
            orderDetail.setOrderQuantity(item.getOrderQuantity());
            orderDetail.setUnitPrice(product.getUnitPrice());
            orderDetail.setName(product.getProductName());

            orderDetailRepository.save(orderDetail);

            product.setStockQuantity(product.getStockQuantity() - item.getOrderQuantity());
            productRepository.save(product);
        }

        shoppingCartRepository.deleteByUser(currentUser);

        if ("paypal".equalsIgnoreCase(request.getPaymentMethod())) {
            try {
                String approvalUrl = payPalService.createPayment(
                        totalPrice.doubleValue(),
                        "VND",
                        "Thanh toán đơn hàng #" + newOrder.getSerialNumber(),
                        "http://localhost:5173/user/cart/checkout/cancel?orderId=" + newOrder.getId(),
                        "http://localhost:5173/user/cart/checkout/success?orderId=" + newOrder.getId(),
                        true
                );

                Map<String, String> response = new HashMap<>();
                response.put("message", "Đơn hàng đã được tạo, vui lòng hoàn tất thanh toán!");
                response.put("redirectUrl", approvalUrl);
                response.put("orderId", newOrder.getId().toString());
                return ResponseEntity.ok(response);
            } catch (PayPalRESTException e) {
                orderRepository.delete(newOrder);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Lỗi khi khởi tạo thanh toán PayPal: " + e.getMessage());
            }
        } else {
            return ResponseEntity.ok("Đặt hàng thành công với phương thức COD!");
        }
    }

    //=======================ACCOUNT=========================

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại!");
        }
        return user;
    }

    @Override
    public UserProfileResponseDTO getUserProfile() {
        Users user = getCurrentUser();
        return new UserProfileResponseDTO(
                user.getUsername(), user.getEmail(), user.getFullname(),
                user.getPhone(), user.getAddress(), user.getAvatar()
        );
    }

    @Override
    public ResponseEntity<?> updateUserProfile(UserProfileUpdateDTO request) {
        Users user = getCurrentUser();
        boolean isUpdated = false;

        if (request.getUsername() != null) {
            if (request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username không được để trống!");
            }
            if (!request.getUsername().equals(user.getUsername())) {
                Users existingUser = userRepository.findUserByUsername(request.getUsername());
                if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                    return ResponseEntity.badRequest().body("Username đã tồn tại!");
                }
                user.setUsername(request.getUsername());
                isUpdated = true;
            }
        }

        if (request.getEmail() != null) {
            if (request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email không được để trống!");
            }
            if (!request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest().body("Email không hợp lệ!");
            }
            if (!request.getEmail().equals(user.getEmail())) {
                Users existingEmailUser = userRepository.findUsersByEmail(request.getEmail());
                if (existingEmailUser != null && !existingEmailUser.getId().equals(user.getId())) {
                    return ResponseEntity.badRequest().body("Email đã tồn tại!");
                }
                user.setEmail(request.getEmail());
                isUpdated = true;
            }
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (!request.getPhone().matches("^\\d{10}$")) {
                return ResponseEntity.badRequest().body("Số điện thoại phải có 10 chữ số!");
            }
            if (!request.getPhone().equals(user.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
                return ResponseEntity.badRequest().body("Số điện thoại đã tồn tại!");
            }
            user.setPhone(request.getPhone());
            isUpdated = true;
        }

        if (request.getFullname() != null && !request.getFullname().trim().isEmpty()
                && !request.getFullname().equals(user.getFullname())) {
            user.setFullname(request.getFullname());
            isUpdated = true;
        }

        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()
                && !request.getAddress().equals(user.getAddress())) {
            user.setAddress(request.getAddress());
            isUpdated = true;
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String imageUrl = uploadService.uploadFile(request.getAvatar());
            user.setAvatar(imageUrl);
            isUpdated = true;
        }

        if (!isUpdated) {
            return ResponseEntity.badRequest().body("Không có dữ liệu nào để cập nhật!");
        }

        userRepository.save(user);
        return ResponseEntity.ok("Cập nhật thông tin thành công!");
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePasswordRequestDTO request) {
        Users user = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không chính xác!");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu mới không khớp!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    @Override
    public ResponseEntity<?> addAddress(AddressDTO request) {
        Users user = getCurrentUser();

        if (request.getFullAddress() == null || request.getFullAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập địa chỉ đầy đủ!");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập số điện thoại!");
        }
        if (request.getReceiveName() == null || request.getReceiveName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập tên người nhận!");
        }

        Address address = new Address(null, user, request.getFullAddress(), request.getPhone(), request.getReceiveName());
        addressRepository.save(address);
        return ResponseEntity.ok("Thêm địa chỉ thành công!");
    }

    @Override
    public ResponseEntity<?> deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));
        Users currentUser = getCurrentUser();
        if (!address.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền xóa địa chỉ này!");
        }
        addressRepository.deleteById(addressId);
        return ResponseEntity.ok("Xóa địa chỉ thành công!");
    }

    @Override
    public List<AddressDTO> getUserAddresses() {
        Users currentUser = getCurrentUser();
        return addressRepository.findByUser(currentUser).stream()
                .map(a -> new AddressDTO(a.getId(), a.getFullAddress(), a.getPhone(), a.getReceiveName()))
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));
        Users currentUser = getCurrentUser();
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập địa chỉ này!");
        }
        return new AddressDTO(address.getId(), address.getFullAddress(), address.getPhone(), address.getReceiveName());
    }

    //=======================HISTORY=========================

    @Override
    public List<OrderResponseDTO> getOrderHistory() {
        Users currentUser = getCurrentUser();
        List<Order> orders = orderRepository.findByUser(currentUser);

        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByStatus(String orderStatus) {
        Users currentUser = getCurrentUser();
        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.valueOf(orderStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái đơn hàng không hợp lệ: " + orderStatus);
        }

        List<Order> orders = orderRepository.findByUserAndStatus(currentUser, status);
        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDetailResponseDTO getOrderDetail(String serialNumber) {
        Users currentUser = getCurrentUser();

        Order order = orderRepository.findBySerialNumberAndUser(serialNumber, currentUser)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại hoặc không thuộc về bạn!"));

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        List<OrderItemDTO> items = orderDetails.stream()
                .map(item -> {
                    // Lấy productId từ OrderDetail
                    Long productId = item.getProductId();
                    // Lấy Product từ productRepository để kiểm tra isReviewed
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
                    // Kiểm tra xem sản phẩm đã được đánh giá bởi người dùng chưa
                    boolean isReviewed = reviewRepository.existsByUserAndProduct(currentUser, product);
                    return new OrderItemDTO(
                            productId,
                            item.getName(),
                            item.getUnitPrice(),
                            item.getOrderQuantity(),
                            isReviewed
                    );
                })
                .collect(Collectors.toList());

        return OrderDetailResponseDTO.builder()
                .id(order.getId())
                .serialNumber(order.getSerialNumber())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> cancelOrder(Long orderId) {
        Users currentUser = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền hủy đơn hàng này!");
        }

        if (!order.getStatus().equals(Order.OrderStatus.WAITING)) {
            return ResponseEntity.badRequest().body("Chỉ có thể hủy đơn hàng khi đang ở trạng thái chờ xác nhận!");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        for (OrderDetail od : orderDetails) {
            Product product = productRepository.findById(od.getProductId())
                    .orElse(null);
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + od.getOrderQuantity());
                productRepository.save(product);
            }
        }

        orderRepository.save(order);
        return ResponseEntity.ok("Hủy đơn hàng thành công và tồn kho đã được khôi phục!");
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .serialNumber(order.getSerialNumber())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .build();
    }

    //=======================WISHLIST=========================

    @Override
    public ResponseEntity<?> addToWishList(Long productId) {
        Users currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (wishListRepository.findByUserAndProductId(currentUser, productId).isPresent()) {
            return ResponseEntity.badRequest().body("Sản phẩm đã có trong danh sách yêu thích!");
        }

        WishList wishList = new WishList();
        wishList.setUser(currentUser);
        wishList.setProduct(product);
        wishListRepository.save(wishList);

        WishListDTO wishListDTO = new WishListDTO(
                product.getId(),
                product.getProductName(),
                product.getImage(),
                product.getDescription(),
                product.getUnitPrice()
        );
        return ResponseEntity.ok(wishListDTO);
    }

    @Override
    @Transactional
    public ResponseEntity<?> removeFromWishList(Long productId) {
        Users currentUser = getCurrentUser();
        if (!wishListRepository.findByUserAndProductId(currentUser, productId).isPresent()) {
            return ResponseEntity.badRequest().body("Sản phẩm không có trong danh sách yêu thích!");
        }

        wishListRepository.deleteByUserAndProductId(currentUser, productId);
        return ResponseEntity.ok("Xóa sản phẩm khỏi danh sách yêu thích thành công!");
    }

    @Override
    public WishListResponseDTO getUserWishList(int page, int size) {
        Users currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<WishList> wishListPage = wishListRepository.findByUser(currentUser, pageable);

        List<WishListDTO> content = wishListPage.getContent().stream()
                .map(wishList -> new WishListDTO(
                        wishList.getProduct().getId(),
                        wishList.getProduct().getProductName(),
                        wishList.getProduct().getImage(),
                        wishList.getProduct().getDescription(),
                        wishList.getProduct().getUnitPrice()
                ))
                .collect(Collectors.toList());

        return WishListResponseDTO.builder()
                .content(content)
                .page(wishListPage.getNumber())
                .size(wishListPage.getSize())
                .totalElements(wishListPage.getTotalElements())
                .totalPages(wishListPage.getTotalPages())
                .build();
    }

    //=======================PayPal=========================

    @Override
    @Transactional
    public ResponseEntity<?> completePaypalPayment(String paymentId, String payerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        if (order.getStatus().equals(Order.OrderStatus.CONFIRMED)) {
            return ResponseEntity.ok("Thanh toán thành công đơn hàng #" + order.getSerialNumber());
        }

        if (!order.getStatus().equals(Order.OrderStatus.WAITING)) {
            return ResponseEntity.badRequest()
                    .body("Đơn hàng không ở trạng thái chờ thanh toán!");
        }

        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            if ("approved".equals(payment.getState())) {
                order.setStatus(Order.OrderStatus.CONFIRMED);
                orderRepository.save(order);
                return ResponseEntity.ok("Thanh toán thành công! Đơn hàng #" + order.getSerialNumber() + " đã được xác nhận.");
            } else {
                return ResponseEntity.badRequest()
                        .body("Thanh toán không được phê duyệt!");
            }
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi hoàn tất thanh toán: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> cancelPaypalPayment(Long orderId) {
        Users currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Bạn không có quyền hủy thanh toán cho đơn hàng này!");
        }

        if (!order.getStatus().equals(Order.OrderStatus.WAITING)) {
            return ResponseEntity.badRequest()
                    .body("Đơn hàng không ở trạng thái chờ thanh toán!");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        for (OrderDetail od : orderDetails) {
            Product product = productRepository.findById(od.getProductId()).orElse(null);
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + od.getOrderQuantity());
                productRepository.save(product);
            }
        }
        orderRepository.save(order);
        return ResponseEntity.ok("Đã hủy thanh toán cho đơn hàng #" + order.getSerialNumber());
    }

    //=======================REVIEW=========================

    @Override
    @Transactional
    public ResponseEntity<?> submitReview(Long productId, ReviewRequestDTO reviewRequest) {
        // Lấy người dùng hiện tại từ Security Context
        Users user = ((UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // Kiểm tra người dùng đã mua và nhận sản phẩm (đơn hàng ở trạng thái DELIVERED)
        boolean hasPurchasedAndDelivered = orderRepository.existsByUserAndProductIdAndStatus(user, productId, Order.OrderStatus.DELIVERED);
        if (!hasPurchasedAndDelivered) {
            return ResponseEntity.badRequest().body("Bạn chỉ có thể đánh giá sản phẩm đã mua và nhận!");
        }

        // Kiểm tra dữ liệu đầu vào
        if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
            return ResponseEntity.badRequest().body("Điểm đánh giá phải từ 1 đến 5!");
        }
        if (reviewRequest.getComment() == null || reviewRequest.getComment().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nội dung đánh giá không được để trống!");
        }

        // Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
        if (reviewRepository.existsByUserAndProduct(user, product)) {
            return ResponseEntity.badRequest().body("Bạn đã đánh giá sản phẩm này trước đó!");
        }

        // Tạo và lưu đánh giá
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .createdAt(new Date())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Chuẩn hóa phản hồi
        ReviewResponseDTO responseDTO = ReviewResponseDTO.builder()
                .id(savedReview.getId())
                .username(user.getUsername())
                .rating(savedReview.getRating())
                .comment(savedReview.getComment())
                .createdAt(savedReview.getCreatedAt())
                .replies(new ArrayList<>())
                .build();

        return ResponseEntity.ok(responseDTO);
    }
}