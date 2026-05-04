package com.example.myexpenses.core.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.annotation.RawRes
import timber.log.Timber
private const val TAG = "MyEx/Sound"

object SoundPlayer {
    fun playOnce(context: Context, @RawRes resId: Int) {
        Timber.tag(TAG).d("playOnce called resId=$resId")
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val player = MediaPlayer.create(context.applicationContext, resId, attrs, AudioManager.AUDIO_SESSION_ID_GENERATE)

        if (player == null) {
            Timber.tag(TAG).e("MediaPlayer.create returned null — bad resource or codec error")
            return
        }

        Timber.tag(TAG).d("player ready duration=${player.duration}ms, starting")

        player.setOnErrorListener { mp, what, extra ->
            Timber.tag(TAG).e("playback error what=$what extra=$extra")
            mp.release()
            true
        }
        player.setOnCompletionListener { mp ->
            Timber.tag(TAG).d("playback complete, releasing")
            mp.release()
        }
        player.start()
    }
}
