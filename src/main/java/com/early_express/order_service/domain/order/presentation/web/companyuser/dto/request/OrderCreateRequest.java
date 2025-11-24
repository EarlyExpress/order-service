package com.early_express.order_service.domain.order.presentation.web.companyuser.dto.request;

import com.early_express.order_service.domain.order.application.dto.OrderCreateCommand;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 주문 생성 요청 DTO (Company User)
 */
@Getter
@Builder
public class OrderCreateRequest {

    // ===== 업체 정보 =====
    @NotBlank(message = "공급 업체 ID는 필수입니다.")
    private String supplierCompanyId;

    @NotBlank(message = "공급 허브 ID는 필수입니다.")
    private String supplierHubId;

    @NotBlank(message = "수령 업체 ID는 필수입니다.")
    private String receiverCompanyId;

    @NotBlank(message = "수령 허브 ID는 필수입니다.")
    private String receiverHubId;

    // ===== 상품 정보 =====
    @NotBlank(message = "상품 ID는 필수입니다.")
    private String productId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @NotNull(message = "단가는 필수입니다.")
    @DecimalMin(value = "0.0", message = "단가는 0 이상이어야 합니다.")
    private BigDecimal unitPrice;

    // ===== 수령자 정보 =====
    @NotBlank(message = "수령자 이름은 필수입니다.")
    private String receiverName;

    @NotBlank(message = "수령자 전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String receiverPhone;

    @NotBlank(message = "수령자 이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String receiverEmail;

    @NotBlank(message = "배송 주소는 필수입니다.")
    private String deliveryAddress;

    private String deliveryAddressDetail;

    @NotBlank(message = "우편번호는 필수입니다.")
    private String deliveryPostalCode;

    private String deliveryNote;

    // ===== 요청사항 =====
    @NotNull(message = "납품 희망 일자는 필수입니다.")
    @Future(message = "납품 희망 일자는 미래 날짜여야 합니다.")
    private LocalDate requestedDeliveryDate;

    @NotNull(message = "납품 희망 시간은 필수입니다.")
    private LocalTime requestedDeliveryTime;

    private String specialInstructions;

    // ===== PG 결제 정보 =====
    @NotBlank(message = "PG 제공자는 필수입니다.")
    private String pgProvider;

    @NotBlank(message = "PG 결제 ID는 필수입니다.")
    private String pgPaymentId;

    /**
     * Request → Command 변환
     */
    public OrderCreateCommand toCommand(String createdBy) {
        return OrderCreateCommand.builder()
                .supplierCompanyId(supplierCompanyId)
                .supplierHubId(supplierHubId)
                .receiverCompanyId(receiverCompanyId)
                .receiverHubId(receiverHubId)
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .receiverEmail(receiverEmail)
                .deliveryAddress(deliveryAddress)
                .deliveryAddressDetail(deliveryAddressDetail)
                .deliveryPostalCode(deliveryPostalCode)
                .deliveryNote(deliveryNote)
                .requestedDeliveryDate(requestedDeliveryDate)
                .requestedDeliveryTime(requestedDeliveryTime)
                .specialInstructions(specialInstructions)
                .pgProvider(pgProvider)
                .pgPaymentId(pgPaymentId)
                .createdBy(createdBy)
                .build();
    }
}