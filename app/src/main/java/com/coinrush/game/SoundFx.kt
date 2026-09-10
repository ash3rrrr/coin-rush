package com.coinrush.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/** Lightweight SoundPool wrapper for the game's short sound effects. */
class SoundFx(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val coinId = pool.load(context, R.raw.coin, 1)
    private val crashId = pool.load(context, R.raw.crash, 1)
    private val swishId = pool.load(context, R.raw.swish, 1)

    fun coin() {
        pool.play(coinId, 1f, 1f, 1, 0, 1f)
    }

    fun crash() {
        pool.play(crashId, 1f, 1f, 1, 0, 1f)
    }

    fun swish() {
        pool.play(swishId, 0.7f, 0.7f, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}
