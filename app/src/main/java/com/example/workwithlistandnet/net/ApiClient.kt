package com.example.workwithlistandnet.net

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.workwithlistandnet.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.util.StringValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.reflect.Field;

class ApiClient {

    private val client = HttpClient(OkHttp)

    suspend fun fetch(url: String, bodyRequest: Map<String, String>): String =
        withContext(Dispatchers.IO) {
            val response = client.get(url) {
                url {
                    parameters.appendAll(StringValues.build {
                        bodyRequest.toString()
                    })
                }
            }
            Log.d("Debug", bodyRequest.toString())
            Log.d("Status code", "fetchImageFromPrompt: ${response.status.value}")
            if (response.status.value != 200) {
                Log.d("Status code", "fetchImageFromPrompt: ${response.status.value}")
                throw Exception(response.status.value.toString())
            }
            return@withContext response.bodyAsText()
        }
}
