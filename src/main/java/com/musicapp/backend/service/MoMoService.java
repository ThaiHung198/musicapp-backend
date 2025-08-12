package com.musicapp.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicapp.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoMoService {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${momo.partner-code}")
    private String partnerCode;
    @Value("${momo.access-key}")
    private String accessKey;
    @Value("${momo.secret-key}")
    private String secretKey;
    @Value("${momo.api-endpoint}")
    private String apiEndpoint;
    @Value("${momo.redirect-url}")
    private String redirectUrl;
    @Value("${momo.ipn-url}")
    private String ipnUrl;

    /**
     * Tạo một yêu cầu thanh toán đến MoMo và trả về URL thanh toán.
     * @param amount Số tiền cần thanh toán.
     * @param orderInfo Thông tin đơn hàng.
     * @param orderId Mã đơn hàng duy nhất.
     * @return URL thanh toán (payUrl) từ MoMo.
     */
    public String createPayment(long amount, String orderInfo, String orderId) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String requestId = UUID.randomUUID().toString();
        String requestType = "captureWallet"; // Loại thanh toán qua ví MoMo
        String extraData = ""; // Dữ liệu thêm (nếu cần)

        // Chuỗi dữ liệu thô để tạo chữ ký
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        // Tạo chữ ký HMAC SHA256
        String signature = generateHmacSHA256(rawSignature, secretKey);

        // Tạo body cho request HTTP
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("partnerCode", partnerCode);
        requestBodyMap.put("requestId", requestId);
        requestBodyMap.put("amount", amount);
        requestBodyMap.put("orderId", orderId);
        requestBodyMap.put("orderInfo", orderInfo);
        requestBodyMap.put("redirectUrl", redirectUrl);
        requestBodyMap.put("ipnUrl", ipnUrl);
        requestBodyMap.put("requestType", requestType);
        requestBodyMap.put("extraData", extraData);
        requestBodyMap.put("signature", signature);
        requestBodyMap.put("lang", "vi"); // Ngôn ngữ hiển thị trên trang thanh toán

        // Gửi request đến MoMo
        String jsonBody = objectMapper.writeValueAsString(requestBodyMap);
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Yêu cầu đến MoMo thất bại: " + response);
            }

            String responseBodyString = response.body().string();
            JsonNode responseBodyJson = objectMapper.readTree(responseBodyString);

            // Kiểm tra resultCode từ MoMo
            int resultCode = responseBodyJson.get("resultCode").asInt();
            if (resultCode != 0) {
                String message = responseBodyJson.get("message").asText();
                throw new BadRequestException("MoMo trả về lỗi: " + message);
            }

            // Trả về URL thanh toán
            return responseBodyJson.get("payUrl").asText();
        }
    }

    /**
     * Tạo chữ ký HMAC SHA256 từ dữ liệu và khóa bí mật.
     * @param data Dữ liệu cần ký.
     * @param key Khóa bí mật.
     * @return Chữ ký đã được mã hóa hex.
     */
    public String generateHmacSHA256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKeySpec);
        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return toHexString(hash);
    }

    /**
     * Chuyển đổi một mảng byte thành chuỗi hex.
     */
    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}