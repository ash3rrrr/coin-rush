package com.coinrush.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.coinrush.game.ads.AdController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var adController: AdController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adController = AdController(this)
        adController.loadAll()

        setContent {
            val state = viewModel.uiState
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0B1026)
            ) {
                GameScreen(
                    state = state,
                    onTap = viewModel::onTap,
                    onPlayAgain = viewModel::onPlayAgain,
                    onWatchRevive = viewModel::onWatchRevive,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B1026))
                )
            }
        }

        lifecycleScope.launch {
            viewModel.showInterstitial.collect { adController.showInterstitial() }
        }
        lifecycleScope.launch {
            viewModel.showRewarded.collect { adController.showRewarded(viewModel::onReviveRewarded) }
        }
    }

    override fun onDestroy() {
        adController.destroy()
        super.onDestroy()
    }
}
