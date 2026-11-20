-- 사장님 등록(국세청 진위확인)으로 만든 가게의 사업자등록번호 (숫자 10자리). 운영진이 넣은 가게는 비어 있을 수 있다.
-- 같은 사업자로 두 번 등록하지 못하게 unique (NULL은 여러 개 허용)
ALTER TABLE store
    ADD COLUMN business_number VARCHAR(10) NULL,
    ADD CONSTRAINT uk_store_business_number UNIQUE (business_number);
