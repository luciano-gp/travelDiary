package com.example.traveldiary.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.traveldiary.helpers.getCurrentLocationIfPermitted
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import java.util.*

@Composable
fun TripMapPicker(
    onLocationSelected: (LatLng) -> Unit,
    modifier: Modifier = Modifier,
    initialLatLng: LatLng? = null
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            getCurrentLocationIfPermitted(context, fusedLocationClient) { latLng ->
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                marker?.remove()
                marker = map?.addMarker(MarkerOptions().position(latLng).title("Minha localização"))
                onLocationSelected(latLng)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocationIfPermitted(context, fusedLocationClient) { latLng ->
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                marker?.remove()
                marker = map?.addMarker(MarkerOptions().position(latLng).title("Minha localização"))
                onLocationSelected(latLng)
            }
        }
    }

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar local...") },
            trailingIcon = {
                IconButton(onClick = {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocationName(searchQuery, 1)
                        val result = addresses?.firstOrNull()
                        if (result != null) {
                            val latLng = LatLng(result.latitude, result.longitude)
                            map?.let {
                                marker?.remove()
                                marker = it.addMarker(
                                    MarkerOptions().position(latLng).title(searchQuery)
                                )
                                it.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            }
                            onLocationSelected(latLng)
                        }
                    } catch (e: Exception) {
                        print(e)
                    }
                }) {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { view ->
            view.getMapAsync @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) { googleMap ->
                map = googleMap
                MapsInitializer.initialize(context)
                googleMap.uiSettings.isZoomControlsEnabled = true

                googleMap.setOnMapClickListener { latLng ->
                    marker?.remove()
                    marker = googleMap.addMarker(
                        MarkerOptions().position(latLng).title("Local selecionado")
                    )
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    onLocationSelected(latLng)
                }

                if (initialLatLng != null) {
                    marker?.remove()
                    marker = googleMap.addMarker(
                        MarkerOptions().position(initialLatLng).title("Local salvo")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 15f))
                    onLocationSelected(initialLatLng)
                } else if (locationPermissionGranted &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val currentLatLng = LatLng(it.latitude, it.longitude)
                            marker?.remove()
                            marker = googleMap.addMarker(
                                MarkerOptions().position(currentLatLng).title("Minha localização")
                            )
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    15f
                                )
                            )
                            onLocationSelected(currentLatLng)
                        }
                    }
                }
            }
        }
    }
}
