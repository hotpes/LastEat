package com.lasteat.registration.nts;

// 진위확인 결과
public enum BusinessVerification {
	// 국세청 정보와 일치하고 계속사업자
	ACTIVE,
	// 일치하지만 휴업 또는 폐업
	NOT_ACTIVE,
	// 일치하지 않거나 등록되지 않은 번호
	NOT_MATCHED
}
