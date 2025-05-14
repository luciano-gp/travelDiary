package com.example.traveldiary.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val email: String
)