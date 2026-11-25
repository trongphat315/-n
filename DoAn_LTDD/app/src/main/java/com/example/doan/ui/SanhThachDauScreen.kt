package com.example.doan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.model.BattleFriendItem
import com.example.doan.viewModel.SanhThachDauUiState
import com.example.doan.viewModel.SanhThachDauViewModel
import java.text.NumberFormat
import java.util.Locale

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val ScreenBgColor = Color(red = 246, green = 247, blue = 251)
private val RoyalBlue = Color(red = 53, green = 67, blue = 178)
private val ElectricBlue = Color(red = 45, green = 85, blue = 198)
private val ArenaOrange = Color(red = 245, green = 146, blue = 0)
private val GoldText = Color(red = 255, green = 214, blue = 36)
private val OnlinePillBg = Color(red = 230, green = 250, blue = 240)
private val OnlineGreen = Color(red = 16, green = 185, blue = 105)
private val TextDark = Color(red = 24, green = 33, blue = 52)
private val TextMuted = Color(red = 136, green = 148, blue = 168)
private val InstantBtnBg = Color(red = 241, green = 245, blue = 255)
private val RoomCodePillBg = Color(red = 243, green = 245, blue = 251)
private val AvatarBlue = Color(red = 99, green = 132, blue = 255)
private val AvatarEmerald = Color(red = 16, green = 185, blue = 129)
private val NavInactiveGray = Color(red = 148, green = 163, blue = 184)
private val DividerGray = Color(red = 232, green = 236, blue = 244)

@Composable
fun SanhThachDauScreen(
    battleViewModel: SanhThachDauViewModel = viewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateTopics: () -> Unit = {},
    onNavigateLeaderboard: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onMatchFound: (matchId: String) -> Unit = {}
) {
    val state by battleViewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(state.matchedMatchId) {
        val matchId = state.matchedMatchId
        if (!matchId.isNullOrBlank()) {
            battleViewModel.consumeMatchNavigation()
            onMatchFound(matchId)
        }
    }

    SanhThachDauContent(
        state = state,
        onSelectTopic = battleViewModel::selectTopic,
        onToggleRandomMatch = battleViewModel::toggleRandomMatchmaking,
        onInstantBattle = battleViewModel::startInstantMatch,
        onCopyRoomCode = {
            clipboardManager.setText(AnnotatedString(state.roomCode))
            battleViewModel.showCopiedNotice()
        },
        onInviteFriend = battleViewModel::inviteFriend,
        onRetryLoad = battleViewModel::loadLobby,
        onNavigateHome = onNavigateHome,
        onNavigateTopics = onNavigateTopics,
        onNavigateLeaderboard = onNavigateLeaderboard,
        onNavigateProfile = onNavigateProfile
    )
}

