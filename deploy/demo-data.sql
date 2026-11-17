-- 시연용 데이터: 숭실대(상도동) 근처 가게 8곳, 메뉴, 할인 공지, 마감할인.
-- 다시 실행하면 가게 관련 데이터를 지우고 새로 넣는다 (회원·로그인 정보는 그대로, 예약은 지워짐).
-- 마감할인 시각은 실행 시점 기준이라 시연 직전에 한 번 더 실행하면 된다.
--   docker exec -i lasteat-mysql-1 sh -c 'mysql -u lasteat -p"$MYSQL_PASSWORD" lasteat' < deploy/demo-data.sql
-- 가게 이름·사진은 예시다. 사진은 picsum.photos 임시 이미지.

SET NAMES utf8mb4;

DELETE FROM reservation;
DELETE FROM closing_deal;
DELETE FROM bookmark;
DELETE FROM benefit;
DELETE FROM menu;
DELETE FROM store_image;
DELETE FROM store;

SET @now = NOW(6);

-- 좌표는 (경도 위도) 순서로 쓴다 (axis-order=long-lat). 기준점: 숭실대 정문 근처 37.4963, 126.9575

-- 1. 북쪽 약 150m, 한식, 쉬는 시간 있음, 일요일 휴무, 추천
INSERT INTO store (name, category, address, location, phone, description, open_time, close_time, break_start,
                   break_end, closed_days, tags, recommended, created_at, updated_at)
VALUES ('상도 한그릇', 'KOREAN', '서울 동작구 상도로 361, 1층',
        ST_GeomFromText('POINT(126.9575 37.4977)', 4326, 'axis-order=long-lat'), '02-812-0101',
        '매일 아침 끓이는 국물로 만드는 든든한 한 그릇.', '11:00', '21:00', '15:00', '17:00', 'SUN',
        '혼밥 좋아요,포장 가능', 1, @now, @now);
SET @s1 = LAST_INSERT_ID();

-- 2. 동쪽 약 250m, 양식, 월요일 휴무
INSERT INTO store (name, category, address, location, phone, description, open_time, close_time, closed_days, tags,
                   recommended, created_at, updated_at)
VALUES ('숭실 파스타하우스', 'WESTERN', '서울 동작구 상도로 375, 2층',
        ST_GeomFromText('POINT(126.9603 37.4965)', 4326, 'axis-order=long-lat'), '02-812-0202',
        '생면으로 만드는 파스타와 화덕 피자.', '11:30', '22:00', 'MON', '데이트,단체석', 0, @now, @now);
SET @s2 = LAST_INSERT_ID();

-- 3. 남서쪽 약 300m, 일식, 쉬는 시간 있음
INSERT INTO store (name, category, address, location, phone, description, open_time, close_time, break_start,
                   break_end, tags, recommended, created_at, updated_at)
VALUES ('정문 스시', 'JAPANESE', '서울 동작구 상도로 340',
        ST_GeomFromText('POINT(126.9553 37.4942)', 4326, 'axis-order=long-lat'), '02-812-0303',
        '점심 초밥 세트와 저녁 사시미.', '11:30', '21:30', '15:00', '17:00', '혼밥 좋아요', 1, @now, @now);
SET @s3 = LAST_INSERT_ID();

-- 4. 북동쪽 약 400m, 중식
INSERT INTO store (name, category, address, location, phone, description, open_time, close_time, tags, recommended,
                   created_at, updated_at)
VALUES ('상도 반점', 'CHINESE', '서울 동작구 상도로 390',
        ST_GeomFromText('POINT(126.9602 37.4990)', 4326, 'axis-order=long-lat'), '02-812-0404',
        '수타 짜장면과 바삭한 탕수육.', '10:30', '21:00', '포장 가능,배달 가능', 0, @now, @now);
SET @s4 = LAST_INSERT_ID();

-- 5. 서쪽 약 200m, 베이커리
INSERT INTO store (name, category, address, location, phone, description, open_time, close_time, tags, recommended,
                   created_at, updated_at)
VALUES ('모닝 베이커리', 'BAKERY', '서울 동작구 상도로 350',
        ST_GeomFromText('POINT(126.9552 37.4966)', 4326, 'axis-order=long-lat'), '02-812-0505',
        '매일 아침 굽는 식빵과 소금빵.', '08:00', '21:00', '포장 가능', 0, @now, @now);
SET @s5 = LAST_INSERT_ID();

-- 6. 남쪽 약 100m, 카페
INSERT INTO store (name, category, address, location, phone, description, open_time, close_time, tags, recommended,
                   created_at, updated_at)
VALUES ('캠퍼스 커피', 'CAFE', '서울 동작구 상도로 365',
        ST_GeomFromText('POINT(126.9576 37.4954)', 4326, 'axis-order=long-lat'), '02-812-0606',
        '시험 기간에는 새벽까지 여는 학교 앞 카페.', '08:00', '23:00', '콘센트 많음,조용함', 1, @now, @now);
