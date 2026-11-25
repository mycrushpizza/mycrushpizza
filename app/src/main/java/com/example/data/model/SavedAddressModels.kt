package com.example.data.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Address type tag selectable when saving a delivery address: Home, Work, or Other.
 */
enum class AddressLabel(val code: String, val displayName: String) {
    HOME("HOME", "Home"),
    WORK("WORK", "Work"),
    OTHER("OTHER", "Other");

    companion object {
        fun fromString(raw: String?): AddressLabel {
            if (raw.isNullOrBlank()) return HOME
            val trimmed = raw.trim()
            return entries.firstOrNull {
                it.code.equals(trimmed, ignoreCase = true) ||
                    it.displayName.equals(trimmed, ignoreCase = true)
            } ?: HOME
        }
    }
}

data class SavedDeliveryAddress(
    val label: AddressLabel = AddressLabel.HOME,
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val landmark: String = "",
    val lat: Double = HajipurLocations.KITCHEN_LAT,
    val lng: Double = HajipurLocations.KITCHEN_LNG
)

data class DecodedProfileAddress(
    val label: AddressLabel,
    val address: String,
    val landmark: String
)

/**
 * Encodes and decodes Home/Work/Other label + address + landmark into the existing Firestore
 * `users/{uid}.defaultAddress` field (max 400 chars in Firestore security rules), staying
 * human-readable in Firestore Console and 100% compatible with legacy plain addresses.
 */
object SavedAddressCodec {
    private const val LANDMARK_DELIMITER = " | Landmark: "

    fun encodeForProfile(
        label: AddressLabel,
        address: String,
        landmark: String
    ): String {
        val cleanAddr = address.trim()
        if (cleanAddr.isBlank()) return ""
        val cleanLandmark = landmark.trim()
        val combined = if (cleanLandmark.isNotBlank()) {
            "[${label.displayName}] $cleanAddr$LANDMARK_DELIMITER$cleanLandmark"
        } else {
            "[${label.displayName}] $cleanAddr"
        }
        return combined.take(350)
    }

    fun decodeFromProfile(rawDefaultAddress: String): DecodedProfileAddress {
        val trimmed = rawDefaultAddress.trim()
        if (trimmed.isBlank()) {
            return DecodedProfileAddress(AddressLabel.HOME, "", "")
        }

        var label = AddressLabel.HOME
        var body = trimmed
        val bracketMatch = Regex("^\\[(Home|Work|Other)]\\s*", RegexOption.IGNORE_CASE).find(trimmed)
        if (bracketMatch != null) {
            label = AddressLabel.fromString(bracketMatch.groupValues[1])
            body = trimmed.substring(bracketMatch.range.last + 1).trim()
        }

        val delimiterIdx = body.indexOf(LANDMARK_DELIMITER, ignoreCase = true)
        return if (delimiterIdx >= 0) {
            val addr = body.substring(0, delimiterIdx).trim()
            val lm = body.substring(delimiterIdx + LANDMARK_DELIMITER.length).trim()
            DecodedProfileAddress(label = label, address = addr, landmark = lm)
        } else {
            DecodedProfileAddress(label = label, address = body, landmark = "")
        }
    }
}

/**
 * Per-account local cache for multiple saved addresses (Home, Work, Other) backed by
 * SharedPreferences and merged with the logged-in user's Firestore profile on every sign-in.
 */
object SavedAddressesPrefs {
    private const val PREFS_NAME = "my_crush_pizza_saved_addresses_v1"

    private fun keyForUser(uid: String): String = "saved_addresses_${uid.trim()}"

    fun loadForUser(context: Context, uid: String): List<SavedDeliveryAddress> {
        if (uid.isBlank()) return emptyList()
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(keyForUser(uid), null) ?: return emptyList()
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val address = obj.optString("address").trim()
                    if (address.isBlank()) continue
                    add(
                        SavedDeliveryAddress(
                            label = AddressLabel.fromString(obj.optString("label")),
                            name = obj.optString("name").trim(),
                            phone = obj.optString("phone").trim(),
                            address = address,
                            landmark = obj.optString("landmark").trim(),
                            lat = obj.optDouble("lat", HajipurLocations.KITCHEN_LAT),
                            lng = obj.optDouble("lng", HajipurLocations.KITCHEN_LNG)
                        )
                    )
                }
            }.distinctBy { it.label }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveForUser(context: Context, uid: String, addresses: List<SavedDeliveryAddress>) {
        if (uid.isBlank()) return
        try {
            val array = JSONArray()
            addresses
                .filter { it.address.isNotBlank() }
                .distinctBy { it.label }
                .forEach { item ->
                    val obj = JSONObject().apply {
                        put("label", item.label.code)
                        put("name", item.name)
                        put("phone", item.phone)
                        put("address", item.address)
                        put("landmark", item.landmark)
                        put("lat", item.lat)
                        put("lng", item.lng)
                    }
                    array.put(obj)
                }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(keyForUser(uid), array.toString()).apply()
        } catch (_: Exception) {
        }
    }
}
