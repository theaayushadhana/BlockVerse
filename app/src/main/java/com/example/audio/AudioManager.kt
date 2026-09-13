package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural low-latency audio synthesizer for Blockverse.
 * Synthesizes pure crystal-clear game sounds and ambient background music
 * directly via AudioTrack without relying on external assets.
 */
class AudioManager {

    private val sampleRate = 22050
    private var isSfxEnabled = true
    private var isMusicEnabled = true
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled) {
            startAmbientMusic()
        } else {
            stopAmbientMusic()
        }
    }

    private fun playPcm(samples: ShortArray) {
        if (!isSfxEnabled) return
        scope.launch {
            try {
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufSize, samples.size * 2)

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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                val durationMs = (samples.size * 1000L) / sampleRate
                delay(durationMs + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Audio safety fallback
            }
        }
    }

    /**
     * Crisp high-tech UI tap
     */
    fun playClick() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.04).toInt()
        val samples = ShortArray(length)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 80.0)
            val wave = sin(2.0 * PI * 1200.0 * t) * decay
            samples[i] = (wave * 20000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Subtle rising tone on block pickup
     */
    fun playPickup() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.08).toInt()
        val samples = ShortArray(length)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val freq = 440.0 + (t / 0.08) * 330.0
            val decay = exp(-t * 25.0)
            val wave = sin(2.0 * PI * freq * t) * decay
            samples[i] = (wave * 22000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Deep punchy solid snap on block place
     */
    fun playPlace() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.12).toInt()
        val samples = ShortArray(length)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val freq = 220.0 * exp(-t * 18.0) + 80.0
            val decay = exp(-t * 22.0)
            val wave = (sin(2.0 * PI * freq * t) + 0.4 * sin(2.0 * PI * (freq * 2.1) * t)) * decay
            samples[i] = (wave * 26000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Line clear sound: rich harmonic chord scale based on number of lines cleared
     */
    fun playLineClear(linesCount: Int) {
        if (!isSfxEnabled) return
        val duration = 0.35 + linesCount * 0.08
        val length = (sampleRate * duration).toInt()
        val samples = ShortArray(length)
        val baseFreqs = when (linesCount) {
            1 -> doubleArrayOf(523.25, 659.25) // C5, E5
            2 -> doubleArrayOf(523.25, 659.25, 783.99) // C5, E5, G5
            3 -> doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            else -> doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51) // Full majestic major 9th chord!
        }
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 6.5)
            var sum = 0.0
            for (f in baseFreqs) {
                sum += sin(2.0 * PI * f * t)
            }
            sum /= baseFreqs.size
            samples[i] = (sum * decay * 28000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Combo chime: ascending pentatonic frequency based on combo streak
     */
    fun playCombo(comboCount: Int) {
        if (!isSfxEnabled) return
        val pentatonicScale = doubleArrayOf(523.25, 587.33, 659.25, 783.99, 880.00, 1046.50, 1174.66, 1318.51)
        val noteIndex = (comboCount - 1).coerceIn(0, pentatonicScale.size - 1)
        val baseFreq = pentatonicScale[noteIndex]
        val length = (sampleRate * 0.28).toInt()
        val samples = ShortArray(length)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 9.0)
            // Bell-like FM synthesis: carrier + modulator
            val mod = 40.0 * sin(2.0 * PI * (baseFreq * 2.0) * t)
            val wave = sin(2.0 * PI * (baseFreq + mod) * t) * decay
            samples[i] = (wave * 26000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Bomb block explosion: sub-bass rumble + noise punch
     */
    fun playBomb() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.4).toInt()
        val samples = ShortArray(length)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 8.0)
            val bass = sin(2.0 * PI * (120.0 * exp(-t * 12.0) + 40.0) * t)
            val noise = ((Math.random() * 2.0) - 1.0) * exp(-t * 20.0)
            val wave = (bass * 0.7 + noise * 0.5) * decay
            samples[i] = (wave * 28000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Laser block beam sound
     */
    fun playLaser() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.25).toInt()
        val samples = ShortArray(length)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val freq = 1800.0 * exp(-t * 15.0) + 200.0
            val decay = exp(-t * 10.0)
            val wave = sin(2.0 * PI * freq * t) * decay
            samples[i] = (wave * 24000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Overdrive meter activated: dramatic cyber pulse
     */
    fun playOverdrive() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.55).toInt()
        val samples = ShortArray(length)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val freq = 300.0 + 800.0 * (t / 0.55)
            val wave = sin(2.0 * PI * freq * t) * (1.0 - t / 0.55)
            val sub = sin(2.0 * PI * (freq / 2.0) * t) * 0.5
            samples[i] = ((wave + sub) * 25000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Perfect clear fanfare
     */
    fun playPerfectClear() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.8).toInt()
        val samples = ShortArray(length)
        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 3.5)
            var wave = 0.0
            for (f in freqs) {
                wave += sin(2.0 * PI * f * t)
            }
            wave /= freqs.size
            samples[i] = (wave * decay * 29000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Game over somber descending chord
     */
    fun playGameOver() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.6).toInt()
        val samples = ShortArray(length)
        val freqs = doubleArrayOf(392.00, 311.13, 261.63) // G4, Eb4, C4 (C minor chord)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 4.0)
            var wave = 0.0
            for (f in freqs) {
                wave += sin(2.0 * PI * f * t)
            }
            wave /= freqs.size
            samples[i] = (wave * decay * 25000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Coins / reward chime
     */
    fun playReward() {
        if (!isSfxEnabled) return
        val length = (sampleRate * 0.22).toInt()
        val samples = ShortArray(length)
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val f = if (t < 0.1) 987.77 else 1318.51 // B5 -> E6
            val decay = exp(-(t % 0.1) * 20.0)
            val wave = sin(2.0 * PI * f * t) * decay
            samples[i] = (wave * 24000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    /**
     * Ambient soft background synth pulses in a gentle chill cyber loop
     */
    private fun startAmbientMusic() {
        musicJob?.cancel()
        musicJob = scope.launch {
            val chordNotes = listOf(
                doubleArrayOf(261.63, 329.63, 392.00), // C major
                doubleArrayOf(220.00, 261.63, 329.63), // A minor
                doubleArrayOf(174.61, 220.00, 261.63), // F major
                doubleArrayOf(196.00, 246.94, 293.66)  // G major
            )
            var step = 0
            while (isActive && isMusicEnabled) {
                val currentChord = chordNotes[step % chordNotes.size]
                val duration = 1.8
                val len = (sampleRate * duration).toInt()
                val chordBuf = ShortArray(len)
                for (i in 0 until len) {
                    val t = i.toDouble() / sampleRate
                    // Smooth envelope attack and release
                    val env = sin(PI * (t / duration))
                    var wave = 0.0
                    for (f in currentChord) {
                        wave += sin(2.0 * PI * (f * 0.5) * t) * 0.6 + sin(2.0 * PI * f * t) * 0.4
                    }
                    wave /= currentChord.size
                    chordBuf[i] = (wave * env * 7000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcm(chordBuf)
                delay(1800)
                step++
            }
        }
    }

    private fun stopAmbientMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopAmbientMusic()
    }
}
