package com.toplu.tavlauygulamasi.core.engine

import com.toplu.tavlauygulamasi.core.models.*
import java.security.SecureRandom

class GameEngine {
    private val random = SecureRandom()

    fun createInitialState(): GameState {
        val board = mutableMapOf<Int, PointState>()
        for (i in 1..24) board[i] = PointState(null, 0)

        // Standard Backgammon Setup
        board[1] = PointState(GameColor.WHITE, 2)
        board[12] = PointState(GameColor.WHITE, 5)
        board[17] = PointState(GameColor.WHITE, 3)
        board[19] = PointState(GameColor.WHITE, 5)

        board[24] = PointState(GameColor.BLACK, 2)
        board[13] = PointState(GameColor.BLACK, 5)
        board[8] = PointState(GameColor.BLACK, 3)
        board[6] = PointState(GameColor.BLACK, 5)

        return GameState(
            board = board,
            currentPlayer = GameColor.WHITE,
            dice = emptyList(),
            bar = mapOf(GameColor.WHITE to 0, GameColor.BLACK to 0),
            bearingOff = mapOf(GameColor.WHITE to 0, GameColor.BLACK to 0),
            matchScore = mapOf(GameColor.WHITE to 0, GameColor.BLACK to 0)
        )
    }

    fun rollDice(): List<Die> {
        val d1 = random.nextInt(6) + 1
        val d2 = random.nextInt(6) + 1
        return if (d1 == d2) {
            listOf(Die(d1), Die(d1), Die(d1), Die(d1))
        } else {
            listOf(Die(d1), Die(d2))
        }
    }

    fun getValidMoves(state: GameState): List<Move> {
        if (state.dice.all { it.isUsed }) return emptyList()
        
        val validMoves = mutableListOf<Move>()
        val currentPlayer = state.currentPlayer
        val unusedDice = state.dice.filter { !it.isUsed }.map { it.value }.distinct()

        // 1. If pieces are on the Bar, they MUST move first
        if (state.bar[currentPlayer] ?: 0 > 0) {
            for (dieValue in unusedDice) {
                val toPoint = if (currentPlayer == GameColor.WHITE) dieValue else 25 - dieValue
                if (isValidTarget(state, toPoint, currentPlayer)) {
                    validMoves.add(Move(0, toPoint, dieValue))
                }
            }
            return validMoves
        }

        // 2. Normal moves
        for (point in 1..24) {
            val pointState = state.board[point]
            if (pointState?.color == currentPlayer) {
                for (dieValue in unusedDice) {
                    val toPoint = if (currentPlayer == GameColor.WHITE) point + dieValue else point - dieValue
                    
                    // Bearing off check
                    if (isBearingOffPossible(state, currentPlayer)) {
                        if (canBearOff(state, point, dieValue, currentPlayer)) {
                            validMoves.add(Move(point, 25, dieValue))
                        }
                    }

                    if (toPoint in 1..24 && isValidTarget(state, toPoint, currentPlayer)) {
                        validMoves.add(Move(point, toPoint, dieValue))
                    }
                }
            }
        }

        return validMoves
    }

    private fun isValidTarget(state: GameState, toPoint: Int, player: GameColor): Boolean {
        val targetPoint = state.board[toPoint] ?: return false
        return targetPoint.color == null || targetPoint.color == player || targetPoint.count == 1
    }

    private fun isBearingOffPossible(state: GameState, player: GameColor): Boolean {
        if (state.bar[player] ?: 0 > 0) return false
        
        val homeBoardRange = if (player == GameColor.WHITE) 19..24 else 1..6
        val piecesOutsideHome = state.board.filter { (point, pState) ->
            pState.color == player && point !in homeBoardRange
        }
        return piecesOutsideHome.isEmpty()
    }

    private fun canBearOff(state: GameState, fromPoint: Int, dieValue: Int, player: GameColor): Boolean {
        if (player == GameColor.WHITE) {
            val target = fromPoint + dieValue
            if (target == 25) return true
            if (target > 25) {
                // Larger die than needed: only allowed if no pieces are further back
                val furtherBackPieces = (19 until fromPoint).any { state.board[it]?.color == player }
                return !furtherBackPieces
            }
        } else {
            val target = fromPoint - dieValue
            if (target == 0) return true
            if (target < 0) {
                val furtherBackPieces = (fromPoint + 1..6).any { state.board[it]?.color == player }
                return !furtherBackPieces
            }
        }
        return false
    }

    fun applyMove(state: GameState, move: Move): GameState {
        val newBoard = state.board.toMutableMap()
        val newBar = state.bar.toMutableMap()
        val newBearingOff = state.bearingOff.toMutableMap()
        val currentPlayer = state.currentPlayer

        // Remove from source
        if (move.from == 0) {
            newBar[currentPlayer] = (newBar[currentPlayer] ?: 1) - 1
        } else {
            val sourcePoint = newBoard[move.from]!!
            newBoard[move.from] = sourcePoint.copy(count = sourcePoint.count - 1, color = if (sourcePoint.count - 1 == 0) null else sourcePoint.color)
        }

        // Add to destination
        if (move.to == 25) {
            newBearingOff[currentPlayer] = (newBearingOff[currentPlayer] ?: 0) + 1
        } else {
            val targetPoint = newBoard[move.to]!!
            if (targetPoint.color != null && targetPoint.color != currentPlayer && targetPoint.count == 1) {
                // HIT!
                newBar[targetPoint.color] = (newBar[targetPoint.color] ?: 0) + 1
                newBoard[move.to] = PointState(currentPlayer, 1)
            } else {
                newBoard[move.to] = PointState(currentPlayer, targetPoint.count + 1)
            }
        }

        // Mark die as used
        val newDice = state.dice.toMutableList()
        val dieIndex = newDice.indexOfFirst { it.value == move.dieUsed && !it.isUsed }
        if (dieIndex != -1) {
            newDice[dieIndex] = newDice[dieIndex].copy(isUsed = true)
        }

        // Check winner
        val isGameOver = newBearingOff[currentPlayer] == 15
        val winner = if (isGameOver) currentPlayer else null
        val newMatchScore = if (isGameOver) {
            state.matchScore.toMutableMap().apply {
                this[currentPlayer] = (this[currentPlayer] ?: 0) + 1
            }
        } else state.matchScore

        return state.copy(
            board = newBoard,
            bar = newBar,
            bearingOff = newBearingOff,
            dice = newDice,
            matchScore = newMatchScore,
            isGameOver = isGameOver,
            winner = winner
        )
    }
    fun isTurnFinished(state: GameState): Boolean {
        return state.dice.all { it.isUsed } || getValidMoves(state).isEmpty()
    }
}
