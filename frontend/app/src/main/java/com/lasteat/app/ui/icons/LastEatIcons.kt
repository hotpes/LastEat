package com.lasteat.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * 리소스 파일 없이 그리는 선형 아이콘 (24 x 24).
 * 사장님 UI의 OwnerIcons.kt 와 같은 방식이라 drawable·material-icons 의존성이 없습니다.
 * Icon(imageVector = LastEatIcons.Map, tint = ...) 처럼 쓰면 tint 로 색이 바뀝니다.
 */
object LastEatIcons {

    private fun line(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach { d ->
                addPath(
                    pathData = addPathNodes(d),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    private fun solid(name: String, d: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            addPath(
                pathData = addPathNodes(d),
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()

    // 하단 탭
    val Map = line("Map", "M3 6.5l6-2.5 6 2.5 6-2.5v13.5l-6 2.5-6-2.5-6 2.5z", "M9 4v13.5", "M15 6.5v13.5")
    val Ticket = line(
        "Ticket",
        "M4 6h16a1 1 0 0 1 1 1v3a2 2 0 0 0 0 4v3a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-3a2 2 0 0 0 0-4V7a1 1 0 0 1 1-1z",
        "M15 6v2", "M15 11v2", "M15 16v2",
    )
    val Bookmark = line("Bookmark", "M6 4h12v17l-6-4-6 4z")
    val BookmarkFilled = solid("BookmarkFilled", "M6 4h12v17l-6-4-6 4z")
    val Person = line("Person", "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z", "M4 21a8 8 0 0 1 16 0")

    // 공통
    val Utensils = line("Utensils", "M7 3v18", "M4 3v5a3 3 0 0 0 6 0V3", "M17 21V3c-2 0-3.5 2.5-3.5 6.5V13H17")
    val Search = line("Search", "M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14z", "M20 20l-4-4")
    val Pin = line(
        "Pin",
        "M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21z",
        "M12 12a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z",
    )
    val ChevronDown = line("ChevronDown", "M6 9l6 6 6-6")
    val ChevronRight = line("ChevronRight", "M9 6l6 6-6 6")
    val ArrowRight = line("ArrowRight", "M5 12h14", "M13 6l6 6-6 6")
    val ArrowBack = line("ArrowBack", "M19 12H5", "M11 6l-6 6 6 6")
    val Check = line("Check", "M5 12.5l4.5 4.5L19 7.5")
    val Plus = line("Plus", "M12 5v14", "M5 12h14")
    val Star = solid("Star", "M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z")
    val Sliders = line("Sliders", "M4 7h10", "M18 7h2", "M16 5v4", "M4 17h4", "M12 17h8", "M10 15v4")
    val Locate = line(
        "Locate",
        "M12 19a7 7 0 1 0 0-14 7 7 0 0 0 0 14z", "M12 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4z",
        "M12 2v3", "M12 19v3", "M2 12h3", "M19 12h3",
    )
    val Refresh = line("Refresh", "M20 12a8 8 0 1 1-2.3-5.7", "M20 4v4h-4")
    val Store = line(
        "Store",
        "M4 10v10h16V10",
        "M3 7l2-4h14l2 4a3 3 0 0 1-6 0 3 3 0 0 1-6 0 3 3 0 0 1-6 0z",
        "M10 20v-5h4v5",
    )
    val Cup = line("Cup", "M5 9h11v5a5 5 0 0 1-5 5h-1a5 5 0 0 1-5-5z", "M16 10h1.5a2.5 2.5 0 0 1 0 5H16", "M8 3v3", "M12 3v3")

    // 마이 탭 목록
    val Megaphone = line("Megaphone", "M3 10v4h4l7 5V5l-7 5z", "M18 9a4 4 0 0 1 0 6")
    val Help = line(
        "Help",
        "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z",
        "M9.5 9.5a2.5 2.5 0 1 1 3.5 2.3c-.6.3-1 .8-1 1.5v.7", "M12 17h.01",
    )
    val Settings = line(
        "Settings",
        "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z",
        "M12 2v3", "M12 19v3", "M2 12h3", "M19 12h3",
        "M4.9 4.9L7 7", "M17 17l2.1 2.1", "M4.9 19.1L7 17", "M17 7l2.1-2.1",
    )
    val Info = line("Info", "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z", "M12 11v5", "M12 8h.01")
    val Chat = solid(
        "Chat",
        "M12 4C7 4 3 7.1 3 11c0 2.5 1.6 4.6 4 5.9L6 21l4.4-2.9c.5.1 1 .1 1.6.1 5 0 9-3.1 9-7s-4-7.2-9-7.2z",
    )
}
