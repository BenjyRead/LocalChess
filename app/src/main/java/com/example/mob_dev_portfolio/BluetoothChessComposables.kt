package com.example.mob_dev_portfolio

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Rank
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.time.LocalDateTime

@Composable
fun BluetoothDrawButton(
    offeredDrawData: MutableState<OfferedDrawData>,
    side: Side,
    writer: BufferedWriter,
) {
    OutlinedButton(
        onClick = {
            when (side) {
                Side.WHITE -> offeredDrawData.value = offeredDrawData.value.copy(
                    whiteOffered = !offeredDrawData.value.whiteOffered
                )

                Side.BLACK -> offeredDrawData.value = offeredDrawData.value.copy(
                    blackOffered = !offeredDrawData.value.blackOffered
                )
            }

            val offeredDraw = when (side) {
                Side.WHITE -> offeredDrawData.value.whiteOffered
                Side.BLACK -> offeredDrawData.value.blackOffered
            }

            writeData(
                writer,
                JSONObject(
                    mapOf(
                        "draw" to when (offeredDraw) {
                            true -> "activate"
                            false -> "deactivate"
                        },
                    )
                ).toString()
            )
        },


        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
//            containerColor = MaterialTheme.colorScheme.primary,
            containerColor = when (side) {
                Side.WHITE -> if (offeredDrawData.value.whiteOffered) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                Side.BLACK -> if (offeredDrawData.value.blackOffered) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            },
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.offer_draw),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
//            color = when (side) {
//                Side.WHITE -> if (offeredDrawData.value.whiteOffered) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
//                Side.BLACK -> if (offeredDrawData.value.blackOffered) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
//            }
        )
    }
}

