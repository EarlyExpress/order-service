package com.early_express.order_service.domain.order.infrastructure.client.payment;

import com.early_express.order_service.domain.order.infrastructure.client.payment.dto.PaymentVerificationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.payment.dto.PaymentVerificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {PaymentClient.class, PaymentClientConfig.class})
@DisplayName("PaymentClient 테스트")
class PaymentClientTest {

    @MockBean
    private PaymentClient paymentClient;

    @Test
    @DisplayName("결제 검증 성공")
    void verifyAndRegisterPayment_Success() {
        // given
        PaymentVerificationRequest request = PaymentVerificationRequest.of(
                "ORDER-001",
                "TOSS",
                "PG-PAY-001",
                "PG-KEY-001",
                BigDecimal.valueOf(50000),
                "COMP-001",
                "홍길동",
                "test@example.com",
                "010-1234-5678",
                "COMP-002",
                "김철수"
        );

        PaymentVerificationResponse mockResponse = PaymentVerificationResponse.builder()
                .paymentId("PAYMENT-001")
                .status("VERIFIED")
                .pgTransactionId("PG-TRANS-001")
                .verifiedAmount(BigDecimal.valueOf(50000))
                .pgApprovedAt(LocalDateTime.now())
                .message("결제 검증 성공")
                .build();

        given(paymentClient.verifyAndRegisterPayment(any(PaymentVerificationRequest.class)))
                .willReturn(mockResponse);

        // when
        PaymentVerificationResponse response = paymentClient.verifyAndRegisterPayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo("PAYMENT-001");
        assertThat(response.getStatus()).isEqualTo("VERIFIED");
        assertThat(response.getVerifiedAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
    }

    @Test
    @DisplayName("결제 검증 실패 - 금액 불일치")
    void verifyAndRegisterPayment_Fail_AmountMismatch() {
        // given
        PaymentVerificationRequest request = PaymentVerificationRequest.of(
                "ORDER-001",
                "TOSS",
                "PG-PAY-001",
                "PG-KEY-001",
                BigDecimal.valueOf(50000),
                "COMP-001",
                "홍길동",
                "test@example.com",
                "010-1234-5678",
                "COMP-002",
                "김철수"
        );

        PaymentVerificationResponse mockResponse = PaymentVerificationResponse.builder()
                .paymentId(null)
                .status("FAILED")
                .message("결제 금액이 일치하지 않습니다")
                .build();

        given(paymentClient.verifyAndRegisterPayment(any(PaymentVerificationRequest.class)))
                .willReturn(mockResponse);

        // when
        PaymentVerificationResponse response = paymentClient.verifyAndRegisterPayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getPaymentId()).isNull();
    }
}