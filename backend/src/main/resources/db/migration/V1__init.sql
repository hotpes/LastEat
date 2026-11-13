-- LastEat 초기 스키마 (API 명세 v0.2 기준)
-- 엔티티를 바꾸면 이 파일을 고치지 말고 V2__설명.sql 처럼 새 파일을 추가한다.
-- 목록형 값(요일·사용 제한·태그)은 쉼표 구분 문자열로 저장한다. 빈 목록은 빈 문자열.

CREATE TABLE member (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email       VARCHAR(255),
    nickname    VARCHAR(30)  NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    fcm_token   VARCHAR(255),
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_provider UNIQUE (provider, provider_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE refresh_token (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE store (
    id                            BIGINT       NOT NULL AUTO_INCREMENT,
    owner_id                      BIGINT,
    name                          VARCHAR(255) NOT NULL,
    category                      VARCHAR(20)  NOT NULL,
    address                       VARCHAR(255) NOT NULL,
    location                      POINT        NOT NULL SRID 4326,
    phone                         VARCHAR(255),
    description                   VARCHAR(300),
    open_time                     TIME,
    close_time                    TIME,
    break_start                   TIME,
    break_end                     TIME,
    closed_days                   VARCHAR(30)  NOT NULL DEFAULT '',
    tags                          VARCHAR(100) NOT NULL DEFAULT '',
    recommended                   BIT(1)       NOT NULL DEFAULT 0,
    business_status_override      VARCHAR(20),
    business_status_override_date DATE,
    created_at                    DATETIME(6)  NOT NULL,
    updated_at                    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    SPATIAL INDEX idx_store_location (location),
    CONSTRAINT fk_store_owner FOREIGN KEY (owner_id) REFERENCES member (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE store_image (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    store_id   BIGINT       NOT NULL,
    image_url  VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_store_image_store_order (store_id, sort_order),
    CONSTRAINT fk_store_image_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    store_id    BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    price       INT          NOT NULL,
    image_url   VARCHAR(500),
    signature   BIT(1)       NOT NULL DEFAULT 0,
    sold_out    BIT(1)       NOT NULL DEFAULT 0,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE benefit (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    store_id         BIGINT       NOT NULL,
    type             VARCHAR(20)  NOT NULL,
    partner          BIT(1)       NOT NULL DEFAULT 0,
    title            VARCHAR(255) NOT NULL,
    description      VARCHAR(300) NOT NULL,
    discount_type    VARCHAR(20),
    discount_value   INT,
    max_discount     INT,
    min_order_amount INT          NOT NULL DEFAULT 0,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    days             VARCHAR(30)  NOT NULL,
    available_from   TIME         NOT NULL,
    available_to     TIME         NOT NULL,
    restrictions     VARCHAR(100) NOT NULL DEFAULT '',
    extra_notice     VARCHAR(255),
    contact_phone    VARCHAR(20)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    reject_reason    VARCHAR(255),
    paused           BIT(1)       NOT NULL DEFAULT 0,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_benefit_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE bookmark (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    store_id   BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bookmark_member_store UNIQUE (member_id, store_id),
    CONSTRAINT fk_bookmark_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_bookmark_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE closing_deal (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    store_id       BIGINT      NOT NULL,
    menu_id        BIGINT      NOT NULL,
    original_price INT         NOT NULL,
    sale_price     INT         NOT NULL,
    stock          INT         NOT NULL,
    open_at        DATETIME(6) NOT NULL,
    pickup_end_at  DATETIME(6) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    version        BIGINT,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_closing_deal_store FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT fk_closing_deal_menu FOREIGN KEY (menu_id) REFERENCES menu (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE reservation (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    member_id       BIGINT      NOT NULL,
    closing_deal_id BIGINT      NOT NULL,
    quantity        INT         NOT NULL,
    status          VARCHAR(20) NOT NULL,
    picked_up_at    DATETIME(6),
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_reservation_closing_deal FOREIGN KEY (closing_deal_id) REFERENCES closing_deal (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
