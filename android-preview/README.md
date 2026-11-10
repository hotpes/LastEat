# LastEat 손님 UI 미리보기

Figma의 **주변 탐색 / 지도·목록 결과 / 저장 / 식당 상세** 네 화면을 Kotlin · Jetpack Compose로 구현한 독립 실행 프로젝트입니다.

기존 `backend/`, `Owner/` 및 `MainUi` 브랜치의 `frontend/` 파일을 수정하거나 의존하지 않습니다.
원본 사진과 SVG 아이콘은 `app/src/main/assets/figma/`에 포함되어 오프라인으로 표시됩니다.

## 실행

Android Studio에서 **이 android-preview 폴더 자체를 Open**하고 Gradle Sync 후 `app`을 실행하세요.
JDK 17 또는 21, Android SDK 36, Android 8.0(API 26) 이상이 필요합니다.
Java 25를 쓰는 경우 Settings → Build Tools → Gradle에서 Gradle JDK를 21로 선택하세요.

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

## UI에서 확인 가능한 동작

- 주변 탐색 → 지도 마커 또는 식당 목록 → 식당 상세 → 뒤로 이동
- 검색창에서 검색어 입력 후 키보드 검색 / 화살표 버튼
- 카테고리 선택, 혜택 필터, 지도·목록 전환과 검색 결과 없음
- 저장 탭과 식당 상세에서 저장·해제 상태 공유, 컬렉션 필터와 빈 목록
- 상세 정보·메뉴·혜택 탭

데이터는 샘플이며 앱 재시작 시 초기화됩니다. 카카오 지도 SDK, 실시간 위치, API,
인증, 영구 저장, 전화·공유·길찾기 실행은 포함하지 않습니다. 지도 영역은 Figma 원본 벡터를
표시하는 정적인 UI입니다. 전화·공유·길찾기는 정보 안내창만 열립니다.
하단의 혜택·이벤트와 My 탭, 새 컬렉션 생성은 이번 작업 범위 밖이며 레이아웃만 제공합니다.
별점과 10월 혜택 문구는 Figma 시안의 샘플 표현입니다.

## 파일 구성

- `CustomerApp.kt`: UI 상태, 주변 탐색, 검색 결과, 저장 화면
- `CustomerComponents.kt`: 헤더, 식당 카드, 칩, 하단 내비게이션
- `RestaurantDetail.kt`: 상세 화면
- `PreviewMap.kt`: 정적인 Figma 지도 일러스트
- `CustomerTheme.kt`, `CustomerData.kt`: 시안 색상과 샘플 데이터
- `FigmaImage.kt`, `FigmaAssets.kt`: 로컬 원본 에셋 표시 및 슬롯 매핑

## 디자인 출처

- [주변 탐색](https://www.figma.com/design/hmz0qVz7iPf2pfz5v8Zry0?node-id=2-3709)
- [지도·목록 결과](https://www.figma.com/design/hmz0qVz7iPf2pfz5v8Zry0?node-id=2-3851)
- [저장](https://www.figma.com/design/hmz0qVz7iPf2pfz5v8Zry0?node-id=2005-315)
- [식당 상세](https://www.figma.com/design/hmz0qVz7iPf2pfz5v8Zry0?node-id=2-3989)

상태 표시줄과 시스템 내비게이션은 가짜 시계·아이콘 대신 실제 Android 시스템 UI를 사용합니다.
기기 높이가 작으면 본문을 스크롤할 수 있고 하단 내비게이션은 고정됩니다.
SVG 표시는 Apache-2.0 라이선스의 [AndroidSVG](https://bigbadaboom.github.io/androidsvg/) 1.4를 사용합니다.
