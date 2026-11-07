package com.lasteat.owner

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Code-native outline icons. No drawable files or material-icons dependency.
internal object OwnerIcons {
    val Restaurant = outline("Restaurant") {
        moveTo(4f, 3f); verticalLineTo(9f); curveTo(4f, 13f, 10f, 13f, 10f, 9f); verticalLineTo(3f)
        moveTo(7f, 3f); verticalLineTo(21f)
        moveTo(19f, 3f); curveTo(15f, 5f, 14f, 10f, 15f, 13f); horizontalLineTo(19f)
        moveTo(19f, 3f); verticalLineTo(21f)
    }
    val NotificationsNone = outline("Notifications") {
        moveTo(5f, 16f); lineTo(7f, 13f); verticalLineTo(9f)
        curveTo(7f, 2f, 17f, 2f, 17f, 9f); verticalLineTo(13f); lineTo(19f, 16f); close()
        moveTo(10f, 20f); curveTo(11f, 22f, 13f, 22f, 14f, 20f)
        moveTo(12f, 2f); verticalLineTo(3f)
    }
    val Close = outline("Close") { moveTo(6f, 6f); lineTo(18f, 18f); moveTo(18f, 6f); lineTo(6f, 18f) }
    val ChevronRight = outline("ChevronRight", true) { moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f) }
    val ArrowBack = outline("ArrowBack", true) { moveTo(20f, 12f); horizontalLineTo(4f); moveTo(11f, 5f); lineTo(4f, 12f); lineTo(11f, 19f) }
    val ExpandMore = outline("ExpandMore") { moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f) }
    val Edit = outline("Edit") {
        moveTo(4f, 15f); lineTo(15f, 4f); lineTo(20f, 9f); lineTo(9f, 20f); lineTo(3f, 21f); close()
        moveTo(12f, 7f); lineTo(17f, 12f)
    }
    val Schedule = outline("Schedule") { circle(12f, 12f, 9f); moveTo(12f, 6f); verticalLineTo(12f); lineTo(16f, 14f) }
    val QrCodeScanner = outline("QrCodeScanner") {
        moveTo(3f, 8f); verticalLineTo(3f); horizontalLineTo(8f)
        moveTo(16f, 3f); horizontalLineTo(21f); verticalLineTo(8f)
        moveTo(21f, 16f); verticalLineTo(21f); horizontalLineTo(16f)
        moveTo(8f, 21f); horizontalLineTo(3f); verticalLineTo(16f)
        box(7f, 7f, 10f, 10f); box(14f, 7f, 17f, 10f); box(7f, 14f, 10f, 17f)
        moveTo(14f, 14f); horizontalLineTo(17f); verticalLineTo(17f); horizontalLineTo(15f)
    }
    val Storefront = outline("Storefront") {
        moveTo(3f, 9f); lineTo(5f, 3f); horizontalLineTo(19f); lineTo(21f, 9f)
        curveTo(21f, 13f, 16f, 13f, 16f, 9f); curveTo(16f, 13f, 12f, 13f, 12f, 9f)
        curveTo(12f, 13f, 8f, 13f, 8f, 9f); curveTo(8f, 13f, 3f, 13f, 3f, 9f); close()
        moveTo(5f, 13f); verticalLineTo(21f); horizontalLineTo(19f); verticalLineTo(13f)
        moveTo(10f, 21f); verticalLineTo(16f); horizontalLineTo(14f); verticalLineTo(21f)
    }
    val RestaurantMenu = outline("RestaurantMenu") {
        moveTo(4f, 3f); lineTo(20f, 21f); moveTo(3f, 7f); lineTo(7f, 3f)
        moveTo(5f, 9f); lineTo(9f, 5f); moveTo(4f, 21f); lineTo(20f, 3f)
        moveTo(20f, 3f); curveTo(15f, 3f, 12f, 8f, 15f, 9f)
    }
    val ConfirmationNumber = outline("Coupon") {
        moveTo(3f, 5f); horizontalLineTo(21f); verticalLineTo(9f)
        curveTo(17f, 9f, 17f, 15f, 21f, 15f); verticalLineTo(19f); horizontalLineTo(3f); verticalLineTo(15f)
        curveTo(7f, 15f, 7f, 9f, 3f, 9f); close()
        moveTo(14f, 8f); verticalLineTo(10f); moveTo(14f, 14f); verticalLineTo(16f)
    }
    val RateReview = outline("Review") {
        moveTo(13f, 4f); horizontalLineTo(3f); verticalLineTo(20f); lineTo(8f, 17f); horizontalLineTo(21f); verticalLineTo(12f)
        moveTo(9f, 10f); lineTo(17f, 2f); lineTo(21f, 6f); lineTo(13f, 14f); horizontalLineTo(9f); close()
    }
    val Campaign = outline("Campaign") {
        moveTo(4f, 9f); horizontalLineTo(9f); lineTo(19f, 4f); verticalLineTo(20f); lineTo(9f, 15f); horizontalLineTo(4f); close()
        moveTo(7f, 15f); lineTo(9f, 21f); horizontalLineTo(12f); lineTo(11f, 16f)
        moveTo(22f, 9f); verticalLineTo(15f)
    }
    val Visibility = outline("Visibility") {
        moveTo(2f, 12f); curveTo(7f, 3f, 17f, 3f, 22f, 12f); curveTo(17f, 21f, 7f, 21f, 2f, 12f); close(); circle(12f, 12f, 3f)
    }
    val NearMe = outline("NearMe") { moveTo(3f, 10f); lineTo(21f, 3f); lineTo(14f, 21f); lineTo(11f, 13f); close() }
    val Star = outline("Star") {
        moveTo(12f, 3f); lineTo(15f, 9f); lineTo(22f, 10f); lineTo(17f, 15f); lineTo(18f, 22f)
        lineTo(12f, 18f); lineTo(6f, 22f); lineTo(7f, 15f); lineTo(2f, 10f); lineTo(9f, 9f); close()
    }
    val ModeComment = outline("Comment") {
        moveTo(3f, 4f); horizontalLineTo(21f); verticalLineTo(17f); horizontalLineTo(8f); lineTo(3f, 21f); close()
        moveTo(7f, 8f); horizontalLineTo(17f); moveTo(7f, 12f); horizontalLineTo(14f)
    }
    val PlayArrow = outline("Play") { moveTo(7f, 4f); lineTo(20f, 12f); lineTo(7f, 20f); close() }
    val Pause = outline("Pause") { box(6f, 4f, 9f, 20f); box(15f, 4f, 18f, 20f) }
    val Image = outline("Image") { box(3f, 3f, 21f, 21f); circle(8f, 8f, 1.5f); moveTo(3f, 17f); lineTo(10f, 11f); lineTo(15f, 16f); lineTo(18f, 13f); lineTo(21f, 16f) }
    val AddPhotoAlternate = outline("AddPhoto") {
        moveTo(13f, 3f); horizontalLineTo(3f); verticalLineTo(21f); horizontalLineTo(21f); verticalLineTo(11f)
        moveTo(3f, 17f); lineTo(10f, 11f); lineTo(15f, 16f); lineTo(18f, 13f); lineTo(21f, 16f)
        moveTo(19f, 2f); verticalLineTo(8f); moveTo(16f, 5f); horizontalLineTo(22f)
    }
    val CalendarMonth = outline("Calendar") {
        box(3f, 5f, 21f, 21f); moveTo(3f, 10f); horizontalLineTo(21f)
        moveTo(8f, 2f); verticalLineTo(7f); moveTo(16f, 2f); verticalLineTo(7f)
        moveTo(7f, 14f); horizontalLineTo(9f); moveTo(14f, 14f); horizontalLineTo(16f)
        moveTo(7f, 18f); horizontalLineTo(9f); moveTo(14f, 18f); horizontalLineTo(16f)
    }
    val CheckCircle = outline("CheckCircle") { circle(12f, 12f, 9f); moveTo(7f, 12f); lineTo(11f, 16f); lineTo(17f, 9f) }
}

private fun outline(name: String, mirrored: Boolean = false, draw: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = mirrored).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = draw)
    }.build()

private fun PathBuilder.box(left: Float, top: Float, right: Float, bottom: Float) {
    moveTo(left, top); lineTo(right, top); lineTo(right, bottom); lineTo(left, bottom); close()
}
private fun PathBuilder.circle(x: Float, y: Float, r: Float) {
    val c = r * .55228475f
    moveTo(x + r, y)
    curveTo(x + r, y + c, x + c, y + r, x, y + r)
    curveTo(x - c, y + r, x - r, y + c, x - r, y)
    curveTo(x - r, y - c, x - c, y - r, x, y - r)
    curveTo(x + c, y - r, x + r, y - c, x + r, y); close()
}
