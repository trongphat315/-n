package com.example.doan.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doan.R
import com.example.doan.model.User
import com.example.doan.viewModel.AuthMode
import com.example.doan.viewModel.AuthViewModel

// Bảng màu định nghĩa bằng tên và hệ RGB (không dùng ký hiệu hex)
private val PageWarmBg = Color(red = 252, green = 246, blue = 245)
private val BannerLightBlueBg = Color(red = 232, green = 245, blue = 253)
private val TitleCornflowerBlue = Color(red = 74, green = 118, blue = 212)
private val SkyButtonBlue = Color(red = 135, green = 197, blue = 245)
private val LinkRoyalBlue = Color(red = 78, green = 124, blue = 230)
private val InputLineGray = Color(red = 185, green = 178, blue = 178)
private val DividerTanGray = Color(red = 205, green = 195, blue = 192)
private val PlaceholderGray = Color(red = 165, green = 158, blue = 158)
private val TextInkBlack = Color(red = 25, green = 25, blue = 28)
private val FacebookBlue = Color(red = 59, green = 89, blue = 245)
private val GoogleRed = Color(red = 234, green = 67, blue = 53)
private val GoogleYellow = Color(red = 251, green = 188, blue = 5)
private val GoogleGreen = Color(red = 52, green = 168, blue = 83)
private val GoogleBlue = Color(red = 66, green = 133, blue = 244)
private val SoftErrorBg = Color(red = 254, green = 226, blue = 226)
private val ErrorRed = Color(red = 210, green = 40, blue = 40)
private val SoftSuccessBg = Color(red = 220, green = 252, blue = 231)
private val SuccessGreen = Color(red = 22, green = 163, blue = 74)