SET @s6 = LAST_INSERT_ID();

-- 7. 남동쪽 약 450m, 한식 술집, 자정 넘겨 영업
INSERT INTO store (name, category, address, location, phone, description, open_time, close_time, tags, recommended,
                   created_at, updated_at)
VALUES ('늦은밤 포차', 'KOREAN', '서울 동작구 상도로 400',
        ST_GeomFromText('POINT(126.9605 37.4935)', 4326, 'axis-order=long-lat'), '02-812-0707',
        '퇴근길, 시험 끝난 날 들르는 동네 포차.', '18:00', '02:00', '단체석', 0, @now, @now);
SET @s7 = LAST_INSERT_ID();

-- 8. 북쪽 약 1.5km, 기본 반경(500m·1km) 밖 (반경 넓히기 확인용)
INSERT INTO store (name, category, address, location, phone, description, open_time, close_time, tags, recommended,
                   created_at, updated_at)
VALUES ('노량진 국밥', 'KOREAN', '서울 동작구 노량진로 10',
        ST_GeomFromText('POINT(126.9575 37.5098)', 4326, 'axis-order=long-lat'), '02-812-0808',
        '24시간 끓이는 돼지국밥.', '00:00', '00:00', '24시간,혼밥 좋아요', 0, @now, @now);
SET @s8 = LAST_INSERT_ID();

INSERT INTO store_image (store_id, image_url, sort_order, created_at, updated_at)
VALUES (@s1, 'https://picsum.photos/seed/lasteat-s1-a/800/600', 0, @now, @now),
       (@s1, 'https://picsum.photos/seed/lasteat-s1-b/800/600', 1, @now, @now),
       (@s2, 'https://picsum.photos/seed/lasteat-s2-a/800/600', 0, @now, @now),
       (@s3, 'https://picsum.photos/seed/lasteat-s3-a/800/600', 0, @now, @now),
       (@s4, 'https://picsum.photos/seed/lasteat-s4-a/800/600', 0, @now, @now),
       (@s5, 'https://picsum.photos/seed/lasteat-s5-a/800/600', 0, @now, @now),
       (@s6, 'https://picsum.photos/seed/lasteat-s6-a/800/600', 0, @now, @now),
       (@s6, 'https://picsum.photos/seed/lasteat-s6-b/800/600', 1, @now, @now),
       (@s7, 'https://picsum.photos/seed/lasteat-s7-a/800/600', 0, @now, @now);

-- 메뉴 (signature = 대표 메뉴)
INSERT INTO menu (store_id, name, description, price, image_url, signature, sold_out, created_at, updated_at)
VALUES (@s1, '소고기 국밥', '양지와 무를 푹 끓인 맑은 국밥', 10000, 'https://picsum.photos/seed/lasteat-m1/600/600', 1, 0, @now, @now),
       (@s1, '제육 덮밥', '매콤한 제육볶음 덮밥', 9000, 'https://picsum.photos/seed/lasteat-m2/600/600', 1, 0, @now, @now),
       (@s1, '된장찌개', '차돌 된장찌개와 공깃밥', 8500, NULL, 0, 0, @now, @now),
       (@s2, '트러플 크림 파스타', '트러플 오일을 더한 크림 생면 파스타', 16000, 'https://picsum.photos/seed/lasteat-m4/600/600', 1, 0, @now, @now),
       (@s2, '마르게리타 피자', '화덕에 구운 기본 피자', 15000, 'https://picsum.photos/seed/lasteat-m5/600/600', 0, 0, @now, @now),
       (@s2, '알리오 올리오', '마늘과 올리브유 파스타', 13000, NULL, 0, 1, @now, @now),
       (@s3, '점심 초밥 세트', '초밥 10피스와 미니 우동', 15000, 'https://picsum.photos/seed/lasteat-m7/600/600', 1, 0, @now, @now),
       (@s3, '연어 덮밥', '숙성 연어 덮밥', 13000, 'https://picsum.photos/seed/lasteat-m8/600/600', 0, 0, @now, @now),
       (@s4, '수타 짜장면', '직접 뽑은 면의 짜장면', 7000, 'https://picsum.photos/seed/lasteat-m9/600/600', 1, 0, @now, @now),
       (@s4, '찹쌀 탕수육', '바삭한 찹쌀 탕수육 (소)', 18000, 'https://picsum.photos/seed/lasteat-m10/600/600', 1, 0, @now, @now),
       (@s4, '짬뽕', '해물 짬뽕', 8000, NULL, 0, 0, @now, @now),
       (@s5, '소금빵 4개', '버터 소금빵 4개 묶음', 9000, 'https://picsum.photos/seed/lasteat-m12/600/600', 1, 0, @now, @now),
       (@s5, '우유 식빵', '매일 아침 굽는 식빵 한 덩이', 5500, 'https://picsum.photos/seed/lasteat-m13/600/600', 0, 0, @now, @now),
       (@s5, '샌드위치', '햄 치즈 샌드위치', 6500, NULL, 0, 0, @now, @now),
       (@s6, '아메리카노', '산미 있는 원두', 3000, 'https://picsum.photos/seed/lasteat-m15/600/600', 1, 0, @now, @now),
       (@s6, '바스크 치즈케이크', '한 조각', 6000, 'https://picsum.photos/seed/lasteat-m16/600/600', 0, 0, @now, @now),
       (@s7, '모둠전', '김치전, 동그랑땡, 깻잎전', 18000, 'https://picsum.photos/seed/lasteat-m17/600/600', 1, 0, @now, @now),
       (@s7, '오뎅탕', '어묵탕', 15000, NULL, 0, 0, @now, @now),
       (@s8, '돼지국밥', '부산식 돼지국밥', 9000, NULL, 1, 0, @now, @now);

