package com.example.traveldiary.helpers

import com.example.traveldiary.model.CurrencyQuote
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

suspend fun fetchCurrencyQuotes(): List<CurrencyQuote> {
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    val response = client.get("https://economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL,BTC-BRL").body<JsonObject>()

    return listOf("USDBRL", "EURBRL", "BTCBRL").mapNotNull { key ->
        response[key]?.jsonObject?.let {
            CurrencyQuote(
                code = it["code"]?.jsonPrimitive?.content ?: key,
                bid = it["bid"]?.jsonPrimitive?.content ?: "0.00",
                name =  it["name"]?.jsonPrimitive?.content ?: "Moeda",
            )
        }
    }
}
