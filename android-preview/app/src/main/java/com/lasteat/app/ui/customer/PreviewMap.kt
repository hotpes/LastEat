package com.lasteat.app.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Figma's static neighborhood illustration; intentionally no live map or location request. */
@Composable
internal fun PreviewMap(results: Boolean, visibleIds: Set<String>, selectedId: String,
    onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val streets = figmaBitmap(NearbyAssets.MainWalkingStreets)
    val forest = figmaBitmap(NearbyAssets.GyeonguiForestTrail)
    val path = figmaBitmap(NearbyAssets.ForestWalkingPath)
    val accuracy = figmaBitmap(NearbyAssets.LocationAccuracy)
    val location = figmaBitmap(NearbyAssets.MyLocation)
    BoxWithConstraints(modifier.fillMaxWidth().aspectRatio(412f / if (results) 286f else 430f)
        .clip(RoundedCornerShape(0.dp)).background(Color(0xFFEEEFE9))) {
        val ratio = maxWidth.value / 412f
        Canvas(Modifier.fillMaxSize()) {
            withTransform({ scale(size.width / 412f, size.width / 412f, Offset.Zero) }) {
                listOf(
                    listOf(8,30,66,64),listOf(92,12,76,46),listOf(183,18,72,64),listOf(278,4,83,54),
                    listOf(23,121,62,54),listOf(100,96,61,75),listOf(185,110,63,68),listOf(277,97,91,57),
                    listOf(20,213,72,74),listOf(118,209,65,57),listOf(205,220,60,79),listOf(293,207,100,65),
                    listOf(12,324,74,70),listOf(112,310,66,90),listOf(210,342,73,70),listOf(302,315,87,91),
                    listOf(24,438,70,45),listOf(130,443,72,63),
                ).forEach { (x,y,w,h) -> drawRoundRect(Color(0xFFE4E6DE), Offset(x.toFloat(),y.toFloat()),
                    Size(w.toFloat(),h.toFloat()), CornerRadius(5f)) }
                fun asset(image: androidx.compose.ui.graphics.ImageBitmap, x: Float, y: Float, w: Float, h: Float) {
                    drawImage(image, dstOffset = IntOffset(x.roundToInt(),y.roundToInt()),
                        dstSize = IntSize(w.roundToInt(),h.roundToInt()))
                }
                asset(streets,72.95f,93.86f,178.583f,294.237f)
                asset(forest,-55.04f,-37.26f,443.726f,555.955f)
                asset(path,-41.57f,-21.82f,415.305f,523.745f)
                asset(accuracy,180f,250f,52f,52f); asset(location,197f,267f,18f,18f)
            }
        }
        Label("연남동", 13, Color(0xFFA4AC9E), true, Modifier.offset((29*ratio).dp,(56*ratio).dp))
        Label("동교로", 10, Color(0xFF989F94), modifier = Modifier.offset((12*ratio).dp,(188*ratio).dp))
        Label("연남파출소", 10, Color(0xFF92998E), modifier = Modifier.offset((306*ratio).dp,(245*ratio).dp))
        Label("경의선숲길", 11, Color(0xFF82967A), true, Modifier.offset((222*ratio).dp,(51*ratio).dp).rotate(-42f))
        Label("동네한입 지도   ━━━━ 100m", 9, Color(0xFF939C8D),
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp, 10.dp))
        listOf(Triple("green",22f,128f),Triple("ongi",154f,188f),Triple("gyeol",28f,287f),Triple("sodam",281f,302f))
            .filter { it.first in visibleIds }.forEach { (id,x,y) ->
                val item = PreviewRestaurants.first { it.id == id }
                val selected = id == selectedId
                val markerY = if (results && id == "ongi") 108f else if (results && id == "green") 64f else if (results && id == "sodam") 164f else y
                Column(Modifier.offset((x*ratio).dp,(markerY*ratio).dp).clickable { onSelect(id) },
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.shadow(3.dp,RoundedCornerShape(10.dp)).height(34.dp)
                        .background(if (selected) CustomerColors.Green else Color.White,RoundedCornerShape(10.dp))
                        .border(1.dp,if (selected) CustomerColors.Green else Color(0xFFC8D4C7),RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssetIcon(if(selected) NearbyAssets.Utensils2 else NearbyAssets.Utensils1,13.dp)
                        Label("${item.name} ${item.rating}",11,if(selected) Color.White else CustomerColors.Ink,true)
                    }
                    FigmaImage(if(selected) NearbyAssets.MarkerTip1 else NearbyAssets.MarkerTip,null,Modifier.size(10.dp,6.dp))
                }
            }
        Box(Modifier.align(Alignment.BottomEnd).padding(end = 20.dp,bottom = if(results) 18.dp else 38.dp)
            .size(40.dp).shadow(3.dp,RoundedCornerShape(99.dp)).background(Color.White,RoundedCornerShape(99.dp)), Alignment.Center) {
            AssetIcon(if(results) ResultsAssets.LocateFixed else NearbyAssets.LocateFixed,21.dp)
        }
        if(!results) Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 29.dp)
            .shadow(3.dp,RoundedCornerShape(99.dp)).background(Color.White,RoundedCornerShape(99.dp))
            .height(38.dp).clickable { onSelect(selectedId) }.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssetIcon(NearbyAssets.RotateCw,14.dp); Label("이 지역 다시 찾기",12,CustomerColors.Green,true)
        }
    }
}