-- 할인 공지 (일반 할인·제휴 혜택). published=0은 비공개라 손님에게 안 보임
INSERT INTO benefit (store_id, title, content, period_text, published, partner, published_at, created_at, updated_at)
VALUES (@s1, '평일 점심 10% 할인', '평일 점심 식사 메뉴를 주문하면 결제 금액의 10%를 할인해 드려요. 포장은 제외.',
        '11월 한 달, 평일 11~14시', 1, 0, @now - INTERVAL 2 DAY, @now, @now),
       (@s1, '숭실대 학생 공깃밥 무료', '학생증을 보여 주시면 공깃밥을 무료로 드려요.', '상시', 1, 1,
        @now - INTERVAL 5 DAY, @now, @now),
       (@s2, '2인 세트 3,000원 할인', '파스타 2개와 피자 1판 주문 시 3,000원 할인.', '11월 30일까지', 1, 0,
        @now - INTERVAL 1 DAY, @now, @now),
       (@s3, '숭실대 학생 제휴 15% 할인', '학생증 제시 시 점심 초밥 세트 15% 할인. 다른 할인과 중복 불가.', '이번 학기', 1, 1,
        @now - INTERVAL 7 DAY, @now, @now),
       (@s4, '탕수육 주문 시 군만두 서비스', '탕수육 주문하면 군만두 5개를 드려요. 매장 식사만.', '상시', 1, 0,
        @now - INTERVAL 3 DAY, @now, @now),
       (@s6, '시험 기간 아메리카노 2,500원', '시험 기간에는 아메리카노를 2,500원에 드려요.', '11월 셋째 주~넷째 주', 1, 1,
        @now - INTERVAL 1 HOUR, @now, @now),
       (@s7, '지난 이벤트', '종료된 이벤트입니다.', '10월', 0, 0, @now - INTERVAL 30 DAY, @now, @now);

-- 마감할인. 노출 시작은 1시간 전, 수령 마감은 노출 시작부터 24시간 안 (실행 시점 기준)
INSERT INTO closing_deal (store_id, menu_id, original_price, sale_price, stock, open_at, pickup_end_at, status,
                          version, created_at, updated_at)
SELECT m.store_id, m.id, m.price, d.sale_price, d.stock, @now - INTERVAL 1 HOUR, @now + INTERVAL d.hours HOUR,
       'SCHEDULED', 0, @now, @now
FROM (SELECT @s1 AS store_id, '제육 덮밥' AS menu_name, 5500 AS sale_price, 4 AS stock, 22 AS hours
      UNION ALL SELECT @s2, '트러플 크림 파스타', 9900, 3, 20
      UNION ALL SELECT @s3, '연어 덮밥', 7900, 2, 6
      UNION ALL SELECT @s5, '소금빵 4개', 5000, 5, 22
      UNION ALL SELECT @s5, '우유 식빵', 3000, 0, 22
      UNION ALL SELECT @s6, '바스크 치즈케이크', 3500, 6, 12
      UNION ALL SELECT @s4, '찹쌀 탕수육', 12000, 2, 3) d
JOIN menu m ON m.store_id = d.store_id AND m.name = d.menu_name;

-- 노출 예정 (아직 손님에게 안 보임, 사장님 목록에만 보임)
INSERT INTO closing_deal (store_id, menu_id, original_price, sale_price, stock, open_at, pickup_end_at, status,
                          version, created_at, updated_at)
SELECT m.store_id, m.id, m.price, 4500, 3, @now + INTERVAL 2 HOUR, @now + INTERVAL 5 HOUR, 'SCHEDULED', 0, @now, @now
FROM menu m
WHERE m.store_id = @s5 AND m.name = '샌드위치';

SELECT (SELECT COUNT(*) FROM store) AS stores, (SELECT COUNT(*) FROM menu) AS menus,
       (SELECT COUNT(*) FROM benefit) AS benefits, (SELECT COUNT(*) FROM closing_deal) AS closing_deals;
