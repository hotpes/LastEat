package com.lasteat.global.geo;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.locationtech.jts.geom.Point;

// 위도·경도 좌표. 거리 계산은 MySQL ST_Distance_Sphere와 같은 지구 반지름을 써서 결과를 맞춘다.
public record GeoPoint(double lat, double lng) {

	// MySQL ST_Distance_Sphere 기본 반지름(m)
	static final double EARTH_RADIUS_METERS = 6_370_986;

	private static final double MAX_LAT = 90;

	private static final double MAX_LNG = 180;

	public GeoPoint {
		if (lat < -MAX_LAT || lat > MAX_LAT || lng < -MAX_LNG || lng > MAX_LNG) {
			throw new IllegalArgumentException("좌표 범위를 벗어났습니다: " + lat + ", " + lng);
		}
	}

	// 선택 좌표(lat, lng). 둘 다 없으면 빈 값, 하나만 있으면 입력값 오류
	public static Optional<GeoPoint> ofNullable(Double lat, Double lng) {
		if (lat == null && lng == null) {
			return Optional.empty();
		}
		if (lat == null || lng == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		return Optional.of(new GeoPoint(lat, lng));
	}

	// 가게 위치 Point는 x=경도, y=위도로 저장되어 있다
	public static GeoPoint from(Point point) {
		return new GeoPoint(point.getY(), point.getX());
	}

	public int distanceMetersTo(GeoPoint other) {
		double dLat = Math.toRadians(other.lat - lat);
		double dLng = Math.toRadians(other.lng - lng);
		double a = Math.pow(Math.sin(dLat / 2), 2)
				+ Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(other.lat)) * Math.pow(Math.sin(dLng / 2), 2);
		return (int) Math.round(2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a)));
	}

	// MySQL ST_GeomFromText(..., 4326, 'axis-order=long-lat')용 WKT. 숫자만 들어가므로 SQL에 안전하다.
	public String toWkt() {
		return "POINT(" + coordinate(lng, lat) + ")";
	}

	// 반경을 감싸는 사각형. 공간 인덱스(MBRContains)로 후보를 먼저 줄이는 데 쓴다.
	public String boundingBoxWkt(int radiusMeters) {
		double dLat = Math.toDegrees(radiusMeters / EARTH_RADIUS_METERS);
		double dLng = Math.toDegrees(radiusMeters / (EARTH_RADIUS_METERS * Math.cos(Math.toRadians(lat))));
		double south = Math.max(-MAX_LAT, lat - dLat);
		double north = Math.min(MAX_LAT, lat + dLat);
		double west = Math.max(-MAX_LNG, lng - dLng);
		double east = Math.min(MAX_LNG, lng + dLng);
		return "POLYGON((" + String.join(", ", coordinate(west, south), coordinate(east, south), coordinate(east, north),
				coordinate(west, north), coordinate(west, south)) + "))";
	}

	// 1.0E-4 같은 지수 표기가 WKT에 들어가지 않도록 일반 소수로 쓴다
	private static String coordinate(double x, double y) {
		return plain(x) + " " + plain(y);
	}

	private static String plain(double value) {
		return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
	}

}
