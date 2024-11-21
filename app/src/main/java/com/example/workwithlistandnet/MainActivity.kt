package com.example.workwithlistandnet

import android.content.Intent
import android.os.Bundle
import androidx.compose.material3.Button
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.workwithlistandnet.content.Internet

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainWindow()
        }
    }
}

@Preview
@Composable
fun MainWindow() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Hello! Please select the appropriate option."
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Button(onClick = {
                    context.startActivity(Intent(context, Internet::class.java))
                }) {
                    Text(text = "Generate pictures from internet")
                }
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Generate pictures from AI")
                }
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Generate gifs from internet")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainWindow()
}