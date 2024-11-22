package com.example.workwithlistandnet.net

import android.util.Log
import org.json.JSONObject

suspend fun getImage(): String {
    val imageUrl = getImageUrl()
    return imageUrl;
}

private suspend fun getImageUrl(): String {
    val apiClient = ApiClient()
    val url = "https://api.waifu.im/search"
    val requestBody = mapOf(
//        "included_tags" to "maid",
        "included_tags" to "is_nsfw",
        "height" to ">=2000"
    )
    val rawJson = apiClient.fetch(url, requestBody)
//    Log.d("JSON", JSONObject(rawJson).toString())
//    Log.d("JSON", JSONObject(rawJson).getJSONArray("images").toString())
    return JSONObject(rawJson).getJSONArray("images").getJSONObject(0).getString("url")
}


