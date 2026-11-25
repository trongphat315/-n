package com.example.doan.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.model.FeaturedTopic
import com.example.doan.model.TopicIconType
import com.example.doan.viewModel.HomeUiState
import com.example.doan.viewModel.HomeViewModel

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val ScreenBgColor = Color(red = 246, green = 247, blue = 251)
private val RoyalBlue = Color(red = 45, green = 62, blue = 168)
private val ElectricBlue = Color(red = 46, green = 114, blue = 216)
private val LinkBlue = Color(red = 37, green = 99, blue = 235)
private val VictoryOrange = Color(red = 245, green = 142, blue = 11)
private val DeepOrange = Color(red = 238, green = 118, blue = 12)
private val CoinPillBg = Color(red = 254, green = 249, blue = 195)
private val GoldBadge = Color(red = 234, green = 179, blue = 8)
private val OnlineGreen = Color(red = 34, green = 197, blue = 94)
private val TextDark = Color(red = 30, green = 41, blue = 59)
private val TextMuted = Color(red = 100, green = 116, blue = 139)
private val SoftPurpleBg = Color(red = 238, green = 242, blue = 255)
private val SoftOrangeBg = Color(red = 255, green = 247, blue = 237)
private val SoftYellowBg = Color(red = 254, green = 252, blue = 232)
private val SoftGreenBg = Color(red = 236, green = 253, blue = 245)
private val MintGreen = Color(red = 16, green = 185, blue = 129)
private val SpacePurple = Color(red = 124, green = 58, blue = 237)
private val TrackGray = Color(red = 226, green = 232, blue = 240)

