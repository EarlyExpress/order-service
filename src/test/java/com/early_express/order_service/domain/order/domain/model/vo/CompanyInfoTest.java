package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CompanyInfo vo 테스트")
class CompanyInfoTest {

    @Test
    @DisplayName("업체 정보를 생성할 수 있다")
    void createCompanyInfo() {
        // given
        String supplierCompanyId = "COMP-001";
        String supplierHubId = "HUB-001";
        String receiverCompanyId = "COMP-002";
        String receiverHubId = "HUB-002";

        // when
        CompanyInfo companyInfo = CompanyInfo.of(
                supplierCompanyId,
                supplierHubId,
                receiverCompanyId,
                receiverHubId
        );

        // then
        assertThat(companyInfo.getSupplierCompanyId()).isEqualTo(supplierCompanyId);
        assertThat(companyInfo.getSupplierHubId()).isEqualTo(supplierHubId);
        assertThat(companyInfo.getReceiverCompanyId()).isEqualTo(receiverCompanyId);
        assertThat(companyInfo.getReceiverHubId()).isEqualTo(receiverHubId);
    }

    @Test
    @DisplayName("공급업체 ID가 null이면 예외가 발생한다")
    void validateSupplierCompanyIdNull() {
        // when & then
        assertThatThrownBy(() -> CompanyInfo.of(null, "HUB-001", "COMP-002", "HUB-002"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("공급업체 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("공급업체 허브 ID가 null이면 예외가 발생한다")
    void validateSupplierHubIdNull() {
        // when & then
        assertThatThrownBy(() -> CompanyInfo.of("COMP-001", null, "COMP-002", "HUB-002"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("공급업체 허브 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("수령업체 ID가 null이면 예외가 발생한다")
    void validateReceiverCompanyIdNull() {
        // when & then
        assertThatThrownBy(() -> CompanyInfo.of("COMP-001", "HUB-001", null, "HUB-002"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수령업체 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("수령업체 허브 ID가 null이면 예외가 발생한다")
    void validateReceiverHubIdNull() {
        // when & then
        assertThatThrownBy(() -> CompanyInfo.of("COMP-001", "HUB-001", "COMP-002", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수령업체 허브 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 업체 정보를 생성하면 예외가 발생한다")
    void validateCompanyInfoEmpty() {
        // when & then
        assertThatThrownBy(() -> CompanyInfo.of("", "HUB-001", "COMP-002", "HUB-002"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("공급업체 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 CompanyInfo는 동일하다")
    void equalCompanyInfos() {
        // given
        CompanyInfo companyInfo1 = CompanyInfo.of("COMP-001", "HUB-001", "COMP-002", "HUB-002");
        CompanyInfo companyInfo2 = CompanyInfo.of("COMP-001", "HUB-001", "COMP-002", "HUB-002");

        // when & then
        assertThat(companyInfo1).isEqualTo(companyInfo2);
        assertThat(companyInfo1.hashCode()).isEqualTo(companyInfo2.hashCode());
    }
}