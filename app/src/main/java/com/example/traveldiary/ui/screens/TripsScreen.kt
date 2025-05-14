package com.example.traveldiary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.traveldiary.data.supabase
import com.example.traveldiary.helpers.fetchCurrencyQuotes
import com.example.traveldiary.model.CurrencyQuote
import com.example.traveldiary.model.Trip
import com.example.traveldiary.ui.components.CurrencyCarousel
import com.example.traveldiary.ui.components.ImagesCarousel
import com.example.traveldiary.ui.navigation.NavRoutes
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(navController: NavController) {
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var quotes by remember { mutableStateOf<List<CurrencyQuote>>(emptyList()) }

    val userId = supabase.auth.currentUserOrNull()?.id
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        quotes = fetchCurrencyQuotes()

        if (userId != null) {
            loading = true
            withContext(Dispatchers.IO) {
                trips = supabase.from("trips")
                    .select(Columns.raw("*, trip_photos(*)")) {
                        filter {
                            eq("user_id", userId)
                        }
                        order(column = "date", order = Order.DESCENDING)
                    }
                    .decodeList()
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Viagens", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            supabase.auth.signOut()
                            navController.navigate(NavRoutes.LOGIN) {
                                popUpTo(NavRoutes.MAIN) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(NavRoutes.TRIP_FORM) },
                containerColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Viagem")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                trips.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhuma viagem encontrada.", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                else -> {
                    CurrencyCarousel(quotes)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)) {
                        items(trips, key = { it.id ?: "" }) { trip ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("trip_form?tripId=${trip.id}")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(trip.title, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (trip.description?.isNotBlank() == true) {
                                            Text(
                                                trip.description,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

                                        if (trip.trip_photos.isNotEmpty()) {
                                            ImagesCarousel(imageUrls = trip.trip_photos.map { it.image_url })
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("📍 ", style = MaterialTheme.typography.bodySmall)
                                                Text(trip.location_name, style = MaterialTheme.typography.bodySmall)
                                            }

                                            val shortDate = trip.date.substringBefore("T").split("-").reversed().joinToString("/")

                                            Text(shortDate, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                supabase.from("trips").delete {
                                                    filter {
                                                        eq("id", trip.id.toString())
                                                    }
                                                }
                                                trips = trips.filterNot { it.id == trip.id }
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir viagem",
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
