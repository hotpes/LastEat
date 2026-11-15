# LastEat API 명세 v0.3

> **2026-10-31 회의 결정**: 리뷰·쿠폰 기능 전부 삭제 (별점 표시도 제거).
> **v0.3 (2026-11-13)**: 마감할인·예약 API 추가 (화면 시안이 아직 없어서 엔티티를 기준으로 설계했고, 시안이 나오면 맞춘다). 일반 할인(혜택)은 사장님 앱 "할인 공지" 화면에 맞춰 글로 쓰는 공지로 바꿨다.
> 기준 화면: 손님 앱 UI 초안 7개 화면, 사장님 앱 UI (`Owner` 브랜치).
> Sprint 2부터 컨트롤러를 구현하면서 Swagger(springdoc)로 옮겨 간다. 구현된 API는 Swagger가 기준이다.

**인증** 공개: 로그인 없이 가능 · 로그인: 로그인 필요 · 사장님: 사장님 계정 로그인 필요
**Base URL** `/api/v1` · 응답 형식·페이지·날짜 규칙은 [공통 규칙](#common)

## 한눈에 보기

| 메서드 | 경로 | 설명 | 인증 | 화면 |
|---|---|---|:-:|---|
| **인증·회원** | | | | |
| POST | [`/auth/kakao`](#auth-kakao) | 카카오 로그인 (첫 로그인 시 가입) | 공개 | 마이 |
| POST | [`/auth/refresh`](#auth-refresh) | 토큰 재발급 | 공개 | 공통 |
| POST | [`/auth/logout`](#auth-logout) | 로그아웃 | 공개 | 마이 |
| GET | [`/members/me`](#members-me) | 내 정보 | 로그인 | 마이 |
| PATCH | [`/members/me/fcm-token`](#members-fcm) | 푸시 토큰 갱신 | 로그인 | 공통 |
| DELETE | [`/members/me`](#members-delete) | 회원 탈퇴 | 로그인 | 마이 |
| **가게** | | | | |
| GET | [`/stores`](#stores-list) | 주변 가게 목록·검색·필터 | 공개 | 주변 탐색, 검색 결과 |
| GET | [`/stores/{storeId}`](#stores-detail) | 가게 상세 | 공개 | 식당 상세 |
| GET | [`/stores/{storeId}/menus`](#stores-menus) | 메뉴 목록 | 공개 | 식당 상세 › 메뉴 |
| GET | [`/stores/{storeId}/benefits`](#stores-benefits) | 가게 할인 공지 목록 | 공개 | 식당 상세 › 혜택 |
| **할인 공지** | | | | |
| GET | [`/benefits`](#benefits-list) | 주변 할인 공지 목록 | 공개 | 혜택·이벤트 |
| GET | [`/benefits/{benefitId}`](#benefits-detail) | 할인 공지 상세 | 공개 | 혜택·이벤트, 식당 상세 |
| **저장** | | | | |
| POST | [`/stores/{storeId}/bookmark`](#bookmark-add) | 가게 저장 | 로그인 | 상세, 목록 |
| DELETE | [`/stores/{storeId}/bookmark`](#bookmark-remove) | 저장 취소 | 로그인 | 상세, 목록 |
| GET | [`/members/me/bookmarks`](#bookmark-list) | 내 저장 목록 | 로그인 | 저장 |
| **마감할인·예약** | | | | |
| GET | [`/closing-deals`](#deals-list) | 주변 마감할인 목록 | 공개 | 마감할인 |
| GET | [`/stores/{storeId}/closing-deals`](#deals-store) | 가게 마감할인 목록 | 공개 | 식당 상세 |
| GET | [`/closing-deals/{dealId}`](#deals-detail) | 마감할인 상세 | 공개 | 마감할인 |
| POST | [`/closing-deals/{dealId}/reservations`](#reserve) | 예약하기 | 로그인 | 마감할인 |
| GET | [`/members/me/reservations`](#my-reservations) | 내 예약 목록 | 로그인 | 마이 › 예약 |
| GET | [`/reservations/{reservationId}`](#reservation-detail) | 내 예약 상세 (수령 화면) | 로그인 | 마이 › 예약 |
| POST | [`/reservations/{reservationId}/cancel`](#reservation-cancel) | 예약 취소 | 로그인 | 마이 › 예약 |
| **사장님 · 가게** | | | | |
| GET | [`/owner/stores`](#owner-stores) | 내 가게 목록 | 사장님 | 사장님 앱 › 홈 |
| GET | [`/owner/stores/{storeId}`](#owner-store-get) | 가게 정보 조회 | 사장님 | 사장님 앱 › 가게 |
| PUT | [`/owner/stores/{storeId}`](#owner-store-put) | 가게 정보 수정 | 사장님 | 사장님 앱 › 가게 |
| PUT | [`/owner/stores/{storeId}/hours`](#owner-hours) | 영업시간 수정 | 사장님 | 사장님 앱 › 영업시간 |
| PATCH | [`/owner/stores/{storeId}/business-status`](#owner-status) | 오늘 영업 상태 변경 | 사장님 | 사장님 앱 › 영업 상태 |
| **사장님 · 메뉴** | | | | |
| GET | [`/owner/stores/{storeId}/menus`](#owner-menus) | 메뉴 목록 | 사장님 | 사장님 앱 › 메뉴 |
| POST | [`/owner/stores/{storeId}/menus`](#owner-menus) | 메뉴 추가 | 사장님 | 〃 |
| PUT | [`/owner/stores/{storeId}/menus/{menuId}`](#owner-menus) | 메뉴 수정 | 사장님 | 〃 |
| DELETE | [`/owner/stores/{storeId}/menus/{menuId}`](#owner-menus) | 메뉴 삭제 | 사장님 | 〃 |
| PATCH | [`/owner/stores/{storeId}/menus/{menuId}/sold-out`](#owner-menus) | 품절 표시 | 사장님 | 〃 |
| **사장님 · 할인 공지** | | | | |
| POST | [`/owner/stores/{storeId}/benefits`](#owner-benefit-create) | 할인 공지 게시 | 사장님 | 사장님 앱 › 새 할인 공지 |
| GET | [`/owner/stores/{storeId}/benefits`](#owner-benefit-list) | 내 가게 할인 공지 목록 | 사장님 | 사장님 앱 › 할인 공지 관리 |
| PUT | [`/owner/stores/{storeId}/benefits/{benefitId}`](#owner-benefit-edit) | 할인 공지 수정 | 사장님 | 〃 |
| PATCH | [`/owner/stores/{storeId}/benefits/{benefitId}/published`](#owner-benefit-edit) | 공개·비공개 전환 | 사장님 | 〃 |
| DELETE | [`/owner/stores/{storeId}/benefits/{benefitId}`](#owner-benefit-edit) | 할인 공지 삭제 | 사장님 | 〃 |
| **사장님 · 마감할인** | | | | |
| POST | [`/owner/stores/{storeId}/closing-deals`](#owner-deal-create) | 마감할인 등록 | 사장님 | 사장님 앱 › 마감할인 |
| GET | [`/owner/stores/{storeId}/closing-deals`](#owner-deal-list) | 마감할인 목록 | 사장님 | 〃 |
| PATCH | [`/owner/stores/{storeId}/closing-deals/{dealId}/stock`](#owner-deal-edit) | 재고 수정 | 사장님 | 〃 |
| POST | [`/owner/stores/{storeId}/closing-deals/{dealId}/close`](#owner-deal-edit) | 조기 마감 | 사장님 | 〃 |
| GET | [`/owner/stores/{storeId}/reservations`](#owner-reservations) | 예약 목록 | 사장님 | 사장님 앱 › 예약 |
| POST | [`/owner/stores/{storeId}/reservations/{reservationId}/pickup`](#owner-reservation-action) | 수령 완료 | 사장님 | 〃 |
| POST | [`/owner/stores/{storeId}/reservations/{reservationId}/no-show`](#owner-reservation-action) | 노쇼 처리 | 사장님 | 〃 |
| **업로드** | | | | |
| POST | [`/uploads/images`](#upload) | 이미지 업로드 | 사장님 | 사장님 앱 › 사진 |

### 범위

| 포함 | 제외 |
|---|---|
| 로그인, 가게 검색·상세, 할인 공지 조회, 저장, 마감할인 조회·예약·수령, 사장님 가게·메뉴·할인 공지·마감할인 관리, 이미지 업로드 | 리뷰·쿠폰 (회의 결정) · 결제 (수령할 때 가게에서 현장 결제) · 예약·마감 알림, 컬렉션·소식·통계 (2차) · 관리자 화면 (제휴 지정은 DB) · 공지·FAQ (외부 링크) |

---

<a id="common"></a>
## 공통 규칙

| 항목 | 값 |
|---|---|
| 형식 | JSON, UTF-8 |
| 인증 헤더 | `Authorization: Bearer {accessToken}` |
| 시간대 | 한국 시간 (Asia/Seoul) |
| 날짜 / 시각 / 날짜+시각 | `2026-11-01` / `14:00` / `2026-11-01T14:00:00+09:00` |
| 거리 | 미터(m) 정수 |
| 좌표 | `lat` 위도, `lng` 경도 |
| 목록 요청 | `page` (0부터, 기본 0), `size` (기본 20, 최대 100) |

공개 API에 토큰을 함께 보내면 `bookmarked`처럼 로그인한 사람 기준 값이 채워진다. 토큰이 없으면 `false`.

**응답 형식** — 모든 API가 같은 틀을 쓴다. 아래 각 API의 응답 예시는 `data` 안의 내용만 적었다.

```json
{ "success": true,  "data": { },  "error": null }
{ "success": false, "data": null, "error": { "code": "STORE_NOT_FOUND", "message": "가게를 찾을 수 없습니다." } }
```

`message`는 앱 화면에 그대로 보여 줘도 되는 문장이다. 에러 코드 전체는 [부록 B](#errors).

<details>
<summary>입력값 오류 응답 예시</summary>

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_INPUT",
    "message": "입력값을 확인해 주세요.",
    "fields": [
      { "field": "title", "reason": "30자 이하로 입력해 주세요." }
    ]
  }
}
```
</details>

<details>
<summary>목록(페이지) 응답 형식</summary>

```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 24,
  "hasNext": true
}
```
</details>

---

## 인증·회원

로그인 흐름: 앱이 카카오 SDK로 로그인 → 카카오 액세스 토큰을 서버에 전달 → 서버가 카카오에 확인 → 서버 자체 토큰 발급. 처음 로그인하면 자동 가입. accessToken(JWT) 1시간, refreshToken 14일. refreshToken은 JWT가 아닌 임의 문자열이고, 서버는 해시만 저장한다. 카카오 토큰은 우리 앱(앱 ID)에서 발급된 것만 받는다.

<a id="auth-kakao"></a>
### POST /auth/kakao — 카카오 로그인 (공개)

| 필드 | 타입 | 필수 | 설명 |
|---|---|:-:|---|
| kakaoAccessToken | string | O | 카카오 SDK에서 받은 액세스 토큰 |
| fcmToken | string | | 푸시 알림 토큰 (2차 알림 기능용, 미리 저장) |

<details>
<summary>응답 예시</summary>

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "q3Xr9...(임의 문자열)",
  "newMember": true,
  "member": { "id": 12, "nickname": "맛집탐험가", "role": "CONSUMER" }
}
```
</details>

에러: `KAKAO_AUTH_FAILED`

<a id="auth-refresh"></a>
### POST /auth/refresh — 토큰 재발급 (공개)

| 필드 | 타입 | 필수 |
|---|---|:-:|
| refreshToken | string | O |

- 응답: `accessToken`, `refreshToken` 둘 다 새로 발급. 이전 refreshToken은 폐기
- 에러: `INVALID_TOKEN`, `EXPIRED_TOKEN` → 카카오 로그인부터 다시

<a id="auth-logout"></a>
### POST /auth/logout — 로그아웃 (공개)

| 필드 | 타입 | 필수 |
|---|---|:-:|
| refreshToken | string | O |

이 기기의 refreshToken을 폐기한다. accessToken이 만료된 상태에서도 로그아웃할 수 있도록 공개로 둔다. 이미 폐기된 토큰이어도 성공. 응답 `data`: `null`. accessToken은 앱에서 지운다 (서버는 만료 전까지 막지 않음)

<a id="members-me"></a>
### GET /members/me — 내 정보 (로그인)

<details>
<summary>응답 예시</summary>

```json
{
  "id": 12,
  "nickname": "맛집탐험가",
  "email": "user@kakao.com",
  "role": "OWNER",
  "ownedStoreCount": 1
}
```
</details>

- `email`은 카카오 동의 항목에 따라 `null`일 수 있음
- `ownedStoreCount`가 1 이상이면 마이 화면에 사장님 메뉴 표시

<a id="members-fcm"></a>
### PATCH /members/me/fcm-token — 푸시 토큰 갱신 (로그인)

| 필드 | 타입 | 필수 |
|---|---|:-:|
| fcmToken | string | O |

<a id="members-delete"></a>
### DELETE /members/me — 회원 탈퇴 (로그인)

- 저장한 가게와 토큰을 함께 삭제
- 사장님으로 연결된 가게가 있으면 `OWNER_HAS_STORE` (운영진에게 연결 해제 요청)

---

## 가게

<a id="stores-list"></a>
### GET /stores — 주변 가게 목록·검색 (공개)

화면: 주변 탐색 (지도 마커 + 하단 목록), 검색 결과. "이 지역 다시 찾기"는 지도 중심 좌표로 다시 호출.

| 파라미터 | 타입 | 필수 | 기본 | 설명 |
|---|---|:-:|---|---|
| lat, lng | number | O | | 기준 좌표 (내 위치 또는 지도 중심) |
| radius | int | | 500 | 반경(m), 최대 3000 |
| keyword | string | | | 가게 이름·카테고리 이름·메뉴 이름 검색, 2자 이상 |
| category | string | | | [카테고리](#code-category), 여러 개는 쉼표 (`KOREAN,CAFE`) |
| openNow | boolean | | false | 지금 영업 중인 곳만 |
| hasBenefit | boolean | | false | 공개 중인 할인 공지가 있는 곳만 |
| sort | string | | DISTANCE | `DISTANCE` 가까운 순 (1차는 이것만) |
| page, size | int | | 0, 20 | 지도 마커를 한 번에 그릴 때는 `size=100` |

<details>
<summary>응답 예시 (<code>content[]</code> 항목)</summary>

```json
{
  "id": 3,
  "name": "연남 온기",
  "category": "KOREAN",
  "signatureMenuName": "연어 아보카도 덮밥",
  "thumbnailUrl": "https://cdn.lasteat.app/stores/3/main.jpg",
  "lat": 37.5622,
  "lng": 126.9255,
  "distance": 240,
  "businessStatus": "OPEN",
  "recommended": true,
  "mainBenefit": { "id": 11, "title": "평일 점심 10% 할인" },
  "bookmarked": false
}
```
</details>

- `signatureMenuName`: 대표 메뉴 중 첫 번째, 없으면 `null`
- `mainBenefit`: 공개 중인 할인 공지 중 가장 최근에 게시한 1개, 없으면 `null`
- `businessStatus`: [영업 상태](#code-status). "지금 영업 중인 24곳"은 `openNow=true`의 `totalElements`
- 반경 안 후보는 최대 1000곳까지만 본다 (1차 상권 범위에서는 넘지 않음)

<a id="stores-detail"></a>
### GET /stores/{storeId} — 가게 상세 (공개)

화면: 식당 상세 (정보 탭, 대표 메뉴, 챙겨가는 혜택)

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:-:|---|
| lat, lng | number | | 보내면 `distance` 계산, 없으면 `null` |

<details>
<summary>응답 예시</summary>

```json
{
  "id": 3,
  "name": "연남 온기",
  "category": "KOREAN",
  "description": "제철 재료로 채운 든든한 한 그릇.",
  "address": "서울 마포구 동교로38길 12, 1층",
  "phone": "02-332-0428",
  "lat": 37.5622,
  "lng": 126.9255,
  "distance": 240,
  "imageUrls": [
    "https://cdn.lasteat.app/stores/3/main.jpg",
    "https://cdn.lasteat.app/stores/3/2.jpg"
  ],
  "tags": ["혼밥 좋아요", "포장 가능", "주차 불가"],
  "recommended": true,
  "businessStatus": "OPEN",
  "businessHours": {
    "openTime": "11:00",
    "closeTime": "21:00",
    "breakStart": "15:30",
    "breakEnd": "17:00",
    "closedDays": ["MON"]
  },
  "signatureMenus": [
    {
      "id": 21,
      "name": "연어 아보카도 덮밥",
      "description": "구운 연어와 고소한 아보카도의 조화",
      "price": 15000,
      "imageUrl": "https://cdn.lasteat.app/menus/21.jpg",
      "soldOut": false
    }
  ],
  "menuCount": 8,
  "benefits": [
    {
      "id": 11,
      "title": "평일 점심 10% 할인",
      "partner": true,
      "periodText": "11월 한 달, 평일 11~14시"
    }
  ],
  "bookmarked": false
}
```
</details>

- `imageUrls[0]`이 대표 사진, 배열 길이가 사진 개수 ("사진 24")
- `benefits`는 공개 중인 할인 공지의 요약, 최근 게시 순. 내용까지 보려면 [가게 할인 공지 목록](#stores-benefits)
- 마지막 주문 시간은 1차 범위에서 제외 (필요하면 `lastOrderTime` 추가)
- 에러: `STORE_NOT_FOUND`

<a id="stores-menus"></a>
### GET /stores/{storeId}/menus — 메뉴 목록 (공개)

화면: 식당 상세 › 메뉴 탭, "전체 메뉴 8". 대표 메뉴 먼저, 그다음 등록 순. 페이지 없음.

<details>
<summary>응답 예시 (배열 항목)</summary>

```json
{
  "id": 21,
  "name": "연어 아보카도 덮밥",
  "description": "구운 연어와 고소한 아보카도의 조화",
  "price": 15000,
  "imageUrl": "https://cdn.lasteat.app/menus/21.jpg",
  "signature": true,
  "soldOut": false
}
```
</details>

<a id="stores-benefits"></a>
### GET /stores/{storeId}/benefits — 가게 할인 공지 목록 (공개)

화면: 식당 상세 › 혜택 탭. [할인 공지 상세](#benefits-detail) 객체의 배열 (`store.distance`는 `null`). 공개 중인 공지만, 최근 게시 순. 페이지 없음. 에러: `STORE_NOT_FOUND`

---

<a id="benefits"></a>
## 할인 공지 (일반 할인·제휴 혜택)

> **2026-11-13 결정**: 일반 할인은 사장님이 글로 쓰는 공지로 받는다 (사장님 앱 "할인 공지" 화면). 할인율·요일·시간 같은 구조화된 입력과 운영진 검토는 없앴다. 예약이 필요한 마감할인은 [마감할인·예약](#closing-deals)에서 따로 다룬다.

- 사장님이 게시하면 바로 손님에게 보인다. 사장님이 비공개로 바꾸거나 삭제하면 사라진다
- 안내 기간은 글이라서 서버가 기간이 끝났는지 판단하지 않는다. 끝난 공지는 사장님이 비공개로 바꾼다
- 제휴 혜택 여부(`partner`)는 운영진이 DB에서 지정한다
- 경로 이름은 `benefits`를 그대로 쓴다

<a id="benefits-list"></a>
### GET /benefits — 주변 할인 공지 목록 (공개)

화면: 혜택·이벤트 탭 "지금, 내 주변 혜택"

| 파라미터 | 타입 | 필수 | 기본 | 설명 |
|---|---|:-:|---|---|
| lat, lng | number | O | | 기준 좌표 |
| radius | int | | 1000 | 반경(m), 최대 3000 |
| partner | boolean | | false | `true`면 제휴 혜택만 |
| page, size | int | | 0, 20 | |

가까운 순, 같은 가게면 최근 게시 순. 화면 필터 매핑: 전체 → 조건 없음 · 제휴 혜택 → `partner=true`. 할인 유형 필터와 "곧 마감" 정렬은 없다 (안내 기간이 글이라서).

<details>
<summary>응답 예시 (<code>content[]</code> 항목)</summary>

```json
{
  "id": 11,
  "title": "평일 점심 10% 할인",
  "content": "평일 점심 식사 메뉴 주문 시 10% 할인해 드려요. 2만원 이상 주문 시, 주류 제외.",
  "periodText": "11월 한 달, 평일 11~14시",
  "partner": true,
  "publishedAt": "2026-11-01T09:30:00+09:00",
  "store": {
    "id": 3,
    "name": "연남 온기",
    "category": "KOREAN",
    "thumbnailUrl": "https://cdn.lasteat.app/stores/3/main.jpg",
    "distance": 240
  }
}
```
</details>

- "제휴 식당 12곳" 배너 숫자는 `partner=true`의 `totalElements` (공지 수 기준)

<a id="benefits-detail"></a>
### GET /benefits/{benefitId} — 할인 공지 상세 (공개)

[목록](#benefits-list) 항목과 같은 형식, `store.distance`는 `null`. 에러: `BENEFIT_NOT_FOUND` (비공개 공지 포함)

---

## 저장

<a id="bookmark-add"></a>
### POST /stores/{storeId}/bookmark — 가게 저장 (로그인)

이미 저장한 가게여도 성공 (중복 저장 안 됨). 응답 `data`: `{ "bookmarked": true }`

<a id="bookmark-remove"></a>
### DELETE /stores/{storeId}/bookmark — 저장 취소 (로그인)

저장하지 않은 가게여도 성공. 응답 `data`: `{ "bookmarked": false }`

<a id="bookmark-list"></a>
### GET /members/me/bookmarks — 내 저장 목록 (로그인)

화면: 저장 탭

| 파라미터 | 타입 | 필수 | 기본 | 설명 |
|---|---|:-:|---|---|
| category | string | | | 카테고리 필터 |
| openNow | boolean | | false | 영업 중만 |
| lat, lng | number | | | 보내면 `distance` 계산 |
| sort | string | | RECENT | `RECENT` 최근 저장 순, `DISTANCE` (좌표 필요, 없으면 `INVALID_INPUT`) |
| page, size | int | | 0, 20 | |

응답 `content[]`: [주변 가게 목록](#stores-list) 항목과 같은 형식 + `bookmarkedAt`

---

<a id="closing-deals"></a>
## 마감할인·예약

마감 시간대에 남은 메뉴를 할인가로 올리면, 손님이 앱으로 예약하고 수령 마감 전까지 가게에서 받아 간다. **결제는 앱에서 하지 않고, 수령할 때 가게에서 한다.**

흐름

1. 사장님이 메뉴·할인가·수량·노출 시작·수령 마감을 정해 등록한다 (혜택과 달리 운영진 검토 없음)
2. 노출 시작 시각이 되면 손님에게 보이고 예약할 수 있다
3. 손님이 수량을 골라 예약하면 그만큼 재고가 줄어든다. 재고가 0이 되면 품절
4. 손님이 가게에서 예약 화면을 보여 주면 사장님이 수령 완료 처리를 한다
5. 수령 마감이 지나면 마감할인이 끝나고, 수령 마감 30분 뒤까지 받아 가지 않은 예약은 노쇼로 바뀐다

규칙

| 항목 | 값 |
|---|---|
| 1회 예약 수량 | 1~5개, 남은 재고 이하 |
| 같은 마감할인 중복 예약 | 불가. 수량을 바꾸려면 취소 후 다시 예약 |
| 손님 취소 | 수령 마감 전까지. 취소하면 재고 복구 |
| 노쇼 | 사장님이 직접 처리하거나, 수령 마감 30분 뒤 서버가 자동 처리 (조기 마감한 마감할인 포함) |
| 동시 예약 | 여러 명이 동시에 예약해도 재고보다 많이 팔리지 않는다 |

상태 코드는 [마감할인 상태](#code-deal-status)와 [예약 상태](#code-reservation-status) 참고. 손님에게는 `OPEN`, `SOLD_OUT`인 마감할인만 보인다. 상태는 시각과 재고로 바로 계산한다. 수령 마감이 지난 마감할인의 마감 처리와 노쇼 처리는 서버가 1분마다 하는데, 조회와 예약은 시각을 직접 보고 판단하므로 이 처리가 조금 늦어도 결과는 같다. 동시에 예약이 몰리면 서버가 몇 번 다시 시도하고, 그래도 안 되면 `RESERVATION_BUSY`.

<a id="deals-list"></a>
### GET /closing-deals — 주변 마감할인 목록 (공개)

| 파라미터 | 타입 | 필수 | 기본 | 설명 |
|---|---|:-:|---|---|
| lat, lng | number | O | | 기준 좌표 |
| radius | int | | 1000 | 반경(m), 최대 3000 |
| category | string | | | [카테고리](#code-category), 여러 개는 쉼표 |
| available | boolean | | false | `true`면 품절 제외 |
| sort | string | | DISTANCE | `DISTANCE` 가까운 순, `ENDING_SOON` 수령 마감이 가까운 순 |
| page, size | int | | 0, 20 | |

<details>
<summary>응답 예시 (<code>content[]</code> 항목)</summary>

```json
{
  "id": 41,
  "status": "OPEN",
  "menu": {
    "id": 21,
    "name": "연어 아보카도 덮밥",
    "imageUrl": "https://cdn.lasteat.app/menus/21.jpg"
  },
  "originalPrice": 15000,
  "salePrice": 9000,
  "discountRate": 40,
  "stock": 3,
  "openAt": "2026-11-16T20:00:00+09:00",
  "pickupEndAt": "2026-11-16T21:30:00+09:00",
  "store": {
    "id": 3,
    "name": "연남 온기",
    "category": "KOREAN",
    "thumbnailUrl": "https://cdn.lasteat.app/stores/3/main.jpg",
    "distance": 240
  }
}
```
</details>

- `discountRate`: `(정가 - 할인가) / 정가`를 % 정수로 내림
- `stock`: 지금 예약할 수 있는 남은 수량

<a id="deals-store"></a>
### GET /stores/{storeId}/closing-deals — 가게 마감할인 목록 (공개)

화면: 식당 상세. [주변 마감할인 목록](#deals-list) 항목의 배열이고, `store.distance`는 `null`이다. 지금 노출 중인 것만 수령 마감이 가까운 순으로 준다. 페이지 없음. 에러: `STORE_NOT_FOUND`

<a id="deals-detail"></a>
### GET /closing-deals/{dealId} — 마감할인 상세 (공개)

[목록](#deals-list) 항목과 같은 형식이다. 아래 필드는 목록에도 들어 있지만 상세 화면에서 쓴다.

| 필드 | 설명 |
|---|---|
| `menu.description` | 메뉴 설명 |
| `store.address`, `store.phone` | 가게 주소·전화번호 |
| `myReservationId` | 로그인한 손님이 이 마감할인을 예약 중(`RESERVED`)이면 예약 ID, 아니면 `null`. 상세에서만 채우고 목록에서는 항상 `null` |

- `lat`, `lng`를 보내면 `store.distance`를 계산한다
- 에러: `CLOSING_DEAL_NOT_FOUND` (노출 전이거나 이미 끝난 마감할인도 포함)

<a id="reserve"></a>
### POST /closing-deals/{dealId}/reservations — 예약하기 (로그인)

| 필드 | 타입 | 필수 | 제약 |
|---|---|:-:|---|
| quantity | int | O | 1~5 |

응답: [예약 상세](#reservation-detail) (`status: "RESERVED"`)

| 에러 | 상황 |
|---|---|
| `CLOSING_DEAL_NOT_FOUND` | 없는 마감할인 |
| `CLOSING_DEAL_NOT_OPEN` | 노출 시작 전, 수령 마감 지남, 사장님이 마감함 |
| `OUT_OF_STOCK` | 남은 재고보다 많이 요청함 (품절 포함). 앱은 상세를 다시 불러 남은 수량을 보여 준다 |
| `ALREADY_RESERVED` | 이 마감할인에 이미 `RESERVED` 상태 예약이 있음 |
| `RESERVATION_BUSY` | 동시 요청이 몰려 처리하지 못함. 잠시 후 다시 시도 |

<a id="my-reservations"></a>
### GET /members/me/reservations — 내 예약 목록 (로그인)

| 파라미터 | 타입 | 필수 | 기본 | 설명 |
|---|---|:-:|---|---|
| status | string | | | [예약 상태](#code-reservation-status), 여러 개는 쉼표. 없으면 전체 |
| page, size | int | | 0, 20 | |

최근 예약 순. 응답 `content[]`는 [예약 상세](#reservation-detail)와 같은 형식이다. 앱에서 "이용 예정" 탭은 `status=RESERVED`, "지난 예약" 탭은 `status=PICKED_UP,CANCELED,NO_SHOW`로 부른다.

<a id="reservation-detail"></a>
### GET /reservations/{reservationId} — 내 예약 상세 (로그인)

화면: 수령할 때 가게에 보여 주는 화면

<details>
<summary>응답 예시</summary>

```json
{
  "id": 105,
  "status": "RESERVED",
  "quantity": 2,
  "totalPrice": 18000,
  "reservedAt": "2026-11-16T20:12:00+09:00",
  "pickedUpAt": null,
  "pickupEndAt": "2026-11-16T21:30:00+09:00",
  "closingDeal": {
    "id": 41,
    "menuName": "연어 아보카도 덮밥",
    "salePrice": 9000
  },
  "store": {
    "id": 3,
    "name": "연남 온기",
    "address": "서울 마포구 동교로38길 12, 1층",
    "phone": "02-332-0428"
  }
}
```
</details>

- `totalPrice` = `salePrice × quantity`. 가게에서 결제할 금액 (예약 당시 할인가 기준)
- 사장님은 예약 번호(`id`)와 닉네임으로 손님을 확인한다
- 에러: `RESERVATION_NOT_FOUND` (다른 사람의 예약도 포함)

<a id="reservation-cancel"></a>
### POST /reservations/{reservationId}/cancel — 예약 취소 (로그인)

`RESERVED` 상태이고 수령 마감 전일 때만 취소할 수 있다. 취소한 수량만큼 재고가 돌아온다 (품절이었으면 다시 예약 가능). 응답: [예약 상세](#reservation-detail) (`status: "CANCELED"`)

에러: `RESERVATION_NOT_FOUND`, `RESERVATION_NOT_CANCELABLE` (이미 수령·취소·노쇼 처리됨, 수령 마감 지남)

---

## 사장님

모든 API 사장님 로그인 필요. `{storeId}`가 본인 가게가 아니면 `NOT_STORE_OWNER` (403). 사장님 계정과 가게 연결은 1차에서 운영진이 직접 처리한다.

<a id="owner-stores"></a>
### GET /owner/stores — 내 가게 목록 (사장님)

등록 순. 연결된 가게가 없으면 빈 배열. `thumbnailUrl`은 사진이 없으면 `null`.

<details>
<summary>응답 예시 (배열 항목)</summary>

```json
{
  "id": 3,
  "name": "연남 온기",
  "address": "서울 마포구 동교로38길 12, 1층",
  "thumbnailUrl": "https://cdn.lasteat.app/stores/3/main.jpg",
  "businessStatus": "OPEN"
}
```
</details>

<a id="owner-store-get"></a>
### GET /owner/stores/{storeId} — 가게 정보 조회 (사장님)

[가게 상세](#stores-detail) 형식에서 `distance`, `bookmarked`를 빼고 사장님 전용 필드 `businessStatusOverride`를 더한다. 오늘 직접 정한 상태가 없거나 지난 날짜면 `null` ([영업 상태 변경](#owner-status) 참고). 에러: `STORE_NOT_FOUND`

<a id="owner-store-put"></a>
### PUT /owner/stores/{storeId} — 가게 정보 수정 (사장님)

| 필드 | 타입 | 필수 | 제약 |
|---|---|:-:|---|
| name | string | O | 1~30자 |
| category | string | O | [카테고리](#code-category) |
| address | string | O | |
| lat, lng | number | O | 주소를 바꾸면 앱이 지도 SDK로 좌표를 구해 함께 보냄 |
| description | string | | 300자 이하 |
| phone | string | | 숫자와 `-` |
| imageUrls | string[] | | 최대 30장, 첫 번째가 대표 사진. [업로드](#upload)에서 받은 URL |
| tags | string[] | | 최대 5개, 각 10자 이하 |

응답: [가게 정보 조회](#owner-store-get)와 같은 형식

<a id="owner-hours"></a>
### PUT /owner/stores/{storeId}/hours — 영업시간 수정 (사장님)

| 필드 | 타입 | 필수 | 설명 |
|---|---|:-:|---|
| openTime | string | O | `11:00` |
| closeTime | string | O | `21:00`. 자정을 넘기면 `openTime`보다 이른 시각 (예: `02:00`) |
| breakStart, breakEnd | string | | 쉬는 시간, 둘 다 보내거나 둘 다 생략 |
| closedDays | string[] | | 정기 휴무 [요일](#code-day) `["MON"]` |

사장님 앱은 요일을 숫자(1=월 ~ 7=일)로 가지고 있으므로 보낼 때 요일 코드로 바꾼다.

<a id="owner-status"></a>
### PATCH /owner/stores/{storeId}/business-status — 오늘 영업 상태 변경 (사장님)

화면: 사장님 앱 › 영업 상태 (영업 중 / 준비 중 / 오늘 휴무)

| 필드 | 타입 | 필수 | 설명 |
|---|---|:-:|---|
| status | string | O | `OPEN`, `BREAK`, `CLOSED`, `AUTO` |

- `OPEN`·`BREAK`·`CLOSED`: 영업시간과 상관없이 **오늘 하루** 이 상태로 표시. 다음 날 0시에 `AUTO`로 돌아감
- `AUTO`: 영업시간 기준 자동 계산
- 응답: `{ "businessStatus": "CLOSED", "businessStatusOverride": "CLOSED" }`

<a id="owner-menus"></a>
### 메뉴 관리 (사장님)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/owner/stores/{storeId}/menus` | 메뉴 목록 ([메뉴 목록](#stores-menus)과 같은 형식) |
| POST | `/owner/stores/{storeId}/menus` | 메뉴 추가 |
| PUT | `/owner/stores/{storeId}/menus/{menuId}` | 메뉴 수정 |
| DELETE | `/owner/stores/{storeId}/menus/{menuId}` | 메뉴 삭제 |
| PATCH | `/owner/stores/{storeId}/menus/{menuId}/sold-out` | 품절 표시 `{ "soldOut": true }` |

추가·수정 요청

| 필드 | 타입 | 필수 | 제약 |
|---|---|:-:|---|
| name | string | O | 1~30자 |
| price | int | O | 0 이상 |
| description | string | | 100자 이하 |
| imageUrl | string | | [업로드](#upload)에서 받은 URL |
| signature | boolean | | 대표 메뉴 여부, 기본 false |

응답: 메뉴 객체. 에러: `MENU_NOT_FOUND`

<a id="owner-benefit-create"></a>
### POST /owner/stores/{storeId}/benefits — 할인 공지 게시 (사장님)

화면: 사장님 앱 › 새 할인 공지 › "공지 게시". 게시하면 바로 공개된다. 입력 제한은 화면과 같다.

| 필드 | 타입 | 필수 | 제약 / 설명 |
|---|---|:-:|---|
| title | string | O | 공지 제목, 60자 이하 |
| content | string | O | 할인 내용·이용 조건, 1000자 이하 |
| periodText | string | O | 안내 기간, 100자 이하 (예: "11월 한 달", "상시") |

공백만 입력하면 `INVALID_INPUT`. 앞뒤 공백은 지우고 저장한다. 응답: [내 가게 할인 공지 목록](#owner-benefit-list) 항목 1개 (`published: true`)

<a id="owner-benefit-list"></a>
### GET /owner/stores/{storeId}/benefits — 내 가게 할인 공지 목록 (사장님)

<details>
<summary>응답 예시 (배열 항목)</summary>

```json
{
  "id": 11,
  "title": "평일 점심 10% 할인",
  "content": "평일 점심 식사 메뉴 주문 시 10% 할인해 드려요. 2만원 이상 주문 시, 주류 제외.",
  "periodText": "11월 한 달, 평일 11~14시",
  "published": true,
  "partner": true,
  "publishedAt": "2026-11-01T09:30:00+09:00"
}
```
</details>

- 공개·비공개 모두, 최근 게시 순. 페이지 없음
- `publishedAt`: 처음 게시한 시각 (사장님 앱의 `Notice.publishedAt`)

<a id="owner-benefit-edit"></a>
### 할인 공지 수정·공개 전환·삭제 (사장님)

| 메서드 | 경로 | 설명 |
|---|---|---|
| PUT | `/owner/stores/{storeId}/benefits/{benefitId}` | 수정. 요청은 [게시](#owner-benefit-create)와 같음. 공개 여부는 바뀌지 않음 |
| PATCH | `/owner/stores/{storeId}/benefits/{benefitId}/published` | 공개·비공개 전환 `{ "published": false }` |
| DELETE | `/owner/stores/{storeId}/benefits/{benefitId}` | 삭제 |

- 응답: PUT·PATCH는 [목록](#owner-benefit-list) 항목 1개, DELETE는 `null`
- 에러: `BENEFIT_NOT_FOUND` (다른 가게의 공지 포함)

<a id="owner-deal-create"></a>
### POST /owner/stores/{storeId}/closing-deals — 마감할인 등록 (사장님)

| 필드 | 타입 | 필수 | 제약 / 설명 |
|---|---|:-:|---|
| menuId | long | O | 이 가게의 메뉴. 정가는 등록 시점의 메뉴 가격으로 저장 |
| salePrice | int | O | 0 이상, 메뉴 가격보다 낮게 |
| stock | int | O | 1~99 |
| openAt | string | O | 노출 시작 (날짜+시각). 지금보다 이르면 바로 노출 |
| pickupEndAt | string | O | 수령 마감. `openAt` 이후이면서 지금 이후, `openAt`부터 24시간 이내 |

- 품절 표시된 메뉴도 등록할 수 있다 (마감할인 재고는 메뉴 품절과 따로 관리)
- 응답: [마감할인 목록](#owner-deal-list) 항목 1개
- 에러: `MENU_NOT_FOUND` (다른 가게 메뉴 포함), `INVALID_INPUT`. 할인가·수령 마감처럼 다른 값과 비교해야 알 수 있는 오류도 `fields`에 칸 이름(`salePrice`, `pickupEndAt`)과 이유를 담는다

<a id="owner-deal-list"></a>
### GET /owner/stores/{storeId}/closing-deals — 마감할인 목록 (사장님)

| 파라미터 | 타입 | 필수 | 기본 | 설명 |
|---|---|:-:|---|---|
| date | string | | 오늘 | `openAt` 날짜 기준 |

<details>
<summary>응답 예시 (배열 항목)</summary>

```json
{
  "id": 41,
  "status": "OPEN",
  "menu": { "id": 21, "name": "연어 아보카도 덮밥", "imageUrl": "https://cdn.lasteat.app/menus/21.jpg" },
  "originalPrice": 15000,
  "salePrice": 9000,
  "discountRate": 40,
  "stock": 3,
  "reservedQuantity": 4,
  "pickedUpQuantity": 1,
  "openAt": "2026-11-16T20:00:00+09:00",
  "pickupEndAt": "2026-11-16T21:30:00+09:00"
}
```
</details>

- `reservedQuantity`: 수령 대기(`RESERVED`) 수량 합계, `pickedUpQuantity`: 수령 완료 수량 합계
- 노출 시작 순, 페이지 없음

<a id="owner-deal-edit"></a>
### 재고 수정·조기 마감 (사장님)

| 메서드 | 경로 | 설명 |
|---|---|---|
| PATCH | `/owner/stores/{storeId}/closing-deals/{dealId}/stock` | 남은 재고 수정 `{ "stock": 5 }` (0 이상). 0이면 품절, 품절 상태에서 늘리면 다시 예약 가능 |
| POST | `/owner/stores/{storeId}/closing-deals/{dealId}/close` | 조기 마감. 더 이상 노출·예약되지 않는다. 이미 받은 예약은 그대로 수령 가능 |

- 이미 끝난(`CLOSED`) 마감할인은 바꿀 수 없다: `CLOSING_DEAL_NOT_OPEN`
- 응답: [마감할인 목록](#owner-deal-list) 항목 1개
- 에러: `CLOSING_DEAL_NOT_FOUND`

<a id="owner-reservations"></a>
### GET /owner/stores/{storeId}/reservations — 예약 목록 (사장님)

| 파라미터 | 타입 | 필수 | 기본 | 설명 |
|---|---|:-:|---|---|
| date | string | | 오늘 | 마감할인 `openAt` 날짜 기준 |
| closingDealId | long | | | 특정 마감할인만 |
| status | string | | | [예약 상태](#code-reservation-status), 여러 개는 쉼표 |
| page, size | int | | 0, 20 | |

<details>
<summary>응답 예시 (<code>content[]</code> 항목)</summary>

```json
{
  "id": 105,
  "status": "RESERVED",
  "quantity": 2,
  "totalPrice": 18000,
  "reservedAt": "2026-11-16T20:12:00+09:00",
  "pickedUpAt": null,
  "memberNickname": "맛집탐험가",
  "closingDeal": { "id": 41, "menuName": "연어 아보카도 덮밥", "pickupEndAt": "2026-11-16T21:30:00+09:00" }
}
```
</details>

수령 대기(`RESERVED`)를 먼저, 그다음은 예약 순. 손님 연락처는 주지 않는다.

<a id="owner-reservation-action"></a>
### 수령 완료·노쇼 처리 (사장님)

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/owner/stores/{storeId}/reservations/{reservationId}/pickup` | 수령 완료. `pickedUpAt` 기록 |
| POST | `/owner/stores/{storeId}/reservations/{reservationId}/no-show` | 노쇼 처리. 재고 복구 (마감할인이 아직 노출 중이면 다시 예약 가능) |

- `RESERVED` 상태일 때만 가능. 손님이 조금 늦게 와도 받아 갈 수 있도록 수령 마감 30분 뒤(자동 노쇼 전)까지는 수령 완료 처리를 할 수 있다
- 손님 취소와 사장님 수령 처리가 동시에 들어오면 하나만 반영되고, 늦은 쪽은 `INVALID_RESERVATION_STATUS`(사장님) 또는 `RESERVATION_NOT_CANCELABLE`(손님)
- 응답: [예약 목록](#owner-reservations) 항목 1개
- 에러: `RESERVATION_NOT_FOUND`, `INVALID_RESERVATION_STATUS`

---

## 업로드

<a id="upload"></a>
### POST /uploads/images — 이미지 업로드 (사장님)

`multipart/form-data`

| 필드 | 타입 | 필수 | 설명 |
|---|---|:-:|---|
| file | file | O | jpg, png, webp, 10MB 이하 |
| purpose | string | O | `STORE`, `MENU` |

- 응답: `{ "url": "https://cdn.lasteat.app/uploads/2026/11/1f3a...jpg" }`
- 받은 URL을 [가게 정보 수정](#owner-store-put)이나 [메뉴 관리](#owner-menus)에 담아 보냄
- 업로드만 하고 저장하지 않은 이미지는 서버가 주기적으로 삭제
- 에러: `INVALID_FILE`, `FILE_TOO_LARGE`

---

# 부록

## A. 코드 값

<a id="code-category"></a>
**가게 카테고리**

| 코드 | KOREAN | WESTERN | JAPANESE | CHINESE | CAFE | BAKERY | ETC |
|---|---|---|---|---|---|---|---|
| 화면 | 한식 | 양식 | 일식 | 중식 | 카페 | 베이커리 | 기타 |

<a id="code-status"></a>
**영업 상태** — 사장님이 [오늘 상태](#owner-status)를 직접 정했으면 그 값이 우선. `openNow=true` 필터는 `OPEN`만 포함.

| 코드 | 화면 | 계산 |
|---|---|---|
| OPEN | 영업 중 | 영업시간 안, 쉬는 시간 아님 |
| BREAK | 준비 중 | 쉬는 시간 |
| CLOSED | 영업 종료 / 오늘 휴무 | 영업시간 밖, 정기 휴무 요일, 영업시간 미등록 |

자정을 넘기는 영업(예: 18:00~02:00)의 새벽 시간은 전날 영업으로 본다. 그래서 전날이 정기 휴무면 새벽에도 `CLOSED`. 여는 시각과 닫는 시각이 같으면 24시간 영업.

<a id="code-day"></a>
**요일** — 사장님 앱 숫자 1~7과 순서 같음

| MON | TUE | WED | THU | FRI | SAT | SUN |
|---|---|---|---|---|---|---|
| 1 | 2 | 3 | 4 | 5 | 6 | 7 |

<a id="code-deal-status"></a>
**마감할인 상태**

| 코드 | 화면 | 언제 |
|---|---|---|
| SCHEDULED | 노출 예정 | 등록 후 노출 시작 전 (사장님에게만 보임) |
| OPEN | 예약 가능 | 노출 시작 ~ 수령 마감, 재고 있음 |
| SOLD_OUT | 품절 | 재고 0. 취소·노쇼로 재고가 돌아오면 다시 `OPEN` |
| CLOSED | 마감 | 수령 마감이 지났거나 사장님이 조기 마감 |

<a id="code-reservation-status"></a>
**예약 상태**

| 코드 | 화면 | 바뀌는 때 |
|---|---|---|
| RESERVED | 수령 대기 | 예약 직후 |
| PICKED_UP | 수령 완료 | 사장님이 수령 완료 처리 |
| CANCELED | 취소 | 손님이 취소 |
| NO_SHOW | 노쇼 | 사장님이 처리하거나, 수령 마감 30분 뒤 자동 처리 |

**회원 권한**

| 코드 | 설명 |
|---|---|
| CONSUMER | 손님 (가입 기본값) |
| OWNER | 사장님 (운영진이 가게와 연결할 때 변경) |

<a id="errors"></a>
## B. 에러 코드

| HTTP | code | 상황 |
|---|---|---|
| 400 | INVALID_INPUT | 필수값 누락, 형식·길이 오류 (`fields`에 상세) |
| 400 | INVALID_FILE | 지원하지 않는 이미지 형식 |
| 401 | UNAUTHORIZED | 토큰 없음 (로그인·사장님 API) |
| 401 | INVALID_TOKEN | 잘못된 토큰 |
| 401 | EXPIRED_TOKEN | accessToken 만료 → [토큰 재발급](#auth-refresh) |
| 401 | KAKAO_AUTH_FAILED | 카카오 토큰 확인 실패 |
| 403 | FORBIDDEN | 사장님 권한 필요 |
| 403 | NOT_STORE_OWNER | 본인 가게가 아님 |
| 404 | RESOURCE_NOT_FOUND | 없는 주소 |
| 404 | STORE_NOT_FOUND | 가게 없음 |
| 404 | MENU_NOT_FOUND | 메뉴 없음 |
| 404 | BENEFIT_NOT_FOUND | 할인 공지 없음 또는 비공개 |
| 404 | CLOSING_DEAL_NOT_FOUND | 마감할인 없음 또는 노출 중 아님 |
| 404 | RESERVATION_NOT_FOUND | 예약 없음 또는 본인 예약 아님 |
| 405 | METHOD_NOT_ALLOWED | 지원하지 않는 HTTP 메서드 |
| 409 | OWNER_HAS_STORE | 가게가 연결된 사장님의 탈퇴 |
| 409 | CLOSING_DEAL_NOT_OPEN | 예약·수정할 수 없는 시간이거나 이미 마감된 마감할인 |
| 409 | OUT_OF_STOCK | 남은 재고 부족 |
| 409 | ALREADY_RESERVED | 같은 마감할인을 이미 예약 중 |
| 409 | RESERVATION_BUSY | 동시 예약이 몰려 처리하지 못함, 다시 시도 |
| 409 | RESERVATION_NOT_CANCELABLE | 취소할 수 없는 예약 (이미 처리됨, 수령 마감 지남) |
| 409 | INVALID_RESERVATION_STATUS | 이미 처리된 예약 (수령·노쇼 처리할 때) |
| 413 | FILE_TOO_LARGE | 이미지 10MB 초과 |
| 500 | INTERNAL_ERROR | 서버 오류 |

## C. 명세 기준 엔티티 변경 사항 (Sprint 2 반영 완료)

Sprint 1 엔티티(PR #11)에서 아래를 바꿨다. 스키마는 Flyway `V1__init.sql`이 기준이다.
ERD: `docs/erd.png` (v0.2, 테이블 9개). 원본은 `docs/erd.dbml`이고 dbdiagram.io에 붙여 넣어 다시 그린다.

| 엔티티 | 변경 |
|---|---|
| Store | `ratingAvg`, `reviewCount` 삭제 (리뷰 삭제) · `thumbnailUrl` → `StoreImage` 테이블 (여러 장, 순서) · `tags` 추가 · `recommended` 추가 · `businessStatusOverride` + 적용 날짜 추가 · `closedDays`를 요일 코드 목록으로 |
| StoreImage | 신규: store, imageUrl, sortOrder |
| Menu | `soldOut` 추가 |
| Benefit | **v0.3에서 할인 공지로 단순화 (Flyway `V2`)**: store, title(60), content(1000), periodText(100), published, partner, publishedAt만 남김. 유형·할인 값·기간 날짜·요일·시간·사용 제한·연락처·검토 상태는 삭제 |
| RefreshToken | 신규: member, tokenHash (SHA-256), expiresAt. 기기마다 1개, Redis 없이 MySQL에 저장 |
| Member | 변경 없음 |
| Bookmark | 변경 없음 |
| ClosingDeal, Reservation | 스키마 변경 없음. 도메인 메서드 추가: 시각과 재고로 상태 계산 `statusAt(now)`·`isVisibleAt(now)`, 조기 마감·종료 `close`, 재고 직접 수정 `changeStock`. 저장된 status는 조기 마감(CLOSED) 여부만 믿는다. 예약 시각 = 예약의 `created_at` |

**저장 방식 (2026-11-03 결정)**: 목록형 값(요일 `days`·`closedDays`, 사용 제한 `restrictions`, 태그 `tags`)은 별도 테이블 없이 쉼표 구분 문자열 한 컬럼에 저장한다 (예: `MON,TUE`). 그래서 태그에는 쉼표를 넣을 수 없다.
