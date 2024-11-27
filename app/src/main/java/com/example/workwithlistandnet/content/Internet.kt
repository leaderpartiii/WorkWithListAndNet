package com.example.workwithlistandnet.content

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.example.workwithlistandnet.R
import kotlinx.coroutines.delay
import com.example.workwithlistandnet.db.*
import androidx.compose.ui.platform.LocalContext

class Internet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainWindow() }
    }


    @Composable
    fun MainWindow() {


        val context = LocalContext.current
        val database = remember { mutableStateOf<AppDatabase?>(null) }
        val imageDao = remember { mutableStateOf<ImageDao?>(null) }

        // Список URL-адресов изображений
        val imagesList = remember { mutableStateListOf<String>() }

        // Инициализация базы данных и загрузка данных
        LaunchedEffect(context) {
            database.value = AppDatabase.getInstance(context)
            imageDao.value = database.value?.imageDao()
            imageDao.value?.let { dao ->
                val imagesFromDB = dao.getAllImages().map { it.url }
                imagesList.addAll(imagesFromDB)
            }
        }

        val error = rememberSaveable { mutableIntStateOf(0) }
        val loadingRequest = rememberSaveable { mutableStateOf(false) }
        val loadingImage = rememberSaveable { mutableStateOf(false) }
        val onSuccess = rememberSaveable { mutableStateOf(false) }

//        val imagesList = GetImagesFromDB(imageDao)
        val listState = rememberLazyListState()


        LoadImage(imagesList, error, loadingRequest, loadingImage)
        AddImageToDB(imageDao, imagesList)


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NewImage(error, loadingRequest, loadingImage, onSuccess)
            if (error.intValue == 0 && (loadingImage.value || loadingRequest.value) && !onSuccess.value)
                LoadingWithDots()
            else if (error.intValue == 0 && (!loadingImage.value && !loadingRequest.value) && onSuccess.value)
                Text(text = "Вот что именно получилось")
            else if (error.intValue != 0)
                Text(text = "Что-то не очень получилось")

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                items(imagesList) { imageUrl ->
                    DisplayContent(
                        imageUrl,
                        error,
                        loadingRequest,
                        loadingImage,
                        imagesList.size,
                        onSuccess
                    )
                }
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
        result: SnapshotStateList<String>,
        error: MutableIntState,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>,
    ) {
        LaunchedEffect(loadingRequest.value) {
            if (loadingRequest.value)
                try {
                    val image = getImage()
                    result.add(image)
                } catch (e: Exception) {
                    error.intValue = 1
                } finally {
                    loadingRequest.value = false
                    loadingImage.value = true
                }
        }
    }

    @Composable
    fun ResetButton(
        result: SnapshotStateList<String>,
        error: MutableIntState,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>,
    ) {
        Button(onClick = {
            result.clear()
            error.intValue = 0
            loadingRequest.value = true
            loadingImage.value = false
        }) {
            Text(text = "Перегенерировать вайфу")
        }
    }

    @Composable
    fun AddImageToDB(imageDao: MutableState<ImageDao?>, result: SnapshotStateList<String>) {
        LaunchedEffect(result) {
            if (result.isNotEmpty()) {
                val newImage = ImageEntity(url = result.last())
                imageDao.value?.insertImage(newImage)
            }
        }
    }

    @Composable
    fun GetImagesFromDB(imageDao: ImageDao): SnapshotStateList<String> {
        val imagesList = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
            val images = imageDao.getAllImages()
            imagesList.addAll(images.map { it.url })
        }
        return imagesList
    }


    @Composable
    fun NewImage(
        error: MutableIntState,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>,
        onSuccess: MutableState<Boolean>,
    ) {
        Button(onClick = {
            Log.d("Debug", "NewImage")
            error.intValue = 0
            loadingRequest.value = true
            loadingImage.value = false
            onSuccess.value = false
        }) {
            Text(text = "Перегенерировать вайфу")
        }
    }

    @Composable
    fun DisplayImage(gifUrl: String, loadingImage: MutableState<Boolean>) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this)
                        .asGif()
                        .load(gifUrl)
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
    fun DisplayContent(
        result: String,
        error: MutableIntState,
        loadingRequest: MutableState<Boolean>,
        loadingImage: MutableState<Boolean>,
        size: Int,
        onSuccess: MutableState<Boolean>
    ) {
        if (error.intValue == 0) {
            when {
                size == 0 -> {
                    DisplayLoadingGif()
                    Log.d("Debug", "First case")
                }

                loadingImage.value && size == 1 -> {
                    Log.d("Debug", "Second case")
                    DisplayLoadingGif()
                    AsyncImage(
                        model = result,
                        contentDescription = null,
                        onSuccess = { onSuccess.value = true;loadingImage.value = false; },
                        onError = { error.intValue = 1 }
                    )
                }

                result.isNotEmpty() -> {
                    Log.d("Debug", "Three case")
                    AsyncImage(
                        model = result,
                        contentDescription = null,
                        onSuccess = { onSuccess.value = true;loadingImage.value = false; },
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
            modifier = Modifier.fillMaxSize()
        )
    }
}

