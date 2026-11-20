package com.lasteat.registration.nts;

import java.time.LocalDate;

// 진위확인에 보내는 사업자등록증 정보. businessNumber는 숫자 10자리
public record BusinessInfo(String businessNumber, LocalDate openDate, String representativeName) {
}
