package com.example.doan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.model.HistoryItem
import com.example.doan.model.HistorySummary
import com.example.doan.viewModel.HistoryFilter
import com.example.doan.viewModel.LichSuUiState
import com.example.doan.viewModel.LichSuViewModel

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val ScreenBgColor = Color(red = 246, green = 247, blue = 251)
private val BannerRoyalBlue = Color(red = 53, green = 67, blue = 178)
private val BannerElectricBlue = Color(red = 52, green = 122, blue = 228)
private val MintAccuracyGreen = Color(red = 52, green = 224, blue = 161)
private val GoldStreakYellow = Color(red = 255, green = 208, blue = 38)
private val FlameOrange = Color(red = 255, green = 115, blue = 29)
private val PracticeBadgeBg = Color(red = 238, green = 242, blue = 255)
private val PracticeBadgeText = Color(red = 59, green = 73, blue = 186)
private val PvpBadgeBg = Color(red = 254, green = 243, blue = 199)
private val PvpBadgeText = Color(red = 217, green = 119, blue = 6)
private val ScoreOrange = Color(red = 245, green = 130, blue = 11)
private val CorrectEmerald = Color(red = 16, green = 163, blue = 82)
private val LinkBlue = Color(red = 37, green = 99, blue = 235)
private val TextDark = Color(red = 26, green = 34, blue = 52)
private val TextMuted = Color(red = 138, green = 150, blue = 168)
private val BorderLightGray = Color(red = 228, green = 233, blue = 242)
private val DividerLineGray = Color(red = 240, green = 243, blue = 248)
private val ErrorRed = Color(red = 210, green = 40, blue = 40)

@Composable
fun LichSuScreen(
    lichSuViewModel: LichSuViewModel = viewModel(),
    onBack: () -> Unit = {},
    onViewAttemptDetail: (attemptId: String) -> Unit = {}
) {
    val state by lichSuViewModel.uiState.collectAsState()

    LichSuContent(
        state = state,
        onBack = onBack,
        onSelectFilter = lichSuViewModel::selectFilter,
        onRetry = { lichSuViewModel.loadHistory() },
        onViewAttemptDetail = onViewAttemptDetail
    )
}

@Composable
private fun LichSuContent(
    state: LichSuUiState,
    onBack: () -> Unit,
    onSelectFilter: (HistoryFilter) -> Unit,
    onRetry: () -> Unit,
    onViewAttemptDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Thanh tiêu đề & nút quay lại
        HistoryTopHeader(onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Thẻ thống kê tổng hợp (Lượt thi • Độ chính xác • Chuỗi cao nhất)
        HistorySummaryBanner(summary = state.summary)

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Thanh lọc 3 chế độ: Tất cả | Luyện tập | Đấu 1v1
        HistoryFilterTabs(
            selectedFilter = state.selectedFilter,
            onSelectFilter = onSelectFilter
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Danh sách chi tiết từng lượt thi
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BannerRoyalBlue)
                }
            }
            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.errorMessage,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = BannerRoyalBlue)
                    ) {
                        Text(text = "Thử lại", color = Color.White)
                    }
                }
            }
            state.items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có dữ liệu lịch sử cho mục này.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(state.items, key = { it.attemptId }) { item ->
                        HistoryAttemptCard(
                            item = item,
                            onViewDetail = { onViewAttemptDetail(item.attemptId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTopHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, BorderLightGray),
            shadowElevation = 1.dp,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBack)
        ) {
            Box(contentAlignment = Alignment.Center) {
                BackArrowIcon(modifier = Modifier.size(16.dp), color = TextDark)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Lịch sử làm bài",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Text(
                text = "Nhật ký luyện tập & đấu 1v1",
                fontSize = 12.5.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun HistorySummaryBanner(summary: HistorySummary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(BannerRoyalBlue, BannerElectricBlue)
                )
            )
            .padding(vertical = 18.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cột 1: Lượt thi
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${summary.totalAttempts}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Lượt thi",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(Color.White.copy(alpha = 0.22f))
            )

            // Cột 2: Độ chính xác
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${summary.accuracyPercent}%",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MintAccuracyGreen
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Độ chính xác",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(Color.White.copy(alpha = 0.22f))
            )

            // Cột 3: Chuỗi cao nhất
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${summary.bestStreak}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldStreakYellow
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    StreakFlameIcon(modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Chuỗi cao nhất",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun HistoryFilterTabs(
    selectedFilter: HistoryFilter,
    onSelectFilter: (HistoryFilter) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HistoryFilter.entries.forEach { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (isSelected) BannerRoyalBlue else Color.Transparent)
                        .clickable { onSelectFilter(filter) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else TextDark
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryAttemptCard(
    item: HistoryItem,
    onViewDetail: () -> Unit
) {
    val isPvp = item.mode == "PVP"
    val badgeBg = if (isPvp) PvpBadgeBg else PracticeBadgeBg
    val badgeTextColor = if (isPvp) PvpBadgeText else PracticeBadgeText
    val badgeLabel = if (isPvp) "Đấu 1v1" else "Luyện tập"
    val formattedTime = "%02d:%02d".format(item.durationSeconds / 60, item.durationSeconds % 60)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.timestamp,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.score} đ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = ScoreOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Đúng ${item.correctCount}/${item.totalQuestions}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CorrectEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerLineGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallClockIcon(modifier = Modifier.size(14.dp), color = TextMuted)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Thời gian: ",
                        fontSize = 12.5.sp,
                        color = TextMuted
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Text(
                    text = "Xem chi tiết >",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = LinkBlue,
                    modifier = Modifier.clickable(onClick = onViewDetail)
                )
            }
        }
    }
}

