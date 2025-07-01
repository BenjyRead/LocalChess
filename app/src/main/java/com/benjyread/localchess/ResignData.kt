package com.benjyread.localchess

import com.github.bhlangonijr.chesslib.Side

data class ResignData(
    val side: Side,
    val resigning: Boolean = false,
    val confirmed: Boolean = false
) {
}