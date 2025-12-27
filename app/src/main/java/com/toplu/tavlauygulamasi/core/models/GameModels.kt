package com.toplu.tavlauygulamasi.core.models

enum class GameColor {
    WHITE, BLACK;

    fun opponent(): GameColor = if (this == WHITE) BLACK else WHITE
}

data class Die(val value: Int, val isUsed: Boolean = false)

data class GameState(
    val board: Map<Int, PointState>, // 1 to 24
    val currentPlayer: GameColor,
    val dice: List<Die>,
    val bar: Map<GameColor, Int>, // Number of checkers on bar for each color
    val bearingOff: Map<GameColor, Int>, // Number of checkers borne off
    val matchScore: Map<GameColor, Int> = mapOf(GameColor.WHITE to 0, GameColor.BLACK to 0),
    val isGameOver: Boolean = false,
    val winner: GameColor? = null
)

data class PointState(
    val color: GameColor?,
    val count: Int
)

data class Move(
    val from: Int, // 0 for bar, 1-24 for board
    val to: Int,   // 1-24 for board, 25 for bearing off
    val dieUsed: Int
)
