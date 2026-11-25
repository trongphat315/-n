package com.example.doan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.model.CategoryIcon
import com.example.doan.model.DifficultyLevel
import com.example.doan.model.QuizSetItem
import com.example.doan.model.TopicItem
import com.example.doan.viewModel.ChuDeViewModel

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val ScreenBgColor = Color(red = 246, green = 247, blue = 251)
private val RoyalBlue = Color(red = 45, green = 62, blue = 168)
private val ElectricBlue = Color(red = 46, green = 114, blue = 216)
private val VictoryOrange = Color(red = 245, green = 142, blue = 11)
private val TextDark = Color(red = 30, green = 41, blue = 59)
private val TextMuted = Color(red = 100, green = 116, blue = 139)
private val SubCardBg = Color(red = 248, green = 250, blue = 252)
private val DividerGray = Color(red = 226, green = 232, blue = 240)

// Màu nhãn danh mục & độ khó
private val SoftBlueBg = Color(red = 238, green = 242, blue = 255)
private val SoftOrangeBg = Color(red = 255, green = 247, blue = 237)
private val SoftPurpleBg = Color(red = 243, green = 232, blue = 255)
private val SpacePurple = Color(red = 124, green = 58, blue = 237)
private val SoftGreenBg = Color(red = 220, green = 252, blue = 231)
private val EmeraldGreen = Color(red = 16, green = 185, blue = 129)
private val DarkGreenText = Color(red = 22, green = 163, blue = 74)
private val SoftYellowBg = Color(red = 254, green = 249, blue = 195)
private val AmberText = Color(red = 180, green = 83, blue = 9)
private val SoftRedBg = Color(red = 254, green = 226, blue = 226)
private val RedText = Color(red = 220, green = 38, blue = 38)