@Composable
private fun BackArrowIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()
        drawLine(color, Offset(w * 0.18f, h * 0.50f), Offset(w * 0.85f, h * 0.50f), stroke, StrokeCap.Round)
        val head = Path().apply {
            moveTo(w * 0.45f, h * 0.22f)
            lineTo(w * 0.16f, h * 0.50f)
            lineTo(w * 0.45f, h * 0.78f)
        }
        drawPath(head, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun SmallClockIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = color, radius = r * 0.88f, center = c, style = Stroke(1.3.dp.toPx()))
        drawLine(color, c, Offset(c.x, c.y - r * 0.48f), 1.3.dp.toPx(), StrokeCap.Round)
        drawLine(color, c, Offset(c.x + r * 0.36f, c.y), 1.3.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun StreakFlameIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val outer = Path().apply {
            moveTo(w * 0.52f, h * 0.06f)
            quadraticTo(w * 0.92f, h * 0.42f, w * 0.82f, h * 0.76f)
            quadraticTo(w * 0.68f, h * 0.96f, w * 0.48f, h * 0.96f)
            quadraticTo(w * 0.20f, h * 0.94f, w * 0.18f, h * 0.66f)
            quadraticTo(w * 0.18f, h * 0.44f, w * 0.36f, h * 0.30f)
            quadraticTo(w * 0.34f, h * 0.46f, w * 0.46f, h * 0.44f)
            quadraticTo(w * 0.44f, h * 0.24f, w * 0.52f, h * 0.06f)
            close()
        }
        drawPath(outer, FlameOrange)

        val inner = Path().apply {
            moveTo(w * 0.50f, h * 0.46f)
            quadraticTo(w * 0.70f, h * 0.64f, w * 0.62f, h * 0.84f)
            quadraticTo(w * 0.50f, h * 0.92f, w * 0.38f, h * 0.82f)
            quadraticTo(w * 0.32f, h * 0.66f, w * 0.50f, h * 0.46f)
            close()
        }
        drawPath(inner, GoldStreakYellow)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LichSuScreenPreview() {
    MaterialTheme {
        LichSuContent(
            state = LichSuUiState(
                isLoading = false,
                selectedFilter = HistoryFilter.ALL,
                summary = HistorySummary(42, 82, 9),
                items = listOf(
                    HistoryItem("1", "PRACTICE", "Công nghệ & AI – Bộ đề #2", "12/11/2026 • 13:45", 850, 8, 10, 102),
                    HistoryItem("2", "PVP", "Đấu 1v1 vs Hoàng Nam (Thắng)", "12/11/2026 • 11:20", 920, 5, 5, 38),
                    HistoryItem("3", "PRACTICE", "Lịch sử Việt Nam – Bộ đề #1", "11/11/2026 • 20:15", 900, 9, 10, 88)
                )
            ),
            onBack = {},
            onSelectFilter = {},
            onRetry = {},
            onViewAttemptDetail = {}
        )
    }
}