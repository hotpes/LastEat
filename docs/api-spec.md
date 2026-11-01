# LastEat API 명세 v0.2

> **2026-10-31 회의 결정**: 리뷰·쿠폰 기능 전부 삭제 (별점 표시도 제거).
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
| POST | [`/auth/logout`](#auth-logout) | 로그아웃 | 로그인 | 마이 |
| GET | [`/members/me`](#members-me) | 내 정보 | 로그인 | 마이 |
| PATCH | [`/members/me/fcm-token`](#members-fcm) | 푸시 토큰 갱신 | 로그인 | 공통 |
| DELETE | [`/members/me`](#members-delete) | 회원 탈퇴 | 로그인 | 마이 |
| **가게** | | | | |
| GET | [`/stores`](#stores-list) | 주변 가게 목록·검색·필터 | 공개 | 주변 탐색, 검색 결과 |
| GET | [`/stores/{storeId}`](#stores-detail) | 가게 상세 | 공개 | 식당 상세 |
| GET | [`/stores/{storeId}/menus`](#stores-menus) | 메뉴 목록 | 공개 | 식당 상세 › 메뉴 |
| GET | [`/stores/{storeId}/benefits`](#stores-benefits) | 가게 혜택 목록 | 공개 | 식당 상세 › 혜택 |
| **혜택** | | | | |
| GET | [`/benefits`](#benefits-list) | 주변 혜택 목록 | 공개 | 혜택·이벤트 |
| GET | [`/benefits/{benefitId}`](#benefits-detail) | 혜택 상세 | 공개 | 혜택·이벤트, 식당 상세 |
| **저장** | | | | |
| POST | [`/stores/{storeId}/bookmark`](#bookmark-add) | 가게 저장 | 로그인 | 상세, 목록 |
| DELETE | [`/stores/{storeId}/bookmark`](#bookmark-remove) | 저장 취소 | 로그인 | 상세, 목록 |
| GET | [`/members/me/bookmarks`](#bookmark-list) | 내 저장 목록 | 로그인 | 저장 |
| **사장님 · 가게** | | | | |
| GET | [`/owner/stores`](#owner-stores) | 내 가게 목록 | 사장님 | 혜택 등록 › 식당 선택 |
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
| **사장님 · 혜택** | | | | |
| POST | [`/owner/stores/{storeId}/benefits`](#owner-benefit-create) | 혜택 등록 신청 | 사장님 | 혜택 등록 |
| GET | [`/owner/stores/{storeId}/benefits`](#owner-benefit-list) | 내 가게 혜택 목록 | 사장님 | 사장님 앱 › 혜택 |
| GET | [`/owner/stores/{storeId}/benefits/{benefitId}`](#owner-benefit-list) | 혜택 상세 (사장님용) | 사장님 | 〃 |
| PUT | [`/owner/stores/{storeId}/benefits/{benefitId}`](#owner-benefit-edit) | 혜택 수정 (다시 검토) | 사장님 | 〃 |
| PATCH | [`/owner/stores/{storeId}/benefits/{benefitId}/pause`](#owner-benefit-edit) | 노출 중지·재개 | 사장님 | 〃 |
| DELETE | [`/owner/stores/{storeId}/benefits/{benefitId}`](#owner-benefit-edit) | 혜택 삭제 | 사장님 | 〃 |
| **업로드** | | | | |
| POST | [`/uploads/images`](#upload) | 이미지 업로드 | 사장님 | 사장님 앱 › 사진 |

### 범위

| 포함 | 제외 |
|---|---|
| 로그인, 가게 검색·상세, 혜택 조회, 저장, 사장님 가게·메뉴·혜택 관리, 이미지 업로드 | 리뷰·쿠폰 (회의 결정) · 결제 · 컬렉션·소식·통계·알림 (2차) · 관리자 화면 (혜택 승인은 DB) · 공지·FAQ (외부 링크) |

> 마감할인·예약 API는 포함하지 않았다. 엔티티(`ClosingDeal`, `Reservation`)는 남아 있고, 진행이 결정되면 추가한다.

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

로그인 흐름: 앱이 카카오 SDK로 로그인 → 카카오 액세스 토큰을 서버에 전달 → 서버가 카카오에 확인 → 서버 자체 토큰 발급. 처음 로그인하면 자동 가입. accessToken 1시간, refreshToken 14일.

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
  "refreshToken": "eyJhbGciOi...",
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
### POST /auth/logout — 로그아웃 (로그인)

요청 본문 없음. 이 기기의 refreshToken을 폐기한다. 응답 `data`: `null`

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
| hasBenefit | boolean | | false | 지금 이용 가능한 혜택이 있는 곳만 |
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
  "mainBenefit": { "id": 11, "title": "점심특가 10% 할인" },
  "bookmarked": false
}
```
</details>

- `signatureMenuName`: 대표 메뉴 중 첫 번째, 없으면 `null`
- `mainBenefit`: 지금 노출 중인 혜택 중 마감이 가장 가까운 1개, 없으면 `null`
- `businessStatus`: [영업 상태](#code-status). "지금 영업 중인 24곳"은 `openNow=true`의 `totalElements`

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
      "title": "식사 금액 10% 할인",
      "type": "DISCOUNT",
      "partner": true,
      "summary": "평일 14~17시 · 2만원 이상 · 최대 5천원"
    }
  ],
  "bookmarked": false
}
```
</details>

- `imageUrls[0]`이 대표 사진, 배열 길이가 사진 개수 ("사진 24")
- `benefits`는 지금 노출 중인 혜택의 요약. 상세는 [혜택 상세](#benefits-detail)
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
### GET /stores/{storeId}/benefits — 가게 혜택 목록 (공개)

화면: 식당 상세 › 혜택 탭. [혜택 상세](#benefits-detail) 객체의 배열. 지금 노출 중인 혜택만 (승인 완료, 기간 안, 사장님이 중지하지 않은 것).

---

## 혜택

<a id="benefits-list"></a>
### GET /benefits — 주변 혜택 목록 (공개)

화면: 혜택·이벤트 탭 "지금, 내 주변 혜택"

| 파라미터 | 타입 | 필수 | 기본 | 설명 |
|---|---|:-:|---|---|
| lat, lng | number | O | | 기준 좌표 |
| radius | int | | 1000 | 반경(m), 최대 3000 |
| type | string | | | [혜택 유형](#code-benefit-type) |
| partner | boolean | | | `true`면 제휴 혜택만 |
| sort | string | | DISTANCE | `DISTANCE` 가까운 순, `ENDING_SOON` 곧 마감 순 |
| page, size | int | | 0, 20 | |

화면 필터 매핑: 전체 → 조건 없음 · 할인 → `type=DISCOUNT` · 제휴 혜택 → `partner=true` · 곧 마감 → `sort=ENDING_SOON`

<details>
<summary>응답 예시 (<code>content[]</code> 항목)</summary>

```json
{
  "id": 11,
  "title": "식사 금액 10% 할인",
  "type": "DISCOUNT",
  "partner": true,
  "summary": "평일 14~17시 · 2만원 이상 · 최대 5천원",
  "startDate": "2026-10-01",
  "endDate": "2026-10-31",
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

- "제휴 식당 12곳" 배너 숫자는 `partner=true`의 `totalElements`

<a id="benefits-detail"></a>
### GET /benefits/{benefitId} — 혜택 상세 (공개)

<details>
<summary>응답 예시</summary>

```json
{
  "id": 11,
  "title": "식사 금액 10% 할인",
  "type": "DISCOUNT",
  "partner": true,
  "description": "여유로운 평일 오후, 식사 메뉴 주문 시 결제 금액의 10%를 할인해 드려요.",
  "discount": { "discountType": "RATE", "value": 10, "maxDiscount": 5000 },
  "freeItem": null,
  "minOrderAmount": 20000,
  "startDate": "2026-11-01",
  "endDate": "2026-11-30",
  "days": ["MON", "TUE", "WED", "THU", "FRI"],
  "availableFrom": "14:00",
  "availableTo": "17:00",
  "restrictions": ["ONE_PER_PERSON", "NO_DUPLICATE", "DINE_IN_ONLY"],
  "extraNotice": "주류 및 포장 주문은 제외됩니다. 결제 전 직원에게 혜택 화면을 보여 주세요.",
  "summary": "평일 14~17시 · 2만원 이상 · 최대 5천원",
  "store": {
    "id": 3,
    "name": "연남 온기",
    "category": "KOREAN",
    "thumbnailUrl": "https://cdn.lasteat.app/stores/3/main.jpg",
    "distance": null
  }
}
```
</details>

- `discount`는 `type=DISCOUNT`일 때만, `freeItem`은 `type=FREE_ITEM`일 때만 (예: `"아메리카노 1잔"`). 나머지는 `null`
- `restrictions`는 안내 문구용. 쿠폰이 없으므로 서버가 사용 여부를 검증하지 않음
- `summary`는 서버가 조건을 조합해 만든 한 줄 문구 (목록 카드용)
- 담당자 연락처는 손님 응답에 포함하지 않음
- 에러: `BENEFIT_NOT_FOUND` (노출 중이 아닌 혜택 포함)

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
| sort | string | | RECENT | `RECENT` 최근 저장 순, `DISTANCE` (좌표 필요) |
| page, size | int | | 0, 20 | |

응답 `content[]`: [주변 가게 목록](#stores-list) 항목과 같은 형식 + `bookmarkedAt`

---

## 사장님

모든 API 사장님 로그인 필요. `{storeId}`가 본인 가게가 아니면 `NOT_STORE_OWNER` (403). 사장님 계정과 가게 연결은 1차에서 운영진이 직접 처리한다.

<a id="owner-stores"></a>
### GET /owner/stores — 내 가게 목록 (사장님)

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

[가게 상세](#stores-detail) 형식 + 사장님 전용 필드 `businessStatusOverride` ([영업 상태 변경](#owner-status) 참고)

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
### POST /owner/stores/{storeId}/benefits — 혜택 등록 신청 (사장님)

화면: 혜택 등록 › "혜택 등록 신청하기". 신청하면 `PENDING`(검토 중), 운영진이 승인하면 손님에게 노출. "임시저장"은 앱 기기에 저장 (서버 저장 없음).

| 필드 | 타입 | 필수 | 제약 / 설명 |
|---|---|:-:|---|
| contactPhone | string | O | 담당자 연락처. 검토 안내용, 손님에게 공개 안 함 |
| type | string | O | [혜택 유형](#code-benefit-type) `DISCOUNT` · `FREE_ITEM` · `ETC` |
| title | string | O | 30자 이하 |
| description | string | O | 300자 이하 |
| discountType | string | DISCOUNT일 때 | `RATE` 비율(%), `AMOUNT` 금액(원) |
| discountValue | int | DISCOUNT일 때 | RATE면 1~100, AMOUNT면 1 이상 |
| maxDiscount | int | RATE일 때 | 최대 할인 금액(원) |
| freeItem | string | FREE_ITEM일 때 | 제공 메뉴, 30자 이하 |
| startDate | string | O | 오늘 이후 |
| endDate | string | O | `startDate` 이후. 지나면 노출 자동 종료 |
| days | string[] | O | 이용 가능 [요일](#code-day), 1개 이상 |
| availableFrom | string | O | 이용 시작 시각 |
| availableTo | string | O | 이용 종료 시각, `availableFrom`보다 늦게 |
| minOrderAmount | int | O | 최소 주문 금액(원), 없으면 0 |
| restrictions | string[] | O | [사용 제한](#code-restriction), 없으면 빈 배열 |
| extraNotice | string | | 추가 안내, 200자 이하 |
| agreed | boolean | O | "입력한 혜택 정보가 사실이며…" 동의, `true`만 허용 |

응답: [내 가게 혜택 목록](#owner-benefit-list) 항목 1개 (`status: "PENDING"`)

<a id="owner-benefit-list"></a>
### GET /owner/stores/{storeId}/benefits — 내 가게 혜택 목록 (사장님)

<details>
<summary>응답 예시 (배열 항목)</summary>

```json
{
  "id": 11,
  "title": "식사 금액 10% 할인",
  "type": "DISCOUNT",
  "status": "APPROVED",
  "rejectReason": null,
  "paused": false,
  "visible": true,
  "startDate": "2026-11-01",
  "endDate": "2026-11-30",
  "summary": "평일 14~17시 · 2만원 이상 · 최대 5천원"
}
```
</details>

- `status`: [검토 상태](#code-review-status)
- `visible`: 지금 손님에게 보이는지 (승인 완료 + 기간 안 + 중지 안 함)
- 상세 `GET /owner/stores/{storeId}/benefits/{benefitId}`: [혜택 상세](#benefits-detail) 형식 + `contactPhone`, `status`, `rejectReason`, `paused`

<a id="owner-benefit-edit"></a>
### 혜택 수정·중지·삭제 (사장님)

| 메서드 | 경로 | 설명 |
|---|---|---|
| PUT | `/owner/stores/{storeId}/benefits/{benefitId}` | 수정. 요청은 [등록 신청](#owner-benefit-create)과 같음. **수정하면 다시 `PENDING`** |
| PATCH | `/owner/stores/{storeId}/benefits/{benefitId}/pause` | 노출 중지·재개 `{ "paused": true }` |
| DELETE | `/owner/stores/{storeId}/benefits/{benefitId}` | 삭제 |

에러: `BENEFIT_NOT_FOUND`

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
| CLOSED | 영업 종료 / 오늘 휴무 | 영업시간 밖 또는 정기 휴무 요일 |

<a id="code-day"></a>
**요일** — 사장님 앱 숫자 1~7과 순서 같음

| MON | TUE | WED | THU | FRI | SAT | SUN |
|---|---|---|---|---|---|---|
| 1 | 2 | 3 | 4 | 5 | 6 | 7 |

<a id="code-benefit-type"></a>
**혜택 유형** — `partner`(제휴 혜택 여부)는 신청 때가 아니라 운영진이 승인할 때 지정

| 코드 | 화면 | 필수 추가 필드 |
|---|---|---|
| DISCOUNT | 금액 할인 | discountType, discountValue (RATE면 maxDiscount) |
| FREE_ITEM | 메뉴 제공 | freeItem |
| ETC | 기타 혜택 | 없음 (description으로 설명) |

<a id="code-review-status"></a>
**혜택 검토 상태**

| 코드 | PENDING | APPROVED | REJECTED |
|---|---|---|---|
| 화면 | 검토 중 | 노출 승인 | 반려 (`rejectReason` 확인 후 수정해서 다시 신청) |

<a id="code-restriction"></a>
**혜택 사용 제한**

| 코드 | 화면 |
|---|---|
| ONE_PER_PERSON | 1인 1회 사용 가능 |
| NO_DUPLICATE | 다른 할인과 중복 사용 불가 |
| DINE_IN_ONLY | 매장 방문 시에만 사용 가능 |

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
| 404 | STORE_NOT_FOUND | 가게 없음 |
| 404 | MENU_NOT_FOUND | 메뉴 없음 |
| 404 | BENEFIT_NOT_FOUND | 혜택 없음 또는 노출 중 아님 |
| 409 | OWNER_HAS_STORE | 가게가 연결된 사장님의 탈퇴 |
| 413 | FILE_TOO_LARGE | 이미지 10MB 초과 |
| 500 | INTERNAL_ERROR | 서버 오류 |

## C. 명세 기준 엔티티 변경 사항

Sprint 1 엔티티(PR #11)에서 이 명세를 구현하려면 아래를 바꾼다. Sprint 2 첫 작업.

| 엔티티 | 변경 |
|---|---|
| Store | `ratingAvg`, `reviewCount` 삭제 (리뷰 삭제) · `thumbnailUrl` → `StoreImage` 테이블 (여러 장, 순서) · `tags` 추가 · `recommended` 추가 · `businessStatusOverride` + 적용 날짜 추가 · `closedDays`를 요일 코드 목록으로 |
| StoreImage | 신규: store, imageUrl, sortOrder |
| Menu | `soldOut` 추가 |
| Benefit | `type`을 `DISCOUNT`/`FREE_ITEM`/`ETC`로 재정의 · `partner`, `description`, `freeItem`, `days`, `restrictions`, `extraNotice`, `contactPhone`, `status`, `rejectReason`, `paused` 추가 · `conditionText` 삭제 (`description`, `extraNotice`로 대체) |
| Member | 변경 없음 (refreshToken은 별도 저장) |
| Bookmark | 변경 없음 |
| ClosingDeal, Reservation | 변경 없음 (마감할인 진행 여부 결정 대기) |
