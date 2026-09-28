package com.kiranaflow.ai.nlu

/**
 * Natural Language Understanding parser contract.
 * Takes a raw transcribed utterance and returns a ranked list of [ParsedCommand] hypotheses,
 * each with a confidence score.
 */
interface IntentParser {
    suspend fun parse(utterance: String): List<ParsedCommand>
}
