# 서버 배포

> **한 줄 요약:** 오라클 클라우드 ARM 서버 한 대에 Docker Compose로 Caddy(HTTPS) + 앱 + MySQL을 띄운다. main에 merge되면 CI가 이미지를 GHCR에 올리고 서버에서 `deploy/deploy.sh`를 실행한다.

```
인터넷 ──80/443──> Caddy ──/api/*, Swagger, health──> app:8080 ──> mysql:3306
                                    (compose 내부 네트워크. MySQL은 인터넷에 열지 않음)
```

| 파일 | 내용 |
|:---|:---|
| `backend/Dockerfile` | 운영 이미지. jar는 빌드 머신에서 한 번 만들고, 실행 이미지는 arm64·amd64 두 가지 |
| `deploy/compose.yaml` | 운영 구성. 앱은 `ghcr.io/hotpes/lasteat-backend` 이미지를 받는다. 데이터는 볼륨(`mysql-data`, `caddy-data`)에 남는다 |
| `deploy/caddy/Caddyfile` | `/api/*`, Swagger(`/swagger-ui/*`, `/v3/api-docs`), `/actuator/health`만 앱으로. 나머지는 404. 인증서 자동 발급 |
| `deploy/deploy.sh` | 서버에서 main 최신 설정·이미지로 다시 띄우고 health 확인. CI가 실행하고, 손으로 실행해도 된다 |
| `deploy/compose.local.yaml` | 로컬에서 이미지를 직접 빌드할 때 덧붙이는 파일 (MySQL은 3307) |
| `deploy/.env.example` | 서버에 채울 값: `DOMAIN`, `DB_PASSWORD`, `DB_ROOT_PASSWORD`, `JWT_SECRET`, `KAKAO_APP_ID`, `APP_TAG`(선택) |
| `.github/workflows/backend-ci.yml` | PR·main: 테스트 + 이미지 빌드 확인. main: GHCR 업로드(`latest`, `sha-xxxxxxx`) → 서버 배포 |

## 로컬에서 운영 구성 띄워 보기

Docker Desktop을 켜고:

```bash
cd deploy
cp .env.example .env      # DOMAIN=localhost, 나머지는 아무 값 (JWT_SECRET은 32자 이상)
docker compose -f compose.yaml -f compose.local.yaml up -d --build --wait
curl -k https://localhost/actuator/health
docker compose -f compose.yaml -f compose.local.yaml down -v   # -v는 DB 데이터까지 지움
```

## 오라클 클라우드에 처음 올리기

1. **인스턴스 만들기** (콘솔 > 컴퓨트 > 인스턴스 > 인스턴스 생성)
   - 이미지: Canonical Ubuntu 24.04 (aarch64)
   - Shape: Ampere `VM.Standard.A1.Flex`, 2 OCPU / 12GB 정도 (Always Free 한도 안). "Out of capacity"가 나면 시간을 두고 다시 시도하거나 가용성 도메인을 바꾼다
   - SSH 키: "키 쌍 생성"에서 개인 키를 받아 `~/.ssh/lasteat-server.key`로 저장 (Git Bash에서 `chmod 600`)
   - 부트 볼륨 50GB
2. **공용 IP 고정**: 인스턴스 > 연결된 VNIC > IPv4 주소 > 예약된 공용 IP로 바꾼다 (안 하면 재시작 때 바뀔 수 있음)
3. **보안 목록**: VCN > 서브넷 > 기본 보안 목록 > 수신 규칙에 TCP 80, 443 추가 (0.0.0.0/0). 22는 기본으로 열려 있다. 3306은 열지 않는다
4. **DuckDNS**: 하위 도메인(예: `lasteat`)을 만들고 3번의 공용 IP를 넣는다. 인증서 발급 전에 도메인이 이 IP를 가리켜야 한다
5. **서버 준비** (`ssh -i ~/.ssh/lasteat-server.key ubuntu@{공용 IP}`)
   ```bash
   # 오라클 Ubuntu 이미지는 iptables가 22번 외에는 막는다. 보안 목록과 별개로 서버에서도 연다
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
   sudo netfilter-persistent save

   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER && exit   # 다시 접속
   sudo timedatectl set-timezone Asia/Seoul
   ```
6. **설정 받고 띄우기** (저장소·이미지 모두 공개라 로그인 필요 없음)
   ```bash
   git clone https://github.com/hotpes/LastEat.git && cd LastEat/deploy
   cp .env.example .env && chmod 600 .env && nano .env
   docker compose pull && docker compose up -d --wait
   ```
   - `pull`이 `denied`로 실패하면 이미지가 비공개인 것: 저장소 주인 계정 > Packages > `lasteat-backend` > Package settings > Change visibility > Public (처음 한 번만)
