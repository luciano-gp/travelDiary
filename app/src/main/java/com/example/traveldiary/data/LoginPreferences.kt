package com.example.traveldiary.data

import android.content.Context

object LoginPreferences {

    private const val PREF_NAME = "login_prefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    fun saveCredentials(context: Context, email: String, password: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    fun getCredentials(context: Context): Pair<String?, String?> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_EMAIL, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        return Pair(email, password)
    }
}
