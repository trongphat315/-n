package com.example.doan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.model.QuestionReviewItem
import com.example.doan.viewModel.ChiTietLichSuUiState
import com.example.doan.viewModel.ChiTietLichSuViewModel
import com.example.doan.viewModel.DetailFilter

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val ScreenBgColor = Color(red = 246, green = 247, blue = 251)
private val RoyalBlue = Color(red = 53, green = 67, blue = 178)
private val TextDark = Color(red = 26, green = 34, blue = 52)
private val TextMuted = Color(red = 138, green = 150, blue = 168)
private val TextBodySlate = Color(red = 71, green = 85, blue = 105)
private val BorderLightGray = Color(red = 228, green = 233, blue = 242)

private val CorrectCardBorder = Color(red = 187, green = 247, blue = 208)
private val CorrectBoxBg = Color(red = 236, green = 253, blue = 245)
private val CorrectBoxBorder = Color(red = 167, green = 243, blue = 208)
private val CorrectBadgeBg = Color(red = 220, green = 252, blue = 231)
private val CorrectDarkGreen = Color(red = 21, green = 128, blue = 61)

private val WrongCardBorder = Color(red = 254, green = 202, blue = 202)
private val WrongBoxBg = Color(red = 254, green = 242, blue = 242)
private val WrongBoxBorder = Color(red = 254, green = 202, blue = 202)
private val WrongBadgeBg = Color(red = 254, green = 226, blue = 226)
private val WrongDarkRed = Color(red = 185, green = 28, blue = 28)

private val ExplanationBoxBg = Color(red = 242, green = 246, blue = 255)
private val BulbYellow = Color(red = 250, green = 204, blue = 21)
private val BulbBaseGray = Color(red = 148, green = 163, blue = 184)

@Composable
fun ChiTietLichSuScreen(
    attemptId: String = "att_01",
    viewModel: ChiTietLichSuViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(attemptId) {
        viewModel.loadAttemptDetails(attemptId)
    }

    ChiTietLichSuContent(
        state = state,
        onBack = onBack,
        onSelectFilter = viewModel::selectFilter,
        onRetry = { viewModel.loadAttemptDetails(attemptId) }
    )
}

@Composable
private fun ChiTietLichSuContent(
    state: ChiTietLichSuUiState,
    onBack: () -> Unit,
    onSelectFilter: (DetailFilter) -> Unit,
    onRetry: () -> Unit
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

        // 1. Thanh tiêu đề & Huy hiệu tổng điểm (850/1000)
        DetailTopHeader(
            quizTitle = state.quizTitle.ifBlank { "Công nghệ & AI – Bộ đề #1" },
            score = state.score,
            maxScore = state.maxScore,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Thanh lọc 3 tab: Tất cả (5) | Đúng (4) | Sai (1)
        DetailFilterTabs(
            selectedFilter = state.selectedFilter,
            totalCount = state.totalCount,
            correctCount = state.correctCount,
            wrongCount = state.wrongCount,
            onSelectFilter = onSelectFilter
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Danh sách câu hỏi chi tiết
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RoyalBlue)
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
                        color = WrongDarkRed,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text(text = "Thử lại", color = Color.White)
                    }
                }
            }
            state.filteredQuestions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có câu hỏi nào trong mục này.",
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
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.filteredQuestions, key = { it.questionNumber }) { item ->
                        QuestionReviewCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopHeader(
    quizTitle: String,
    score: Int,
    maxScore: Int,
    onBack: () -> Unit
) {
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
                DetailBackArrowIcon(modifier = Modifier.size(16.dp), color = TextDark)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chi tiết bài làm",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Text(
                text = quizTitle,
                fontSize = 12.5.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Huy hiệu điểm số bên phải (VD: 850/1000)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(RoyalBlue)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score/$maxScore",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DetailFilterTabs(
    selectedFilter: DetailFilter,
    totalCount: Int,
    correctCount: Int,
    wrongCount: Int,
    onSelectFilter: (DetailFilter) -> Unit
) {
    val tabs = listOf(
        Triple(DetailFilter.ALL, "Tất cả ($totalCount)", selectedFilter == DetailFilter.ALL),
        Triple(DetailFilter.CORRECT, "Đúng ($correctCount)", selectedFilter == DetailFilter.CORRECT),
        Triple(DetailFilter.WRONG, "Sai ($wrongCount)", selectedFilter == DetailFilter.WRONG)
    )

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
            tabs.forEach { (filter, label, isSelected) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (isSelected) RoyalBlue else Color.Transparent)
                        .clickable { onSelectFilter(filter) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
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
private fun QuestionReviewCard(item: QuestionReviewItem) {
    val cardBorderColor = if (item.isCorrect) CorrectCardBorder else WrongCardBorder
    val badgeBg = if (item.isCorrect) CorrectBadgeBg else WrongBadgeBg
    val badgeColor = if (item.isCorrect) CorrectDarkGreen else WrongDarkRed

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, cardBorderColor),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Hàng 1: Câu số • Chủ đề & Nhãn trạng thái (✓ Đúng / ✕ Sai)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Câu ${item.questionNumber} • ${item.categoryLabel}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isCorrect) {
                        CheckMarkIcon(modifier = Modifier.size(11.dp), color = badgeColor)
                    } else {
                        CrossMarkIcon(modifier = Modifier.size(10.dp), color = badgeColor)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.isCorrect) "Đúng" else "Sai",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hàng 2: Nội dung câu hỏi
            Text(
                text = item.questionContent,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Hàng 3: Khung đáp án người chơi đã chọn
            if (item.isCorrect) {
                AnswerHighlightBox(
                    prefix = "Bạn chọn: ",
                    answerText = item.userAnswerText ?: item.correctAnswerText,
                    bgColor = CorrectBoxBg,
                    borderColor = CorrectBoxBorder,
                    textColor = CorrectDarkGreen,
                    isCheckIcon = true
                )
            } else {
                AnswerHighlightBox(
                    prefix = "Bạn chọn: ",
                    answerText = item.userAnswerText ?: "Hết giờ (Chưa chọn)",
                    bgColor = WrongBoxBg,
                    borderColor = WrongBoxBorder,
                    textColor = WrongDarkRed,
                    isCheckIcon = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hiển thị thêm khung Đáp án đúng khi trả lời sai
                AnswerHighlightBox(
                    prefix = "Đáp án đúng: ",
                    answerText = item.correctAnswerText,
                    bgColor = CorrectBoxBg,
                    borderColor = CorrectBoxBorder,
                    textColor = CorrectDarkGreen,
                    isCheckIcon = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hàng 4: Khung Giải thích kèm biểu tượng bóng đèn
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExplanationBoxBg)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                LightbulbIcon(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalBlue
                            )
                        ) {
                            append("Giải thích: ")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                color = TextBodySlate
                            )
                        ) {
                            append(item.explanation)
                        }
                    },
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AnswerHighlightBox(
    prefix: String,
    answerText: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    isCheckIcon: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                    append(prefix)
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(answerText)
                }
            },
            fontSize = 13.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (isCheckIcon) {
            CheckMarkIcon(modifier = Modifier.size(13.dp), color = textColor)
        } else {
            CrossMarkIcon(modifier = Modifier.size(12.dp), color = textColor)
        }
    }
}

