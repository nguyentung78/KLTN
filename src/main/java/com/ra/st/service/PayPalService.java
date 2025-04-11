package com.ra.st.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PayPalService {

    @Autowired
    private APIContext apiContext;

    // Tỷ giá hối đoái (hardcode tạm thời, bạn có thể lấy từ API)
    private static final double EXCHANGE_RATE_VND_TO_USD = 1.0 / 24000.0; // 1 USD = 24,000 VND

    // Phương thức chuyển đổi từ VND sang USD
    private Double convertVndToUsd(Double amountInVnd) {
        return BigDecimal.valueOf(amountInVnd)
                .multiply(BigDecimal.valueOf(EXCHANGE_RATE_VND_TO_USD))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public String createPayment(Double total, String currency, String description, String cancelUrl, String successUrl, boolean isVnd)
            throws PayPalRESTException {
        // Nếu số tiền là VND và cần chuyển sang USD
        Double finalTotal = isVnd ? convertVndToUsd(total) : total;
        String finalCurrency = isVnd ? "USD" : currency;

        log.info("Creating PayPal payment with total: {} {}, final: {} {}", total, currency, finalTotal, finalCurrency);

        BigDecimal totalBigDecimal = BigDecimal.valueOf(finalTotal).setScale(2, RoundingMode.HALF_UP);
        Amount amount = new Amount();
        amount.setCurrency(finalCurrency);
        amount.setTotal(totalBigDecimal.toString());

        // Thêm chi tiết mặt hàng
        ItemList itemList = new ItemList();
        List<Item> items = new ArrayList<>();
        Item item = new Item();
        item.setName("Sản phẩm mẫu");
        item.setCurrency(finalCurrency);
        item.setPrice(totalBigDecimal.toString());
        item.setQuantity("1");
        items.add(item);
        itemList.setItems(items);

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setItemList(itemList);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl); // successUrl đã được xây dựng đúng
        payment.setRedirectUrls(redirectUrls);

        Payment createdPayment = payment.create(apiContext);
        log.info("Payment created with ID: {}", createdPayment.getId());

        List<Links> links = createdPayment.getLinks();
        for (Links link : links) {
            if ("approval_url".equalsIgnoreCase(link.getRel())) {
                return link.getHref();
            }
        }

        throw new PayPalRESTException("Approval URL not found");
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        log.info("Executing payment with paymentId: {}, payerId: {}", paymentId, payerId);
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        Payment executedPayment = payment.execute(apiContext, paymentExecution);
        log.info("Payment executed with state: {}", executedPayment.getState());
        return executedPayment;
    }
}