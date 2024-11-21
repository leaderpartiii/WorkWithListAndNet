package com.example.workwithlistandnet.content

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.workwithlistandnet.net.getImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.workwithlistandnet.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class Internet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainWindow() }
    }
}

@Composable
fun MainWindow() {
    val result = remember { mutableStateOf("") }
    val error = remember { mutableIntStateOf(0) }
    val loading = remember { mutableStateOf(true) }
    if (loading.value == true)
        LaunchedEffect(Unit) {
            try {
                val image = getImage()
                result.value = image
            } catch (e: Exception) {
                error.intValue = 1
                Log.d("Error", e.toString())
            } finally {
                loading.value = false
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
                    )
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.cat), contentDescription = null
                )
            }
        }

    }
}