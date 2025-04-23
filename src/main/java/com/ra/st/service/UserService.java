package com.ra.st.service;

import com.ra.st.model.dto.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    // CART
    List<CartItemResponseDTO> getCartItems();
    ResponseEntity<?> addToCart(CartItemRequestDTO request);
    ResponseEntity<?> updateCartItem(Long cartItemId, Integer quantity);
    ResponseEntity<?> removeCartItem(Long cartItemId);
    ResponseEntity<?> clearCart();
    ResponseEntity<?> checkout(CheckoutRequestDTO request);

    // ACCOUNT
    UserProfileResponseDTO getUserProfile();
    ResponseEntity<?> updateUserProfile(UserProfileUpdateDTO request);
    ResponseEntity<?> changePassword(ChangePasswordRequestDTO request);

    // ADDRESS
    ResponseEntity<?> addAddress(AddressDTO request);
    ResponseEntity<?> deleteAddress(Long addressId);
    List<AddressDTO> getUserAddresses();
    AddressDTO getAddressById(Long addressId);

    // HISTORY
    List<OrderResponseDTO> getOrderHistory();
    OrderDetailResponseDTO getOrderDetail(String serialNumber);
    List<OrderResponseDTO> getOrdersByStatus(String orderStatus);
    ResponseEntity<?> cancelOrder(Long orderId);

    // WISHLIST
    ResponseEntity<?> addToWishList(Long productId);
    ResponseEntity<?> removeFromWishList(Long productId);
    WishListResponseDTO getUserWishList(int page, int size);

    //PayPal
    ResponseEntity<?> completePaypalPayment(String paymentId, String payerId, Long orderId);
    ResponseEntity<?> cancelPaypalPayment(Long orderId);

    //REVIEW
    ResponseEntity<?> submitReview(Long productId, ReviewRequestDTO reviewRequest);

}
