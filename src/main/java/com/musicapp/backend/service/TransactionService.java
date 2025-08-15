package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.transaction.CreatePaymentRequest; // <<< THÊM IMPORT
import com.musicapp.backend.dto.transaction.PaymentResponse; // <<< THÊM IMPORT
import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.entity.Transaction;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException; // <<< THÊM IMPORT
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

import java.math.BigDecimal; // <<< THÊM IMPORT
import java.net.URLEncoder;
import java.util.Map; // <<< THÊM IMPORT

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    /**
     * <<< ĐÃ KHÔI PHỤC LẠI ĐỊNH NGHĨA PHƯƠNG THỨC >>>
     * Tạo URL thanh toán cho người dùng.
     */
    @Transactional
    public PaymentResponse createPaymentUrl(String username, CreatePaymentRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));

        if (subscriptionService.hasActivePremiumSubscription(user.getId())) {
            throw new BadRequestException("Bạn đã có một gói Premium đang hoạt động.");
        }

        Map<String, Object> packageInfo = subscriptionService.getPackageInfo(request.getPackageId());
        String packageName = (String) packageInfo.get("name");
        BigDecimal amount = (BigDecimal) packageInfo.get("price");
        String orderInfo = "Thanh toán cho gói " + packageName;

        Transaction transaction = Transaction.builder()
                .user(user)
                .packageName(packageName)
                .amount(amount)
                .paymentMethod("MOMO")
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        String orderId = "MUSICAPP_SUB_" + savedTransaction.getId();
        savedTransaction.setTransactionCode(orderId);
        transactionRepository.save(savedTransaction);

        try {
            String payUrl = moMoService.createPayment(amount.longValue(), orderInfo, orderId);
            return new PaymentResponse(payUrl);
        } catch (Exception e) {
            log.error("Không thể tạo yêu cầu thanh toán MoMo: ", e);
            throw new RuntimeException("Không thể tạo yêu cầu thanh toán. Vui lòng thử lại sau.");
        }
    }

    @Transactional
    public PaymentResponse createVnpayPaymentUrl(String username, CreatePaymentRequest paymentReq, HttpServletRequest httpReq) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));

        if (subscriptionService.hasActivePremiumSubscription(user.getId())) {
            throw new BadRequestException("Bạn đã có một gói Premium đang hoạt động.");
        }

        Map<String, Object> packageInfo = subscriptionService.getPackageInfo(paymentReq.getPackageId());
        String packageName = (String) packageInfo.get("name");
        BigDecimal amount = (BigDecimal) packageInfo.get("price");
        String orderInfo = "Thanh toan cho goi " + packageName; // VNPay không hỗ trợ dấu

        Transaction transaction = Transaction.builder()
                .user(user)
                .packageName(packageName)
                .amount(amount)
                .paymentMethod("VNPAY") // <<< ĐÁNH DẤU LÀ VNPAY
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        String orderId = "MUSICAPP_SUB_" + savedTransaction.getId();
        savedTransaction.setTransactionCode(orderId);
        transactionRepository.save(savedTransaction);

        // Gọi VnpayService để tạo URL
        String payUrl = vnpayService.createPaymentUrl(httpReq, amount.longValue(), orderInfo, orderId);

        return new PaymentResponse(payUrl);
    }

    @Transactional
    public boolean processVnpayPaymentResult(Map<String, String> allParams) {
        // Lấy vnp_SecureHash từ các tham số
        String vnp_SecureHash = allParams.get("vnp_SecureHash");

        // Xóa hash ra khỏi map trước khi tính toán lại
        if (allParams.containsKey("vnp_SecureHash")) {
            allParams.remove("vnp_SecureHash");
        }
        if (allParams.containsKey("vnp_SecureHashType")) {
            allParams.remove("vnp_SecureHashType");
        }

        // Sắp xếp các tham số theo alphabet
        List<String> fieldNames = new ArrayList<>(allParams.keySet());
        Collections.sort(fieldNames);

        // --- BẮT ĐẦU PHẦN SỬA LỖI ---
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = allParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                // SỬA LỖI QUAN TRỌNG: Dùng URLEncoder.encode để xử lý đúng các ký tự đặc biệt và tiếng Việt
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                hashData.append('&');
            }
        }
        // Xóa dấu & cuối cùng
        hashData.deleteCharAt(hashData.length() - 1);
        // --- KẾT THÚC PHẦN SỬA LỖI ---

        // Tính toán lại chữ ký
        String calculatedHash = vnpayService.hmacSHA512(vnpayService.getVnp_HashSecret(), hashData.toString());

        // 1. KIỂM TRA CHỮ KÝ BẢO MẬT
        if (!calculatedHash.equals(vnp_SecureHash)) {
            log.error("VNPay IPN: Chữ ký không hợp lệ! Calculated: {}, VNPay's: {}", calculatedHash, vnp_SecureHash);
            return false;
        }

        String vnp_TxnRef = allParams.get("vnp_TxnRef");
        String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
        long vnp_Amount = Long.parseLong(allParams.get("vnp_Amount")) / 100;

        Transaction transaction = transactionRepository.findByTransactionCode(vnp_TxnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với mã: " + vnp_TxnRef));

        // Các bước kiểm tra còn lại giữ nguyên...
        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            log.warn("Giao dịch {} đã được xử lý trước đó.", vnp_TxnRef);
            return true;
        }

        if (transaction.getAmount().longValue() != vnp_Amount) {
            log.error("VNPay IPN: Số tiền không khớp! Expected: {}, Actual: {}", transaction.getAmount().longValue(), vnp_Amount);
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return false;
        }

        if ("00".equals(vnp_ResponseCode)) {
            log.info("Giao dịch VNPay {} thành công.", vnp_TxnRef);
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            subscriptionService.activateSubscriptionFromTransaction(transaction);
        } else {
            log.warn("Giao dịch VNPay {} thất bại với mã lỗi: {}", vnp_TxnRef, vnp_ResponseCode);
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
        }

        transactionRepository.save(transaction);
        return "00".equals(vnp_ResponseCode);
    }

    /**
     * <<< ĐÃ THÊM PHƯƠNG THỨC NÀY >>>
     * Xử lý thông báo IPN từ MoMo.
     */
    @Transactional
    public void processMomoIpn(Map<String, Object> payload) {
        String secretKey = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa"; // Lấy từ application.properties
        String momoSignature = (String) payload.get("signature");

        // Sắp xếp các key theo alphabet để tạo chuỗi raw
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
                log.error("CẢNH BÁO BẢO MẬT: Chữ ký IPN của MoMo không hợp lệ! OrderId: {}", payload.get("orderId"));
                throw new UnauthorizedException("Chữ ký không hợp lệ.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xác thực chữ ký IPN của MoMo.", e);
        }
        // TODO: XÁC THỰC CHỮ KÝ TỪ MOMO ĐỂ ĐẢM BẢO AN TOÀN
        String orderId = (String) payload.get("orderId");
        int resultCode = ((Number) payload.get("resultCode")).intValue();
        log.info("Nhận được IPN từ MoMo cho đơn hàng {}. ResultCode: {}", orderId, resultCode);

        Transaction transaction = transactionRepository.findByTransactionCode(orderId)
                .orElseThrow(() -> {
                    log.error("IPN không hợp lệ: Không tìm thấy giao dịch với mã: {}", orderId);
                    return new ResourceNotFoundException("Không tìm thấy giao dịch.");
                });

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            log.warn("Giao dịch {} đã được xử lý trước đó. Bỏ qua IPN.", orderId);
            return;
        }

        if (resultCode == 0) {
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            subscriptionService.activateSubscriptionFromTransaction(transaction);
            log.info("Giao dịch {} thành công. Gói Premium đã được kích hoạt.", orderId);
        } else {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            log.warn("Giao dịch {} thất bại với resultCode: {}", orderId, resultCode);
        }
        transactionRepository.save(transaction);
    }

    // --- CÁC PHƯƠNG THỨC CŨ GIỮ NGUYÊN ---
    public PagedResponse<TransactionDto> getUserTransactions(String username, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        Page<Transaction> transactionPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        Page<TransactionDto> dtoPage = transactionPage.map(transactionMapper::toDto);
        return PagedResponse.of(dtoPage.getContent(), dtoPage);
    }

    public TransactionDto getTransactionById(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        if (!transaction.getUser().getEmail().equals(username)) {
            throw new SecurityException("User does not have permission to view this transaction.");
        }
        return transactionMapper.toDto(transaction);
    }
}