package com.example.minermerge.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackIndex = 0
    private val totalTracks = 7
    private var musicVolume = 0.5f
    private val scope = CoroutineScope(Dispatchers.IO)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        startBackgroundMusic()
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(musicVolume, musicVolume)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun startBackgroundMusic() {
        playTrack(currentTrackIndex)
    }

    private fun playTrack(index: Int) {
        scope.launch {
            try {
                mediaPlayer?.release()
                mediaPlayer = null

                val trackName = "music/track_$index.mp4"
                var afd: AssetFileDescriptor? = null
                try {
                    afd = context.assets.openFd(trackName)
                } catch (e: Exception) {
                    // Track not found or format issue
                }

                if (afd != null) {
                    val player = MediaPlayer().apply {
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        setVolume(musicVolume, musicVolume)
                        setOnCompletionListener {
                            currentTrackIndex = (currentTrackIndex + 1) % totalTracks
                            playTrack(currentTrackIndex)
                        }
                        setOnErrorListener { _, _, _ ->
                            currentTrackIndex = (currentTrackIndex + 1) % totalTracks
                            playTrack(currentTrackIndex)
                            true
                        }
                        prepare()
                        start()
                    }
                    mediaPlayer = player
                }
            } catch (e: Exception) {
                // Ignore audio init issues
            }
        }
    }

    fun playHitSound() {
        playTone(220.0, 80)
    }

    fun playMergeSound() {
        scope.launch {
            playTone(880.0, 100)
            kotlinx.coroutines.delay(60)
            playTone(1320.0, 120)
        }
    }

    fun playGiftSound() {
        playTone(1046.0, 140)
    }

    fun playExplosionSound() {
        playNoise(200)
    }

    fun playChestSound() {
        scope.launch {
            playTone(523.0, 90)
            kotlinx.coroutines.delay(80)
            playTone(659.0, 90)
            kotlinx.coroutines.delay(80)
            playTone(784.0, 140)
        }
    }

    private fun playTone(freqHz: Double, durationMs: Int) {
        scope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Apply smooth envelope to avoid clicking
                    val envelope = when {
                        i < numSamples * 0.1 -> i / (numSamples * 0.1)
                        i > numSamples * 0.7 -> (numSamples - i) / (numSamples * 0.3)
                        else -> 1.0
                    }
                    val sample = sin(2.0 * Math.PI * freqHz * time) * Short.MAX_VALUE * 0.4 * envelope
                    samples[i] = sample.toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, numSamples)
                track.play()
                kotlinx.coroutines.delay(durationMs.toLong() + 50)
                track.release()
            } catch (e: Exception) {
                // Ignore audio synth errors
            }
        }
    }

    private fun playNoise(durationMs: Int) {
        scope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                var last = 0.0
                for (i in 0 until numSamples) {
                    val envelope = (numSamples - i).toDouble() / numSamples
                    val white = (Math.random() * 2.0 - 1.0)
                    // Brown/low-pass noise for explosion rumble
                    val brown = (last + (0.02 * white)) / 1.02
                    last = brown
                    samples[i] = (brown * Short.MAX_VALUE * 0.6 * envelope).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, numSamples)
                track.play()
                kotlinx.coroutines.delay(durationMs.toLong() + 50)
                track.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun triggerHaptic(type: String = "medium") {
        try {
            if (vibrator?.hasVibrator() == true) {
                val duration = when (type) {
                    "light" -> 25L
                    "heavy" -> 80L
                    else -> 45L
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val amplitude = when (type) {
                        "light" -> 80
                        "heavy" -> 255
                        else -> 160
                    }
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
