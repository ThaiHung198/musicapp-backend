package com.musicapp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[WebMusic] Mã OTP đặt lại mật khẩu của bạn");
        message.setText("Chào bạn,\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản WebMusic của mình.\n\n"
                + "Mã OTP của bạn là: " + otpCode + "\n\n"
                + "Mã này sẽ hết hạn trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ WebMusic");

        javaMailSender.send(message);
    }
}