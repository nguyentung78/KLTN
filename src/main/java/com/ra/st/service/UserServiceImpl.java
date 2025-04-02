package com.ra.st.service;

import com.ra.st.model.dto.*;
import com.ra.st.model.entity.*;
import com.ra.st.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

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
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private WishListRepository wishListRepository;

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

        // Kiểm tra stockQuantity
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

        if (quantity <= 0) {
            shoppingCartRepository.delete(cartItem);
            return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng.");
        }

        cartItem.setOrderQuantity(quantity);
        shoppingCartRepository.save(cartItem);
        return ResponseEntity.ok("Cập nhật số lượng thành công!");
    }

    @Override
    public ResponseEntity<?> removeCartItem(Long cartItemId) {
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

        // Kiểm tra stockQuantity của từng sản phẩm trong giỏ hàng
        for (ShoppingCart item : cartItems) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getOrderQuantity()) {
                return ResponseEntity.badRequest()
                        .body("Sản phẩm " + product.getProductName() + " không đủ hàng! Còn lại: " + product.getStockQuantity());
            }
        }

        Address selectedAddress = null;

        // Trường hợp 1: Người dùng chọn địa chỉ đã lưu (addressId)
        if (request.getAddressId() != null) {
            // Lấy địa chỉ từ repository
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));

            // Kiểm tra xem địa chỉ có thuộc về người dùng hiện tại không
            if (!address.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest()
                        .body("Địa chỉ này không tồn tại hoặc không phải của bạn!");
            }
            selectedAddress = address;
        }
        // Trường hợp 2: Người dùng nhập địa chỉ mới
        else if (request.getReceiveAddress() != null && !request.getReceiveAddress().trim().isEmpty()
                && request.getReceivePhone() != null && !request.getReceivePhone().trim().isEmpty()
                && request.getReceiveName() != null && !request.getReceiveName().trim().isEmpty()) {
            // Kiểm tra định dạng số điện thoại
            if (!request.getReceivePhone().matches("^\\d{10}$")) {
                return ResponseEntity.badRequest()
                        .body("Số điện thoại nhận hàng phải có 10 chữ số!");
            }
            selectedAddress = new Address(null, currentUser, request.getReceiveAddress(),
                    request.getReceivePhone(), request.getReceiveName());
            // Lưu địa chỉ mới vào cơ sở dữ liệu (tùy chọn, nếu bạn muốn lưu lại)
            addressRepository.save(selectedAddress);
        }
        // Kiểm tra xem người dùng có chọn địa chỉ hay không
        if (selectedAddress == null) {
            List<Address> userAddresses = addressRepository.findByUser(currentUser);
            if (userAddresses.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Người dùng chưa có địa chỉ giao hàng, vui lòng thêm địa chỉ giao hàng!");
            }
            return ResponseEntity.badRequest()
                    .body("Vui lòng chọn địa chỉ giao hàng!");
        }

        // Tạo đơn hàng mới
        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setSerialNumber(UUID.randomUUID().toString());
        newOrder.setTotalPrice(cartItems.stream()
                .map(item -> item.getProduct().getUnitPrice().multiply(BigDecimal.valueOf(item.getOrderQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        newOrder.setStatus(Order.OrderStatus.WAITING);
        newOrder.setCreatedAt(new Date());
        newOrder.setReceiveAddress(selectedAddress.getFullAddress());
        newOrder.setReceivePhone(selectedAddress.getPhone());
        newOrder.setReceiveName(selectedAddress.getReceiveName());
        newOrder.setNote(request.getNote() != null ? request.getNote() : ""); // Gán note, mặc định là chuỗi rỗng nếu null

        orderRepository.save(newOrder);

        // Lưu chi tiết đơn hàng và giảm stockQuantity
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

            // Giảm stockQuantity
            product.setStockQuantity(product.getStockQuantity() - item.getOrderQuantity());
            productRepository.save(product);
        }

        shoppingCartRepository.deleteByUser(currentUser);

        return ResponseEntity.ok("Đặt hàng thành công!");
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
        boolean isUpdated = false; // Kiểm tra xem có gì được cập nhật không
        if (request.getEmail() != null && !request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.badRequest().body("Email không hợp lệ");
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (!request.getPhone().matches("^\\d{10}$")) {
                throw new RuntimeException("Số điện thoại phải có 10 chữ số!");
            }
            if (userRepository.existsByPhone(request.getPhone()) && !request.getPhone().equals(user.getPhone())) {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (userRepository.findUserByUsername(request.getUsername()) != null) {
                return ResponseEntity.badRequest().body("Username đã tồn tại!");
            }
            user.setUsername(request.getUsername());
            isUpdated = true;
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.findUsersByEmail(request.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email đã tồn tại!");
            }
            user.setEmail(request.getEmail());
            isUpdated = true;
        }

        if (request.getFullname() != null && !request.getFullname().trim().isEmpty()) {
            user.setFullname(request.getFullname());
            isUpdated = true;
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone());
            isUpdated = true;
        }

        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
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

        // Tìm đơn hàng theo serialNumber và user
        Order order = orderRepository.findBySerialNumberAndUser(serialNumber, currentUser)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại hoặc không thuộc về bạn!"));

        // Lấy danh sách chi tiết đơn hàng
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        List<OrderItemDTO> items = orderDetails.stream()
                .map(item -> new OrderItemDTO(
                        item.getProductId(),
                        item.getName(),
                        item.getUnitPrice(),
                        item.getOrderQuantity()
                ))
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

        // Tìm đơn hàng theo ID và user
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        // Kiểm tra quyền sở hữu
        if (!order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền hủy đơn hàng này!");
        }

        // Kiểm tra trạng thái đơn hàng
        if (!order.getStatus().equals(Order.OrderStatus.WAITING)) {
            return ResponseEntity.badRequest().body("Chỉ có thể hủy đơn hàng khi đang ở trạng thái chờ xác nhận!");
        }

        // Cập nhật trạng thái đơn hàng
        order.setStatus(Order.OrderStatus.CANCELLED);

        // Khôi phục số lượng tồn kho
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

    // Helper method để chuyển đổi Order thành OrderResponseDTO
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

        return ResponseEntity.ok("Thêm vào danh sách yêu thích thành công!");
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
    public List<WishListDTO> getUserWishList() {
        Users currentUser = getCurrentUser();
        List<WishList> wishLists = wishListRepository.findByUser(currentUser);

        return wishLists.stream().map(wishList -> new WishListDTO(
                wishList.getProduct().getId(),
                wishList.getProduct().getProductName(),
                wishList.getProduct().getImage(),
                wishList.getProduct().getDescription(),
                wishList.getProduct().getUnitPrice()
        )).collect(Collectors.toList());
    }
}