package com.coinrush.game

import kotlin.random.Random

/** Immutable snapshot of a falling entity for rendering. */
data class ObstacleSnapshot(val id: Long, val lane: Int, val y: Float, val isCoin: Boolean)

private data class Entity(val id: Long, val lane: Int, var y: Float, val isCoin: Boolean)

/**
 * Pure game engine for a 3-lane lane-dodger. All coordinates are normalized
 * (0..1 of screen height), so the engine knows nothing about pixels and is
 * fully unit-testable.
 *
 * Obstacles and coins fall from the top. Every tap cycles the player through
 * the lanes. Score grows over time; coins are worth +50 and +1 coin currency.
 */
class GameEngine {
    companion object {
        const val LANE_COUNT = 3
        const val PLAYER_LANE_START = 1
        const val PLAYER_Y = 0.86f
        const val COLLISION_TOP = 0.80f
        const val COLLISION_BOTTOM = 0.93f
        const val ENTITY_SIZE = 0.11f
        const val START_SPEED = 0.010f
        const val MAX_SPEED = 0.030f
        const val ACCELERATION = 0.000006f
        const val BASE_SPAWN_INTERVAL_TICKS = 22
        const val MIN_SPAWN_INTERVAL_TICKS = 13
        const val OFF_SCREEN_Y = 1.2f
        const val TICK_MS = 32L
    }

    var playerLane = PLAYER_LANE_START
        private set
    var score = 0
        private set
    var coins = 0
        private set

    private val entities = mutableListOf<Entity>()
    private var tickCount = 0
    private var nextId = 0L

    fun reset() {
        playerLane = PLAYER_LANE_START
        score = 0
        coins = 0
        entities.clear()
        tickCount = 0
    }

    fun switchLane() {
        playerLane = (playerLane + 1) % LANE_COUNT
    }

    /** Moves the player `delta` lanes left/right, clamped to the outer lanes. */
    fun moveBy(delta: Int) {
        playerLane = (playerLane + delta).coerceIn(0, LANE_COUNT - 1)
    }

    /** Advances the world one tick. Returns true when the run ends. */
    fun tick(): Boolean {
        tickCount++
        score += 1

        val speed = (START_SPEED + ACCELERATION * tickCount).coerceAtMost(MAX_SPEED)
        val spawnInterval = (BASE_SPAWN_INTERVAL_TICKS - tickCount / 400)
            .coerceAtLeast(MIN_SPAWN_INTERVAL_TICKS)
        if (tickCount % spawnInterval == 0) spawnWave()

        val iterator = entities.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            entity.y += speed

            if (entity.lane == playerLane && entity.y in COLLISION_TOP..COLLISION_BOTTOM) {
                if (entity.isCoin) {
                    coins++
                    score += 50
                    iterator.remove()
                    continue
                }
                return true
            }

            if (entity.y > OFF_SCREEN_Y) iterator.remove()
        }
        return false
    }

    /** Clears the screen of obstacles after a rewarded revive (coins stay). */
    fun revive() {
        entities.removeAll { !it.isCoin }
    }

    fun snapshot(): List<ObstacleSnapshot> =
        entities.map { ObstacleSnapshot(it.id, it.lane, it.y, it.isCoin) }

    /** Test hook so unit tests can place entities deterministically. */
    internal fun debugSpawn(lane: Int, y: Float, isCoin: Boolean) {
        entities.add(Entity(nextId++, lane, y, isCoin))
    }

    private fun spawnWave() {
        val lanes = (0 until LANE_COUNT).shuffled()
        val blockedCount = 1 + Random.nextInt(2) // 1 or 2 lanes blocked
        val blocked = lanes.take(blockedCount)
        blocked.forEach { lane ->
            entities.add(Entity(nextId++, lane, -ENTITY_SIZE, isCoin = false))
        }
        val freeLanes = lanes.drop(blockedCount)
        if (freeLanes.isNotEmpty() && Random.nextBoolean()) {
            entities.add(Entity(nextId++, freeLanes.random(), -ENTITY_SIZE, isCoin = true))
        }
    }
}
