package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.transaction.CreatePaymentRequest; // <<< THÊM IMPORT
import com.musicapp.backend.dto.transaction.PaymentResponse; // <<< THÊM IMPORT
import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.entity.Transaction;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException; // <<< THÊM IMPORT
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.TransactionMapper;
import com.musicapp.backend.repository.TransactionRepository;
import com.musicapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // <<< THÊM IMPORT
import java.util.Map; // <<< THÊM IMPORT

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final MoMoService moMoService;
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

    /**
     * <<< ĐÃ THÊM PHƯƠNG THỨC NÀY >>>
     * Xử lý thông báo IPN từ MoMo.
     */
    @Transactional
    public void processMomoIpn(Map<String, Object> payload) {
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