package com.example.doan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.model.QuizResultResponse
import com.example.doan.viewModel.KetQuaViewModel

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val ScreenBgColor = Color(red = 246, green = 247, blue = 251)
private val BannerRoyalBlue = Color(red = 53, green = 67, blue = 178)
private val BannerElectricBlue = Color(red = 47, green = 116, blue = 224)
private val TrophyGold = Color(red = 255, green = 204, blue = 21)
private val ScoreYellow = Color(red = 255, green = 224, blue = 46)
private val XpBadgeBg = Color(red = 32, green = 126, blue = 178)
private val XpTextCyan = Color(red = 125, green = 242, blue = 255)
private val EmeraldRingGreen = Color(red = 10, green = 196, blue = 118)
private val TrackLightGray = Color(red = 230, green = 234, blue = 240)
private val DividerGray = Color(red = 238, green = 241, blue = 246)
private val TextDark = Color(red = 30, green = 41, blue = 59)
private val TextMuted = Color(red = 135, green = 148, blue = 168)
private val StatTimeBg = Color(red = 245, green = 247, blue = 250)
private val StatCorrectBg = Color(red = 234, green = 252, blue = 241)
private val StatCorrectLabel = Color(red = 22, green = 163, blue = 74)
private val StatCorrectValue = Color(red = 21, green = 128, blue = 61)
private val StatWrongBg = Color(red = 254, green = 242, blue = 242)
private val StatWrongLabel = Color(red = 239, green = 68, blue = 68)
private val StatWrongValue = Color(red = 185, green = 28, blue = 28)
private val PrimaryActionBlue = Color(red = 52, green = 65, blue = 176)
private val RetryOrange = Color(red = 245, green = 146, blue = 0)
private val OutlineBorderGray = Color(red = 226, green = 232, blue = 240)

@Composable
fun KetQuaScreen(
    attemptId: String = "attempt_default",
    ketQuaViewModel: KetQuaViewModel = viewModel(),
    onViewDetails: (attemptId: String) -> Unit = {},
    onRetryQuiz: (quizId: Int, quizTitle: String) -> Unit = { _, _ -> },
    onBackHome: () -> Unit = {}
) {
    val state by ketQuaViewModel.uiState.collectAsState()

    LaunchedEffect(attemptId) {
        ketQuaViewModel.loadResult(attemptId)
    }

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBgColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BannerRoyalBlue)
            }
        }
        state.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBgColor)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.errorMessage ?: "",
                    color = StatWrongValue,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { ketQuaViewModel.loadResult(attemptId) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionBlue)
                ) {
                    Text(text = "Thử lại", color = Color.White)
                }
            }
        }
        state.result != null -> {
            KetQuaContent(
                result = state.result!!,
                onViewDetails = { onViewDetails(state.result!!.attemptId) },
                onRetryQuiz = {
                    onRetryQuiz(state.result!!.quizId, state.result!!.quizTitle)
                },
                onBackHome = onBackHome
            )
        }
    }
}

@Composable
private fun KetQuaContent(
    result: QuizResultResponse,
    onViewDetails: () -> Unit,
    onRetryQuiz: () -> Unit,
    onBackHome: () -> Unit
) {
    val formattedTime = "%02d:%02d".format(
        result.durationSeconds / 60,
        result.durationSeconds % 60
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Thẻ Gradient Xanh Dương Tổng Kết Điểm & XP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BannerRoyalBlue, BannerElectricBlue)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 26.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    ResultTrophyIcon(modifier = Modifier.size(32.dp), color = TrophyGold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HOÀN THÀNH BÀI THI!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.4.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = result.quizTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 26.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${result.score} ĐIỂM",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = ScoreYellow,
                        letterSpacing = 0.6.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nhãn phần thưởng XP
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(XpBadgeBg)
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+${result.earnedXp} XP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = XpTextCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Thẻ Trắng Đánh Giá Hiệu Suất & Thống Kê Chi Tiết
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccuracyDonutRing(
                        percent = result.accuracyPercent,
                        modifier = Modifier.size(76.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ĐÁNH GIÁ HIỆU SUẤT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.performanceTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.performanceSubtitle,
                            fontSize = 12.5.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryBox(
                        label = "Thời gian",
                        value = formattedTime,
                        bgColor = StatTimeBg,
                        labelColor = TextMuted,
                        valueColor = TextDark,
                        modifier = Modifier.weight(1f)
                    )

                    StatSummaryBox(
                        label = "Câu đúng",
                        value = "${result.correctCount}/${result.totalQuestions}",
                        bgColor = StatCorrectBg,
                        labelColor = StatCorrectLabel,
                        valueColor = StatCorrectValue,
                        modifier = Modifier.weight(1f)
                    )

                    StatSummaryBox(
                        label = "Câu sai",
                        value = "${result.wrongCount}/${result.totalQuestions}",
                        bgColor = StatWrongBg,
                        labelColor = StatWrongLabel,
                        valueColor = StatWrongValue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Nút chính: Xem chi tiết đáp án
        Button(
            onClick = onViewDetails,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryActionBlue,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Xem chi tiết đáp án",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Hàng 2 nút phụ: Thi lại bộ đề & Về Trang chủ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onRetryQuiz,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetryOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Thi lại bộ đề",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onBackHome,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, OutlineBorderGray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TextDark
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Về Trang chủ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AccuracyDonutRing(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val sweepAngle = (percent.coerceIn(0, 100) / 100f) * 360f
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            drawCircle(
                color = TrackLightGray,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = EmeraldRingGreen,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$percent%",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = TextDark
        )
    }
}

@Composable
private fun StatSummaryBox(
    label: String,
    value: String,
    bgColor: Color,
    labelColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@Composable
private fun ResultTrophyIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cup = Path().apply {
            moveTo(w * 0.24f, h * 0.14f)
            lineTo(w * 0.76f, h * 0.14f)
            lineTo(w * 0.68f, h * 0.54f)
            quadraticTo(w * 0.50f, h * 0.64f, w * 0.32f, h * 0.54f)
            close()
        }
        drawPath(path = cup, color = color)

        drawArc(
            color = color,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.20f),
            size = Size(w * 0.22f, h * 0.26f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.68f, h * 0.20f),
            size = Size(w * 0.22f, h * 0.26f),
            style = Stroke(width = 2.dp.toPx())
        )

        drawLine(color, Offset(w * 0.50f, h * 0.58f), Offset(w * 0.50f, h * 0.80f), 2.5.dp.toPx())
        drawLine(color, Offset(w * 0.28f, h * 0.84f), Offset(w * 0.72f, h * 0.84f), 3.dp.toPx(), StrokeCap.Round)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KetQuaScreenPreview() {
    MaterialTheme {
        KetQuaContent(
            result = QuizResultResponse(
                attemptId = "attempt_preview_1",
                quizId = 1,
                quizTitle = "Công nghệ & AI – Bộ đề #1",
                score = 700,
                earnedXp = 120,
                accuracyPercent = 80,
                performanceTitle = "Tốc độ & Chính xác Tuyệt vời!",
                performanceSubtitle = "Bạn nhanh hơn 84% người chơi cùng chủ đề.",
                durationSeconds = 16,
                correctCount = 4,
                wrongCount = 1,
                totalQuestions = 5
            ),
            onViewDetails = {},
            onRetryQuiz = {},
            onBackHome = {}
        )
    }
}