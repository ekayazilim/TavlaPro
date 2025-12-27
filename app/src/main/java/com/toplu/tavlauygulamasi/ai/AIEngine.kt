package com.toplu.tavlauygulamasi.ai

import com.toplu.tavlauygulamasi.core.engine.GameEngine
import com.toplu.tavlauygulamasi.core.models.GameState
import com.toplu.tavlauygulamasi.core.models.Move
import com.toplu.tavlauygulamasi.core.models.PointState
import com.toplu.tavlauygulamasi.core.models.GameColor

enum class AIDifficulty {
    EASY, MEDIUM, HARD
}

class AIEngine(private val engine: GameEngine) {

    fun calculateBestMove(state: GameState, difficulty: AIDifficulty): Move? {
        val validMoves = engine.getValidMoves(state)
        if (validMoves.isEmpty()) return null

        return when (difficulty) {
            AIDifficulty.EASY -> validMoves.random()
            AIDifficulty.MEDIUM -> calculateHeuristicMove(state, validMoves)
            AIDifficulty.HARD -> calculateMinimaxMove(state, validMoves)
        }
    }

    private fun calculateHeuristicMove(state: GameState, moves: List<Move>): Move {
        // Simple scoring:
        // 1. Hitting is good (+100)
        // 2. Making a point (2+ checkers) is good (+50)
        // 3. Staying exposed (blot) is bad (-30)
        // 4. Moving closer to home is okay (+1 per step)
        
        return moves.maxByOrNull { move ->
            evaluateMove(state, move)
        } ?: moves.random()
    }

    private fun evaluateMove(state: GameState, move: Move): Int {
        var score = 0
        val targetPoint = state.board[move.to]
        
        // Is it a hit?
        if (targetPoint != null && targetPoint.color == state.currentPlayer.opponent() && targetPoint.count == 1) {
            score += 100
        }
        
        // Making a point (or reinforcing one)
        if (targetPoint != null && targetPoint.color == state.currentPlayer) {
            score += 50
        }

        // Bearing off
        if (move.to == 25) {
            score += 150
        }

        return score
    }

    private fun calculateMinimaxMove(state: GameState, moves: List<Move>): Move {
        // Placeholder for HARD: currently uses better heuristic or 1-step lookahead
        return calculateHeuristicMove(state, moves)
    }
}
