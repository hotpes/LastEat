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
├─ data/
│  ├─ Models.kt             Restaurant · Benefit · SavedCollection · UserSession
│  └─ SampleData.kt         시안과 같은 샘플 데이터 (백엔드 연동 후 교체)
└─ ui/
   ├─ MainScaffold.kt       하단 탭 4개 + 사장님 화면 진입
   ├─ theme/                디자인 토큰 (Color · Type · Theme/Shapes/Spacing)
   ├─ icons/                리소스 없이 그리는 벡터 아이콘
   ├─ components/           상단 바 · 칩 · 식당 카드 · 배지 · 배너 · 빈 상태 · 이미지 슬롯
   ├─ screens/              주변 탐색 · 혜택·이벤트 · 저장 · 마이
   └─ owner/OwnerEntry.kt   사장님 화면(OwnerScreen) 연결 자리
```

## 디자인 토큰

| 토큰 | 값 | 쓰임 |
|---|---|---|
| primary | `#2A6B47` | 메인 버튼, 선택 칩, 로고 |
| primaryContainer | `#E6F0E9` | 혜택 배너, 배지, 탭 선택 표시 |
| secondaryContainer | `#F2F7F3` | 연한 안내 카드 |
| onSurfaceVariant | `#7D827F` | 캡션 (대비 확보를 위해 시안보다 약간 진하게) |
| outline / outlineVariant | `#D5D9D6` / `#E8EBE9` | 테두리 / 구분선 |
| Star | `#E5A21A` | 별점 |
| Kakao | `#FEE500` | 카카오 로그인 |

모서리: 배지 6 · 버튼/입력 12 · 카드 16 · 배너 24 · 칩 999 / 화면 좌우 여백 20dp
글꼴: `LastEatFont` 한 곳에서 바꾸면 사장님 화면까지 함께 바뀝니다 (Pretendard 추가 예정).

## 사장님 화면(Owner UI) 붙이는 법

1. 전달받은 Kotlin 7개 파일을 `ui/owner/`에 복사하고 package를 `com.lasteat.app.ui.owner`로 바꿉니다.
2. `OwnerEntry.kt`의 `OwnerPlaceholder(onClose)`를 지우고 주석 처리된 `OwnerScreen(...)` 블록을 해제합니다.
3. `ownerState` / `handleOwnerAction`은 백엔드 연동 담당과 함께 만듭니다.

진입 경로: 마이 탭 → "우리 식당 혜택 등록하기". 뒤로가기는 메인 탭으로 돌아옵니다.
이미지 슬롯(`LocalImageContent`)은 Owner UI의 `imageContent`와 시그니처가 같아 같은 로더를 그대로 넘길 수 있습니다.

## 아직 정해야 할 것

- [ ] 지도 SDK (네이버/카카오) → `ExploreScreen`의 `MapPlaceholder` 교체
- [ ] 이미지 로더 (예: Coil) → `MainActivity`에서 `LocalImageContent` 주입
- [ ] 카카오 로그인 → `MainScaffold`의 `fakeLogin` 교체
- [ ] 식당 상세 · 검색 · 혜택 상세 화면 (지금은 "준비 중" 스낵바)
- [ ] 화면이 늘어나면 Navigation Compose 도입 여부
- [ ] 백엔드 API 필드명과 `data/Models.kt` 맞추기

## 브랜치 규칙 (제안)

`main` ← PR로만 병합 · 기능 브랜치는 `feature/<영역>` (예: `feature/main-ui`, `feature/owner`, `feature/landing`)