@Composable
fun XacThucScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (User) -> Unit = {}
) {
    val state by authViewModel.uiState.collectAsState()

    LaunchedEffect(state.loggedInUser) {
        state.loggedInUser?.let { user ->
            onLoginSuccess(user)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageWarmBg)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Ảnh Banner thương hiệu phía trên cùng
        BrandTopBanner()

        Spacer(modifier = Modifier.height(28.dp))

        // 2. Tiêu đề chính màn hình (ĐĂNG NHẬP / ĐĂNG KÝ / QUÊN MẬT KHẨU)
        val screenTitle = when (state.mode) {
            AuthMode.LOGIN -> "ĐĂNG NHẬP"
            AuthMode.REGISTER -> "ĐĂNG KÝ"
            AuthMode.FORGOT_PASSWORD -> "QUÊN MẬT KHẨU"
        }
        Text(
            text = screenTitle,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TitleCornflowerBlue,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Thông báo lỗi hoặc thành công từ API
        state.errorMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftErrorBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = msg, color = ErrorRed, fontSize = 12.5.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        state.successMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftSuccessBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = msg, color = SuccessGreen, fontSize = 12.5.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Khu vực nhập liệu dạng gạch chân đặt trực tiếp trên nền trang
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
        ) {
            // Ô nhập Tên người dùng (hiển thị khi chọn Đăng ký)
            if (state.mode == AuthMode.REGISTER) {
                UnderlinedInputField(
                    value = state.displayName,
                    onValueChange = authViewModel::onDisplayNameChange,
                    placeholder = "Nhập tên người dùng"
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Ô nhập Số điện thoại
            UnderlinedInputField(
                value = state.phone,
                onValueChange = authViewModel::onPhoneChange,
                placeholder = "Nhập số điện thoại",
                keyboardType = KeyboardType.Phone
            )

            // Ô nhập Mật khẩu
            if (state.mode != AuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(18.dp))
                UnderlinedInputField(
                    value = state.password,
                    onValueChange = authViewModel::onPasswordChange,
                    placeholder = "Nhập mật khẩu",
                    isPassword = true,
                    passwordVisible = state.isPasswordVisible,
                    onTogglePassword = authViewModel::togglePasswordVisibility
                )
            }

            // Ô Xác nhận mật khẩu (hiển thị khi chọn Đăng ký)
            if (state.mode == AuthMode.REGISTER) {
                Spacer(modifier = Modifier.height(18.dp))
                UnderlinedInputField(
                    value = state.confirmPassword,
                    onValueChange = authViewModel::onConfirmPasswordChange,
                    placeholder = "Nhập lại mật khẩu",
                    isPassword = true,
                    passwordVisible = state.isConfirmPasswordVisible,
                    onTogglePassword = authViewModel::toggleConfirmPasswordVisibility
                )
            }

            // Hàng Ghi nhớ đăng nhập & Quên mật khẩu ?
            if (state.mode == AuthMode.LOGIN) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            authViewModel.onRememberMeChange(!state.rememberMe)
                        }
                    ) {
                        SquareCheckBox(checked = state.rememberMe)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ghi nhớ đăng nhập",
                            fontSize = 13.5.sp,
                            color = TextInkBlack
                        )
                    }

                    Text(
                        text = "Quên mật khẩu ?",
                        fontSize = 13.5.sp,
                        color = LinkRoyalBlue,
                        modifier = Modifier.clickable {
                            authViewModel.switchMode(AuthMode.FORGOT_PASSWORD)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 4. Nút hành động chính bo tròn màu xanh da trời nhạt
        Button(
            onClick = authViewModel::submit,
            enabled = !state.isLoading,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SkyButtonBlue,
                contentColor = TextInkBlack
            ),
            modifier = Modifier
                .width(210.dp)
                .height(54.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = TextInkBlack,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                val btnLabel = when (state.mode) {
                    AuthMode.LOGIN -> "ĐĂNG NHẬP"
                    AuthMode.REGISTER -> "ĐĂNG KÝ"
                    AuthMode.FORGOT_PASSWORD -> "GỬI MÃ OTP"
                }
                Text(
                    text = btnLabel,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = TextInkBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Dòng chuyển đổi giữa Đăng nhập và Đăng ký
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (state.mode) {
                AuthMode.LOGIN -> {
                    Text(
                        text = "Bạn chưa có tài khoản?  ",
                        fontSize = 14.sp,
                        color = TextInkBlack
                    )
                    Text(
                        text = "Đăng ký",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = LinkRoyalBlue,
                        modifier = Modifier.clickable { authViewModel.switchMode(AuthMode.REGISTER) }
                    )
                }
                AuthMode.REGISTER, AuthMode.FORGOT_PASSWORD -> {
                    Text(
                        text = "Bạn đã có tài khoản?  ",
                        fontSize = 14.sp,
                        color = TextInkBlack
                    )
                    Text(
                        text = "Đăng nhập",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = LinkRoyalBlue,
                        modifier = Modifier.clickable { authViewModel.switchMode(AuthMode.LOGIN) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // 6. Hai đường gạch ngang phân cách
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(1.dp)
                    .background(DividerTanGray)
            )
            Box(
                modifier = Modifier
                    .width(95.dp)
                    .height(1.dp)
                    .background(DividerTanGray)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 7. Hai ô vuông đăng nhập nhanh Facebook & Google
        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialSquareCard(onClick = { authViewModel.loginSocial("Facebook") }) {
                FacebookCircleLogo(modifier = Modifier.size(46.dp))
            }

            SocialSquareCard(onClick = { authViewModel.loginSocial("Google") }) {
                GoogleGLogo(modifier = Modifier.size(44.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BrandTopBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BannerLightBlueBg)
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Banner Ai Nhanh Sẽ Thắng - CPN Group",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UnderlinedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = PlaceholderGray
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = TextInkBlack),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = if (isPassword && !passwordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isPassword && onTogglePassword != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onTogglePassword),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val w = size.width
                        val h = size.height
                        drawArc(
                            color = PlaceholderGray,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(0f, h * 0.24f),
                            size = Size(w, h * 0.52f),
                            style = Stroke(1.4.dp.toPx())
                        )
                        drawArc(
                            color = PlaceholderGray,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(0f, h * 0.24f),
                            size = Size(w, h * 0.52f),
                            style = Stroke(1.4.dp.toPx())
                        )
                        drawCircle(PlaceholderGray, w * 0.12f, Offset(w / 2f, h / 2f))
                        if (!passwordVisible) {
                            drawLine(
                                color = PlaceholderGray,
                                start = Offset(w * 0.15f, h * 0.85f),
                                end = Offset(w * 0.85f, h * 0.15f),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(InputLineGray)
        )
    }
}

@Composable
private fun SquareCheckBox(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(15.dp)
            .background(if (checked) TitleCornflowerBlue else Color.White)
            .border(width = 1.dp, color = TextInkBlack),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Canvas(modifier = Modifier.size(10.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.52f)
                    lineTo(size.width * 0.42f, size.height * 0.80f)
                    lineTo(size.width * 0.85f, size.height * 0.22f)
                }
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

@Composable
private fun SocialSquareCard(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 76.dp, height = 64.dp)
            .background(Color.White.copy(alpha = 0.55f))
            .border(width = 1.dp, color = DividerTanGray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun FacebookCircleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = FacebookBlue, radius = r, center = c)

        val w = size.width
        val h = size.height
        val fPath = Path().apply {
            moveTo(w * 0.54f, h * 0.98f)
            lineTo(w * 0.54f, h * 0.58f)
            lineTo(w * 0.67f, h * 0.58f)
            lineTo(w * 0.69f, h * 0.44f)
            lineTo(w * 0.54f, h * 0.44f)
            lineTo(w * 0.54f, h * 0.36f)
            quadraticTo(w * 0.54f, h * 0.30f, w * 0.61f, h * 0.30f)
            lineTo(w * 0.69f, h * 0.30f)
            lineTo(w * 0.69f, h * 0.18f)
            lineTo(w * 0.58f, h * 0.18f)
            quadraticTo(w * 0.42f, h * 0.18f, w * 0.42f, h * 0.34f)
            lineTo(w * 0.42f, h * 0.44f)
            lineTo(w * 0.31f, h * 0.44f)
            lineTo(w * 0.31f, h * 0.58f)
            lineTo(w * 0.42f, h * 0.58f)
            lineTo(w * 0.42f, h * 0.98f)
            close()
        }
        drawPath(path = fPath, color = Color.White)
    }
}

@Composable
private fun GoogleGLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.19f
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)

        drawArc(GoogleRed, 200f, 115f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        drawArc(GoogleYellow, 145f, 60f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        drawArc(GoogleGreen, 35f, 115f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        drawArc(GoogleBlue, -12f, 52f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))

        drawLine(
            color = GoogleBlue,
            start = Offset(size.width * 0.52f, size.height * 0.50f),
            end = Offset(size.width * 0.92f, size.height * 0.50f),
            strokeWidth = stroke
        )
    }
}
