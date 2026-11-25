package com.example.util

import android.content.Context
import org.json.JSONArray

/**
 * Per-account notification read state & account-creation floor persistence.
 * - Remembers every notification ID a user has already viewed so the unread badge (1, 2, 3...)
 *   clears permanently after opening Notifications (including broadcast "ALL" notifications).
 * - Tracks each account's creation timestamp floor so brand-new accounts never inherit old
 *   historical notifications created before the user signed up.
 */
object NotificationReadStatePrefs {
    private const val PREFS_NAME = "my_crush_pizza_notif_state_v1"
    private const val MAX_STORED_READ_IDS = 200
    const val ACCOUNT_FLOOR_GRACE_MILLIS = 15_000L

    private fun readIdsKey(uid: String): String = "read_notif_ids_${uid.trim()}"
    private fun floorKey(uid: String): String = "notif_floor_ms_${uid.trim()}"

    fun loadReadIds(context: Context, uid: String): Set<String> {
        if (uid.isBlank()) return emptySet()
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(readIdsKey(uid), null) ?: return emptySet()
            val arr = JSONArray(raw)
            buildSet {
                for (i in 0 until arr.length()) {
                    val id = arr.optString(i).trim()
                    if (id.isNotBlank()) add(id)
                }
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun saveReadIds(context: Context, uid: String, ids: Set<String>) {
        if (uid.isBlank()) return
        try {
            val trimmed = ids.filter { it.isNotBlank() }.takeLast(MAX_STORED_READ_IDS)
            val arr = JSONArray()
            trimmed.forEach { arr.put(it) }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(readIdsKey(uid), arr.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun loadAccountFloorMillis(context: Context, uid: String): Long {
        if (uid.isBlank()) return 0L
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getLong(floorKey(uid), 0L)
        } catch (_: Exception) {
            0L
        }
    }

    fun saveAccountFloorMillis(context: Context, uid: String, floorMillis: Long) {
        if (uid.isBlank() || floorMillis <= 0L) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existing = prefs.getLong(floorKey(uid), 0L)
            val effective = maxOf(existing, floorMillis)
            prefs.edit().putLong(floorKey(uid), effective).apply()
        } catch (_: Exception) {
        }
    }

    /**
     * Pure helper: returns true only if the notification was created on/after the user's account
     * creation floor (with a small grace window so instant post-signup alerts are kept).
     */
    fun isNotificationEligibleForAccount(
        notifCreatedMillis: Long?,
        accountFloorMillis: Long,
        graceMillis: Long = ACCOUNT_FLOOR_GRACE_MILLIS
    ): Boolean {
        if (accountFloorMillis <= 0L) return true
        val created = notifCreatedMillis ?: return false
        if (created <= 0L) return false
        return created >= (accountFloorMillis - graceMillis)
    }
}
