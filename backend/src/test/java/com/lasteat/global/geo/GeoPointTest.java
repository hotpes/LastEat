package com.lasteat.global.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class GeoPointTest {

	private final GeoPoint school = new GeoPoint(37.4963, 126.9575);

	@Test
	void 위도_0_0009도는_약_100m다() {
		assertThat(school.distanceMetersTo(new GeoPoint(37.4972, 126.9575))).isBetween(99, 101);
		assertThat(school.distanceMetersTo(school)).isZero();
	}

	@Test
	void WKT는_경도_위도_순서의_일반_소수다() {
		assertThat(school.toWkt()).isEqualTo("POINT(126.9575 37.4963)");
		assertThat(new GeoPoint(0.0001, 0.0001).toWkt()).isEqualTo("POINT(0.0001 0.0001)");
		assertThat(school.boundingBoxWkt(500)).startsWith("POLYGON((").doesNotContain("E");
	}

	@Test
	void 선택_좌표는_둘_다_없거나_둘_다_있어야_한다() {
		assertThat(GeoPoint.ofNullable(null, null)).isEmpty();
		assertThat(GeoPoint.ofNullable(37.4963, 126.9575)).contains(school);
		assertThatThrownBy(() -> GeoPoint.ofNullable(37.4963, null))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
	}

	@Test
	void 범위를_벗어난_좌표는_만들_수_없다() {
		assertThatThrownBy(() -> new GeoPoint(91, 0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new GeoPoint(0, 181)).isInstanceOf(IllegalArgumentException.class);
	}

}
