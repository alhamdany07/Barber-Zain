package com.example.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val localeId = Locale("id", "ID")
                val result = tts?.setLanguage(localeId)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to default
                    tts?.setLanguage(Locale.getDefault())
                }
                isInitialized = true
            } else {
                Log.w("TtsHelper", "TextToSpeech initialization failed: $status")
            }
        }
    }

    fun speak(text: String, speed: Float = 1.0f) {
        if (!isInitialized || tts == null) return
        tts?.setSpeechRate(speed)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "QUEUE_CALL_${System.currentTimeMillis()}")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