@Composable
private fun DetailBackArrowIcon(modifier: Modifier = Modifier, color: Color) {
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
private fun CheckMarkIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.14f, size.height * 0.52f)
            lineTo(size.width * 0.40f, size.height * 0.78f)
            lineTo(size.width * 0.86f, size.height * 0.22f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun CrossMarkIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.18f),
            end = Offset(size.width * 0.82f, size.height * 0.82f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.82f, size.height * 0.18f),
            end = Offset(size.width * 0.18f, size.height * 0.82f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun LightbulbIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Bóng thủy tinh vàng
        drawCircle(
            color = BulbYellow,
            radius = w * 0.34f,
            center = Offset(w * 0.50f, h * 0.38f)
        )
        drawRect(
            color = BulbYellow,
            topLeft = Offset(w * 0.34f, h * 0.52f),
            size = Size(w * 0.32f, h * 0.18f)
        )

        // Đuôi đèn
        drawLine(
            color = BulbBaseGray,
            start = Offset(w * 0.36f, h * 0.76f),
            end = Offset(w * 0.64f, h * 0.76f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = BulbBaseGray,
            start = Offset(w * 0.40f, h * 0.88f),
            end = Offset(w * 0.60f, h * 0.88f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChiTietLichSuScreenPreview() {
    MaterialTheme {
        ChiTietLichSuContent(
            state = ChiTietLichSuUiState(
                isLoading = false,
                quizTitle = "Công nghệ & AI – Bộ đề #1",
                score = 850,
                maxScore = 1000,
                selectedFilter = DetailFilter.ALL,
                allQuestions = listOf(
                    QuestionReviewItem(
                        1,
                        "Công nghệ & AI",
                        "Thuật ngữ \"LLM\" trong lĩnh vực Trí tuệ nhân tạo hiện đại là viết tắt của cụm từ nào?",
                        "A. Large Language Model",
                        "A. Large Language Model",
                        true,
                        "LLM là viết tắt của Large Language Model (Mô hình Ngôn ngữ Lớn), nền tảng của các trợ lý AI tạo sinh hiện đại."
                    ),
                    QuestionReviewItem(
                        2,
                        "Lịch sử Việt Nam",
                        "Chiến thắng Bạch Đằng lẫy lừng đánh tan quân Nam Hán của Ngô Quyền diễn ra vào năm nào?",
                        "A. Năm 938",
                        "A. Năm 938",
                        true,
                        "Năm 938, Ngô Quyền dùng kế đóng cọc gỗ đầu nhọn xuống lòng sông Bạch Đằng, đánh bại quân Nam Hán, mở ra kỷ nguyên độc lập tự chủ lâu dài."
                    ),
                    QuestionReviewItem(
                        3,
                        "Khoa học Vũ trụ",
                        "Hành tinh nào trong Hệ Mặt Trời có kích thước lớn nhất?",
                        "A. Sao Thổ (Saturn)",
                        "B. Sao Mộc (Jupiter)",
                        false,
                        "Sao Mộc (Jupiter) là hành tinh khí khổng lồ lớn nhất trong Hệ Mặt Trời với đường kính gấp khoảng 11 lần Trái Đất."
                    )
                )
            ),
            onBack = {},
            onSelectFilter = {},
            onRetry = {}
        )
    }
}