7. **확인**: `curl https://{도메인}/actuator/health`, 브라우저로 `https://{도메인}/swagger-ui/index.html`

**무료 인스턴스 회수 주의**: Always Free 계정은 CPU·네트워크 사용률이 계속 낮으면 인스턴스가 회수될 수 있다. 계정을 종량제(Pay As You Go)로 바꾸면 무료 한도 안에서는 요금 없이 회수 대상에서 빠진다.

## 업데이트

`backend/`나 `deploy/`가 바뀐 PR이 main에 merge되면 자동 배포된다: `test`·`docker` → `publish` → `deploy`.
`deploy` job은 저장소 Variables에 `DEPLOY_HOST`(공용 IP)와 `DEPLOY_DOMAIN`(도메인)이 있을 때만 돈다.

손으로 배포할 때(예: `.env`를 바꾼 뒤): `~/LastEat/deploy/deploy.sh`

- 스크립트는 `git reset --hard origin/main`으로 서버의 추적 파일을 main과 맞춘다. 서버에서 저장소 파일을 직접 고치지 않는다(`.env`는 남는다)
- 되돌리기: `.env`의 `APP_TAG`를 이전 태그(`sha-xxxxxxx`)로 바꾸고 `deploy.sh`. 고친 뒤에는 비운다
- DB 스키마는 앱이 시작할 때 Flyway가 적용한다. 스키마를 바꿀 때는 `V1`을 고치지 말고 `V2__설명.sql`을 추가한다

### 자동 배포용 SSH 키

CI 전용 키를 만들고, 서버의 `~/.ssh/authorized_keys`에서 그 키는 `deploy.sh`만 실행하도록 묶는다. 키가 새어도 셸을 얻을 수 없다.

```
command="/home/ubuntu/LastEat/deploy/deploy.sh",restrict ssh-ed25519 AAAA… github-actions-deploy
```

| 저장소 설정 (environment `production`) | 값 |
|:---|:---|
| Secret `DEPLOY_SSH_KEY` | 위 키의 개인 키 |
| Secret `DEPLOY_KNOWN_HOSTS` | `ssh-keyscan -t ed25519 {공용 IP}` 결과 |
| Variable `DEPLOY_HOST` | 공용 IP |
| Variable `DEPLOY_DOMAIN` | 도메인 (예: `lasteat.duckdns.org`) |

## 팀원이 DB 보기

MySQL은 서버 안(`127.0.0.1:3306`)에서만 열려 있다. 팀원은 SSH 터널로 접속한다.

1. 팀원이 SSH 키를 만들어 공개 키를 서버 담당에게 준다. 서버 담당이 `~/.ssh/authorized_keys`에 한 줄 추가
2. 조회 전용 계정을 한 번 만든다
   ```bash
   docker compose exec mysql mysql -u root -p -e "
     CREATE USER 'viewer'@'%' IDENTIFIED BY '{비밀번호}';
     GRANT SELECT ON lasteat.* TO 'viewer'@'%';"
   ```
3. DBeaver: 새 연결 > MySQL > Host `127.0.0.1`, Port `3306`, DB `lasteat`, 사용자 `viewer` / SSH 탭에서 Host `{공용 IP}`, User `ubuntu`, 인증 방식 공개 키(본인 개인 키)

## 운영

| 할 일 | 명령 (`~/LastEat/deploy`에서) |
|:---|:---|
| 상태 | `docker compose ps` |
| 로그 | `docker compose logs -f app` |
| DB 백업 | `docker compose exec -T mysql sh -c 'mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" lasteat' \| gzip > backup-$(date +%F).sql.gz` |
| DB 복원 | `gunzip -c backup.sql.gz \| docker compose exec -T mysql sh -c 'mysql -u root -p"$MYSQL_ROOT_PASSWORD" lasteat'` |

- `DB_PASSWORD`, `DB_ROOT_PASSWORD`는 MySQL을 **처음 띄울 때** 저장된다. 나중에 `.env`만 바꾸면 앱이 접속하지 못한다
- `JWT_SECRET`을 바꾸면 모든 사용자가 다시 로그인해야 한다

## 아직 안 한 것

- DB 자동 백업. 지금은 위 `mysqldump`를 손으로 실행
- 업로드 이미지 저장소 (미정)