@Composable
fun BluetoothLaunchedEffects(
    hostTime: MutableState<Int>,
    joinerTime: MutableState<Int>,
    increment: Int,
    gameOverData: MutableState<gameOverData>,
    board: MutableState<Board>,
    selectedSquare: MutableState<Square?>,
    highlightedSquares: MutableState<Set<Square>>,
    hostColor: Side,
    reader: BufferedReader,
    soundManager: SoundManager,
    whiteResignData: MutableState<ResignData>,
    blackResignData: MutableState<ResignData>,
    promotionData: MutableState<PromotionData>,
    offeredDrawData: MutableState<OfferedDrawData>,
) {


    LaunchedEffect(Unit) {
        while (!gameOverData.value.gameOver) {
            delay(1000)
            if (board.value.sideToMove == hostColor) {
                hostTime.value -= 1
            } else {
                joinerTime.value -= 1
            }
        }

    }

    Log.d("PlayLocally", "recomposition isKingAttacked: ${board.value.isKingAttacked}")
    LaunchedEffect(board.value.isKingAttacked) {
        if (board.value.isKingAttacked) {
            soundManager.playCheckWinSound()
        }
        Log.d("PlayLocally", "isKingAttacked: ${board.value.isKingAttacked}")
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            while (!gameOverData.value.gameOver) {
                var line: String;
                while (reader.readLine().also { line = it } != null) {
                    val json = JSONObject(line)
                    if (json.has("move")) {
                        val moveString = json.getString("move")
                        executeMove(
                            moveString,
                            selectedSquare = selectedSquare,
                            highlightedSquares = highlightedSquares,
                            mainPlayerTime = hostTime,
                            opponentPlayerTime = joinerTime,
                            oppositionColor = hostColor,
                            increment = increment,
                            promotionData = promotionData,
                            gameOverData = gameOverData,
                            board = board,
                            soundManager = soundManager
                        )
//                        board.value.doMove(moveString)
                    }
                    if (json.has("resign")) {
                        val resignData = if (json.getString("resign") == "WHITE") {
                            whiteResignData
                        } else {
                            blackResignData
                        }

                        resignData.value = resignData.value.copy(
                            confirmed = true,
                        )
                    }
                    if (json.has("draw")) {
                        when (json.getString("draw")) {
                            "activate" -> {
                                when (hostColor) {
                                    Side.WHITE -> {
                                        offeredDrawData.value = offeredDrawData.value.copy(
                                            blackOffered = true,
                                        )
                                    }

                                    Side.BLACK -> {
                                        offeredDrawData.value = offeredDrawData.value.copy(
                                            whiteOffered = true,
                                        )
                                    }
                                }
                            }

                            "deactivate" -> {
                                when (hostColor) {
                                    Side.WHITE -> {
                                        offeredDrawData.value = offeredDrawData.value.copy(
                                            blackOffered = false,
                                        )
                                    }

                                    Side.BLACK -> {
                                        offeredDrawData.value = offeredDrawData.value.copy(
                                            whiteOffered = false,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (json.has("timeout")) {
                        gameOverData.value.gameOver = true
                        gameOverData.value.gameOverMessageId = when (json.getString("timeout")) {
                            "WHITE" -> R.string.Black_Wins
                            "BLACK" -> R.string.White_Wins
                            else -> null
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun BluetoothChessScreen(
    increment: Int,
    contentPadding: PaddingValues,
    soundManager: SoundManager,
    gameState: MutableState<GameState>,
    hostTime: MutableIntState,
    joinerTime: MutableIntState,
    board: MutableState<Board>,
    highlightedSquares: MutableState<Set<Square>>,
    hostColor: Side,
    selectedSquare: MutableState<Square?>,
    gameOverData: MutableState<gameOverData>,
    promotionData: MutableState<PromotionData>,
    whiteResignData: MutableState<ResignData>,
    blackResignData: MutableState<ResignData>,
    offeredDrawData: MutableState<OfferedDrawData>,
    writer: BufferedWriter,
    reader: BufferedReader,
    onSquareClick: (Square) -> Unit,
) {
    BluetoothLaunchedEffects(
        hostTime = hostTime,
        joinerTime = joinerTime,
        increment = increment,
        gameOverData = gameOverData,
        board = board,
        selectedSquare = selectedSquare,
        highlightedSquares = highlightedSquares,
        hostColor = hostColor,
        reader = reader,
        soundManager = soundManager,
        whiteResignData = whiteResignData,
        blackResignData = blackResignData,
        promotionData = promotionData,
        offeredDrawData = offeredDrawData,
    )

    if (hostTime.intValue <= 0) {
        gameOverData.value.gameOver = true

        when (hostColor) {
            Side.WHITE -> gameOverData.value.gameOverMessageId = R.string.Black_Wins
            Side.BLACK -> gameOverData.value.gameOverMessageId = R.string.White_Wins
        }

        writeData(
            writer,
            JSONObject().put("timeout", hostColor.name)
        )
    } else if (joinerTime.intValue <= 0) {
        gameOverData.value.gameOver = true

        when (hostColor) {
            Side.WHITE -> gameOverData.value.gameOverMessageId = R.string.White_Wins
            Side.BLACK -> gameOverData.value.gameOverMessageId = R.string.Black_Wins
        }

        writeData(
            writer,
            JSONObject().put(
                "timeout",
                if (hostColor == Side.WHITE) Side.BLACK.name else Side.WHITE.name
            )
        )
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
            JoinerRow(
                time = joinerTime.intValue,
                side = if (hostColor == Side.WHITE) Side.BLACK else Side.WHITE,
                board = board.value,
                offeredDrawData,
            )
            BluetoothPlaySection(
                board = board,
                highlightedSquares = highlightedSquares.value,
                hostColor = hostColor,
                promotionData = promotionData,
                gameOverData = gameOverData,
                writer,
                onSquareClick
            )
            HostMainRow(
                time = hostTime.intValue,
                side = hostColor,
                board = board.value,
                resignData = if (hostColor == Side.WHITE) whiteResignData else blackResignData,
                offeredDrawData = offeredDrawData,
                writer,
            )
        }

        if (whiteResignData.value.confirmed) {
            gameOverData.value.gameOver = true
            gameOverData.value.gameOverMessageId = R.string.Black_Wins
        } else if (blackResignData.value.confirmed) {
            gameOverData.value.gameOver = true
            gameOverData.value.gameOverMessageId = R.string.White_Wins
        }

        if (offeredDrawData.value.blackOffered && offeredDrawData.value.whiteOffered) {
            gameOverData.value.gameOver = true
            gameOverData.value.gameOverMessageId = R.string.draw_by_agreement
        }

        if (gameOverData.value.gameOver) {
            EndGameDialog(
                gameOverData = gameOverData.value,
            )
        }
        if (gameState.value == GameState.EXITING) {
            BluetoothExitDialog(
                gameState = gameState,
                mainResignData = if (hostColor == Side.WHITE) whiteResignData else blackResignData,
                writer,
            )
        }

        if (gameState.value == GameState.SAVING) {
            gameState.value = GameState.IN_GAME
        }


    }

}

@Composable
fun BluetoothExitDialog(
    gameState: MutableState<GameState>,
    mainResignData: MutableState<ResignData>,
    writer: BufferedWriter,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.are_you_sure_you_want_to_exit),
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier.padding(16.dp),
        ) {
            BluetoothExitButton(mainResignData, writer)
            CancelButton(gameState)
        }
    }

}

@Composable
fun BluetoothExitButton(
    mainResignData: MutableState<ResignData>,
    writer: BufferedWriter,
) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            mainResignData.value = mainResignData.value.copy(
                confirmed = true,
            )
            writeData(
                writer,
                JSONObject().put(
                    "resign",
                    mainResignData.value.side.name
                )
            )
            context.startActivity(Intent(context, MainActivity::class.java))
        },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary),
    ) {
        Text(
            text = stringResource(id = R.string.exit),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun HostMainRow(
    time: Int,
    side: Side,
    board: Board,
    resignData: MutableState<ResignData>,
    offeredDrawData: MutableState<OfferedDrawData>,
    writer: BufferedWriter,
) {
    Row(
//        TODO: maybe bad hard coding a dp value this high
        modifier = Modifier.width(320.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
        ) {

            DisplayTime(time)
            BluetoothDrawButton(offeredDrawData, side, writer)
        }

        DisplayCapturedPieces(side, board)

        BluetoothResignButton(resignData, side, writer)
    }
}

@Composable
fun BluetoothResignButton(
    resignData: MutableState<ResignData>,
    side: Side,
    writer: BufferedWriter,
) {
    if (resignData.value.side != side) {
        throw IllegalStateException("Resign button called with wrong side/resign data")
    }
    Column(
        horizontalAlignment = Alignment.End
    ) {
        OutlinedButton(
            onClick = {
                if (resignData.value.resigning) {
                    resignData.value = resignData.value.copy(
                        confirmed = true,
                    )

                    val resignData = JSONObject().put("resign", side.name)
                    writeData(writer, resignData)

                } else {
                    resignData.value = resignData.value.copy(
                        resigning = true,
                    )
                }

            },
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (resignData.value.resigning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.width(90.dp)
        ) {
            Text(
                text = if (resignData.value.resigning) stringResource(R.string.confirm_q) else stringResource(
                    id = R.string.resign
                ),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (resignData.value.resigning) {
            OutlinedButton(
                onClick = {
                    resignData.value = resignData.value.copy(
                        confirmed = false,
                        resigning = false
                    )
                },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (resignData.value.resigning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.width(90.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

}

@Composable
fun JoinerRow(
    time: Int,
    side: Side,
    board: Board,
    offeredDrawData: MutableState<OfferedDrawData>
) {
    Row(
        modifier = Modifier.width(320.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
        ) {
            DrawButton(
                offeredDrawData = offeredDrawData,
                side = side,
                clickable = false
            )
            DisplayTime(time)
        }

        DisplayCapturedPieces(
            side,
            board,
        )

        Box(
            modifier = Modifier
                .width(90.dp)
        ) {}
    }
}

@Composable
fun BluetoothChessBoard(
    board: Board,
    highlightedSquares: Set<Square>,
    promotionData: PromotionData,
    hostColor: Side,
    onSquareClick: (Square) -> Unit,
) {
    val blurBoard by animateDpAsState(
        targetValue = if (promotionData.midPromotion) 5.dp else 0.dp,
        animationSpec = tween(durationMillis = if (promotionData.midPromotion) 1000 else 300)
    )

    Column(
        modifier = Modifier
            .blur(blurBoard)
            .graphicsLayer { rotationZ = if (hostColor == Side.BLACK) 180f else 0f },
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
                        pieceFlipped = hostColor == Side.BLACK,
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

fun bluetoothHandleBoardClick(
    square: Square,
    selectedSquare: MutableState<Square?>,
    highlightedSquares: MutableState<Set<Square>>,
    promotionData: MutableState<PromotionData>,
    board: MutableState<Board>,
    gameOverData: MutableState<gameOverData>,
    hostTime: MutableIntState,
    joinerTime: MutableIntState,
    hostColor: Side,
    increment: Int,
    soundManager: SoundManager,
    writer: BufferedWriter,
) {
    if (board.value.sideToMove != hostColor) {
        return
    }

    updatePromotionPiece(selectedSquare, promotionData, board, square)

    val move = Move(selectedSquare.value, square)

    val isLegalMove =
        move in board.value.legalMoves()
    if (isLegalMove) {
        bluetoothExecuteMove(
            square,
            selectedSquare,
            highlightedSquares,
            promotionData,
            board,
            gameOverData,
            hostTime,
            joinerTime,
            hostColor,
            increment,
            soundManager,
            writer
        )
    } else {
        highlightLegalMoves(selectedSquare, highlightedSquares, board, square)
    }

    updateKingCheckHighlight(board.value, highlightedSquares)
}

fun bluetoothExecuteMove(
    square: Square,
    selectedSquare: MutableState<Square?>,
    highlightedSquares: MutableState<Set<Square>>,
    promotionData: MutableState<PromotionData>,
    board: MutableState<Board>,
    gameOverData: MutableState<gameOverData>,
    hostTime: MutableIntState,
    joinerTime: MutableIntState,
    hostColor: Side,
    increment: Int,
    soundManager: SoundManager,
    writer: BufferedWriter,
) {
    if (selectedSquare.value == null) {
        return
    }


    val move = Move(selectedSquare.value, square)


    if (board.value.sideToMove == hostColor) {
        hostTime.value += increment
    } else {
        joinerTime.value += increment
    }

    board.value.doMove(move)
    writeData(
        writer,
        JSONObject()
            .put("move", move.toString())
    )
    soundManager.playMoveSound()
    selectedSquare.value = null
    highlightedSquares.value = emptySet()
    promotionData.value = PromotionData()


    checkEndGameConditions(board, gameOverData)

    Log.d("executeMove", "board: ${board.value}")
}

@Composable
fun BluetoothPlaySection(
    board: MutableState<Board>,
    highlightedSquares: Set<Square>,
    hostColor: Side,
    promotionData: MutableState<PromotionData>,
    gameOverData: MutableState<gameOverData>,
    writer: BufferedWriter,
    onSquareClick: (Square) -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
    ) {
        BluetoothChessBoard(
            board = board.value,
            highlightedSquares = highlightedSquares,
            promotionData = promotionData.value,
            hostColor = hostColor,
            onSquareClick = if (!promotionData.value.midPromotion) onSquareClick else { _ ->
            }
        )

//        if (promotionData.value.midPromotion && promotionData.value.promotionPiece?.pieceSide == hostColor) {
        if (promotionData.value.midPromotion) {
            BluetoothPromotionChoice(
                gameOverData = gameOverData,
                board = board,
                hostColor = hostColor,
                promotionData = promotionData,
                writer
            )
        }

    }

}

@Composable
fun BluetoothPromotionChoice(
    gameOverData: MutableState<gameOverData>,
    board: MutableState<Board>,
    hostColor: Side,
    promotionData: MutableState<PromotionData>,
    writer: BufferedWriter
) {
    val sideToMove = board.value.sideToMove

    if (promotionData.value.pawnSquare == null || promotionData.value.promotionSquare == null || !promotionData.value.midPromotion || sideToMove != hostColor) {
        return
    }

    val promotionOptions = if (hostColor == Side.WHITE) {

        listOf(
            Piece.WHITE_QUEEN,
            Piece.WHITE_ROOK,
            Piece.WHITE_BISHOP,
            Piece.WHITE_KNIGHT
        )
    } else {
        listOf(
            Piece.BLACK_QUEEN,
            Piece.BLACK_ROOK,
            Piece.BLACK_BISHOP,
            Piece.BLACK_KNIGHT
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = stringResource(id = R.string.Promote_To),
            modifier = Modifier,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,

            ) {

            for (piece in promotionOptions) {
                Image(
                    painter = painterResource(id = getPieceImageFromFen(piece.fenSymbol)!!),
                    contentDescription = piece.fenSymbol,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            promotionData.value.promotionPiece = piece
                            bluetoothExecutePromotion(
                                board,
                                promotionData,
                                gameOverData,
                                writer
                            )
                        }
                )
            }
        }
    }

}

fun bluetoothExecutePromotion(
    board: MutableState<Board>,
    promotionData: MutableState<PromotionData>,
    gameOverData: MutableState<gameOverData>,
    writer: BufferedWriter,
) {
    val move = Move(
        promotionData.value.pawnSquare!!,
        promotionData.value.promotionSquare,
        promotionData.value.promotionPiece
    )

    board.value.doMove(move)
    writeData(
        writer,
        JSONObject()
            .put("move", move.toString())
    )

    promotionData.value = PromotionData()

    checkEndGameConditions(board, gameOverData)
}

fun writeData(
    writer: BufferedWriter,
    data: JSONObject
) {
    writer.write(data.toString())
    writer.newLine()
    writer.flush()
}

fun writeData(
    writer: BufferedWriter,
    data: String
) {
    writer.write(data)
    writer.newLine()
    writer.flush()
}
