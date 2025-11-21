package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 업체 정보 Value Object
 * 공급업체와 수령업체 정보를 담음
 */
@Getter
@EqualsAndHashCode
public class CompanyInfo {

    private final String supplierCompanyId;
    private final String supplierHubId;
    private final String receiverCompanyId;
    private final String receiverHubId;

    @Builder
    private CompanyInfo(
            String supplierCompanyId,
            String supplierHubId,
            String receiverCompanyId,
            String receiverHubId) {

        validateNotNull(supplierCompanyId, "공급업체 ID");
        validateNotNull(supplierHubId, "공급업체 허브 ID");
        validateNotNull(receiverCompanyId, "수령업체 ID");
        validateNotNull(receiverHubId, "수령업체 허브 ID");

        this.supplierCompanyId = supplierCompanyId;
        this.supplierHubId = supplierHubId;
        this.receiverCompanyId = receiverCompanyId;
        this.receiverHubId = receiverHubId;
    }

    /**
     * 수령 업체 허브 ID 업데이트
     * Hub Service가 주소 기반으로 결정한 허브로 변경
     */
    public CompanyInfo withReceiverHubId(String receiverHubId) {
        return CompanyInfo.builder()
                .supplierCompanyId(this.supplierCompanyId)
                .supplierHubId(this.supplierHubId)
                .receiverCompanyId(this.receiverCompanyId)
                .receiverHubId(receiverHubId)  // 업데이트
                .build();
    }

    public static CompanyInfo of(
            String supplierCompanyId,
            String supplierHubId,
            String receiverCompanyId,
            String receiverHubId) {

        return CompanyInfo.builder()
                .supplierCompanyId(supplierCompanyId)
                .supplierHubId(supplierHubId)
                .receiverCompanyId(receiverCompanyId)
                .receiverHubId(receiverHubId)
                .build();
    }

    private void validateNotNull(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "는 null이거나 빈 값일 수 없습니다.");
        }
    }
}
