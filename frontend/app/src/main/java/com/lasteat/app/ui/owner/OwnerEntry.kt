package com.lasteat.app.ui.owner

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lasteat.app.ui.components.LocalImageContent
import com.lasteat.owner.OwnerScreen

/**
 * 사장님 홈 — 팀원 Owner UI(com.lasteat.owner) 를 그대로 띄웁니다.
 *
 * - 가게 정보 등록 → 영업시간 → 메뉴 → 혜택 만들기는 Owner UI 안에서 진행됩니다.
 * - 저장 처리는 host(지금은 DemoOwnerHost)가 맡습니다. 백엔드가 붙으면 host 만 교체하세요.
 * - Owner UI 에는 나가기 버튼이 없어서, 뒤로가기 시 손님 화면 전환 여부를 묻습니다.
 */
@Composable
fun OwnerEntry(
    host: DemoOwnerHost,
    onSwitchToCustomer: () -> Unit,
) {
    var askLeave by remember { mutableStateOf(false) }
    BackHandler { askLeave = true }

    val imageContent = LocalImageContent.current
    OwnerScreen(
        state = host.state,
        onAction = host::handle,
        imageContent = { url, description, modifier -> imageContent(url.ifBlank { null }, description, modifier) },
        // 사진 선택·업로드는 이미지 업로드 API(/uploads/images) 연동 때 연결합니다.
        // null 이면 Owner UI 가 사진 변경 버튼을 비활성화합니다.
        onPickImage = null,
    )

    if (askLeave) {
        AlertDialog(
            onDismissRequest = { askLeave = false },
            title = { Text("손님 화면으로 이동할까요?") },
            text = { Text("사장님 화면은 마이 탭의 '우리 가게 관리'에서 다시 열 수 있어요.") },
            confirmButton = {
                TextButton(onClick = { askLeave = false; onSwitchToCustomer() }) { Text("손님 화면으로") }
            },
            dismissButton = {
                TextButton(onClick = { askLeave = false }) { Text("계속 관리하기") }
            },
        )
    }
}
