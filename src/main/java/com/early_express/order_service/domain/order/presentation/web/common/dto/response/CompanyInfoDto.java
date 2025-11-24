package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.vo.CompanyInfo;
import lombok.Builder;
import lombok.Getter;

/**
 * 업체 정보 DTO
 */
@Getter
@Builder
public class CompanyInfoDto {

    private String supplierCompanyId;
    private String supplierHubId;
    private String receiverCompanyId;
    private String receiverHubId;

    public static CompanyInfoDto from(CompanyInfo companyInfo) {
        return CompanyInfoDto.builder()
                .supplierCompanyId(companyInfo.getSupplierCompanyId())
                .supplierHubId(companyInfo.getSupplierHubId())
                .receiverCompanyId(companyInfo.getReceiverCompanyId())
                .receiverHubId(companyInfo.getReceiverHubId())
                .build();
    }
}