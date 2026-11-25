package com.example.doan.navigation

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.doan.api.RetrofitClient
import com.example.doan.ui.ChiTietLichSuScreen
import com.example.doan.ui.DanhSachChuDeScreen
import com.example.doan.ui.KetQuaScreen
import com.example.doan.ui.LamBaiThiScreen
import com.example.doan.ui.LichSuScreen
import com.example.doan.ui.SanhThachDauScreen
import com.example.doan.ui.ThiDau1v1Screen
import com.example.doan.ui.TrangChuScreen
import com.example.doan.ui.XacThucScreen

private val RoyalBlue = Color(red = 45, green = 62, blue = 168)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.XacThuc.route
    ) {
        // MH_01: Xác thực
        composable(route = Screen.XacThuc.route) {
            XacThucScreen(
                onLoginSuccess = { user ->
                    RetrofitClient.authToken = user.token
                    RetrofitClient.currentUserId = user.userId
                    val encodedName = Uri.encode(user.displayName)
                    navController.navigate(Screen.TrangChu.createRoute(encodedName)) {
                        popUpTo(Screen.XacThuc.route) { inclusive = true }
                    }
                }
            )
        }

        // MH_02: Trang chủ
        composable(
            route = Screen.TrangChu.route,
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "Minh Tuấn"
            TrangChuScreen(
                initialUserName = userName,
                onNavigateTopics = { navController.navigate(Screen.DanhSachChuDe.route) },
                onNavigateHistory = { navController.navigate(Screen.LichSu.route) },
                onNavigateLeaderboard = { navController.navigate(Screen.BangXepHang.route) },
                onNavigateFriends = { navController.navigate(Screen.BanBe.route) },
                onNavigateBattle1v1 = { navController.navigate(Screen.SanhThachDau.route) },
                onNavigateProfile = { navController.navigate(Screen.CaNhan.route) },
                onStartTopicQuiz = { topic ->
                    val encodedTitle = Uri.encode(topic.title)
                    navController.navigate(Screen.LamBaiThi.createRoute(topic.id, encodedTitle))
                }
            )
        }

        // MH_03: Danh sách chủ đề
        composable(route = Screen.DanhSachChuDe.route) {
            DanhSachChuDeScreen(
                onNavigateHome = { navController.popBackStack() },
                onNavigateBattle1v1 = { navController.navigate(Screen.SanhThachDau.route) },
                onNavigateLeaderboard = { navController.navigate(Screen.BangXepHang.route) },
                onNavigateProfile = { navController.navigate(Screen.CaNhan.route) },
                onStartQuizSet = { quizSet ->
                    val encodedTitle = Uri.encode(quizSet.title)
                    navController.navigate(Screen.LamBaiThi.createRoute(quizSet.id, encodedTitle))
                }
            )
        }

        // MH_04: Làm bài thi
        composable(
            route = Screen.LamBaiThi.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.IntType },
                navArgument("quizTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getInt("quizId") ?: 1
            val quizTitle = backStackEntry.arguments?.getString("quizTitle") ?: "Công nghệ & AI"
            LamBaiThiScreen(
                quizId = quizId,
                quizTitle = quizTitle,
                onExitQuiz = { navController.popBackStack() },
                onQuizCompleted = { attemptId ->
                    navController.navigate(Screen.KetQua.createRoute(attemptId)) {
                        popUpTo(Screen.LamBaiThi.route) { inclusive = true }
                    }
                }
            )
        }

        // MH_05: Kết quả bài thi
        composable(
            route = Screen.KetQua.route,
            arguments = listOf(navArgument("attemptId") { type = NavType.StringType })
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString("attemptId") ?: "attempt_default"
            KetQuaScreen(
                attemptId = attemptId,
                onViewDetails = { id ->
                    navController.navigate(Screen.ChiTietLichSu.createRoute(id))
                },
                onRetryQuiz = { quizId, quizTitle ->
                    val encodedTitle = Uri.encode(quizTitle)
                    navController.navigate(Screen.LamBaiThi.createRoute(quizId, encodedTitle)) {
                        popUpTo(Screen.KetQua.route) { inclusive = true }
                    }
                },
                onBackHome = {
                    navController.popBackStack(Screen.TrangChu.route, inclusive = false)
                }
            )
        }

        // MH_06: Lịch sử làm bài
        composable(route = Screen.LichSu.route) {
            LichSuScreen(
                onBack = { navController.popBackStack() },
                onViewAttemptDetail = { attemptId ->
                    navController.navigate(Screen.ChiTietLichSu.createRoute(attemptId))
                }
            )
        }

        // MH_07: Chi tiết đáp án
        composable(
            route = Screen.ChiTietLichSu.route,
            arguments = listOf(navArgument("attemptId") { type = NavType.StringType })
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString("attemptId") ?: "att_01"
            ChiTietLichSuScreen(
                attemptId = attemptId,
                onBack = { navController.popBackStack() }
            )
        }

        // MH_08: Sảnh thách đấu 1v1
        composable(route = Screen.SanhThachDau.route) {
            SanhThachDauScreen(
                onNavigateHome = {
                    navController.popBackStack(Screen.TrangChu.route, inclusive = false)
                },
                onNavigateTopics = {
                    navController.navigate(Screen.DanhSachChuDe.route) {
                        popUpTo(Screen.SanhThachDau.route) { inclusive = true }
                    }
                },
                onNavigateLeaderboard = {
                    navController.navigate(Screen.BangXepHang.route)
                },
                onNavigateProfile = {
                    navController.navigate(Screen.CaNhan.route)
                },
                onMatchFound = { matchId ->
                    navController.navigate(Screen.ThiDau1v1.createRoute(matchId))
                }
            )
        }
        composable(
            route = Screen.ThiDau1v1.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: "match_default"
            ThiDau1v1Screen(
                matchId = matchId,
                onMatchCompleted = { attemptId ->
                    navController.navigate(Screen.KetQua.createRoute(attemptId)) {
                        popUpTo(Screen.ThiDau1v1.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.BangXepHang.route) {
            PlaceholderScreen(title = "MH_10 • Bảng xếp hạng", onBack = { navController.popBackStack() })
        }
        composable(route = Screen.BanBe.route) {
            PlaceholderScreen(title = "MH_11 • Danh sách bạn bè", onBack = { navController.popBackStack() })
        }
        composable(route = Screen.CaNhan.route) {
            PlaceholderScreen(
                title = "MH_12 • Trang cá nhân",
                actionLabel = "Đăng xuất",
                onAction = {
                    RetrofitClient.authToken = null
                    RetrofitClient.currentUserId = ""
                    navController.navigate(Screen.XacThuc.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) {
            Text(text = "← Quay lại", color = Color.White)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onAction) {
                Text(text = actionLabel, color = Color.Red)
            }
        }
    }
}