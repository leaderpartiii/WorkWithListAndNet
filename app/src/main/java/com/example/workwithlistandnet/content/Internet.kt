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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.room.Room
import coil3.compose.AsyncImage
import com.bumptech.glide.Glide
import com.example.workwithlistandnet.R
import com.example.workwithlistandnet.db.AppDatabase
import com.example.workwithlistandnet.db.ImageDao
import com.example.workwithlistandnet.db.ImageEntity
import com.example.workwithlistandnet.net.getImage
import kotlinx.coroutines.delay

class Internet : AppCompatActivity() {
    enum class State {
        Error,
        Pause,
        LoadingRequest,
        LoadingImage,
        Success
    }

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
        val imagesList = getImagesFromDB(imageDao)


        val currState =
            rememberSaveable { mutableStateOf(if (imagesList.isEmpty()) State.Pause else State.LoadingImage) }

        val listState = rememberLazyListState()

        LoadImage(imagesList, currState)
        AddImageToDB(imageDao, imagesList)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NewImage(currState)
            ResetButton(currState, imagesList, imageDao)
            when (currState.value) {
                State.LoadingImage, State.LoadingRequest -> LoadingWithDots()
                State.Success -> Text(text = "Вот что именно получилось")
                State.Error -> Text(text = "Что-то не очень получилось")
                State.Pause -> Text(text = "Ждем инструкции")
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                items(imagesList) { imageUrl ->
                    DisplayContent(
                        imageUrl,
                        currState,
                        imagesList.size
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
        currState: MutableState<State>,
    ) {
        LaunchedEffect(currState.value) {
            if (currState.value == State.LoadingRequest)
                try {
                    result.add(getImage())
                    currState.value = State.LoadingImage
                } catch (e: Exception) {
                    currState.value = State.Error
                }
        }
    }

    @Composable
    fun ResetButton(
        currState: MutableState<State>,
        result: SnapshotStateList<String>,
        imageDao: ImageDao,
    ) {
        Button(onClick = {
            currState.value = State.Pause
            result.clear()
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
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    @Composable
    fun getImagesFromDB(imageDao: ImageDao): SnapshotStateList<String> {
        val imagesList = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val images = imageDao.getAllImages()
                    imagesList.addAll(images.map { it.url })
                } catch (_: Exception) {
                }
            }
        }

        return imagesList
    }


    @Composable
    fun NewImage(
        currState: MutableState<State>
    ) {
        Button(onClick = {
            currState.value = State.LoadingRequest
        }) {
            Text(text = "Сгенерировать вайфу")
        }
    }

    @Composable
    fun DisplayContent(
        result: String,
        currState: MutableState<State>,
        size: Int,
    ) {
        if (currState.value == State.Error) {
            Image(
                painter = painterResource(id = R.drawable.cat),
                contentDescription = null
            )
            return
        }
        when {

            currState.value == State.LoadingRequest && size == 0 -> {
                DisplayLoadingGif()
            }

            else -> {

                AsyncImage(
                    model = result,
                    contentDescription = null,
                    onSuccess = { currState.value = State.Success },
                    onError = { currState.value = State.Error }
                )
            }
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

