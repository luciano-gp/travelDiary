package com.example.traveldiary.helpers

import com.example.traveldiary.data.supabase
import io.github.jan.supabase.storage.storage
import java.io.File
import java.util.UUID

suspend fun uploadImageToSupabase(file: File, userId: String): String {
    val fileName = "${UUID.randomUUID()}.jpg"
    val path = "$userId/$fileName"

    try {
        supabase.storage["trip-photos"].upload(
            path = path,
            data = file.readBytes(),
            {
                upsert = false
            }
        )
    } catch (e: Error) {
        println(e)
    }
    return supabase.storage["trip-photos"]
        .publicUrl(path)
}

