package com.example.traveldiary.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

val supabase = createSupabaseClient(
    supabaseUrl = "https://gnhundomhnlujnmgtwyj.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImduaHVuZG9taG5sdWpubWd0d3lqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY0OTU3OTksImV4cCI6MjA2MjA3MTc5OX0.LE_0qYsta60JcmFcQauRAqFRxr4ik1N6o6jR_CUw5Tk"
) {
    defaultSerializer = KotlinXSerializer(Json)
    install(Postgrest)
    install(Auth)
    install(Storage)
}