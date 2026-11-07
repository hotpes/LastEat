# LastEat 메인 앱 (사용자 화면)

동네 식당 정보와 사장님 혜택을 모아 보는 앱 **LastEat**의 메인(사용자) 앱 뼈대입니다.
Kotlin · Jetpack Compose · Material3만 사용하고, 사장님 화면(Owner UI)과 같은 의존성 원칙을 따릅니다.

## 실행

Android Studio에서 이 폴더를 열고 `app`을 실행하세요. (Gradle Wrapper가 없으면 Studio가 처음 열 때 만들어 줍니다.)

- compileSdk 35 · **minSdk 26** (사장님 UI의 `java.time` 사용 때문)
- Kotlin 2.0.21 · Compose BOM 2024.12.01 · AGP 8.7.3

## 폴더 구조

```
app/src/main/java/com/lasteat/app/
├─ MainActivity.kt          테마 적용, 이미지 로더 주입 자리
├─ (com/lasteat/owner/)     팀원 사장님 UI 원본 7개 파일
├─ data/
│  ├─ AppPrefs.kt           처음 실행 선택값 저장
│  ├─ Models.kt             Restaurant · Benefit · SavedCollection · UserSession
│  └─ SampleData.kt         시안과 같은 샘플 데이터 (백엔드 연동 후 교체)
└─ ui/
   ├─ AppRoot.kt            앱 시작 흐름 (온보딩 → 손님 홈 / 사장님 홈)
   ├─ MainScaffold.kt       손님 홈: 하단 탭 4개
   ├─ onboarding/           스플래시 · 역할 선택 · 주민 온보딩 · 사장님 등록·인증·첫 혜택
   ├─ theme/                디자인 토큰 (Color · Type · Theme/Shapes/Spacing)
   ├─ icons/                리소스 없이 그리는 벡터 아이콘
   ├─ components/           상단 바 · 칩 · 식당 카드 · 배지 · 배너 · 빈 상태 · 이미지 슬롯
   ├─ screens/              주변 탐색 · 혜택·이벤트 · 저장 · 마이
   └─ owner/                사장님 홈(OwnerScreen) 연결 + 시연용 상태 처리
```

## 디자인 토큰

| 토큰 | 값 | 쓰임 |
|---|---|---|
| primary | `#25744F` | 메인 버튼, 선택 칩, 로고 |
| primaryContainer | `#EAF3E8` | 혜택 배너, 배지, 탭 선택 표시 |
| secondaryContainer | `#F6F8F4` | 연한 안내 카드 |
| onSurfaceVariant | `#7D827F` | 캡션 (대비 확보를 위해 시안보다 약간 진하게) |
| outline / outlineVariant | `#D5D9D6` / `#E8EBE9` | 테두리 / 구분선 |
| Star | `#E5A21A` | 별점 |
| Kakao | `#FEE500` | 카카오 로그인 |

모서리: 배지 6 · 버튼/입력 12 · 카드 16 · 배너 24 · 칩 999 / 화면 좌우 여백 20dp
글꼴: `LastEatFont` 한 곳에서 바꾸면 사장님 화면까지 함께 바뀝니다 (Pretendard 추가 예정).

## 앱 시작 흐름 (온보딩)

```
스플래시 → 역할 선택
 ├ A 동네 주민: 로그인 선택 ─┬ 카카오 로그인 → (첫 로그인) 약관 동의 ┐
 │                        └ 둘러보기(비로그인) ──────────────────┤
 │          위치 권한 안내 ─ 허용 → 주변 탐색                       ◀┘
 │                       └ 거부/직접 선택 → 동네 직접 선택 → 주변 탐색
 └ B 사장님: 카카오 로그인(필수) → 약관 → 내 식당 찾기 ─ 선택 → 정보 확인 ┐
                                           └ 없음 → 직접 등록 신청 ─┤
             사업자 인증 → 첫 혜택 등록 → 신청 완료 → 사장님 홈       ◀┘
```

- 화면 단계와 뒤로가기는 `ui/AppRoot.kt`의 스택으로 관리합니다.
- 선택한 동네는 손님 홈 전체(주변 탐색·혜택·저장·마이)에 반영되고, 홈 상단 동네 버튼으로 같은 동네 선택 화면을 다시 엽니다.
- 사장님 신청 내용(식당·영업시간·첫 혜택)은 사장님 홈에 그대로 채워지고, 혜택은 '검토 중'으로 표시됩니다.
- 다음 실행부터는 스플래시 뒤 마지막으로 쓴 홈으로 바로 들어갑니다.
- 마이 탭의 **처음 화면 다시 보기**로 역할 선택부터 다시 볼 수 있습니다. (시연용, 출시 전 삭제)
- 시안에 없던 **첫 혜택 등록(3/3)** 화면은 사장님 화면의 '새 혜택 만들기'와 같은 항목으로 만들었습니다.

관련 파일: `ui/onboarding/`(Common·Customer·Owner 화면, 공통 부품, 시연 데이터), `data/AppPrefs.kt`(선택값 저장)

## 사장님 화면(Owner UI) 연결

- 팀원 코드 7개 파일을 `com/lasteat/owner/`에 **그대로 복사**했습니다. (패키지 `com.lasteat.owner` 유지, 수정 없음)
  `Owner/Owner_UI`가 바뀌면 이 폴더에 다시 복사해 주세요.
- `ui/owner/OwnerEntry.kt`가 `OwnerScreen`을 띄우고, `ui/owner/DemoOwnerHost.kt`가 저장 요청(`OwnerAction`)을 메모리에서 처리합니다.
  백엔드 연동 시 `DemoOwnerHost.handle()` 안에서 `/owner/stores/...` API를 호출하도록 바꾸면 화면 코드는 그대로입니다.
- 온보딩 신청 내용으로 가게 정보·영업시간·첫 혜택이 사장님 화면에 미리 채워집니다.
- 시연 데이터는 앱을 껐다 켜면 초기화됩니다. 사진 변경은 이미지 업로드 API 연동 때 열립니다.

## 아직 정해야 할 것

- [ ] 지도 SDK  → `ExploreScreen`의 `MapPlaceholder` 교체
- [ ] 이미지 로더 (예: Coil) → `MainActivity`에서 `LocalImageContent` 주입
- [ ] 카카오 로그인 → `AppRoot`의 `fakeKakaoLogin` 교체, `AppPrefs` 값을 `/members/me` 결과로 대체
- [ ] 사장님 신청 API (식당 등록·사업자 인증·사업자등록증 업로드·첫 혜택) — 명세에 없음
- [ ] 동네·식당 검색을 주소 검색 API / `GET /stores`로 교체 (지금은 `OnboardingSamples`)
- [ ] 현재 위치 → 동네 이름 변환 (지도 SDK 결정 후)
- [ ] 약관 전문 링크
- [ ] 식당 상세 · 검색 · 혜택 상세 화면 (지금은 "준비 중" 스낵바)
- [ ] 화면이 늘어나면 Navigation Compose 도입 여부
- [ ] 백엔드 API 필드명과 `data/Models.kt` 맞추기

## 브랜치 규칙 (제안)

`main` ← PR로만 병합 · 기능 브랜치는 `feature/<영역>` (예: `feature/main-ui`, `feature/owner`, `feature/landing`)
