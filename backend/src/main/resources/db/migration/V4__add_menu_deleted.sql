-- 2026-11-17: 사장님 메뉴 삭제. 지난 마감할인·예약이 메뉴를 참조하므로 행을 지우지 않고 삭제 표시만 한다.
-- 삭제된 메뉴는 손님·사장님 메뉴 목록, 대표 메뉴, 메뉴 이름 검색, 마감할인 등록에서 빠진다.
ALTER TABLE menu ADD COLUMN deleted BIT(1) NOT NULL DEFAULT 0;
