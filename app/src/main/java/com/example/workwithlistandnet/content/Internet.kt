package com.example.workwithlistandnet.content

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.workwithlistandnet.net.getImage
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.workwithlistandnet.R
import kotlinx.coroutines.delay


class Internet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainWindow() }
    }
}

@Composable
fun LoadingWithDots() {
    val dots = rememberSaveable { mutableStateOf(".") }

    LaunchedEffect(Unit) {
        while (true) {
            dots.value = when (dots.value) {
                "." -> ".."
                ".." -> "..."
                "..." -> "."
                else -> "."
            }
            delay(500)
        }
    }

    Text(
        text = "Загрузка${dots.value}",
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
fun MainWindow() {
    val result = rememberSaveable { mutableStateOf("") }
    val error = rememberSaveable { mutableIntStateOf(0) }
    val loading = rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(result.value == "") {
        try {
            val image = getImage()
            result.value = image
        } catch (e: Exception) {
            error.intValue = 1
            Log.d("Error", e.toString())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.wrapContentSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                result.value = ""
                error.intValue = 0
                loading.value = true
            }) { Text(text = "Перегенерировать вайфу") }
            if (error.intValue == 0) {
                if (loading.value)
                    LoadingWithDots()
                else
                    Text(text = "Вот что именно получилось")
            } else {
                Text(text = "Что то не очень получилось")
            }
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (error.intValue == 0) {
                AsyncImage(
                    model = result.value,
                    contentDescription = null,
                    placeholder = painterResource(
                        id = R.drawable.cat
                    ),
                    onSuccess = { loading.value = false }
                )

            } else {
                Image(
                    painter = painterResource(id = R.drawable.cat), contentDescription = null
                )
            }
        }

    }
}