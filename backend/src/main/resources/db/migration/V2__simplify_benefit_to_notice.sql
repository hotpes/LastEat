-- 2026-11-13: 일반 할인(혜택)을 사장님이 글로 쓰는 할인 공지로 바꾼다 (사장님 앱 "할인 공지" 화면).
-- 할인율·기간 날짜·요일·시간·사용 제한·연락처·운영진 검토를 없앤다.
-- 아직 운영 데이터가 없어서 테이블을 새로 만든다. 입력 제한(60/1000/100자)은 화면과 같다.
DROP TABLE benefit;

CREATE TABLE benefit (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    store_id     BIGINT        NOT NULL,
    title        VARCHAR(60)   NOT NULL,
    content      VARCHAR(1000) NOT NULL,
    period_text  VARCHAR(100)  NOT NULL,
    published    BIT(1)        NOT NULL DEFAULT 1,
    partner      BIT(1)        NOT NULL DEFAULT 0,
    published_at DATETIME(6)   NOT NULL,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    KEY idx_benefit_store_published (store_id, published, published_at),
    CONSTRAINT fk_benefit_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
