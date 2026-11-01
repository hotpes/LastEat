package com.lasteat.app.ui.owner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lasteat.app.ui.components.EmptyState
import com.lasteat.app.ui.icons.LastEatIcons
import androidx.compose.material3.MaterialTheme

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 사장님 화면(Owner UI) 연결 자리                                     │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * 마이 탭 → "우리 식당 혜택 등록하기" 를 누르면 이 화면이 열립니다.
 *
 * 연결 방법 (팀원이 전달한 Kotlin 7개 파일 기준)
 * 1. OwnerScreen.kt, OwnerUi.kt, OwnerForms.kt, OwnerSheets.kt, OwnerState.kt,
 *    OwnerActions.kt, OwnerIcons.kt 를 이 폴더(ui/owner/)에 복사합니다.
 * 2. 각 파일 맨 위 package 를 `com.lasteat.app.ui.owner` 로 바꿉니다.
 * 3. 아래 OwnerPlaceholder(onClose) 줄을 지우고, 주석 처리된 OwnerScreen 블록을 해제합니다.
 * 4. ownerState / handleOwnerAction 은 백엔드 연동 담당과 함께 만듭니다.
 *    (전달 문서의 "데이터 처리 경계" 표 참고 — UI 는 서버 호출을 하지 않습니다)
 *
 * 이 함수는 이미 LastEatTheme(MaterialTheme) 안에서 호출되므로
 * 사장님 화면도 메인 앱 글꼴을 그대로 상속합니다.
 */
@Composable
fun OwnerEntry(onClose: () -> Unit) {
    // 시스템 뒤로가기 → 메인 탭으로 복귀
    BackHandler(onBack = onClose)

    // ── 3단계에서 아래 블록 주석 해제 ────────────────────────────────
    // val imageContent = LocalImageContent.current
    // OwnerScreen(
    //     state = ownerState,                       // 서버 조회 결과 (isLoading, isSubmitting, result 포함)
    //     onAction = { action -> handleOwnerAction(action) },
    //     imageContent = { url, description, modifier -> imageContent(url, description, modifier) },
    //     onPickImage = { currentUrl, onSelectedUrl ->
    //         // 사진 선택 → 업로드 성공 시 onSelectedUrl(uploadedUrl)
    //         // 취소·실패 시에는 호출하지 않고 스낵바 등으로 안내
    //     },
    // )
    // ────────────────────────────────────────────────────────────────

    OwnerPlaceholder(onClose)
}

/** 팀원 코드를 붙이기 전, 이동 흐름 확인용 임시 화면 */
@Composable
private fun OwnerPlaceholder(onClose: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(LastEatIcons.ArrowBack, "뒤로", Modifier.size(22.dp))
            }
            Text("사장님 화면", style = MaterialTheme.typography.titleMedium)
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            EmptyState(
                icon = LastEatIcons.Store,
                title = "사장님 화면 연결 자리",
                description = "OwnerEntry.kt 주석의 순서대로 팀원 코드를 붙이면 이 자리에 사장님 화면이 열려요.",
                actionText = "메인으로 돌아가기",
                onAction = onClose,
            )
        }
    }
}
