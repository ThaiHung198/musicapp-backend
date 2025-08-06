package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.subscription.CreateSubscriptionRequest;
import com.musicapp.backend.dto.subscription.SubscriptionDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.entity.UserSubscription;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.SubscriptionMapper;
import com.musicapp.backend.repository.UserRepository;
import com.musicapp.backend.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final TransactionService transactionService; // Cần để ghi lại giao dịch
    private final UserRepository userRepository;

    /**
     * Tạo một gói đăng ký mới cho người dùng dựa trên packageId.
     * Logic đã được viết lại hoàn toàn để phù hợp với backlog mới.
     */
    @Transactional
    public SubscriptionDto createSubscription(CreateSubscriptionRequest request, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        // Kiểm tra xem người dùng đã có gói active chưa
        if (hasActivePremiumSubscription(user.getId())) {
            throw new BadRequestException("User already has an active subscription.");
        }

        int durationInDays;
        BigDecimal price;
        String packageName;

        // Xác định thông tin gói dựa trên packageId
        if ("monthly_premium".equals(request.getPackageId())) {
            durationInDays = 30;
            price = new BigDecimal("50000.00"); // Ví dụ giá gói tháng
            packageName = "Gói Premium 1 Tháng";
        } else if ("yearly_premium".equals(request.getPackageId())) {
            durationInDays = 365;
            price = new BigDecimal("500000.00"); // Ví dụ giá gói năm
            packageName = "Gói Premium 1 Năm";
        } else {
            throw new BadRequestException("Invalid package identifier: " + request.getPackageId());
        }

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(durationInDays);

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .price(price) // Lưu lại giá tại thời điểm mua
                .status(UserSubscription.SubscriptionStatus.ACTIVE)
                .autoRenewal(false) // Mặc định không tự động gia hạn
                .build();

        UserSubscription savedSubscription = subscriptionRepository.save(subscription);

        // Giả sử thanh toán thành công và tạo một bản ghi giao dịch.
        // Chúng ta có thể cần tạo phương thức này trong TransactionService.
        transactionService.createTransactionForSubscription(user, savedSubscription, packageName, "MOMO");

        return subscriptionMapper.toDto(savedSubscription);
    }

    /**
     * Kiểm tra xem người dùng có gói Premium đang hoạt động hay không.
     * Logic đã được đơn giản hóa.
     */
    public boolean hasActivePremiumSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscription(userId, LocalDateTime.now()).isPresent();
    }

    /**
     * Lấy gói đăng ký đang hoạt động của người dùng.
     */
    public SubscriptionDto getUserActiveSubscription(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Optional<UserSubscription> subscription = subscriptionRepository.findActiveSubscription(user.getId(), LocalDateTime.now());
        return subscription.map(subscriptionMapper::toDto).orElse(null);
    }

    /**
     * Hủy một gói đăng ký đang hoạt động.
     */
    @Transactional
    public SubscriptionDto cancelSubscription(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        UserSubscription activeSubscription = subscriptionRepository.findActiveSubscription(user.getId(), LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("No active subscription found to cancel"));

        activeSubscription.setStatus(UserSubscription.SubscriptionStatus.CANCELLED);
        activeSubscription.setCancelledAt(LocalDateTime.now());
        activeSubscription.setAutoRenewal(false);

        UserSubscription updatedSubscription = subscriptionRepository.save(activeSubscription);
        return subscriptionMapper.toDto(updatedSubscription);
    }

    /**
     * Lấy các gói có sẵn để hiển thị cho người dùng.
     * Phương thức này thay thế cho getSubscriptionTiers() cũ.
     */
    public Object getAvailablePackages() {
        return Map.of(
                "monthly_premium", Map.of(
                        "name", "Gói Premium 1 Tháng",
                        "price", new BigDecimal("50000.00"),
                        "durationDays", 30,
                        "features", List.of("Nghe nhạc không quảng cáo", "Chất lượng cao", "Truy cập toàn bộ bài hát")
                ),
                "yearly_premium", Map.of(
                        "name", "Gói Premium 1 Năm",
                        "price", new BigDecimal("500000.00"),
                        "durationDays", 365,
                        "features", List.of("Tất cả quyền lợi của gói Tháng", "Tiết kiệm hơn 15%")
                )
        );
    }

    // Các phương thức dành cho Admin hoặc Scheduler có thể được giữ lại và điều chỉnh nếu cần.
    // Ví dụ: processExpiredSubscriptions() trong SubscriptionSchedulerService vẫn hoạt động tốt.

    /**
     * [ADMIN] Lấy danh sách tất cả các gói đăng ký trong hệ thống, có phân trang.
     * Có thể lọc theo trạng thái active.
     */
    public PagedResponse<SubscriptionDto> getAllSubscriptions(Pageable pageable, Boolean isActive) {
        Page<UserSubscription> subscriptions;
        if (isActive != null) {
            if (isActive) {
                subscriptions = subscriptionRepository.findByStatusAndEndDateAfter(
                        UserSubscription.SubscriptionStatus.ACTIVE, LocalDateTime.now(), pageable);
            } else {
                subscriptions = subscriptionRepository.findByStatusNot(
                        UserSubscription.SubscriptionStatus.ACTIVE, pageable);
            }
        } else {
            subscriptions = subscriptionRepository.findAll(pageable);
        }
        return createPagedResponse(subscriptions.map(subscriptionMapper::toDto));
    }

    /**
     * [ADMIN] Lấy lịch sử đăng ký của một người dùng cụ thể.
     */
    public PagedResponse<SubscriptionDto> getUserSubscriptionHistory(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        Page<UserSubscription> subscriptions = subscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return createPagedResponse(subscriptions.map(subscriptionMapper::toDto));
    }

    /**
     * [ADMIN] Lấy các số liệu thống kê về các gói đăng ký.
     * Đã cập nhật để không còn thống kê theo cấp độ.
     */
    public Object getSubscriptionStats() {
        long totalSubscriptions = subscriptionRepository.count();
        long activeSubscriptions = subscriptionRepository.countActiveSubscriptions(LocalDateTime.now());

        return Map.of(
                "totalSubscriptions", totalSubscriptions,
                "activeSubscriptions", activeSubscriptions,
                "expiredOrCancelledSubscriptions", totalSubscriptions - activeSubscriptions
        );
    }

    /**
     * [ADMIN] Lấy doanh thu từ các giao dịch trong một khoảng thời gian.
     */
    public Object getSubscriptionRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        // Logic này cần được triển khai trong TransactionRepository để tính tổng doanh thu
        // Ví dụ:
        // BigDecimal totalRevenue = transactionRepository.sumRevenueBetweenDates(startDate, endDate);

        // Trả về dữ liệu giả để minh họa
        return Map.of(
                "totalRevenue", "Chưa được triển khai",
                "period", Map.of("start", startDate, "end", endDate)
        );
    }

    // Helper method để tạo PagedResponse, bạn có thể đặt nó ở cuối class
    private <T> PagedResponse<T> createPagedResponse(Page<T> page) {
        return PagedResponse.of(page.getContent(), page);
    }
}