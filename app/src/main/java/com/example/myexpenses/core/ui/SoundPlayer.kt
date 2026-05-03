package com.example.myexpenses.core.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object SoundPlayer {
    fun playOnce(context: Context, @RawRes resId: Int) {
        MediaPlayer.create(context, resId)?.apply {
            setOnCompletionListener { release() }
            start()
        }
    }
}
