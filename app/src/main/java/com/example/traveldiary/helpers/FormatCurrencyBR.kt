package com.example.traveldiary.helpers

import java.util.Locale

fun formatCurrencyBR(value: String): String {
    return try {
        val number = value.toDouble()
        val format = java.text.NumberFormat.getNumberInstance(Locale("pt", "BR")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
            roundingMode = java.math.RoundingMode.UP
        }
        format.format(number)
    } catch (e: Exception) {
        "0,00"
    }
}