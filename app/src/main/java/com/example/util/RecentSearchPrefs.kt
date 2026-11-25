package com.example.util

import android.content.Context
import org.json.JSONArray

/**
 * Lightweight local persistence for Customer Menu Recent Searches using SharedPreferences.
 * Stores up to MAX_RECENT_SEARCHES unique queries (case-insensitive deduplication, newest first).
 */
object RecentSearchPrefs {
    private const val PREFS_NAME = "my_crush_pizza_recent_searches"
    private const val KEY_RECENT_LIST = "recent_food_searches_json"
    const val MAX_RECENT_SEARCHES = 8

    fun load(context: Context): List<String> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_RECENT_LIST, null) ?: return emptyList()
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val value = array.optString(i).trim()
                    if (value.isNotEmpty()) add(value)
                }
            }.take(MAX_RECENT_SEARCHES)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, searches: List<String>) {
        try {
            val array = JSONArray()
            searches.take(MAX_RECENT_SEARCHES).forEach { array.put(it) }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_RECENT_LIST, array.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun mergeQuery(existing: List<String>, rawQuery: String): List<String> {
        val cleaned = rawQuery.trim()
        if (cleaned.length < 2) return existing
        val withoutDuplicate = existing.filterNot { it.equals(cleaned, ignoreCase = true) }
        return (listOf(cleaned) + withoutDuplicate).take(MAX_RECENT_SEARCHES)
    }
}
