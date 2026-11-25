package com.example.doan.model

data class User(
    val userId: String,
    val displayName: String,
    val phoneOrEmail: String,
    val token: String = ""
)