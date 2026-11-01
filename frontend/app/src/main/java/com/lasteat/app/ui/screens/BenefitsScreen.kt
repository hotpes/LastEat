package com.lasteat.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lasteat.app.data.Benefit
import com.lasteat.app.data.BenefitKind
import com.lasteat.app.ui.components.ChipRow
import com.lasteat.app.ui.components.EmptyState
import com.lasteat.app.ui.components.LocationSelector
import com.lasteat.app.ui.components.RatingRow
import com.lasteat.app.ui.components.RemoteImage
import com.lasteat.app.ui.components.formatDistance
import com.lasteat.app.ui.icons.LastEatIcons
import com.lasteat.app.ui.theme.Spacing

private val BenefitFilters = listOf("전체", "마감 할인", "제휴 혜택")

@Composable
fun BenefitsScreen(
    locationName: String,
    benefits: List<Benefit>,
    onLocationClick: () -> Unit,
    onBenefitClick: (Benefit) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf("전체") }
    val shown = remember(filter, benefits) {
        when (filter) {
            "마감 할인" -> benefits.filter { it.kind == BenefitKind.LastCall }
            "제휴 혜택" -> benefits.filter { it.kind == BenefitKind.Partner }
            else -> benefits
        }
    }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Text(
                "혜택 · 이벤트",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = Spacing.Screen, vertical = Spacing.lg),
            )
        }
        item { HeroBanner(Modifier.padding(horizontal = Spacing.Screen)) }
        item {
            Row(
                Modifier.padding(start = Spacing.Screen, end = Spacing.md, top = Spacing.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("지금, 내 주변 혜택", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${locationName}에서 챙길 수 있는 혜택 ${benefits.size}개",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LocationSelector(locationName, onLocationClick)
            }
        }
        item {
            ChipRow(BenefitFilters, filter, { filter = it }, Modifier.padding(vertical = Spacing.md))
        }
        if (shown.isEmpty()) {
            item {
                EmptyState(
                    icon = LastEatIcons.Ticket,
                    title = "아직 이 동네 혜택이 없어요",
                    description = "새 혜택이 등록되면 이곳에 먼저 알려드릴게요.",
                )
            }
        } else {
            items(shown, key = { it.id }) { b ->
                BenefitCard(
                    benefit = b,
                    onClick = { onBenefitClick(b) },
                    modifier = Modifier.padding(horizontal = Spacing.Screen, vertical = Spacing.sm),
                )
            }
        }
        item {
            Row(Modifier.padding(Spacing.Screen)) {
                Icon(LastEatIcons.Info, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(
                    "혜택마다 이용 조건이 달라요. 결제 전 직원에게 혜택 적용을 요청해 주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HeroBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(Modifier.padding(Spacing.xl), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("11월, 동네의 맛을 나눠요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "마감 할인에\n혜택 한 스푼",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(Spacing.sm))
                Text("우리 동네 제휴 식당 만나기 →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            RemoteImage(
                url = null,
                description = "이달의 혜택 식당",
                modifier = Modifier.size(96.dp).clip(MaterialTheme.shapes.extraLarge),
            )
        }
    }
}

@Composable
fun BenefitCard(benefit: Benefit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val r = benefit.restaurant
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Row {
                RemoteImage(r.photoUrl, "${r.name} 대표 사진", Modifier.size(64.dp).clip(MaterialTheme.shapes.small))
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Row {
                        Text(r.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text(formatDistance(r.distanceM), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(benefit.headline, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    RatingRow(r.rating, r.reviewCount)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = Spacing.md), color = MaterialTheme.colorScheme.outlineVariant)
            InfoLine("이용 조건", benefit.condition)
            Spacer(Modifier.height(4.dp))
            InfoLine("유효 기간", benefit.periodText, emphasize = true)
            Spacer(Modifier.height(Spacing.md))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(LastEatIcons.Ticket, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text("${benefit.kind.label} 자세히 보기", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String, emphasize: Boolean = false) {
    Row {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(64.dp))
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
