package com.example.doan.repository

import com.example.doan.api.AuthApi
import com.example.doan.api.LoginRequest
import com.example.doan.api.OtpRequest
import com.example.doan.api.RegisterRequest
import com.example.doan.api.RetrofitClient
import com.example.doan.api.SocialLoginRequest
import com.example.doan.model.User
import kotlinx.coroutines.delay

class AuthRepository(
    private val authApi: AuthApi = RetrofitClient.authApi
) {
    companion object {
        private val localUsers = mutableMapOf<String, Pair<String, String>>()
    }

    suspend fun login(phone: String, password: String): Result<User> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(300L)
            val saved = localUsers[phone]
            if (saved != null && saved.second != password) {
                return Result.failure(Exception("Mật khẩu không chính xác."))
            }
            return Result.success(
                User(
                    userId = "user_${phone.hashCode()}",
                    displayName = saved?.first ?: "Minh Tuấn",
                    phoneOrEmail = phone,
                    token = "mock_token_$phone"
                )
            )
        }

        return try {
            val response = authApi.requestLogin(LoginRequest(phone, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Sai tài khoản hoặc mật khẩu (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối máy chủ: ${e.localizedMessage}"))
        }
    }

    suspend fun register(displayName: String, phone: String, password: String): Result<String> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(300L)
            localUsers[phone] = Pair(displayName, password)
            return Result.success("Đăng ký thành công! Vui lòng đăng nhập.")
        }

        return try {
            val response = authApi.requestRegister(RegisterRequest(displayName, phone, password))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Đăng ký tài khoản thành công!")
            } else {
                Result.failure(Exception("Đăng ký thất bại (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng khi đăng ký."))
        }
    }

    suspend fun sendResetOtp(phone: String): Result<String> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(250L)
            return Result.success("Mã OTP (123456) đã được gửi tới số $phone.")
        }

        return try {
            val response = authApi.requestResetOtp(OtpRequest(phone))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Đã gửi mã OTP tới $phone.")
            } else {
                Result.failure(Exception("Không tìm thấy số điện thoại này."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng."))
        }
    }

    suspend fun loginWithSocial(provider: String, accessToken: String = ""): Result<User> {
        if (RetrofitClient.USE_MOCK_DATA) {
            delay(250L)
            return Result.success(
                User(
                    userId = "social_${provider.lowercase()}_01",
                    displayName = "Người chơi $provider",
                    phoneOrEmail = "${provider.lowercase()}@demo.vn",
                    token = "mock_social_token"
                )
            )
        }

        return try {
            val response = authApi.requestSocialLogin(SocialLoginRequest(provider, accessToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Đăng nhập $provider thất bại (Mã ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng khi đăng nhập $provider."))
        }
    }
}