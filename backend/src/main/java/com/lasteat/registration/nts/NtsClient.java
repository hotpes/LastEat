package com.lasteat.registration.nts;

public interface NtsClient {

	// 국세청에 사업자 진위를 확인한다. 국세청 API를 쓸 수 없으면 BusinessException(BUSINESS_VERIFICATION_UNAVAILABLE)
	BusinessVerification verify(BusinessInfo info);

}
