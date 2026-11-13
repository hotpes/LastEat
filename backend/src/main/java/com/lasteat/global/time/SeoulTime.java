package com.lasteat.global.time;

import com.lasteat.BackendApplication;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

// DB에는 한국 시각(LocalDateTime)으로 저장하고, API는 "2026-11-01T14:00:00+09:00" 형식으로 주고받는다.
public final class SeoulTime {

	public static final ZoneId ZONE = ZoneId.of(BackendApplication.TIME_ZONE);

	private SeoulTime() {
	}

	public static OffsetDateTime toOffset(LocalDateTime time) {
		return time == null ? null : time.atZone(ZONE).toOffsetDateTime();
	}

	// 다른 시간대로 보내도 한국 시각으로 바꿔 저장한다
	public static LocalDateTime toLocal(OffsetDateTime time) {
		return time == null ? null : time.atZoneSameInstant(ZONE).toLocalDateTime();
	}

}
