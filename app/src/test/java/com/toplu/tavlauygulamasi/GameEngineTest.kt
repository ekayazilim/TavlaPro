package com.toplu.tavlauygulamasi

import com.toplu.tavlauygulamasi.core.engine.GameEngine
import com.toplu.tavlauygulamasi.core.models.Die
import com.toplu.tavlauygulamasi.core.models.GameColor
import com.toplu.tavlauygulamasi.core.models.Move
import org.junit.Test
import org.junit.Assert.*

class GameEngineTest {

    private val engine = GameEngine()

    @Test
    fun testInitialState() {
        val state = engine.createInitialState()
        assertEquals(2, state.board[1]?.count)
        assertEquals(GameColor.WHITE, state.board[1]?.color)
        assertEquals(2, state.board[24]?.count)
        assertEquals(GameColor.BLACK, state.board[24]?.color)
        assertEquals(0, state.bar[GameColor.WHITE])
        assertEquals(0, state.bar[GameColor.BLACK])
    }

    @Test
    fun testDiceRollDoubles() {
        // Since it's random, we can't test values easily, but we can verify mechanics
        val dice = engine.rollDice()
        if (dice.size == 4) {
            assertTrue(dice.all { it.value == dice[0].value })
        } else {
            assertEquals(2, dice.size)
        }
    }

    @Test
    fun testValidMovesFromStart() {
        var state = engine.createInitialState()
        state = state.copy(dice = listOf(Die(1), Die(3)))
        
        val moves = engine.getValidMoves(state)
        
        // White can move from 1 to 2, 1 to 4, 12 to 13, 12 to 15, etc.
        assertTrue(moves.any { it.from == 1 && it.to == 2 })
        assertTrue(moves.any { it.from == 1 && it.to == 4 })
        assertTrue(moves.any { it.from == 12 && it.to == 13 })
    }

    @Test
    fun testHitMechanic() {
        var state = engine.createInitialState()
        // Put a single BLACK checker on point 2
        val board = state.board.toMutableMap()
        board[2] = board[2]!!.copy(color = GameColor.BLACK, count = 1)
        state = state.copy(board = board, dice = listOf(Die(1)))
        
        val move = Move(1, 2, 1) // White moves from 1 to 2, hitting Black
        val newState = engine.applyMove(state, move)
        
        assertEquals(GameColor.WHITE, newState.board[2]?.color)
        assertEquals(1, newState.board[2]?.count)
        assertEquals(1, newState.bar[GameColor.BLACK])
    }
}
