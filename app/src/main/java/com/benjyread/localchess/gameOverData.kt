package com.benjyread.localchess

data class gameOverData(var gameOver: Boolean = false) {
    var gameOverMessageId: Int? = null
        set(value) {
            if (gameOver) {
                field = value
            } else {
                field = null
            }
        }

}
