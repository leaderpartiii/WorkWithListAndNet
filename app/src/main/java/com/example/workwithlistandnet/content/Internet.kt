package com.example.workwithlistandnet.content

import android.os.Bundle
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
import androidx.room.Room

class Internet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainWindow() }
    }


    @Composable
    fun MainWindow() {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()
        val imageDao = db.imageDao()
        val imagesList = GetImagesFromDB(imageDao)


        val error = rememberSaveable { mutableIntStateOf(0) }
        val loadingRequest = rememberSaveable { mutableStateOf(false) }
        val loadingImage =
            rememberSaveable { mutableStateOf(imagesList.isNotEmpty()) }
        val onSuccess = rememberSaveable { mutableStateOf(false) }

        val listState = rememberLazyListState()


        AddImageToDB(imageDao, imagesList)
        LoadImage(imagesList, error, loadingRequest, loadingImage)


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NewImage(error, loadingRequest, loadingImage, onSuccess)
            ResetButton(
                result = imagesList,
                error = error,
                loadingRequest = loadingRequest,
                loadingImage = loadingImage,
                imageDao
            )
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
        imageDao: ImageDao,
    ) {
        Button(onClick = {
            result.clear()
            error.intValue = 0
            loadingRequest.value = true
            loadingImage.value = false
            imageDao.clearImages()
        }) {
            Text(text = "Сбросить всё")
        }
    }

    @Composable
    fun AddImageToDB(
        imageDao: ImageDao,
        result: SnapshotStateList<String>,
    ) {
        LaunchedEffect(if (result.isEmpty()) result.isEmpty() else result.last()) {
            if (result.isNotEmpty()) {
                val newImage = ImageEntity(url = result.last())
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        imageDao.insertImage(newImage)
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    @Composable
    fun GetImagesFromDB(imageDao: ImageDao): SnapshotStateList<String> {
        val imagesList = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val images = imageDao.getAllImages()
                    imagesList.addAll(images.map { it.url })
                } catch (e: Exception) {
                }
            }
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
                loadingRequest.value -> {
                    DisplayLoadingGif()
                }

                loadingImage.value && size != 0 -> {
                    DisplayLoadingGif()
                    AsyncImage(
                        model = result,
                        contentDescription = null,
                        onSuccess = { onSuccess.value = true;loadingImage.value = false; },
                        onError = { error.intValue = 1 }
                    )
                }

                result.isNotEmpty() -> {
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

