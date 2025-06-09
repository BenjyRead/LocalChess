package com.example.mob_dev_portfolio

import StockfishEngine
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.mob_dev_portfolio.ui.theme.MobdevportfolioTheme
import kotlinx.coroutines.launch

class EngineInitialChoices : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val stockfish = StockfishEngine
            val outputChannel = stockfish.outputChannel
            val inputChannel = stockfish.inputChannel
            val stockfishOutput = remember { mutableStateOf("") };
            stockfish.start(this)
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    while (stockfishOutput.value.isEmpty()) {
                        // Wait for the engine to output something
                        inputChannel.send("uci")
                        stockfishOutput.value = outputChannel.receive()
                        Log.d("StockfishOutput", stockfishOutput.value)
                    }
                    Log.d(
                        "StockfishOutput",
                        "Engine started successfully: ${stockfishOutput.value}"
                    )
                }
            }
            MobdevportfolioTheme {
                Text(
                    text = stockfishOutput.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "^^^",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}