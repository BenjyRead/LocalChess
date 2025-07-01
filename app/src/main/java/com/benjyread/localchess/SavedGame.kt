package com.benjyread.localchess

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side

data class SavedGame(
    val board: Board,
    val mainPlayerTime: Int,
    val opponentTime: Int,
    val opponentColor: Side,
    val timeControlMain: Int,
    val increment: Int
)