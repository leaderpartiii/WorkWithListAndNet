package com.example.workwithlistandnet.content

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.workwithlistandnet.net.getImage
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.bumptech.glide.Glide
import com.example.workwithlistandnet.R
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay


class Internet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainWindow() }
    }


    @Composable
    fun MainWindow() {
        val result = rememberSaveable { mutableStateOf("") }
        val error = rememberSaveable { mutableIntStateOf(0) }
        val loadingRequest = rememberSaveable { mutableStateOf(true) }
        val loadingImage = rememberSaveable { mutableStateOf(false) }

        LoadImage(result, error, loadingRequest, loadingImage)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButton(result, error, loadingRequest, loadingImage)
            if (error.intValue == 0 && (loadingImage.value || loadingRequest.value))
                LoadingWithDots()
            else if (error.intValue == 0 && (!loadingImage.value && !loadingRequest.value))
                Text(text = "Вот что именно получилось")
            else
                Text(text = "Что-то не очень получилось")
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                DisplayContent(result, error, loadingRequest, loadingImage)
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
        result: MutableState<String>,
        error: MutableIntState,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>,
    ) {
        LaunchedEffect(result.value.isEmpty()) {
            try {
                val image = getImage()
                result.value = image
            } catch (e: Exception) {
                error.intValue = 1
            } finally {
                loadingRequest.value = false
                loadingImage.value = true
            }
        }
    }

    @Composable
    fun ActionButton(
        result: MutableState<String>,
        error: MutableIntState,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>
    ) {
        Button(onClick = {
            result.value = ""
            error.intValue = 0
            loadingRequest.value = true
            loadingImage.value = false
        }) {
            Text(text = "Перегенерировать вайфу")
        }
    }

    @Composable
    fun DisplayContent(
        result: MutableState<String>,
        error: MutableIntState,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>
    ) {
        if (error.intValue == 0) {
            when {
                loadingRequest.value -> {
                    DisplayLoadingGif()
                }

                loadingImage.value -> {
                    DisplayLoadingGif()
                    AsyncImage(
                        model = result.value,
                        contentDescription = null,
                        onSuccess = { loadingImage.value = false },
                        onError = { error.intValue = 1 }
                    )
                }

                else -> {
                    AsyncImage(
                        model = result.value,
                        contentDescription = null,
                        onSuccess = { loadingImage.value = false },
                        onError = { error.intValue = 1 }
                    )
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
    fun DisplayLoadingGif() {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this)
                        .asGif()
                        .load(R.drawable.loading)
                        .into(this)
                }
            },
            modifier = Modifier.size(200.dp)
        )
    }
}

