package com.example.traveldiary.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.traveldiary.data.supabase
import com.example.traveldiary.helpers.uploadImageToSupabase
import com.example.traveldiary.model.Trip
import com.example.traveldiary.model.TripPhoto
import com.example.traveldiary.ui.components.DatePickerModal
import com.example.traveldiary.ui.components.ImagesCarousel
import com.example.traveldiary.ui.components.TripMapPicker
import com.google.android.gms.maps.model.LatLng
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormScreen(navController: NavController, tripId: String = "") {
    var trip by remember { mutableStateOf<Trip?>(null) }
    val scope = rememberCoroutineScope()
    var context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var date by remember { mutableStateOf(currentDate()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }

    val imageFiles = remember { mutableStateListOf<File>() }
    val imageUris = remember { mutableStateListOf<Uri>() }

    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        if (tripId.isNotBlank()) {
            scope.launch {
                try {
                    trip = supabase.from("trips")
                        .select {
                            filter {
                                eq("id", tripId)
                            }
                        }
                        .decodeSingleOrNull<Trip>()

                    val photos = supabase.from("trip_photos")
                        .select {
                            filter {
                                eq("trip_id", tripId)
                            }
                        }
                        .decodeList<TripPhoto>()

                    if (trip != null) {
                        title = trip!!.title
                        description = trip!!.description ?: ""
                        locationName = trip!!.location_name
                        selectedLatLng = LatLng(trip!!.latitude, trip!!.longitude)
                        date = trip!!.date
                        isEditing = true
                    }

                    if (photos.isNotEmpty()) {
                        photos.map { photo ->
                            imageUris.add(photo.image_url.toUri())
                        }
                    }

                } catch (e: Exception) {
                    println(e)
                    errorMessage = "Erro ao carregar viagem"
                }
            }
        }
    }

    fun copyUriToCache(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File.createTempFile("gallery_photo_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            val file = copyUriToCache(context, uri)
            if (file != null) {
                imageFiles.add(file)
                imageUris.add(uri)
            }
        }
    }

    fun saveBitmapToCacheAsFile(context: Context, bitmap: Bitmap): File? {
        return try {
            val file = File.createTempFile("trip_photo_", ".jpg", context.cacheDir)
            val output = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            output.flush()
            output.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = saveBitmapToCacheAsFile(context, bitmap)
            if (file != null) {
                imageFiles.add(file)
                imageUris.add(file.toUri())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Viagem") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da viagem*") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text("Selecione a localização*")
                TripMapPicker(
                    onLocationSelected = { latLng ->
                        selectedLatLng = latLng
                    },
                    initialLatLng = selectedLatLng,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Nome do local*") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date.substringBefore("T").split("-").reversed().joinToString("/"),
                onValueChange = {},
                label = { Text("Data da viagem*") },
                modifier = Modifier
                    .fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Selecionar data"
                        )
                    }
                }
            )

            Text("Fotos")

            ImagesCarousel(imageUris = imageUris)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    onClick = { cameraLauncher.launch(null) }
                ) {
                    Text("Câmera")
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { galleryLauncher.launch("image/*") }
                ) {
                    Text("Galeria")
                }
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (title.isBlank() || locationName.isBlank() || selectedLatLng == null || date.isBlank()) {
                        errorMessage = "Preencha todos os campos obrigatórios"
                        return@Button
                    }

                    scope.launch {
                        try {
                            val userId = supabase.auth.currentUserOrNull()?.id
                                ?: throw Exception("Usuário não autenticado")

                            val tripInsert = Trip(
                                id = if (isEditing && tripId.isNotBlank()) tripId else null,
                                user_id = userId,
                                title = title,
                                description = description,
                                date = date,
                                location_name = locationName,
                                latitude = selectedLatLng!!.latitude,
                                longitude = selectedLatLng!!.longitude
                            )

                            val toUpsert = supabase.from("trips").upsert(tripInsert) {
                                select()
                            }.decodeSingle<Trip>()

                            if (imageFiles.isNotEmpty()) {
                                imageFiles.forEach { file ->
                                    val imageUrl = uploadImageToSupabase(file, userId)

                                    supabase.from("trip_photos").insert(
                                        mapOf(
                                            "trip_id" to toUpsert.id,
                                            "image_url" to imageUrl
                                        )
                                    )
                                }
                            }

                            navController.popBackStack()
                        } catch (e: Exception) {
                            println("TripFormScreen onClick save button $e")
                            errorMessage = e.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Salvar alterações" else "Salvar viagem")
            }

        }
    }


    if (showDatePicker) {
        DatePickerModal(
            selectedDate = date,
            onDateSelected = {
                date = Instant.ofEpochMilli(it!!)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .toString()
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

fun currentDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