@Composable
fun TrangChuScreen(
    homeViewModel: HomeViewModel = viewModel(),
    initialUserName: String = "Minh Tuấn",
    onNavigateTopics: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateLeaderboard: () -> Unit = {},
    onNavigateFriends: () -> Unit = {},
    onNavigateBattle1v1: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onStartTopicQuiz: (FeaturedTopic) -> Unit = {}
) {
    val state by homeViewModel.uiState.collectAsState()

    LaunchedEffect(initialUserName) {
        homeViewModel.setUserName(initialUserName)
    }

    Scaffold(
        containerColor = ScreenBgColor,
        bottomBar = {
            HomeBottomNavigationBar(
                selectedTab = state.selectedBottomTab,
                onTabSelected = { index ->
                    homeViewModel.selectBottomTab(index)
                    when (index) {
                        1 -> onNavigateTopics()
                        2 -> onNavigateBattle1v1()
                        3 -> onNavigateLeaderboard()
                        4 -> onNavigateProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Thanh chào người dùng, số Xu và Chuông thông báo
            HomeTopBar(
                state = state,
                onBellClick = homeViewModel::clearNotificationBadge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Banner Giải đấu tốc độ CPN Cup
            EventCupBanner(onJoinClick = onNavigateBattle1v1)

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Thẻ Thách đấu nhanh 1v1
            QuickBattleCard(
                onlinePlayers = state.onlinePlayers,
                onClick = onNavigateBattle1v1
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Lối tắt chức năng (4 ô)
            Text(
                text = "LỐI TẮT CHỨC NĂNG",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            ShortcutRow(
                onTopicsClick = onNavigateTopics,
                onHistoryClick = onNavigateHistory,
                onLeaderboardClick = onNavigateLeaderboard,
                onFriendsClick = onNavigateFriends
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Chủ đề nổi bật
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHỦ ĐỀ NỔI BẬT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Xem tất cả",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LinkBlue,
                    modifier = Modifier.clickable(onClick = onNavigateTopics)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            state.featuredTopics.forEach { topic ->
                FeaturedTopicCard(
                    topic = topic,
                    onPlayClick = { onStartTopicQuiz(topic) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HomeTopBar(state: HomeUiState, onBellClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + Huy hiệu cấp độ Lv.14
        Box(contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RoyalBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.avatarInitials,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VictoryOrange)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "Lv.${state.level}",
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Lời chào
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Xin chào,", fontSize = 12.sp, color = TextMuted)
            Text(
                text = "${state.userName}!",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }

        // Thẻ hiển thị số Xu
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CoinPillBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(GoldBadge),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "★", color = Color.White, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "%,d".format(state.coins),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.5.sp,
                color = TextDark
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Nút chuông thông báo
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable(onClick = onBellClick),
            contentAlignment = Alignment.Center
        ) {
            BellIcon(modifier = Modifier.size(20.dp), color = TextDark)
            if (state.hasUnreadNotification) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
            }
        }
    }
}

@Composable
private fun EventCupBanner(onJoinClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(RoyalBlue, ElectricBlue)
                )
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Nhãn Sự kiện mùa 4
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrophyIcon(modifier = Modifier.size(12.dp), color = GoldBadge)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "SỰ KIỆN MÙA 4",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "GIẢI ĐẤU TỐC ĐỘ CPN CUP",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tranh tài đỉnh cao, rinh ngay huy hiệu Vàng & 10,000 Xu thưởng!",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onJoinClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = RoyalBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Tham gia ngay →",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Vòng tròn chiếc cúp vàng bên phải
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrophyIcon(modifier = Modifier.size(38.dp), color = GoldBadge)
            }
        }
    }
}

@Composable
private fun QuickBattleCard(onlinePlayers: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(VictoryOrange, DeepOrange)
                )
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Số người trực tuyến
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "%,d người đang trực tuyến".format(onlinePlayers),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BoltIcon(modifier = Modifier.size(18.dp), color = GoldBadge)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "THÁCH ĐẤU NHANH 1V1",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ghép trận 3 giây • Đấu trí thời gian thực",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }

            // Nút tia sét bên phải
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                BoltIcon(modifier = Modifier.size(24.dp), color = Color.White)
            }
        }
    }
}

@Composable
private fun ShortcutRow(
    onTopicsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onFriendsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShortcutCard(
            title = "Chủ đề",
            iconBg = SoftPurpleBg,
            modifier = Modifier.weight(1f),
            onClick = onTopicsClick
        ) {
            GridIcon(modifier = Modifier.size(20.dp), color = RoyalBlue)
        }

        ShortcutCard(
            title = "Lịch sử",
            iconBg = SoftOrangeBg,
            modifier = Modifier.weight(1f),
            onClick = onHistoryClick
        ) {
            ClockIcon(modifier = Modifier.size(20.dp), color = VictoryOrange)
        }

        ShortcutCard(
            title = "Xếp hạng",
            iconBg = SoftYellowBg,
            modifier = Modifier.weight(1f),
            onClick = onLeaderboardClick
        ) {
            TrophyIcon(modifier = Modifier.size(20.dp), color = GoldBadge)
        }

        ShortcutCard(
            title = "Bạn bè",
            iconBg = SoftGreenBg,
            modifier = Modifier.weight(1f),
            onClick = onFriendsClick
        ) {
            FriendsIcon(modifier = Modifier.size(20.dp), color = MintGreen)
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
        }
    }
}

@Composable
private fun FeaturedTopicCard(topic: FeaturedTopic, onPlayClick: () -> Unit) {
    val badgeColor = when (topic.iconType) {
        TopicIconType.TECH -> ElectricBlue
        TopicIconType.HISTORY -> VictoryOrange
        TopicIconType.SPACE -> SpacePurple
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Biểu tượng chủ đề
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                when (topic.iconType) {
                    TopicIconType.TECH -> ChipIcon(modifier = Modifier.size(22.dp))
                    TopicIconType.HISTORY -> TempleIcon(modifier = Modifier.size(22.dp))
                    TopicIconType.SPACE -> RocketIcon(modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${topic.questionCount} câu hỏi • Hoàn thành ${topic.completionPercent}%",
                    fontSize = 11.5.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(TrackGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(topic.completionPercent / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(ElectricBlue)
                    )
                }
            }

            // Nút Chơi ngay
            Button(
                onClick = onPlayClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "Chơi ngay",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HomeBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomNavItem(
                label = "Trang chủ",
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            ) { color -> HomeIcon(modifier = Modifier.size(20.dp), color = color) }

            BottomNavItem(
                label = "Chủ đề",
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            ) { color -> GridIcon(modifier = Modifier.size(20.dp), color = color) }

            // Nút Đấu 1v1 nổi bật ở giữa
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .clickable { onTabSelected(2) }
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(VictoryOrange),
                    contentAlignment = Alignment.Center
                ) {
                    BoltIcon(modifier = Modifier.size(22.dp), color = Color.White)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Đấu 1v1",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 2) VictoryOrange else TextMuted
                )
            }

            BottomNavItem(
                label = "Xếp hạng",
                selected = selectedTab == 3,
                onClick = { onTabSelected(3) }
            ) { color -> TrophyIcon(modifier = Modifier.size(20.dp), color = color) }

            BottomNavItem(
                label = "Cá nhân",
                selected = selectedTab == 4,
                onClick = { onTabSelected(4) }
            ) { color -> ProfileIcon(modifier = Modifier.size(20.dp), color = color) }
        }
    }
}

