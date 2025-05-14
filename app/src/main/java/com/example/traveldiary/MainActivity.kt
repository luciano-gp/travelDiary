package com.example.traveldiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.traveldiary.data.supabase
import com.example.traveldiary.ui.navigation.AppNavHost
import com.example.traveldiary.ui.navigation.NavRoutes
import com.example.traveldiary.ui.theme.TravelDiaryTheme
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val startDestination = if (supabase.auth.currentSessionOrNull() != null) {
                NavRoutes.MAIN
            } else {
                NavRoutes.LOGIN
            }

            TravelDiaryTheme {
                AppNavHost(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}