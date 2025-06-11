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
        setContent {
            MobdevportfolioTheme {
                val pieceColor = remember { mutableStateOf<ColorChoice?>(null) }
                val timeControlSelected = remember { mutableStateOf<TimeControl?>(null) }
                val timeControlMainInSeconds = remember { mutableStateOf(300) }
                val incrementInSeconds = remember { mutableStateOf(0) }
                val eloSelected = remember { mutableStateOf<Elo?>(null) }
                val stockfishElo = remember { mutableStateOf<Int?>(null) }
                val error = remember { mutableStateOf<Error?>(null) }
                EngineInitialChoices(
                    pieceColor,
                    timeControlSelected,
                    timeControlMainInSeconds,
                    incrementInSeconds,
                    eloSelected,
                    stockfishElo,
                    error,
                    EngineGame::class.java
                )
            }
        }
    }
}