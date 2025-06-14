package com.example.mob_dev_portfolio

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

@Composable
fun EngineChessScreen(
    timeControlMain: Int,
    increment: Int,
    contentPadding: PaddingValues,
    soundManager: SoundManager,
    board: MutableState<Board>,
    highlightedSquares: Set<Square>,
    playerColor: Side,
    playerTime: MutableState<Int>,
    engineTime: MutableState<Int>,
    promotionData: MutableState<PromotionData>,
    gameOverData: MutableState<gameOverData>,
    gameState: MutableState<GameState>,
    playerResignData: MutableState<ResignData>,
    onSquareClick: (Square) -> Unit
) {
    val context = LocalContext.current

    val engineColor = if (playerColor == Side.WHITE) Side.BLACK else Side.WHITE

    LaunchedEffects(
        playerTime,
        engineTime,
        gameOverData,
        board,
        engineColor,
        soundManager,
    )

    if (playerTime.value <= 0) {
        gameOverData.value.gameOver = true
        when (engineColor) {
            Side.WHITE -> gameOverData.value.gameOverMessageId = R.string.White_Wins
            Side.BLACK -> gameOverData.value.gameOverMessageId = R.string.Black_Wins
        }
    } else if (engineTime.value <= 0) {
        gameOverData.value.gameOver = true
        when (engineColor) {
            Side.WHITE -> gameOverData.value.gameOverMessageId = R.string.Black_Wins
            Side.BLACK -> gameOverData.value.gameOverMessageId = R.string.White_Wins
        }
    }

    Box() {
        val blurBoard by animateDpAsState(
            targetValue = if (gameState.value in listOf(
                    GameState.SAVING,
                    GameState.EXITING
                ) || gameOverData.value.gameOver
            ) 5.dp else 0.dp,
            animationSpec = tween(durationMillis = 1000)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .blur(
                    blurBoard
                )
                .clickable { gameState.value = GameState.IN_GAME },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlayerEngineRow(
                engineTime.value,
                engineColor,
                board.value,
                null
            )
            EnginePlaySection(
                board,
                highlightedSquares,
                playerColor,
                promotionData,
                gameOverData,
                onSquareClick
            )
            PlayerEngineRow(
                playerTime.value,
                playerColor,
                board.value,
                playerResignData
            )
        }

    }

}

@Composable
fun PlayerEngineRow(
    time: Int,
    side: Side,
    board: Board,
    resignData: MutableState<ResignData>?,
) {
    Column {
        Row(
            modifier = Modifier.width(320.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DisplayTime(time)
            DisplayCapturedPieces(side, board)//, oppositionColor == side)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (resignData != null) {
                    ResignButton(resignData, side)
                }
            }
        }
    }
}

@Composable
fun EnginePlaySection(
    board: MutableState<Board>,
    highlightedSquares: Set<Square>,
    playerColor: Side,
    promotionData: MutableState<PromotionData>,
    gameOverData: MutableState<gameOverData>,
    onSquareClick: (Square) -> Unit
) {

    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {

        EngineChessBoard(
            board = board.value,
            highlightedSquares = highlightedSquares,
            promotionData = promotionData.value,
            onSquareClick = onSquareClick
        )

        if (promotionData.value.midPromotion) {
            EnginePromotionChoice(

            )
        }
    }

}

@Composable
fun EngineChessBoard(
    board: Board,
    highlightedSquares: Set<Square>,
    promotionData: PromotionData,
    onSquareClick: (Square) -> Unit,
) {
    val blurBoard by animateDpAsState(
        targetValue = if (promotionData.midPromotion) 5.dp else 0.dp,
        animationSpec = tween(durationMillis = if (promotionData.midPromotion) 1000 else 300)
    )

    Column(
        modifier = Modifier
            .blur(blurBoard),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        for (row in 7 downTo 0) {
            Row {
                for (col in 0..7) {
                    val square = Square.entries[row * 8 + col]
                    val piece = board.getPiece(square)

                    val highlighted = highlightedSquares.contains(square)
                    ChessTile(
                        square,
                        piece,
                        highlighted = highlighted,
                        onClick = {
                            onSquareClick(square)
                        }
                    )

                }

            }
        }
    }
}

fun engineHandleBoardClick(
    square: Square,
    selectedSquare: MutableState<Square?>,
    highlightedSquares: MutableState<Set<Square>>,
    playerTime: MutableState<Int>,
    engineTime: MutableState<Int>,
    increment: Int,
    promotionData: MutableState<PromotionData>,
    gameOverData: MutableState<gameOverData>,
    playerColor: Side,
    board: MutableState<Board>,
    soundManager: SoundManager
) {
    if (board.value.sideToMove != playerColor) {
        return
    }

    updatePromotionPiece(selectedSquare, promotionData, board, square)

    val move = Move(selectedSquare.value, square)

    val isLegalMove =
        move in board.value.legalMoves()
    if (isLegalMove) {
        executeMove(
            selectedSquare,
            highlightedSquares,
            playerTime,
            engineTime,
            oppositionColor = if (playerColor == Side.WHITE) Side.BLACK else Side.WHITE,
            increment,
            promotionData,
            gameOverData,
            board,
            square,
            soundManager
        )
    } else {
        highlightLegalMoves(selectedSquare, highlightedSquares, board, square)
    }

}

@Composable
fun EnginePromotionChoice() {

}