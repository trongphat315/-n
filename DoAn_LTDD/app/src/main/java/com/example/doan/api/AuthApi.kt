package com.example.doan.api

import com.example.doan.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val phone: String,
    val password: String
)

data class RegisterRequest(
    val displayName: String,
    val phone: String,
    val password: String
)

data class OtpRequest(
    val phone: String
)

data class SocialLoginRequest(
    val provider: String,
    val accessToken: String = ""
)

data class MessageResponse(
    val message: String
)

interface AuthApi {

    @POST("auth/login")
    suspend fun requestLogin(@Body body: LoginRequest): Response<User>

    @POST("auth/register")
    suspend fun requestRegister(@Body body: RegisterRequest): Response<MessageResponse>

    @POST("auth/forgot-password")
    suspend fun requestResetOtp(@Body body: OtpRequest): Response<MessageResponse>

    @POST("auth/social-login")
    suspend fun requestSocialLogin(@Body body: SocialLoginRequest): Response<User>
}