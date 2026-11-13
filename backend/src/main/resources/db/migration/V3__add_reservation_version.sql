-- 2026-11-13: 예약 상태 전이(취소·수령·노쇼)가 동시에 일어나도 하나만 반영되도록 낙관적 락 버전을 둔다.
ALTER TABLE reservation ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
