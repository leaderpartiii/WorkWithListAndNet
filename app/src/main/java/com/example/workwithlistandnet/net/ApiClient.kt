package com.example.workwithlistandnet.net

import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.util.StringValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiClient {

    private val client = HttpClient(OkHttp)

    suspend fun fetch(url: String, bodyRequest: Map<String, String> = mapOf()): String =
        withContext(Dispatchers.IO) {
            val response = client.get(url) {
                url {
                    parameters.appendAll(StringValues.build {
                        bodyRequest.toString()
                    })
                }
            }
            if (response.status.value != 200) {
                throw Exception(response.status.value.toString())
            }
            return@withContext response.bodyAsText()
        }
}
