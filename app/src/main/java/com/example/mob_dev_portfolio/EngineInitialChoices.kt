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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EngineInitialChoices : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stockfish = StockfishEngine
        stockfish.start(this)
        setContent {
            val outputChannel = stockfish.outputChannel
            val inputChannel = stockfish.inputChannel
            val stockfishOutputFull = remember { mutableStateOf("") };
            LaunchedEffect(Unit) {
                CoroutineScope(Dispatchers.IO).launch {
                    for (output in outputChannel) {
                        Log.d("EngineChoices", "Stockfish Output: $output")
                        stockfishOutputFull.value += output + "\n"
                    }
                }
            }
//            LaunchedEffect(Unit) {
//                CoroutineScope(Dispatchers.IO).launch {
//                    for (input in inputChannel) {
//                        Log.d("EngineChoices", "Input: $input")
//                    }
//                }
//            }
            LaunchedEffect(Unit) {
                inputChannel.send("position startpos")
                inputChannel.send("go depth 1")
            }
            MobdevportfolioTheme {
                Text(
                    text = stockfishOutputFull.value,
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