package com.toplu.tavlauygulamasi.ui.board

import android.app.Application
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toplu.tavlauygulamasi.R
import com.toplu.tavlauygulamasi.ai.AIDifficulty
import com.toplu.tavlauygulamasi.ai.AIEngine
import com.toplu.tavlauygulamasi.core.engine.GameEngine
import com.toplu.tavlauygulamasi.core.models.GameColor
import com.toplu.tavlauygulamasi.core.models.GameState
import com.toplu.tavlauygulamasi.core.models.Move
import com.toplu.tavlauygulamasi.core.models.PointState
import com.toplu.tavlauygulamasi.data.db.MatchDao
import com.toplu.tavlauygulamasi.data.db.MatchResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.toplu.tavlauygulamasi.ui.theme.GameTheme
import com.toplu.tavlauygulamasi.ui.theme.GameThemes

class GameViewModel(application: Application, private val matchDao: MatchDao) : AndroidViewModel(application) {
    private val engine = GameEngine()
    private val aiEngine = AIEngine(engine)

    private val _currentTheme = MutableStateFlow(GameThemes.Classic)
    val currentTheme = _currentTheme.asStateFlow()

    fun setTheme(theme: GameTheme) {
        _currentTheme.value = theme
    }

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Placeholder for sounds - To enable sounds:
        // 1. Add dice_roll.wav, piece_move.wav, piece_hit.wav, win.wav to res/raw
        // 2. Uncomment the lines below:
        /*
        soundMap["dice"] = soundPool.load(application, R.raw.dice_roll, 1)
        soundMap["move"] = soundPool.load(application, R.raw.piece_move, 1)
        soundMap["hit"] = soundPool.load(application, R.raw.piece_hit, 1)
        soundMap["win"] = soundPool.load(application, R.raw.win, 1)
        */
    }

    private fun playSound(key: String) {
        soundMap[key]?.let { id ->
            soundPool.play(id, 1f, 1f, 0, 0, 1f)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPool.release()
    }

    private val _isAiMode = MutableStateFlow(true)
    val isAiMode = _isAiMode.asStateFlow()

    fun setAiMode(isAi: Boolean) {
        _isAiMode.value = isAi
    }

    private val _aiDifficulty = MutableStateFlow(AIDifficulty.MEDIUM)
    val aiDifficulty = _aiDifficulty.asStateFlow()

    fun setAiDifficulty(difficulty: AIDifficulty) {
        _aiDifficulty.value = difficulty
    }

    private val _gameState = MutableStateFlow(engine.createInitialState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _validMoves = MutableStateFlow<List<Move>>(emptyList())
    val validMoves: StateFlow<List<Move>> = _validMoves.asStateFlow()

    private val _selectedPoint = MutableStateFlow<Int?>(null)
    val selectedPoint: StateFlow<Int?> = _selectedPoint.asStateFlow()

    private val _highlightedPoints = MutableStateFlow<Set<Int>>(emptySet())
    val highlightedPoints: StateFlow<Set<Int>> = _highlightedPoints.asStateFlow()

    private val _aiHint = MutableStateFlow<Move?>(null)
    val aiHint: StateFlow<Move?> = _aiHint.asStateFlow()

    private val _lastMove = MutableStateFlow<Move?>(null)
    val lastMove: StateFlow<Move?> = _lastMove.asStateFlow()

    fun getHint() {
        if (_gameState.value.dice.any { !it.isUsed }) {
            _aiHint.value = aiEngine.calculateBestMove(_gameState.value, AIDifficulty.HARD)
            viewModelScope.launch {
                delay(2000)
                _aiHint.value = null
            }
        }
    }

    private val _moveHistory = MutableStateFlow<List<GameState>>(emptyList())
    val canUndo: StateFlow<Boolean> = _moveHistory.asStateFlow().map { it.isNotEmpty() && !engine.isTurnFinished(_gameState.value) }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    private val _rollingDice = MutableStateFlow<List<Int>>(emptyList())
    val rollingDice: StateFlow<List<Int>> = _rollingDice.asStateFlow()

    fun onRollDice() {
        if (_isRolling.value) return
        
        viewModelScope.launch {
            _isRolling.value = true
            val startTime = System.currentTimeMillis()
            val duration = 2500L // 2.5 seconds spinning
            
            while (System.currentTimeMillis() - startTime < duration) {
                _rollingDice.value = listOf((1..6).random(), (1..6).random())
                playSound("dice")
                delay(150) // Synchronized with sound
            }
            
            // Final roll from engine
            val dice = engine.rollDice()
            _gameState.value = _gameState.value.copy(dice = dice)
            _rollingDice.value = dice.map { it.value }
            _isRolling.value = false
            updateValidMoves()
        }
    }

    fun onPointClick(point: Int) {
        val currentSelected = _selectedPoint.value
        val currentPlayer = _gameState.value.currentPlayer

        if (currentSelected == null) {
            // Trying to select a piece
            val pointState = _gameState.value.board[point] ?: (if (point == 0) PointState(currentPlayer, _gameState.value.bar[currentPlayer] ?: 0) else null)
            
            if (pointState?.color == currentPlayer && pointState.count > 0) {
                // Must move from bar if there are pieces there
                val hasBarPieces = (_gameState.value.bar[currentPlayer] ?: 0) > 0
                if (hasBarPieces && point != 0) {
                    // Invalid selection: must move from bar first
                    return
                }
                
                _selectedPoint.value = point
                _highlightedPoints.value = _validMoves.value
                    .filter { it.from == point }
                    .map { it.to }
                    .toSet()
            }
        } else {
            // Trying to move selected piece to 'point'
            val move = _validMoves.value.find { it.from == currentSelected && it.to == point }
            if (move != null) {
                onMove(move)
                _selectedPoint.value = null
                _highlightedPoints.value = emptySet()
            } else {
                // Deselect or select another one
                _selectedPoint.value = null
                _highlightedPoints.value = emptySet()
                onPointClick(point)
            }
        }
    }

    fun onMove(move: Move) {
        _lastMove.value = move
        // Save current state to history before moving
        _moveHistory.value = _moveHistory.value + _gameState.value
        
        val isHit = move.to != 25 && _gameState.value.board[move.to]?.let { it.color != null && it.color != _gameState.value.currentPlayer && it.count == 1 } ?: false
        if (isHit) playSound("hit") else playSound("move")

        val newState = engine.applyMove(_gameState.value, move)
        _gameState.value = newState
        _selectedPoint.value = null
        _highlightedPoints.value = emptySet()
        updateValidMoves()
        
        if (newState.isGameOver) {
            playSound("win")
            saveGameResult(newState)
            return
        }
        
        if (_validMoves.value.isEmpty() && _gameState.value.dice.any { !it.isUsed }) {
            nextTurn()
        } else if (_gameState.value.dice.all { it.isUsed }) {
            nextTurn()
        }
    }

    fun onUndo() {
        if (_moveHistory.value.isNotEmpty()) {
            val lastState = _moveHistory.value.last()
            _gameState.value = lastState
            _moveHistory.value = _moveHistory.value.dropLast(1)
            _selectedPoint.value = null
            _highlightedPoints.value = emptySet()
            updateValidMoves()
        }
    }

    private fun nextTurn() {
        val nextPlayer = _gameState.value.currentPlayer.opponent()
        _gameState.value = _gameState.value.copy(
            currentPlayer = nextPlayer,
            dice = emptyList()
        )
        _validMoves.value = emptyList()
        _moveHistory.value = emptyList() // Reset history for new player turn

        if (_isAiMode.value && nextPlayer == GameColor.BLACK) {
            triggerAiTurn()
        }
    }

    private fun triggerAiTurn() {
        viewModelScope.launch {
            delay(1000) // Realistic delay
            onRollDice()
            delay(500)
            
            while (_gameState.value.dice.any { !it.isUsed } && !_gameState.value.isGameOver) {
                val bestMove = aiEngine.calculateBestMove(_gameState.value, _aiDifficulty.value)
                if (bestMove != null) {
                    onMove(bestMove)
                    delay(800)
                } else {
                    break // No more valid moves
                }
            }
        }
    }

    fun resetGame() {
        _gameState.value = engine.createInitialState()
        _validMoves.value = emptyList()
        _selectedPoint.value = null
        _highlightedPoints.value = emptySet()
    }

    private fun saveGameResult(state: GameState) {
        viewModelScope.launch {
            val winner = if (state.winner == GameColor.WHITE) "BEYAZ" else "SİYAH"
            val loser = if (state.winner == GameColor.WHITE) "SİYAH" else "BEYAZ"
            matchDao.insertMatch(
                MatchResult(
                    winner = winner,
                    loser = loser,
                    mode = if (_isAiMode.value) "AI" else "LOCAL_PVP"
                )
            )
        }
    }

    private fun updateValidMoves() {
        _validMoves.value = engine.getValidMoves(_gameState.value)
    }
}
