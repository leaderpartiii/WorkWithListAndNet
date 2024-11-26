package com.example.workwithlistandnet.content

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.example.workwithlistandnet.R
import com.example.workwithlistandnet.net.getGif
import kotlinx.coroutines.delay

class Gif : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { MainWindow() }
    }

    @Composable
    fun MainWindow() {

        val textFromKeyboard = rememberSaveable { mutableStateOf("") }
        val resultUrl = rememberSaveable { mutableStateOf("") }
        val error = rememberSaveable { mutableStateOf("") }
        val loadingRequest = rememberSaveable { mutableStateOf(false) }
        val loadingImage = rememberSaveable { mutableStateOf(false) }

        LoadImage(resultUrl, error, loadingRequest, loadingImage, textFromKeyboard)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ActionButton(resultUrl, error, loadingRequest, loadingImage, textFromKeyboard)
            TextInputWithCloseKeyboard(textFromKeyboard, loadingRequest)
            if (textFromKeyboard.value.isNotEmpty() && error.value.isEmpty() && (loadingImage.value || loadingRequest.value))
                LoadingWithDots()
            else if (textFromKeyboard.value.isNotEmpty() && error.value.isEmpty() && (!loadingImage.value && !loadingRequest.value))
                Text(text = "Вот что именно получилось")
            else if (textFromKeyboard.value.isNotEmpty() && error.value.isNotEmpty()) {
                Text(text = "Что-то не очень получилось ${error.value}")
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                DisplayContent(resultUrl, error, loadingRequest, loadingImage, textFromKeyboard)
            }
        }
    }

    @Composable
    fun LoadingWithDots() {
        val dots = rememberSaveable { mutableStateOf("") }

        LaunchedEffect(Unit) {
            while (true) {
                dots.value = when (dots.value) {
                    "" -> "."
                    "." -> ".."
                    ".." -> "..."
                    "..." -> ""
                    else -> ""
                }
                delay(500)
            }
        }

        Text(
            text = "Загрузка ${dots.value}",
            style = MaterialTheme.typography.bodyLarge,
        )
    }

    @Composable
    fun LoadImage(
        resultUrl: MutableState<String>,
        error: MutableState<String>,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>,
        prompt: MutableState<String>
    ) {
        LaunchedEffect(prompt.value.isNotEmpty()) {
            try {
                val image = getGif(prompt.value)
                resultUrl.value = image
            } catch (e: Exception) {
                error.value = e.message.toString()
            } finally {
                loadingRequest.value = false
                loadingImage.value = true
            }
        }
    }

    @Composable
    fun ActionButton(
        resultUrl: MutableState<String>,
        error: MutableState<String>,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>,
        textFromKeyboard: MutableState<String>
    ) {
        Button(onClick = {
            resultUrl.value = ""
            error.value = ""
            loadingRequest.value = false
            loadingImage.value = false
            textFromKeyboard.value = ""
        }) {
            Text(text = "Перегенерировать гифку")
        }
    }

    @Composable
    fun DisplayContent(
        resultUrl: MutableState<String>,
        error: MutableState<String>,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>,
        textFromKeyboard: MutableState<String>
    ) {
        if (error.value.isEmpty() && textFromKeyboard.value.isNotEmpty()) {
            when {
                loadingRequest.value -> {
                    DisplayLoadingGif(loadingImage)
                }

                loadingImage.value -> {

                    DisplayImage(resultUrl.value)

                }

                else -> {

                    DisplayImage(resultUrl.value)
                }
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.cat),
                contentDescription = null
            )
        }
    }

    @Composable
    fun DisplayLoadingGif(loadingImage: MutableState<Boolean>) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this)
                        .asGif()
                        .load(R.drawable.loading)
                        .listener(loadGifListener(loadingImage))
                        .into(this)
                }
            },
            modifier = Modifier.size(200.dp)
        )
    }

    private fun loadGifListener(loadingImage: MutableState<Boolean>): RequestListener<GifDrawable> {
        return object : RequestListener<GifDrawable> {

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<GifDrawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: GifDrawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<GifDrawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                loadingImage.value = false;
                return false
            }
        }
    }

    @Composable
    fun DisplayImage(gifUrl: String) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this)
                        .asGif()
                        .load(gifUrl)
                        .into(this)
                }
            },
            modifier = Modifier.size(200.dp)
        )
    }

    @Composable
    fun TextInputWithCloseKeyboard(
        text: MutableState<String>,
        loadingRequest: MutableState<Boolean>
    ) {
        val words = remember { mutableStateOf(text.value) }
        val keyboardController = LocalSoftwareKeyboardController.current

        TextField(
            value = words.value,
            onValueChange = { newText -> words.value = newText },
            label = { Text("Введите текст") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                keyboardController?.hide()
                text.value = words.value
                loadingRequest.value = true
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Закрыть клавиатуру и отправить запрос")
        }
    }

}

