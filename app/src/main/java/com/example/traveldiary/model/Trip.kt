package com.example.traveldiary.model

import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String? = null,
    val user_id: String,
    val title: String,
    val description: String? = null,
    val date: String,
    val location_name: String,
    val latitude: Double,
    val longitude: Double,
    val trip_photos: List<TripPhoto> = emptyList()
)