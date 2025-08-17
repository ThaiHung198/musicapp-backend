package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.subscription.SubscriptionPlanDto;
import com.musicapp.backend.dto.transaction.CreatePaymentRequest;
import com.musicapp.backend.dto.transaction.PaymentResponse;
import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.entity.Transaction;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.exception.UnauthorizedException;
import com.musicapp.backend.mapper.TransactionMapper;
import com.musicapp.backend.repository.TransactionRepository;
import com.musicapp.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final MoMoService moMoService;
    private final VnpayService vnpayService;
    private final TransactionMapper transactionMapper;

    @Transactional
    public PaymentResponse createPaymentUrl(String username, CreatePaymentRequest request, HttpServletRequest httpServletRequest) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));

        if (subscriptionService.hasActivePremiumSubscription(user.getId())) {
            throw new BadRequestException("Bạn đã có một gói Premium đang hoạt động.");
        }

        SubscriptionPlanDto packageInfo = subscriptionService.getPackageInfo(request.getPackageId());
        BigDecimal amount = packageInfo.getPrice();
        String orderInfo = "Thanh toan goi " + packageInfo.getName();

        Transaction transaction = Transaction.builder()
                .user(user)
                .packageName(request.getPackageId())
                .amount(amount)
                .paymentMethod(request.getPaymentMethod())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        String orderId = "MUSICAPP_SUB_" + savedTransaction.getId();
        savedTransaction.setTransactionCode(orderId);
        transactionRepository.save(savedTransaction);

        try {
            String payUrl;
            if ("MOMO".equalsIgnoreCase(request.getPaymentMethod())) {
                payUrl = moMoService.createPayment(amount.longValue(), orderInfo, orderId);
            } else if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
                payUrl = vnpayService.createPaymentUrl(httpServletRequest, amount.longValue(), orderInfo, orderId);
            } else {
                throw new BadRequestException("Phương thức thanh toán không được hỗ trợ.");
            }
            return new PaymentResponse(payUrl);
        } catch (Exception e) {
            log.error("Không thể tạo yêu cầu thanh toán: ", e);
            throw new RuntimeException("Không thể tạo yêu cầu thanh toán. Vui lòng thử lại sau.");
        }
    }

    @Transactional
    public void processMomoIpn(Map<String, Object> payload) {
        String secretKey = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa"; // Should be from config
        String momoSignature = (String) payload.get("signature");

        String rawSignature = "accessKey=" + payload.get("accessKey") +
                "&amount=" + payload.get("amount") +
                "&extraData=" + payload.get("extraData") +
                "&message=" + payload.get("message") +
                "&orderId=" + payload.get("orderId") +
                "&orderInfo=" + payload.get("orderInfo") +
                "&orderType=" + payload.get("orderType") +
                "&partnerCode=" + payload.get("partnerCode") +
                "&payType=" + payload.get("payType") +
                "&requestId=" + payload.get("requestId") +
                "&responseTime=" + payload.get("responseTime") +
                "&resultCode=" + payload.get("resultCode") +
                "&transId=" + payload.get("transId");

        try {
            String calculatedSignature = moMoService.generateHmacSHA256(rawSignature, secretKey);
            if (!calculatedSignature.equals(momoSignature)) {
                log.error("SECURITY WARNING: Invalid MoMo IPN signature! OrderId: {}", payload.get("orderId"));
                throw new UnauthorizedException("Invalid signature.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error validating MoMo IPN signature.", e);
        }

        String orderId = (String) payload.get("orderId");
        int resultCode = ((Number) payload.get("resultCode")).intValue();
        log.info("Received MoMo IPN for order {}. ResultCode: {}", orderId, resultCode);

        Transaction transaction = transactionRepository.findByTransactionCode(orderId)
                .orElseThrow(() -> {
                    log.error("Invalid IPN: Transaction not found with code: {}", orderId);
                    return new ResourceNotFoundException("Transaction not found.");
                });

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            log.warn("Transaction {} has already been processed. Skipping IPN.", orderId);
            return;
        }

        if (resultCode == 0) {
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            subscriptionService.activateSubscriptionFromTransaction(transaction);
            log.info("Transaction {} successful. Premium subscription activated.", orderId);
        } else {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            log.warn("Transaction {} failed with resultCode: {}", orderId, resultCode);
        }
        transactionRepository.save(transaction);
    }

    @Transactional
    public boolean processVnpayPaymentResult(Map<String, String> allParams) {
        String vnp_SecureHash = allParams.get("vnp_SecureHash");

        if (allParams.containsKey("vnp_SecureHash")) {
            allParams.remove("vnp_SecureHash");
        }
        if (allParams.containsKey("vnp_SecureHashType")) {
            allParams.remove("vnp_SecureHashType");
        }

        List<String> fieldNames = new ArrayList<>(allParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = allParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }

        String calculatedHash = vnpayService.hmacSHA512(vnpayService.getVnp_HashSecret(), hashData.toString());

        if (!calculatedHash.equals(vnp_SecureHash)) {
            log.error("VNPay IPN: Invalid signature! Calculated: {}, VNPay's: {}", calculatedHash, vnp_SecureHash);
            return false;
        }

        String vnp_TxnRef = allParams.get("vnp_TxnRef");
        String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
        long vnp_Amount = Long.parseLong(allParams.get("vnp_Amount")) / 100;

        Transaction transaction = transactionRepository.findByTransactionCode(vnp_TxnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với mã: " + vnp_TxnRef));

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            log.warn("Giao dịch {} đã được xử lý trước đó.", vnp_TxnRef);
            return true;
        }

        if (transaction.getAmount().longValue() != vnp_Amount) {
            log.error("VNPay IPN: Amount mismatch! Expected: {}, Actual: {}", transaction.getAmount().longValue(), vnp_Amount);
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return false;
        }

        if ("00".equals(vnp_ResponseCode)) {
            log.info("VNPay transaction {} successful.", vnp_TxnRef);
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            subscriptionService.activateSubscriptionFromTransaction(transaction);
        } else {
            log.warn("VNPay transaction {} failed with errorCode: {}", vnp_TxnRef, vnp_ResponseCode);
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
        }

        transactionRepository.save(transaction);
        return "00".equals(vnp_ResponseCode);
    }

    public PagedResponse<TransactionDto> getUserTransactions(String username, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        Page<Transaction> transactionPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<TransactionDto> dtos = transactionPage.getContent().stream()
                .map(transaction -> {
                    TransactionDto dto = transactionMapper.toDto(transaction);
                    SubscriptionPlanDto planInfo = subscriptionService.getPackageInfo(transaction.getPackageName());
                    dto.setPackageName(planInfo.getName());
                    return dto;
                })
                .collect(Collectors.toList());

        return PagedResponse.of(dtos, transactionPage);
    }

    public TransactionDto getTransactionById(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        if (!transaction.getUser().getEmail().equals(username)) {
            throw new SecurityException("User does not have permission to view this transaction.");
        }

        TransactionDto dto = transactionMapper.toDto(transaction);
        SubscriptionPlanDto planInfo = subscriptionService.getPackageInfo(transaction.getPackageName());
        dto.setPackageName(planInfo.getName());
        return dto;
    }
}