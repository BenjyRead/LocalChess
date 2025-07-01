package com.benjyread.localchess

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.benjyread.localchess.ui.theme.MobdevportfolioTheme
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square

enum class GameState {
    IN_GAME,
    EXITING,
    SAVING
}

class PlayLocally : ComponentActivity() {
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)

        setContent {
            val highlightedSquares = remember { mutableStateOf<Set<Square>>(emptySet()) }
            val selectedSquare = remember { mutableStateOf<Square?>(null) }
            val gameOverData = remember { mutableStateOf(gameOverData()) }
            val promotionData = remember { mutableStateOf(PromotionData()) }

            val mainColor = intent.getStringExtra("color")
            val oppositionColor =
                    when (intent.getStringExtra("opponentColor")) {
                        "WHITE" -> Side.WHITE
                        "BLACK" -> Side.BLACK
                        null ->
                                when (mainColor) {
                                    "WHITE" -> Side.BLACK
                                    "BLACK" -> Side.WHITE
                                    "RANDOM" -> listOf(Side.BLACK, Side.WHITE).random()
                                    else ->
                                            throw IllegalArgumentException(
                                                    "Invalid color (mainColor): ${mainColor}"
                                            )
                                }
                        else ->
                                throw IllegalArgumentException(
                                        "Invalid color (opponentColor): ${
                            intent.getStringExtra(
                                "opponentColor"
                            )
                        }"
                                )
                    }

            val timeControlMain = intent.getIntExtra("timeControlMain", 300)
            val increment = intent.getIntExtra("increment", 0)

            val mainPlayerTime = remember {
                mutableIntStateOf(intent.getIntExtra("mainPlayerTime", timeControlMain))
            }
            val opponentPlayerTime = remember {
                mutableIntStateOf(intent.getIntExtra("opponentPlayerTime", timeControlMain))
            }

            val gameState = remember { mutableStateOf(GameState.IN_GAME) }
            val whiteResignData = remember { mutableStateOf(ResignData(Side.WHITE)) }
            val blackResignData = remember { mutableStateOf(ResignData(Side.BLACK)) }
            val offeredDrawData = remember { mutableStateOf(OfferedDrawData()) }

            //            TODO: gpt ass code, please verify
            //            I mean it works? but still check if its good
            onBackPressedDispatcher.addCallback(
                    this,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            gameState.value = GameState.EXITING
                        }
                    }
            )

            MobdevportfolioTheme {
                Log.d("PlayLocally", "PlayLocally")
                val board = remember {
                    mutableStateOf(
                            Board().apply {
                                intent.getStringExtra("boardFEN")?.let { loadFromFen(it) }
                            }
                    )
                }

                Scaffold(
                        topBar = { TopBar(soundManager, gameState) },
                        bottomBar = { BottomBar(gameState) },
                ) { contentPadding ->
                    ChessScreen(
                            LocalContext.current,
                            timeControlMain,
                            increment,
                            contentPadding,
                            soundManager,
                            board,
                            highlightedSquares.value,
                            oppositionColor,
                            mainPlayerTime,
                            opponentPlayerTime,
                            promotionData,
                            gameOverData,
                            gameState,
                            whiteResignData,
                            blackResignData,
                            offeredDrawData,
                    ) { square ->
                        handleBoardClick(
                                selectedSquare,
                                highlightedSquares,
                                mainPlayerTime,
                                opponentPlayerTime,
                                oppositionColor,
                                increment,
                                promotionData,
                                gameOverData,
                                board,
                                square,
                                soundManager
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.releaseAll()
    }
}
