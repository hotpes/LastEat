-- 시연용 사장님 계정 연결: 카카오 로그인은 손님(CONSUMER)으로만 가입되므로, 테스트할 계정을 사장님으로 바꾼다.
-- 1) 앱에서 사장님으로 쓸 카카오 계정으로 한 번 로그인한다 (회원이 만들어짐)
-- 2) 아래를 실행한다. 기본값은 가장 최근에 가입한 회원이다. 다른 회원이면 @member_id 줄을 숫자로 바꾼다
--      docker exec -i lasteat-mysql-1 sh -c 'mysql -u lasteat -p"$MYSQL_PASSWORD" lasteat' < deploy/owner-setup.sql
-- 3) 앱에서 다시 로그인하거나 토큰을 갱신하면 사장님 권한이 들어간 토큰이 나온다
-- 연결되는 가게는 시연용 데이터의 '상도 한그릇'이다. demo-data.sql을 다시 실행해도 이 연결은 유지된다.

SET NAMES utf8mb4;

SET @member_id = (SELECT id FROM member ORDER BY id DESC LIMIT 1);

UPDATE member SET role = 'OWNER', updated_at = NOW(6) WHERE id = @member_id;
UPDATE store SET owner_id = @member_id, updated_at = NOW(6) WHERE name = '상도 한그릇';

SELECT m.id, m.nickname, m.role, s.id AS store_id, s.name AS store_name
FROM member m
LEFT JOIN store s ON s.owner_id = m.id
WHERE m.id = @member_id;
