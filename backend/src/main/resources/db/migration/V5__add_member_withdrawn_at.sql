-- 회원 탈퇴: 지난 예약 기록이 회원을 참조하므로 행은 남기고 개인정보를 지운 뒤 탈퇴 시각을 남긴다
ALTER TABLE member ADD COLUMN withdrawn_at DATETIME(6) NULL;
