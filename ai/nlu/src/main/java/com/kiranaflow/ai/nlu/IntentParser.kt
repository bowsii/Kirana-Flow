package com.kiranaflow.ai.nlu

import com.kiranaflow.core.model.VoiceCommand

interface IntentParser {
    suspend fun parse(utterance: String): VoiceCommand
}
