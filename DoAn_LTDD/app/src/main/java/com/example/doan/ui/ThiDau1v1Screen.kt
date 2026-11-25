package com.example.doan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.model.BattleQuestionItem
import com.example.doan.model.QuestionDotStatus
import com.example.doan.viewModel.ThiDau1v1UiState
import com.example.doan.viewModel.ThiDau1v1ViewModel

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val ScreenBgColor = Color(red = 246, green = 247, blue = 251)
private val RoyalBlue = Color(red = 53, green = 67, blue = 178)
private val ElectricBlue = Color(red = 47, green = 114, blue = 224)
private val ArenaOrange = Color(red = 245, green = 146, blue = 0)
private val OpponentEmerald = Color(red = 16, green = 185, blue = 129)
private val PlayerScoreYellow = Color(red = 255, green = 222, blue = 46)
private val OpponentScoreMint = Color(red = 52, green = 235, blue = 174)
private val DotGreen = Color(red = 16, green = 196, blue = 116)
private val DotRed = Color(red = 239, green = 68, blue = 68)
private val DotYellowCurrent = Color(red = 250, green = 204, blue = 21)
private val SpeedBannerBg = Color(red = 255, green = 251, blue = 224)
private val SpeedBannerBorder = Color(red = 253, green = 228, blue = 128)
private val SpeedTextBrown = Color(red = 146, green = 64, blue = 14)
private val SpeedHighlightOrange = Color(red = 234, green = 88, blue = 12)
private val CategoryLinkBlue = Color(red = 37, green = 99, blue = 235)
private val TextDark = Color(red = 24, green = 33, blue = 52)
private val OptionCircleBg = Color(red = 241, green = 245, blue = 249)
private val OptionBorderGray = Color(red = 228, green = 233, blue = 242)
private val CorrectOptionBg = Color(red = 232, green = 252, blue = 241)
private val CorrectOptionBorder = Color(red = 34, green = 197, blue = 94)
private val WrongOptionBg = Color(red = 254, green = 236, blue = 236)
private val WrongOptionBorder = Color(red = 239, green = 68, blue = 68)

@Composable
fun ThiDau1v1Screen(
    matchId: String = "match_default",
    matchViewModel: ThiDau1v1ViewModel = viewModel(),
    onMatchCompleted: (attemptId: String) -> Unit = {}
) {
    val state by matchViewModel.uiState.collectAsState()

    LaunchedEffect(matchId) {
        matchViewModel.startMatch(matchId)
    }

    LaunchedEffect(state.isMatchFinished) {
        if (state.isMatchFinished) {
            onMatchCompleted(state.submittedAttemptId ?: "attempt_default")
        }
    }

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBgColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RoyalBlue)
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
                    color = DotRed,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { matchViewModel.startMatch(matchId) },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(text = "Thử lại", color = Color.White)
                }
            }
        }
        state.currentQuestion != null -> {
            ThiDau1v1Content(
                state = state,
                onSelectOption = matchViewModel::selectAnswer
            )
        }
    }
}

