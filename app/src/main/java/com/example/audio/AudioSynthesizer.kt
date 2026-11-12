package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural low-latency Audio Synthesizer for PolyCode & Poly Bird game.
 * Generates arcade sound effects dynamically using Android AudioTrack without external audio files.
 */
class AudioSynthesizer {

    private val sampleRate = 22050
    var isMuted: Boolean = false

    private val audioScope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays a synthesized swoosh sound when flapping.
     * Ascending frequency sweep from ~320Hz to ~680Hz.
     */
    fun playFlapSound() {
        if (isMuted) return
        audioScope.launch {
            val durationMs = 85
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                // Frequency rises from 320 to 680 Hz
                val freq = 320.0 + progress * 360.0
                // Envelope decay to prevent clipping
                val envelope = 1.0 - (progress * progress)
                val sample = sin(2.0 * Math.PI * freq * t) * envelope * Short.MAX_VALUE * 0.65
                buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    /**
     * Plays a crisp double-chime when scoring a point.
     * 880Hz (A5) followed by 1320Hz (E6).
     */
    fun playScoreSound() {
        if (isMuted) return
        audioScope.launch {
            val durationMs = 120
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            val half = numSamples / 2

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val freq = if (i < half) 880.0 else 1320.0
                val segmentProgress = if (i < half) i.toDouble() / half else (i - half).toDouble() / half
                val envelope = 1.0 - segmentProgress * 0.7
                val sample = sin(2.0 * Math.PI * freq * t) * envelope * Short.MAX_VALUE * 0.6
                buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    /**
     * Plays a low-impact crunch/thud when colliding with an obstacle.
     */
    fun playCrashSound() {
        if (isMuted) return
        audioScope.launch {
            val durationMs = 160
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                // Frequency drops rapidly from 220Hz down to 50Hz with noise
                val freq = 220.0 * (1.0 - progress * 0.75)
                val noise = (Math.random() - 0.5) * 0.4
                val envelope = (1.0 - progress)
                val tone = sin(2.0 * Math.PI * freq * t)
                val sample = (tone + noise) * envelope * Short.MAX_VALUE * 0.7
                buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val trackBufferSize = buffer.size * 2

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBufferSize, trackBufferSize))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Clean up track after playback finishes
            Thread.sleep((buffer.size * 1000L / sampleRate) + 50L)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // AudioTrack failure non-blocking fallback
        }
    }
}
