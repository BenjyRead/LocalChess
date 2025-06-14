package com.example.mob_dev_portfolio

import StockfishEngine
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.mob_dev_portfolio.ui.theme.MobdevportfolioTheme
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square

class EngineGame : ComponentActivity() {
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)
        val stockfish = StockfishEngine
        stockfish.start(this, "nn-1c0000000000.nnue", "nn-37f18f62d772.nnue")
        val playerColor = when (intent.getStringExtra("color")) {
            "WHITE" -> Side.WHITE
            "BLACK" -> Side.BLACK
            "RANDOM" -> listOf(Side.BLACK, Side.WHITE).random()
            else -> Side.WHITE
        }
        val timeControlMain = intent.getIntExtra("timeControlMain", 300)
        val increment = intent.getIntExtra("increment", 0)
        val stockfishElo = intent.getIntExtra("elo", -1)
        if (stockfishElo == -1) {
            throw IllegalArgumentException("No stockfish elo provided")
        }
        setContent {
            val outputChannel = stockfish.outputChannel
            val inputChannel = stockfish.inputChannel
            val highlightedSquares = remember { mutableStateOf<Set<Square>>(emptySet()) }
            val selectedSquare = remember { mutableStateOf<Square?>(null) }
            val gameOverData = remember { mutableStateOf(gameOverData()) }
            val promotionData = remember { mutableStateOf(PromotionData()) }

            val gameState = remember { mutableStateOf(GameState.IN_GAME) }
            val playerResignData = remember { mutableStateOf(ResignData(playerColor)) }
            val playerTime = remember {
                mutableIntStateOf(timeControlMain)
            }
            val engineTime = remember {
                mutableIntStateOf(timeControlMain)
            }

            val board = remember {
                mutableStateOf(Board().apply {
                    intent.getStringExtra("boardFEN")?.let { loadFromFen(it) }
                })
            }
            MobdevportfolioTheme {
                Scaffold(
                    topBar = {

                    },
                    bottomBar = {

                    },
                ) { contentPadding ->
                    EngineChessScreen(
                        timeControlMain = timeControlMain,
                        increment = increment,
                        contentPadding = contentPadding,
                        soundManager = soundManager,
                        board = board,
                        highlightedSquares = highlightedSquares.value,
                        playerColor = playerColor,
                        playerTime = playerTime,
                        engineTime = engineTime,
                        promotionData = promotionData,
                        gameOverData = gameOverData,
                        gameState = gameState,
                        playerResignData = playerResignData,
                        onSquareClick = { square ->
                            engineHandleBoardClick(
                                square = square,
                                board = board,
                                selectedSquare = selectedSquare,
                                highlightedSquares = highlightedSquares,
                                playerTime = playerTime,
                                engineTime = engineTime,
                                increment = increment,
                                promotionData = promotionData,
                                gameOverData = gameOverData,
                                playerColor = playerColor,
                                soundManager = soundManager
                            )
                        }
                    )

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.releaseAll()
    }
}
