package com.benjyread.localchess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.benjyread.localchess.ui.theme.MobdevportfolioTheme

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