@Composable
fun DanhSachChuDeScreen(
    chuDeViewModel: ChuDeViewModel = viewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateBattle1v1: () -> Unit = {},
    onNavigateLeaderboard: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onStartQuizSet: (QuizSetItem) -> Unit = {}
) {
    val state by chuDeViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ScreenBgColor,
        bottomBar = {
            TopicsBottomBar(
                onHomeClick = onNavigateHome,
                onBattleClick = onNavigateBattle1v1,
                onLeaderboardClick = onNavigateLeaderboard,
                onProfileClick = onNavigateProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Tiêu đề màn hình & Nhãn tổng số chủ đề
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Danh sách Chủ đề",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Chọn chủ đề và bộ đề trắc nghiệm phù hợp",
                        fontSize = 12.5.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftBlueBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${state.allTopics.size} chủ đề",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Thanh tìm kiếm chủ đề, bộ đề
            SearchInputBar(
                query = state.searchQuery,
                onQueryChange = chuDeViewModel::onSearchQueryChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Hàng bộ lọc danh mục cuộn ngang
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.categories) { category ->
                    val isSelected = state.selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RoyalBlue else Color.White)
                            .clickable { chuDeViewModel.onCategorySelected(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Danh sách các thẻ Chủ đề & Bộ đề trắc nghiệm
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.filteredTopics, key = { it.id }) { topic ->
                    TopicAccordionCard(
                        topic = topic,
                        isExpanded = state.expandedTopicId == topic.id,
                        onHeaderClick = { chuDeViewModel.toggleTopicExpand(topic.id) },
                        onStartQuizClick = onStartQuizSet
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchMagnifierIcon(modifier = Modifier.size(18.dp), color = TextMuted)
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Tìm kiếm chủ đề, bộ đề...",
                        color = TextMuted,
                        fontSize = 13.5.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = TextDark, fontSize = 13.5.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Text(
                    text = "✕",
                    color = TextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onQueryChange("") }
                )
            }
        }
    }
}

@Composable
private fun TopicAccordionCard(
    topic: TopicItem,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onStartQuizClick: (QuizSetItem) -> Unit
) {
    val (iconBg, tagBg, tagText) = when (topic.icon) {
        CategoryIcon.TECH -> Triple(ElectricBlue, SoftBlueBg, RoyalBlue)
        CategoryIcon.HISTORY -> Triple(VictoryOrange, SoftOrangeBg, VictoryOrange)
        CategoryIcon.SPACE -> Triple(SpacePurple, SoftPurpleBg, SpacePurple)
        CategoryIcon.ENGLISH -> Triple(EmeraldGreen, SoftGreenBg, DarkGreenText)
        CategoryIcon.GEOGRAPHY -> Triple(ElectricBlue, SoftBlueBg, RoyalBlue)
        CategoryIcon.LITERATURE -> Triple(VictoryOrange, SoftOrangeBg, VictoryOrange)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = if (isExpanded) BorderStroke(1.5.dp, ElectricBlue) else null,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Phần đầu thẻ Chủ đề
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hộp biểu tượng chủ đề
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    TopicCategoryIcon(icon = topic.icon, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Tên chủ đề & số câu hỏi
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${topic.totalQuestions} câu • ${topic.quizSets.size} bộ đề",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                // Nhãn danh mục
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(tagBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = topic.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagText
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mũi tên mở rộng / thu gọn
                ChevronArrowIcon(
                    isUp = isExpanded,
                    color = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Danh sách bộ đề trắc nghiệm khi mở rộng
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    HorizontalDivider(color = DividerGray, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "CHỌN BỘ ĐỀ TRẮC NGHIỆM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        topic.quizSets.forEach { quizSet ->
                            QuizSetSubCard(
                                quizSet = quizSet,
                                onStartClick = { onStartQuizClick(quizSet) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizSetSubCard(
    quizSet: QuizSetItem,
    onStartClick: () -> Unit
) {
    val (diffBg, diffColor) = when (quizSet.difficulty) {
        DifficultyLevel.EASY -> Pair(SoftGreenBg, DarkGreenText)
        DifficultyLevel.MEDIUM -> Pair(SoftYellowBg, AmberText)
        DifficultyLevel.HARD -> Pair(SoftRedBg, RedText)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SubCardBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quizSet.title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(diffBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = quizSet.difficulty.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = diffColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+${quizSet.xpReward} XP",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = VictoryOrange
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onStartClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VictoryOrange),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = "Bắt đầu làm bài",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun TopicsBottomBar(
    onHomeClick: () -> Unit,
    onBattleClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavTabItem(label = "Trang chủ", selected = false, onClick = onHomeClick) { color ->
                HomeNavIcon(modifier = Modifier.size(20.dp), color = color)
            }

            NavTabItem(label = "Chủ đề", selected = true, onClick = {}) { color ->
                GridNavIcon(modifier = Modifier.size(20.dp), color = color)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .clickable(onClick = onBattleClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(VictoryOrange),
                    contentAlignment = Alignment.Center
                ) {
                    BoltNavIcon(modifier = Modifier.size(22.dp), color = Color.White)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Đấu 1v1",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }

            NavTabItem(label = "Xếp hạng", selected = false, onClick = onLeaderboardClick) { color ->
                TrophyNavIcon(modifier = Modifier.size(20.dp), color = color)
            }

            NavTabItem(label = "Cá nhân", selected = false, onClick = onProfileClick) { color ->
                ProfileNavIcon(modifier = Modifier.size(20.dp), color = color)
            }
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val itemColor = if (selected) RoyalBlue else TextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        icon(itemColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = itemColor
        )
    }
}

// --- Bộ biểu tượng vẽ gọn nhẹ bằng Canvas ---

@Composable
private fun SearchMagnifierIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val r = size.minDimension * 0.34f
        val c = Offset(size.width * 0.42f, size.height * 0.42f)
        drawCircle(color = color, radius = r, center = c, style = Stroke(1.7.dp.toPx()))
        drawLine(
            color = color,
            start = Offset(c.x + r * 0.72f, c.y + r * 0.72f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ChevronArrowIcon(isUp: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            if (isUp) {
                moveTo(w * 0.20f, h * 0.65f)
                lineTo(w * 0.50f, h * 0.35f)
                lineTo(w * 0.80f, h * 0.65f)
            } else {
                moveTo(w * 0.20f, h * 0.35f)
                lineTo(w * 0.50f, h * 0.65f)
                lineTo(w * 0.80f, h * 0.35f)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun TopicCategoryIcon(icon: CategoryIcon, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        when (icon) {
            CategoryIcon.TECH -> {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(w * 0.24f, h * 0.24f),
                    size = Size(w * 0.52f, h * 0.52f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(1.8.dp.toPx())
                )
            }
            CategoryIcon.HISTORY -> {
                val roof = Path().apply {
                    moveTo(w * 0.16f, h * 0.40f)
                    lineTo(w * 0.50f, h * 0.18f)
                    lineTo(w * 0.84f, h * 0.40f)
                    close()
                }
                drawPath(roof, Color.White)
                drawLine(Color.White, Offset(w * 0.20f, h * 0.80f), Offset(w * 0.80f, h * 0.80f), 2.dp.toPx())
            }
            CategoryIcon.SPACE -> {
                val body = Path().apply {
                    moveTo(w * 0.78f, h * 0.22f)
                    lineTo(w * 0.46f, h * 0.74f)
                    lineTo(w * 0.26f, h * 0.54f)
                    close()
                }
                drawPath(body, Color.White)
            }
            CategoryIcon.ENGLISH -> {
                val c = Offset(w / 2f, h / 2f)
                val r = size.minDimension * 0.38f
                drawCircle(Color.White, r, c, style = Stroke(1.7.dp.toPx()))
                drawLine(Color.White, Offset(c.x - r, c.y), Offset(c.x + r, c.y), 1.4.dp.toPx())
                drawLine(Color.White, Offset(c.x, c.y - r), Offset(c.x, c.y + r), 1.4.dp.toPx())
            }
            CategoryIcon.GEOGRAPHY -> {
                drawCircle(Color.White, size.minDimension * 0.36f, style = Stroke(1.8.dp.toPx()))
            }
            CategoryIcon.LITERATURE -> {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(w * 0.22f, h * 0.18f),
                    size = Size(w * 0.56f, h * 0.64f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(1.8.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun HomeNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val roof = Path().apply {
            moveTo(w * 0.14f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.86f, h * 0.48f)
            lineTo(w * 0.76f, h * 0.48f)
            lineTo(w * 0.76f, h * 0.84f)
            lineTo(w * 0.24f, h * 0.84f)
            lineTo(w * 0.24f, h * 0.48f)
            close()
        }
        drawPath(roof, color, style = Stroke(1.7.dp.toPx()))
    }
}

@Composable
private fun GridNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val box = size.width * 0.36f
        val gap = size.width * 0.14f
        val stroke = Stroke(width = 1.7.dp.toPx())
        listOf(
            Offset(gap, gap),
            Offset(gap + box + gap * 0.7f, gap),
            Offset(gap, gap + box + gap * 0.7f),
            Offset(gap + box + gap * 0.7f, gap + box + gap * 0.7f)
        ).forEach { pos ->
            drawRoundRect(color, pos, Size(box, box), CornerRadius(3.dp.toPx()), style = stroke)
        }
    }
}

@Composable
private fun BoltNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.58f, h * 0.06f)
            lineTo(w * 0.20f, h * 0.54f)
            lineTo(w * 0.48f, h * 0.54f)
            lineTo(w * 0.38f, h * 0.94f)
            lineTo(w * 0.80f, h * 0.44f)
            lineTo(w * 0.52f, h * 0.44f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
private fun TrophyNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cup = Path().apply {
            moveTo(w * 0.24f, h * 0.16f)
            lineTo(w * 0.76f, h * 0.16f)
            lineTo(w * 0.68f, h * 0.54f)
            lineTo(w * 0.32f, h * 0.54f)
            close()
        }
        drawPath(path = cup, color = color)
        drawLine(color, Offset(w * 0.50f, h * 0.54f), Offset(w * 0.50f, h * 0.78f), 2.2.dp.toPx())
        drawLine(color, Offset(w * 0.30f, h * 0.82f), Offset(w * 0.70f, h * 0.82f), 2.5.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun ProfileNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color, w * 0.18f, Offset(w * 0.50f, h * 0.34f), style = Stroke(1.7.dp.toPx()))
        drawArc(color, 180f, 180f, false, Offset(w * 0.22f, h * 0.58f), Size(w * 0.56f, h * 0.44f), style = Stroke(1.7.dp.toPx()))
    }
}