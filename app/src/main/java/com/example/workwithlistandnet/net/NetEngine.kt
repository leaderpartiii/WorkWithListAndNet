package com.example.workwithlistandnet.net

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
//        "included_tags" to "is_nsfw",
        "height" to ">=2000"
    )
    val rawJson = apiClient.fetch(url, requestBody)
    return JSONObject(rawJson).getJSONArray("images").getJSONObject(0).getString("url")
}

suspend fun getGif(prompt: String): String {
    return getGifUrl(prompt)
}

private suspend fun getGifUrl(prompt: String): String {
//    val API_KEY = BuildConfig.API_KEY
    val API_KEY = "NCXrQHFUi7ZOT7G6Uy4OY5LuFqK3SU7L"

    val apiClient = ApiClient()
    val url =
        "https://api.giphy.com/v1/gifs/search?api_key=$API_KEY&q=$prompt&limit=1&offset=0&rating=g&lang=en&bundle=messaging_non_clips"
    val rawJson = apiClient.fetch(url)
//    Log.d("Debug", rawJson)
//    Log.d("Debug", JSONObject(rawJson).getJSONArray("data").getJSONObject(0).getString("url"))
    return JSONObject(rawJson).getJSONArray("data").getJSONObject(0).getString("url")
}