@Composable
private fun BottomNavItem(
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

// --- Bộ biểu tượng vẽ nhẹ bằng Canvas (không cần thư viện ngoài) ---

@Composable
private fun BoltIcon(modifier: Modifier = Modifier, color: Color) {
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
private fun TrophyIcon(modifier: Modifier = Modifier, color: Color) {
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
        drawLine(color, Offset(w * 0.50f, h * 0.54f), Offset(w * 0.50f, h * 0.78f), 2.5.dp.toPx())
        drawLine(color, Offset(w * 0.30f, h * 0.82f), Offset(w * 0.70f, h * 0.82f), 3.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun GridIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val box = size.width * 0.36f
        val gap = size.width * 0.14f
        val stroke = Stroke(width = 1.6.dp.toPx())
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
private fun ClockIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = color, radius = size.minDimension * 0.40f, style = Stroke(1.7.dp.toPx()))
        drawLine(color, c, Offset(c.x, size.height * 0.28f), 1.7.dp.toPx(), StrokeCap.Round)
        drawLine(color, c, Offset(size.width * 0.68f, c.y), 1.7.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun FriendsIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color, w * 0.16f, Offset(w * 0.38f, h * 0.34f), style = Stroke(1.6.dp.toPx()))
        drawArc(color, 180f, 180f, false, Offset(w * 0.14f, h * 0.56f), Size(w * 0.48f, h * 0.44f), style = Stroke(1.6.dp.toPx()))
        drawCircle(color, w * 0.13f, Offset(w * 0.70f, h * 0.38f), style = Stroke(1.5.dp.toPx()))
    }
}

@Composable
private fun BellIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bell = Path().apply {
            moveTo(w * 0.22f, h * 0.72f)
            lineTo(w * 0.78f, h * 0.72f)
            lineTo(w * 0.70f, h * 0.36f)
            quadraticTo(w * 0.50f, h * 0.14f, w * 0.30f, h * 0.36f)
            close()
        }
        drawPath(bell, color, style = Stroke(1.6.dp.toPx()))
        drawCircle(color, w * 0.08f, Offset(w * 0.50f, h * 0.82f))
    }
}

@Composable
private fun HomeIcon(modifier: Modifier = Modifier, color: Color) {
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
private fun ProfileIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color, w * 0.18f, Offset(w * 0.50f, h * 0.34f), style = Stroke(1.7.dp.toPx()))
        drawArc(color, 180f, 180f, false, Offset(w * 0.22f, h * 0.58f), Size(w * 0.56f, h * 0.44f), style = Stroke(1.7.dp.toPx()))
    }
}

@Composable
private fun ChipIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRoundRect(Color.White, Offset(size.width * 0.24f, size.height * 0.24f), Size(size.width * 0.52f, size.height * 0.52f), CornerRadius(3.dp.toPx()), style = Stroke(1.8.dp.toPx()))
    }
}

@Composable
private fun TempleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val roof = Path().apply {
            moveTo(w * 0.16f, h * 0.40f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.84f, h * 0.40f)
            close()
        }
        drawPath(roof, Color.White)
        drawLine(Color.White, Offset(w * 0.20f, h * 0.80f), Offset(w * 0.80f, h * 0.80f), 2.dp.toPx())
    }
}

@Composable
private fun RocketIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(w * 0.76f, h * 0.24f)
            lineTo(w * 0.46f, h * 0.74f)
            lineTo(w * 0.26f, h * 0.54f)
            close()
        }
        drawPath(body, Color.White)
    }
}