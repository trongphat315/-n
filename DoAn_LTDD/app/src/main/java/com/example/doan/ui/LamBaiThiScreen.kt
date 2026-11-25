package com.example.doan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.viewModel.LamBaiThiUiState
import com.example.doan.viewModel.LamBaiThiViewModel

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val ScreenBgColor = Color(red = 246, green = 247, blue = 251)
private val RoyalBlue = Color(red = 55, green = 65, blue = 178)
private val RingBlue = Color(red = 66, green = 133, blue = 244)
private val LinkBlue = Color(red = 37, green = 99, blue = 235)
private val SoftBlueBg = Color(red = 238, green = 242, blue = 255)
private val SoftBlueBorder = Color(red = 199, green = 210, blue = 254)
private val VictoryOrange = Color(red = 245, green = 130, blue = 11)
private val SoftOrangeBorder = Color(red = 253, green = 224, blue = 171)
private val EmeraldGreen = Color(red = 16, green = 185, blue = 129)
private val SoftGreenBg = Color(red = 220, green = 252, blue = 231)
private val SoftRedBg = Color(red = 254, green = 226, blue = 226)
private val ErrorRed = Color(red = 220, green = 38, blue = 38)
private val TextDark = Color(red = 30, green = 41, blue = 59)
private val TextMuted = Color(red = 148, green = 163, blue = 184)
private val OptionBadgeBg = Color(red = 241, green = 245, blue = 249)
private val TrackInactiveGray = Color(red = 226, green = 232, blue = 240)

@Composable
fun LamBaiThiScreen(
    quizId: Int = 1,
    quizTitle: String = "Công nghệ & AI",
    quizViewModel: LamBaiThiViewModel = viewModel(),
    onExitQuiz: () -> Unit = {},
    onQuizCompleted: (attemptId: String) -> Unit = {}
) {
    val state by quizViewModel.uiState.collectAsState()

    LaunchedEffect(quizId) {
        quizViewModel.startQuiz(quizId, quizTitle)
    }

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            onQuizCompleted(state.submittedAttemptId ?: "attempt_default")
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
                    color = ErrorRed,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onExitQuiz) {
                        Text(text = "Quay lại", color = TextDark)
                    }
                    Button(
                        onClick = { quizViewModel.startQuiz(quizId, quizTitle, forceReload = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text(text = "Thử lại", color = Color.White)
                    }
                }
            }
        }
        else -> {
            LamBaiThiContent(
                state = state,
                onExitClick = onExitQuiz,
                onOptionClick = quizViewModel::selectAnswer,
                onUse5050 = quizViewModel::useFiftyFifty,
                onUseSkip = quizViewModel::useSkipQuestion
            )
        }
    }
}

@Composable
private fun LamBaiThiContent(
    state: LamBaiThiUiState,
    onExitClick: () -> Unit,
    onOptionClick: (Int) -> Unit,
    onUse5050: () -> Unit,
    onUseSkip: () -> Unit
) {
    val question = state.currentQuestion
    val optionLabels = listOf("A", "B", "C", "D")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        QuizTopHeader(
            timeLeft = state.timeLeft,
            maxTime = state.maxTime,
            score = state.score,
            onExitClick = onExitClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        QuizProgressCard(
            currentNumber = state.currentIndex + 1,
            totalNumber = state.questions.size.coerceAtLeast(1),
            totalElapsedSeconds = state.totalElapsedSeconds,
            topicTitle = state.topicTitle
        )

        Spacer(modifier = Modifier.height(22.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (question != null) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftBlueBg)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "CÂU HỎI TRẮC NGHIỆM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalBlue,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = question.content,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            lineHeight = 25.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    question.options.forEachIndexed { index, optionText ->
                        val isHidden = index in state.hiddenOptions
                        val isSelected = state.selectedOptionIndex == index
                        val isCorrect = index == question.correctIndex

                        val (cardBg, borderStroke, badgeBg, badgeTextColor) = when {
                            state.isAnswerRevealed && isCorrect -> OptionCardStyle(
                                SoftGreenBg, BorderStroke(1.5.dp, EmeraldGreen), EmeraldGreen, Color.White
                            )
                            state.isAnswerRevealed && isSelected && !isCorrect -> OptionCardStyle(
                                SoftRedBg, BorderStroke(1.5.dp, ErrorRed), ErrorRed, Color.White
                            )
                            else -> OptionCardStyle(
                                Color.White, null, OptionBadgeBg, TextDark
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = borderStroke,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isHidden) 0.25f else 1f)
                                .clickable(enabled = !isHidden && !state.isAnswerRevealed) {
                                    onOptionClick(index)
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(badgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionLabels.getOrElse(index) { "" },
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = badgeTextColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = optionText,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TacticalLifelinesSection(
            used5050 = state.used5050,
            usedSkip = state.usedSkip || !state.hasBackupQuestion,
            onUse5050 = onUse5050,
            onUseSkip = onUseSkip
        )
    }
}

private data class OptionCardStyle(
    val bg: Color,
    val border: BorderStroke?,
    val badgeBg: Color,
    val badgeText: Color
)

@Composable
private fun QuizTopHeader(
    timeLeft: Int,
    maxTime: Int,
    score: Int,
    onExitClick: () -> Unit
) {
    val progress = (timeLeft.toFloat() / maxTime.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val ringColor = if (timeLeft <= 5) ErrorRed else RingBlue

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onExitClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val stroke = 2.dp.toPx()
                    drawLine(TextDark, Offset(0f, 0f), Offset(size.width, size.height), stroke, StrokeCap.Round)
                    drawLine(TextDark, Offset(size.width, 0f), Offset(0f, size.height), stroke, StrokeCap.Round)
                }
            }
        }

        Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 4.dp.toPx()
                drawCircle(color = TrackInactiveGray, style = Stroke(width = stroke))
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${timeLeft}s",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(RoyalBlue)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                text = "$score điểm",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun QuizProgressCard(
    currentNumber: Int,
    totalNumber: Int,
    totalElapsedSeconds: Int,
    topicTitle: String
) {
    val formattedTotalTime = "%02d:%02d".format(totalElapsedSeconds / 60, totalElapsedSeconds % 60)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Câu $currentNumber/$totalNumber",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftBlueBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⏱ $formattedTotalTime",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    }
                }

                Text(
                    text = topicTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LinkBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (step in 1..totalNumber) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (step <= currentNumber) VictoryOrange else TrackInactiveGray)
                    )
                }
            }
        }
    }
}

@Composable
private fun TacticalLifelinesSection(
    used5050: Boolean,
    usedSkip: Boolean,
    onUse5050: () -> Unit,
    onUseSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QUYỀN TRỢ GIÚP CHIẾN THUẬT",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextMuted,
            letterSpacing = 0.6.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LifelineCard(
                title = "50:50",
                subtitle = "Bỏ 2 đáp án",
                accentColor = RoyalBlue,
                borderColor = SoftBlueBorder,
                used = used5050,
                onClick = onUse5050,
                modifier = Modifier.weight(1f)
            )

            LifelineCard(
                title = "Skip ⏭",
                subtitle = "Đổi câu hỏi",
                accentColor = VictoryOrange,
                borderColor = SoftOrangeBorder,
                used = usedSkip,
                onClick = onUseSkip,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LifelineCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    borderColor: Color,
    used: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, if (used) TrackInactiveGray else borderColor),
        modifier = modifier
            .alpha(if (used) 0.45f else 1f)
            .clickable(enabled = !used, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}