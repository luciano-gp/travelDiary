package com.example.traveldiary.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng

fun getCurrentLocationIfPermitted(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocation: (LatLng) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocation(LatLng(it.latitude, it.longitude))
            }
        }
    }
}