@Composable
private fun ThiDau1v1Content(
    state: ThiDau1v1UiState,
    onSelectOption: (Int) -> Unit
) {
    val question = state.currentQuestion ?: return
    val optionLabels = listOf("A", "B", "C", "D")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Bảng tỉ số trực tiếp 1v1 Gradient Xanh Dương
        LiveBattleScoreboard(
            playerName = state.playerName,
            playerInitials = state.playerInitials,
            playerScore = state.playerScore,
            playerDots = state.playerDots,
            opponentName = state.opponentName,
            opponentInitials = state.opponentInitials,
            opponentScore = state.opponentScore,
            opponentDots = state.opponentDots,
            timeLeft = state.timeLeft
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Thanh thông báo thưởng tốc độ dưới 3s
        SpeedBonusBanner(
            thresholdSeconds = state.speedBonusThreshold,
            bonusPoints = state.speedBonusPoints,
            earnedJustNow = state.lastSpeedBonusEarned && state.isAnswerRevealed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Thẻ câu hỏi trung tâm
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CÂU ${state.currentIndex + 1}/${state.questions.size} • ĐẤU TRỰC TIẾP",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalBlue,
                        letterSpacing = 0.3.sp
                    )

                    Text(
                        text = question.category.uppercase(),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CategoryLinkBlue,
                        letterSpacing = 0.3.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = question.content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    lineHeight = 23.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Danh sách 4 thẻ lựa chọn A, B, C, D
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, optionText ->
                val isSelected = state.selectedOptionIndex == index
                val isCorrectOption = index == question.correctIndex

                val cardBg = when {
                    !state.isAnswerRevealed -> Color.White
                    isCorrectOption -> CorrectOptionBg
                    isSelected -> WrongOptionBg
                    else -> Color.White
                }

                val cardBorder = when {
                    !state.isAnswerRevealed -> OptionBorderGray
                    isCorrectOption -> CorrectOptionBorder
                    isSelected -> WrongOptionBorder
                    else -> OptionBorderGray
                }

                val badgeBg = when {
                    !state.isAnswerRevealed -> OptionCircleBg
                    isCorrectOption -> CorrectOptionBorder
                    isSelected -> WrongOptionBorder
                    else -> OptionCircleBg
                }

                val badgeTextColor = when {
                    !state.isAnswerRevealed -> TextDark
                    isCorrectOption || isSelected -> Color.White
                    else -> TextDark
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.2.dp, cardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isAnswerRevealed) {
                            onSelectOption(index)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optionLabels.getOrElse(index) { "" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = badgeTextColor
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = optionText,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LiveBattleScoreboard(
    playerName: String,
    playerInitials: String,
    playerScore: Int,
    playerDots: List<QuestionDotStatus>,
    opponentName: String,
    opponentInitials: String,
    opponentScore: Int,
    opponentDots: List<QuestionDotStatus>,
    timeLeft: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(RoyalBlue, ElectricBlue)
                )
            )
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Cột trái: Bạn
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ArenaOrange)
                        .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playerInitials,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = playerName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "$playerScore",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = PlayerScoreYellow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ProgressDotsRow(dots = playerDots, highlightCurrent = true)
                }
            }

            // Cột giữa: Huy hiệu VS + Đồng hồ tròn 12s
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArenaOrange)
                        .padding(horizontal = 11.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${timeLeft}s",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // Cột phải: Đối thủ (Hoàng Nam)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = opponentName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "$opponentScore",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = OpponentScoreMint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ProgressDotsRow(dots = opponentDots, highlightCurrent = false)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(OpponentEmerald)
                        .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opponentInitials,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressDotsRow(
    dots: List<QuestionDotStatus>,
    highlightCurrent: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        dots.forEach { status ->
            when {
                status == QuestionDotStatus.CORRECT -> {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(DotGreen)
                    )
                }
                status == QuestionDotStatus.WRONG -> {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(DotRed)
                    )
                }
                status == QuestionDotStatus.CURRENT && highlightCurrent -> {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(DotYellowCurrent)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.32f))
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedBonusBanner(
    thresholdSeconds: Int,
    bonusPoints: Int,
    earnedJustNow: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SpeedBannerBg)
            .border(1.dp, SpeedBannerBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpeedLightningIcon(modifier = Modifier.size(15.dp), color = SpeedHighlightOrange)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = buildAnnotatedString {
                if (earnedJustNow) {
                    withStyle(
                        style = SpanStyle(
                            color = DotGreen,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("Tuyệt vời! Bạn vừa nhận +$bonusPoints điểm tốc độ!")
                    }
                } else {
                    withStyle(
                        style = SpanStyle(
                            color = SpeedTextBrown,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("Trả lời dưới ${thresholdSeconds}s nhận ngay ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = SpeedHighlightOrange,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("+$bonusPoints điểm tốc độ!")
                    }
                }
            },
            fontSize = 12.5.sp
        )
    }
}

@Composable
private fun SpeedLightningIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bolt = Path().apply {
            moveTo(w * 0.56f, h * 0.06f)
            lineTo(w * 0.20f, h * 0.54f)
            lineTo(w * 0.48f, h * 0.54f)
            lineTo(w * 0.42f, h * 0.94f)
            lineTo(w * 0.80f, h * 0.44f)
            lineTo(w * 0.52f, h * 0.44f)
            close()
        }
        drawPath(path = bolt, color = color)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ThiDau1v1ScreenPreview() {
    MaterialTheme {
        ThiDau1v1Content(
            state = ThiDau1v1UiState(
                isLoading = false,
                playerScore = 420,
                playerDots = listOf(
                    QuestionDotStatus.CORRECT,
                    QuestionDotStatus.CORRECT,
                    QuestionDotStatus.WRONG,
                    QuestionDotStatus.CURRENT,
                    QuestionDotStatus.PENDING
                ),
                opponentScore = 380,
                opponentDots = listOf(
                    QuestionDotStatus.CORRECT,
                    QuestionDotStatus.WRONG,
                    QuestionDotStatus.CORRECT,
                    QuestionDotStatus.PENDING,
                    QuestionDotStatus.PENDING
                ),
                timeLeft = 12,
                questions = listOf(
                    BattleQuestionItem(
                        id = 1,
                        category = "CÔNG NGHỆ & AI",
                        content = "Thuật ngữ “LLM” trong lĩnh vực Trí tuệ nhân tạo hiện đại là viết tắt của cụm từ nào?",
                        options = listOf(
                            "Large Language Model",
                            "Linear Logic Machine",
                            "Low Latency Memory",
                            "Layered Learning Module"
                        ),
                        correctIndex = 0,
                        explanation = ""
                    )
                )
            ),
            onSelectOption = {}
        )
    }
}