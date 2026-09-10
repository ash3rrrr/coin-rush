package com.coinrush.game

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Phase { MENU, PLAYING, GAME_OVER }

data class UiState(
    val phase: Phase = Phase.MENU,
    val playerLane: Int = GameEngine.PLAYER_LANE_START,
    val score: Int = 0,
    val coins: Int = 0,
    val obstacles: List<ObstacleSnapshot> = emptyList(),
    val bestScore: Int = 0,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var uiState by mutableStateOf(UiState())
        private set

    private val _showInterstitial =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val showInterstitial: SharedFlow<Unit> = _showInterstitial

    private val _showRewarded =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val showRewarded: SharedFlow<Unit> = _showRewarded

    private val engine = GameEngine()
    private var bestScore = prefs.getInt(KEY_BEST_SCORE, 0)
    private var gamesSinceInterstitial = 0

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(GameEngine.TICK_MS)
                if (uiState.phase == Phase.PLAYING) step()
            }
        }
        publish()
    }

    fun onTap() {
        when (uiState.phase) {
            Phase.MENU -> startGame()
            Phase.PLAYING -> {
                engine.switchLane()
                publish()
            }
            // Tapping anywhere after the game-over dialog is dismissed starts a new run.
            Phase.GAME_OVER -> startGame()
        }
    }

    fun onPlayAgain() = startGame()

    fun onWatchRevive() {
        _showRewarded.tryEmit(Unit)
    }

    fun onReviveRewarded() {
        if (uiState.phase != Phase.GAME_OVER) return
        engine.revive()
        publish(phase = Phase.PLAYING)
    }

    private fun startGame() {
        engine.reset()
        publish(phase = Phase.PLAYING)
    }

    private fun step() {
        val gameOver = engine.tick()
        if (gameOver) endGame() else publish()
    }

    private fun endGame() {
        if (engine.score > bestScore) {
            bestScore = engine.score
            prefs.edit().putInt(KEY_BEST_SCORE, bestScore).apply()
        }
        gamesSinceInterstitial++
        publish(phase = Phase.GAME_OVER)
        // Show an interstitial at most every other game-over, so ads never smother play.
        if (gamesSinceInterstitial >= 2) {
            gamesSinceInterstitial = 0
            _showInterstitial.tryEmit(Unit)
        }
    }

    private fun publish(phase: Phase = uiState.phase) {
        uiState = UiState(
            phase = phase,
            playerLane = engine.playerLane,
            score = engine.score,
            coins = engine.coins,
            obstacles = engine.snapshot(),
            bestScore = bestScore,
        )
    }

    private companion object {
        const val PREFS_NAME = "coin_rush_prefs"
        const val KEY_BEST_SCORE = "best_score"
    }
}
