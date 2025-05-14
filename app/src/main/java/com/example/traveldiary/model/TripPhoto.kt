package com.example.traveldiary.model

import kotlinx.serialization.Serializable

@Serializable
data class TripPhoto(
    val id: String? = null,
    val trip_id: String,
    val image_url: String
)