@Composable
private fun SanhThachDauContent(
    state: SanhThachDauUiState,
    onSelectTopic: (String) -> Unit,
    onToggleRandomMatch: () -> Unit,
    onInstantBattle: () -> Unit,
    onCopyRoomCode: () -> Unit,
    onInviteFriend: (BattleFriendItem) -> Unit,
    onRetryLoad: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateTopics: () -> Unit,
    onNavigateLeaderboard: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    Scaffold(
        containerColor = ScreenBgColor,
        bottomBar = {
            BattleBottomNavBar(
                onNavigateHome = onNavigateHome,
                onNavigateTopics = onNavigateTopics,
                onNavigateLeaderboard = onNavigateLeaderboard,
                onNavigateProfile = onNavigateProfile
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RoyalBlue)
            }
            return@Scaffold
        }

        if (state.errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.errorMessage,
                    color = Color(red = 200, green = 40, blue = 40),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onRetryLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(text = "Thử lại", color = Color.White)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Tiêu đề Sảnh Thách đấu 1v1 + Nhãn số người trực tuyến
            LobbyTopHeader(onlineCount = state.onlineCount)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Thẻ Đấu trường VS Gradient Xanh Dương
            VsArenaBannerCard(
                playerName = state.playerName,
                playerInitials = state.playerInitials,
                playerLevel = state.playerLevel,
                questionCount = state.questionCount,
                secondsPerQuestion = state.secondsPerQuestion,
                isSearching = state.isSearchingMatch
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Mục Chủ đề thi đấu
            Text(
                text = "Chủ đề thi đấu",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.topics.forEach { topic ->
                    val isSelected = topic == state.selectedTopic
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) RoyalBlue else Color.White)
                            .clickable { onSelectTopic(topic) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = topic,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else TextDark
                        )
                    }
                }
            }

            // Thông báo trạng thái nhanh (Copy mã / Đang ghép cặp)
            if (!state.bannerNotice.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnlinePillBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = state.bannerNotice,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnlineGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Thẻ Tìm trận ngẫu nhiên
            RandomMatchCard(
                isSearching = state.isSearchingMatch,
                onToggleRandomMatch = onToggleRandomMatch,
                onInstantBattle = onInstantBattle
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Thẻ Tạo phòng mời bạn bè & Danh sách bạn bè
            InviteFriendsCard(
                roomCode = state.roomCode,
                friends = state.friends,
                invitingFriendId = state.invitingFriendId,
                onCopyRoomCode = onCopyRoomCode,
                onInviteFriend = onInviteFriend
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LobbyTopHeader(onlineCount: Int) {
    val formattedOnline = NumberFormat.getNumberInstance(Locale.US).format(onlineCount)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sảnh Thách đấu 1v1",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Đấu trí đối kháng trực tiếp theo thời gian thực",
                fontSize = 12.5.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(OnlinePillBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
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
                text = "$formattedOnline Online",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnlineGreen
            )
        }
    }
}

@Composable
private fun VsArenaBannerCard(
    playerName: String,
    playerInitials: String,
    playerLevel: Int,
    questionCount: Int,
    secondsPerQuestion: Int,
    isSearching: Boolean
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
            .padding(horizontal = 16.dp, vertical = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Người chơi hiện tại (Bên trái)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ArenaOrange)
                        .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playerInitials,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = playerName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Level $playerLevel • Bạn",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Huy hiệu VS ở giữa
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(ArenaOrange)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "$questionCount Câu • ${secondsPerQuestion}s",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldText
                )
            }

            // Ô chờ đối thủ viền nét đứt (Bên phải)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.12f),
                            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.55f),
                            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                            style = Stroke(
                                width = 1.6.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                            )
                        )
                    }

                    if (isSearching) {
                        CircularProgressIndicator(
                            color = GoldText,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Text(
                            text = "?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isSearching) "Đang tìm..." else "Chờ đối thủ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Xứng tầm Lv.$playerLevel",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun RandomMatchCard(
    isSearching: Boolean,
    onToggleRandomMatch: () -> Unit,
    onInstantBattle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tìm trận ngẫu nhiên",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Ghép cặp tự động với người chơi cùng hạng",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                LightningBoltIcon(
                    modifier = Modifier.size(20.dp),
                    color = ArenaOrange
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onToggleRandomMatch,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSearching) RoyalBlue else ArenaOrange,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1.55f)
                        .height(46.dp)
                ) {
                    Text(
                        text = if (isSearching) "HỦY TÌM TRẬN" else "TÌM TRẬN NGẪU NHIÊN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onInstantBattle,
                    enabled = !isSearching,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InstantBtnBg,
                        contentColor = RoyalBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text(
                        text = "Đấu ngay →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteFriendsCard(
    roomCode: String,
    friends: List<BattleFriendItem>,
    invitingFriendId: String?,
    onCopyRoomCode: () -> Unit,
    onInviteFriend: (BattleFriendItem) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tạo phòng mời bạn bè",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Chia sẻ mã phòng hoặc mời trực tiếp",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Huy hiệu mã phòng #CPN-889 Copy
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoomCodePillBg)
                        .clickable(onClick = onCopyRoomCode)
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = roomCode,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArenaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                friends.forEach { friend ->
                    FriendChallengeRow(
                        friend = friend,
                        isInviting = invitingFriendId == friend.userId,
                        onInvite = { onInviteFriend(friend) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendChallengeRow(
    friend: BattleFriendItem,
    isInviting: Boolean,
    onInvite: () -> Unit
) {
    val avatarBg = when (friend.avatarColorType) {
        "ORANGE" -> ArenaOrange
        "BLUE" -> AvatarBlue
        else -> AvatarEmerald
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ảnh đại diện chữ cái kèm chấm xanh trực tuyến
        Box(modifier = Modifier.size(44.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.initials,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            if (friend.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(OnlineGreen)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.fullName,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Lv.${friend.level} • ${friend.statusText}",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        Button(
            onClick = onInvite,
            enabled = !isInviting,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RoyalBlue,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = if (isInviting) "Đang mời..." else "Mời đấu",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun BattleBottomNavBar(
    onNavigateHome: () -> Unit,
    onNavigateTopics: () -> Unit,
    onNavigateLeaderboard: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            HorizontalDivider(color = DividerGray, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Trang chủ
                BottomNavTextTab(
                    label = "Trang chủ",
                    isActive = false,
                    onClick = onNavigateHome,
                    modifier = Modifier.weight(1f),
                    icon = { HomeNavIcon(modifier = Modifier.size(20.dp), color = NavInactiveGray) }
                )

                // Tab 2: Chủ đề
                BottomNavTextTab(
                    label = "Chủ đề",
                    isActive = false,
                    onClick = onNavigateTopics,
                    modifier = Modifier.weight(1f),
                    icon = { TopicsGridNavIcon(modifier = Modifier.size(20.dp), color = NavInactiveGray) }
                )

                // Tab 3 (Ở giữa): Đấu 1v1
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ArenaOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        LightningBoltIcon(modifier = Modifier.size(22.dp), color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Đấu 1v1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArenaOrange
                    )
                }

                // Tab 4: Xếp hạng
                BottomNavTextTab(
                    label = "Xếp hạng",
                    isActive = false,
                    onClick = onNavigateLeaderboard,
                    modifier = Modifier.weight(1f),
                    icon = { TrophyNavIcon(modifier = Modifier.size(20.dp), color = NavInactiveGray) }
                )

                // Tab 5: Cá nhân
                BottomNavTextTab(
                    label = "Cá nhân",
                    isActive = false,
                    onClick = onNavigateProfile,
                    modifier = Modifier.weight(1f),
                    icon = { ProfileNavIcon(modifier = Modifier.size(20.dp), color = NavInactiveGray) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavTextTab(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val contentColor = if (isActive) RoyalBlue else NavInactiveGray
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun LightningBoltIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bolt = Path().apply {
            moveTo(w * 0.56f, h * 0.08f)
            lineTo(w * 0.22f, h * 0.54f)
            lineTo(w * 0.48f, h * 0.54f)
            lineTo(w * 0.42f, h * 0.92f)
            lineTo(w * 0.78f, h * 0.44f)
            lineTo(w * 0.52f, h * 0.44f)
            close()
        }
        drawPath(path = bolt, color = color)
    }
}

@Composable
private fun HomeNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.6.dp.toPx()
        val roof = Path().apply {
            moveTo(w * 0.14f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.86f, h * 0.48f)
        }
        drawPath(roof, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.24f, h * 0.44f),
            size = Size(w * 0.52f, h * 0.42f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(stroke)
        )
    }
}

@Composable
private fun TopicsGridNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.6.dp.toPx()
        val boxSize = Size(w * 0.30f, h * 0.30f)
        val r = CornerRadius(2.dp.toPx())
        drawRoundRect(color, Offset(w * 0.14f, h * 0.14f), boxSize, r, style = Stroke(stroke))
        drawRoundRect(color, Offset(w * 0.56f, h * 0.14f), boxSize, r, style = Stroke(stroke))
        drawRoundRect(color, Offset(w * 0.14f, h * 0.56f), boxSize, r, style = Stroke(stroke))
        drawRoundRect(color, Offset(w * 0.56f, h * 0.56f), boxSize, r, style = Stroke(stroke))
    }
}

@Composable
private fun TrophyNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.6.dp.toPx()
        val cup = Path().apply {
            moveTo(w * 0.26f, h * 0.18f)
            lineTo(w * 0.74f, h * 0.18f)
            lineTo(w * 0.66f, h * 0.54f)
            quadraticTo(w * 0.50f, h * 0.64f, w * 0.34f, h * 0.54f)
            close()
        }
        drawPath(cup, color, style = Stroke(stroke, join = StrokeJoin.Round))
        drawLine(color, Offset(w * 0.50f, h * 0.60f), Offset(w * 0.50f, h * 0.82f), stroke)
        drawLine(color, Offset(w * 0.30f, h * 0.84f), Offset(w * 0.70f, h * 0.84f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun ProfileNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.6.dp.toPx()
        drawCircle(color, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.32f), style = Stroke(stroke))
        val shoulders = Path().apply {
            moveTo(w * 0.20f, h * 0.84f)
            quadraticTo(w * 0.22f, h * 0.60f, w * 0.50f, h * 0.60f)
            quadraticTo(w * 0.78f, h * 0.60f, w * 0.80f, h * 0.84f)
        }
        drawPath(shoulders, color, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SanhThachDauScreenPreview() {
    MaterialTheme {
        SanhThachDauContent(
            state = SanhThachDauUiState(
                isLoading = false,
                friends = listOf(
                    BattleFriendItem("1", "HN", "Trần Hoàng Nam", 18, "Trực tuyến", true, "ORANGE"),
                    BattleFriendItem("2", "LC", "Nguyễn Linh Chi", 17, "Đang thi đấu", true, "BLUE"),
                    BattleFriendItem("3", "QB", "Lê Quốc Bảo", 16, "Trực tuyến", true, "GREEN")
                )
            ),
            onSelectTopic = {},
            onToggleRandomMatch = {},
            onInstantBattle = {},
            onCopyRoomCode = {},
            onInviteFriend = {},
            onRetryLoad = {},
            onNavigateHome = {},
            onNavigateTopics = {},
            onNavigateLeaderboard = {},
            onNavigateProfile = {}
        )
    }
}