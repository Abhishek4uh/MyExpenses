package com.example.myexpenses.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

// ─── Voice State ──────────────────────────────────────────────────────────────

sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data class Partial(val text: String) : VoiceState
    data class Result(val text: String) : VoiceState
    data class Error(val message: String) : VoiceState
}

// ─── Handler ──────────────────────────────────────────────────────────────────

@Singleton
class VoiceInputHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Held outside the flow so the UI can ask the live recognizer to finalize
    // (WhatsApp-style press-and-hold release) without cancelling the flow.
    @Volatile private var activeRecognizer: SpeechRecognizer? = null

    /**
     * Force the active recognizer to stop capturing and emit a final result
     * via onResults. Use this when the user lifts their finger off a
     * press-and-hold mic button — Android otherwise waits for natural silence.
     */
    fun stopListening() {
        activeRecognizer?.stopListening()
    }

    /**
     * Returns a cold Flow that:
     *  1. Creates a SpeechRecognizer on collection
     *  2. Emits VoiceState updates (Listening → Partial* → Result | Error)
     *  3. Destroys the recognizer when the flow is cancelled
     *
     * Call on the MAIN dispatcher — SpeechRecognizer requires main thread.
     */
    fun startListening(): Flow<VoiceState> = callbackFlow {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        activeRecognizer = recognizer

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // "en-IN" gives better Hinglish + Indian accent recognition
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(VoiceState.Listening)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return
                trySend(VoiceState.Partial(partial))
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: run {
                    trySend(VoiceState.Error("No speech detected"))
                    channel.close()
                    return
                }
                trySend(VoiceState.Result(text))
                channel.close()
            }

            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission missing"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Nothing recognised — please try again"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recogniser busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Recognition error ($error)"
                }
                trySend(VoiceState.Error(msg))
                channel.close()
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        recognizer.startListening(intent)
        awaitClose {
            if (activeRecognizer === recognizer) activeRecognizer = null
            recognizer.destroy()
        }
    }
}
