package com.coinrush.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LaneLineColor = Color.White.copy(alpha = 0.10f)
private val PlayerColor = Color(0xFF6C8CFF)
private val ObstacleColor = Color(0xFFFF5A5F)
private val CoinOuter = Color(0xFFFFC93C)
private val CoinInner = Color(0xFFE8A81C)

@Composable
fun GameScreen(
    state: UiState,
    onTap: () -> Unit,
    onPlayAgain: () -> Unit,
    onWatchRevive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.pointerInput(Unit) { detectTapGestures { onTap() } }) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val laneWidth = size.width / GameEngine.LANE_COUNT
            val radius = GameEngine.ENTITY_SIZE * size.height / 2f

            // Lane dividers
            for (i in 1 until GameEngine.LANE_COUNT) {
                drawLine(
                    color = LaneLineColor,
                    start = Offset(laneWidth * i, 0f),
                    end = Offset(laneWidth * i, size.height),
                    strokeWidth = 2f,
                )
            }

            // Falling entities
            state.obstacles.forEach { entity ->
                val cx = laneWidth * entity.lane + laneWidth / 2f
                val cy = entity.y * size.height
                if (entity.isCoin) {
                    drawCircle(CoinOuter, radius, Offset(cx, cy))
                    drawCircle(CoinInner, radius * 0.62f, Offset(cx, cy))
                } else {
                    drawRoundRect(
                        color = ObstacleColor,
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2, radius * 2),
                        cornerRadius = CornerRadius(radius * 0.3f),
                    )
                }
            }

            // Player
            val px = laneWidth * state.playerLane + laneWidth / 2f
            val py = GameEngine.PLAYER_Y * size.height
            drawRoundRect(
                color = PlayerColor,
                topLeft = Offset(px - radius, py - radius),
                size = Size(radius * 2, radius * 2.3f),
                cornerRadius = CornerRadius(radius * 0.55f),
            )
        }

        when (state.phase) {
            Phase.MENU -> MenuOverlay()
            Phase.PLAYING -> Hud(state)
            Phase.GAME_OVER -> Hud(state)
        }

        if (state.phase == Phase.GAME_OVER) {
            var dialogVisible by remember { mutableStateOf(true) }
            LaunchedEffect(state.phase) {
                if (state.phase != Phase.GAME_OVER) dialogVisible = false
            }
            if (dialogVisible) {
                GameOverDialog(
                    state = state,
                    onPlayAgain = {
                        dialogVisible = false
                        onPlayAgain()
                    },
                    onWatchRevive = {
                        dialogVisible = false
                        onWatchRevive()
                    },
                    onDismiss = { dialogVisible = false },
                )
            }
        }
    }
}

@Composable
private fun MenuOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "COIN RUSH",
                color = CoinOuter,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Tap anywhere to switch lanes.\nDodge the red blocks. Grab the gold.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "TAP TO START",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun Hud(state: UiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("SCORE", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
            Text("${state.score}", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("BEST", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
            Text("${state.bestScore}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("COINS", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("●", color = CoinOuter, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text("${state.coins}", color = CoinOuter, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun GameOverDialog(
    state: UiState,
    onPlayAgain: () -> Unit,
    onWatchRevive: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Game Over", fontWeight = FontWeight.Bold, color = Color(0xFF0B1026))
        },
        text = {
            Column {
                Text("Score: ${state.score}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0B1026))
                Text("Best: ${state.bestScore}", fontSize = 14.sp, color = Color(0xFF0B1026).copy(alpha = 0.7f))
                Text("Coins: ${state.coins}", fontSize = 14.sp, color = Color(0xFFB07A00))
            }
        },
        confirmButton = {
            Button(onClick = onPlayAgain) { Text("Play Again") }
        },
        dismissButton = {
            TextButton(onClick = onWatchRevive) {
                Text("▶ Watch ad to revive", color = Color(0xFF3B5BDB))
            }
        },
        containerColor = Color.White,
    )
}
