package com.kiranaflow.core.domain.validation

import com.kiranaflow.core.model.CatalogItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Catalog Validator: matches a raw item string from NLU output
 * against the loaded catalog using fuzzy string matching.
 *
 * Strategy (in priority order):
 *   1. Exact name match (case-insensitive)
 *   2. Exact alias match
 *   3. Name starts with query
 *   4. Name contains query
 *   5. Alias contains query
 *   6. Token overlap (word-level similarity)
 *   7. Levenshtein distance <= 3 fallback
 */
@Singleton
class CatalogValidator @Inject constructor() {

    /**
     * Find the best-matching [CatalogItem] for a raw item string.
     *
     * @param query   The item name parsed by NLU
     * @param catalog The full active catalog from the database
     * @return        Best match or null if none is confident enough
     */
    fun findBestMatch(query: String, catalog: List<CatalogItem>): CatalogItem? {
        if (query.isBlank() || catalog.isEmpty()) return null

        val q = query.trim().lowercase()

        // 1. Exact name match
        catalog.firstOrNull { it.name.lowercase() == q }?.let { return it }

        // 2. Exact alias match
        catalog.firstOrNull { parseAliases(it.aliases).any { a -> a.lowercase() == q } }?.let { return it }

        // 3. Name starts with query (longest prefix wins)
        catalog.filter { it.name.lowercase().startsWith(q) }
            .maxByOrNull { it.name.length }?.let { return it }

        // 4. Name contains query
        catalog.firstOrNull { it.name.lowercase().contains(q) }?.let { return it }

        // 5. Alias contains query
        catalog.firstOrNull {
            parseAliases(it.aliases).any { a -> a.lowercase().contains(q) }
        }?.let { return it }

        // 6. Token overlap (word-level match for tokens of length > 2)
        val qWords = q.split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }
        if (qWords.isNotEmpty()) {
            val tokenMatch = catalog.mapNotNull { item ->
                val nameWords = item.name.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }
                val aliasWords = parseAliases(item.aliases).flatMap {
                    it.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { a -> a.length > 2 }
                }
                val allWords = (nameWords + aliasWords).toSet()
                val matchCount = qWords.count { w ->
                    allWords.any { word -> word == w || (word.length >= 4 && (word.startsWith(w) || w.startsWith(word))) }
                }
                if (matchCount > 0) Pair(item, matchCount) else null
            }.maxByOrNull { it.second }
            if (tokenMatch != null) return tokenMatch.first
        }

        // 7. Levenshtein distance fallback (only for short tokens)
        if (q.length <= 12) {
            val fuzzy = catalog.mapNotNull { item ->
                val nameDist = levenshtein(q, item.name.lowercase())
                val aliasDist = parseAliases(item.aliases)
                    .minOfOrNull { levenshtein(q, it.lowercase()) } ?: Int.MAX_VALUE
                val best = minOf(nameDist, aliasDist)
                if (best <= 3) Pair(item, best) else null
            }.minByOrNull { it.second }
            if (fuzzy != null) return fuzzy.first
        }

        return null
    }

    private fun parseAliases(json: String): List<String> {
        return try {
            json.trim('[', ']').split(",")
                .map { it.trim().trim('"') }
                .filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                else 1 + min(dp[i - 1][j - 1], min(dp[i - 1][j], dp[i][j - 1]))
            }
        }
        return dp[a.length][b.length]
    }
}
