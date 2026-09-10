package com.coinrush.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun `tap cycles through lanes and wraps around`() {
        val engine = GameEngine()
        assertEquals(1, engine.playerLane)
        engine.switchLane()
        assertEquals(2, engine.playerLane)
        engine.switchLane()
        assertEquals(0, engine.playerLane)
        engine.switchLane()
        assertEquals(1, engine.playerLane)
    }

    @Test
    fun `coin in the player lane is collected and awards score`() {
        val engine = GameEngine()
        engine.debugSpawn(lane = 1, y = 0.85f, isCoin = true)
        val gameOver = engine.tick()
        assertFalse(gameOver)
        assertEquals(1, engine.coins)
        assertEquals(51, engine.score) // +1 for surviving the tick, +50 for the coin
    }

    @Test
    fun `obstacle in the player lane ends the run`() {
        val engine = GameEngine()
        engine.debugSpawn(lane = 1, y = 0.85f, isCoin = false)
        assertTrue(engine.tick())
    }

    @Test
    fun `obstacle in another lane is harmless`() {
        val engine = GameEngine()
        engine.debugSpawn(lane = 0, y = 0.85f, isCoin = false)
        assertFalse(engine.tick())
    }

    @Test
    fun `revive clears obstacles but keeps coins`() {
        val engine = GameEngine()
        engine.debugSpawn(lane = 1, y = 0.5f, isCoin = false)
        engine.debugSpawn(lane = 2, y = 0.6f, isCoin = true)
        engine.revive()
        val remaining = engine.snapshot()
        assertEquals(1, remaining.size)
        assertTrue(remaining.single().isCoin)
    }

    @Test
    fun `score grows every tick survived`() {
        val engine = GameEngine()
        repeat(5) { engine.tick() }
        assertEquals(5, engine.score)
    }
}
