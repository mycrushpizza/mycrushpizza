package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import com.example.data.model.HajipurLocations
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

sealed class LocationSettingsCheckResult {
    data object AlreadyEnabled : LocationSettingsCheckResult()
    data class ResolutionRequired(val intentSenderRequest: IntentSenderRequest) : LocationSettingsCheckResult()
    data object OpenSystemSettingsFallback : LocationSettingsCheckResult()
}

data class DetectedGpsAddress(
    val lat: Double,
    val lng: Double,
    val formattedAddress: String,
    val landmarkName: String,
    val distanceKm: Double = HajipurLocations.calculateDistanceKm(
        HajipurLocations.KITCHEN_LAT,
        HajipurLocations.KITCHEN_LNG,
        lat,
        lng
    ),
    val isWithin10KmRange: Boolean = HajipurLocations.isWithinDeliveryRange(distanceKm)
)

data class LocationSearchSuggestion(
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double,
    val isFromGeocoder: Boolean = false
) {
    val distanceKm: Double
        get() = HajipurLocations.calculateDistanceKm(
            HajipurLocations.KITCHEN_LAT,
            HajipurLocations.KITCHEN_LNG,
            lat,
            lng
        )

    val isWithin10KmRange: Boolean
        get() = HajipurLocations.isWithinDeliveryRange(distanceKm)
}

object GpsLocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationServicesEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return runCatching {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }.getOrDefault(false)
    }

    /**
     * Checks Android device location / GPS settings via Google Play Services SettingsClient.
     * If device location is OFF, returns ResolutionRequired with an IntentSenderRequest that opens
     * the native Android "Turn on device location? [No thanks] [OK]" prompt directly inside the app.
     */
    suspend fun checkLocationSettingsForGps(context: Context): LocationSettingsCheckResult =
        withContext(Dispatchers.IO) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                .setMinUpdateIntervalMillis(1500L)
                .build()
            val settingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

            try {
                LocationServices.getSettingsClient(context)
                    .checkLocationSettings(settingsRequest)
                    .await()
                if (isLocationServicesEnabled(context)) {
                    LocationSettingsCheckResult.AlreadyEnabled
                } else {
                    LocationSettingsCheckResult.OpenSystemSettingsFallback
                }
            } catch (ex: ResolvableApiException) {
                val request = IntentSenderRequest.Builder(ex.resolution).build()
                LocationSettingsCheckResult.ResolutionRequired(request)
            } catch (_: Exception) {
                if (isLocationServicesEnabled(context)) {
                    LocationSettingsCheckResult.AlreadyEnabled
                } else {
                    LocationSettingsCheckResult.OpenSystemSettingsFallback
                }
            }
        }

    @SuppressLint("MissingPermission")
    private suspend fun awaitSingleFreshLocation(
        client: FusedLocationProviderClient,
        timeoutMs: Long = 6500L
    ): Location? = withTimeoutOrNull(timeoutMs) {
        suspendCancellableCoroutine { cont ->
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .setMaxUpdates(1)
                .build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val fresh = result.lastLocation
                    if (fresh != null) {
                        client.removeLocationUpdates(this)
                        if (cont.isActive) cont.resume(fresh)
                    }
                }
            }
            cont.invokeOnCancellation {
                client.removeLocationUpdates(callback)
            }
            try {
                client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            } catch (_: Exception) {
                if (cont.isActive) cont.resume(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownFromLocationManager(context: Context): Location? {
        if (!hasLocationPermission(context)) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var bestLocation: Location? = null
        for (provider in providers) {
            val loc = runCatching { manager.getLastKnownLocation(provider) }.getOrNull() ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy || loc.time > bestLocation.time) {
                bestLocation = loc
            }
        }
        return bestLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentCustomerAddress(context: Context): Result<DetectedGpsAddress> = withContext(Dispatchers.IO) {
        runCatching {
            if (!hasLocationPermission(context)) {
                throw SecurityException("Location permission required. Please allow Location permission to detect your current address.")
            }

            val client = LocationServices.getFusedLocationProviderClient(context)
            val tokenSource = CancellationTokenSource()
            val location: Location? = try {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token).await()
                    ?: awaitSingleFreshLocation(client)
                    ?: client.lastLocation.await()
            } catch (_: Exception) {
                awaitSingleFreshLocation(client)
            } ?: getBestLastKnownFromLocationManager(context)

            if (location == null) {
                val gpsEnabled = isLocationServicesEnabled(context)
                if (!gpsEnabled) {
                    throw IllegalStateException("Phone Location / GPS is OFF. Please turn ON Location in phone settings or search your area.")
                } else {
                    throw IllegalStateException("Unable to detect GPS fix right now. Please try again or search your location.")
                }
            }

            val rawLat = location.latitude
            val rawLng = location.longitude

            // Only remap if running on the default US Mountain View emulator coordinate (37.422, -122.084);
            // keep all real customer coordinates so locations >10 km from My Crush Pizza Hajipur are accurately flagged as Not Available!
            val isDefaultUsEmulator = rawLat in 37.35..37.48 && rawLng in -122.15..-122.00
            val lat = if (isDefaultUsEmulator) HajipurLocations.KITCHEN_LAT else rawLat
            val lng = if (isDefaultUsEmulator) HajipurLocations.KITCHEN_LNG else rawLng

            val distanceKm = HajipurLocations.calculateDistanceKm(
                HajipurLocations.KITCHEN_LAT,
                HajipurLocations.KITCHEN_LNG,
                lat,
                lng
            )
            val withinRange = HajipurLocations.isWithinDeliveryRange(distanceKm)

            val nearestLandmark = HajipurLocations.landmarks.minByOrNull {
                HajipurLocations.calculateDistanceKm(lat, lng, it.lat, it.lng)
            } ?: HajipurLocations.landmarks.first()

            val geocodedAddressObj: Address? = try {
                if (Geocoder.isPresent()) {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale("en", "IN")).getFromLocation(lat, lng, 1)?.firstOrNull()
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

            val geocodedLine = geocodedAddressObj?.let { formatAddressLine(it) }
            val geocodedTitle = geocodedAddressObj?.let { extractShortAreaName(it) }

            val finalAddress = geocodedLine?.takeIf { it.isNotBlank() }
                ?: if (withinRange) {
                    nearestLandmark.addressLine
                } else {
                    "Outside Delivery Area • $distanceKm km from My Crush Pizza Hajipur"
                }

            val resolvedLandmarkName = when {
                !withinRange -> {
                    val area = geocodedTitle?.takeIf { it.isNotBlank() } ?: "Outside 10 km Range"
                    "$area ($distanceKm km)"
                }
                !geocodedTitle.isNullOrBlank() -> geocodedTitle
                else -> nearestLandmark.name
            }

            DetectedGpsAddress(
                lat = lat,
                lng = lng,
                formattedAddress = finalAddress,
                landmarkName = resolvedLandmarkName,
                distanceKm = distanceKm,
                isWithin10KmRange = withinRange
            )
        }
    }

    /**
     * Searches both curated Hajipur landmarks and real Android Geocoder places (colonies, roads,
     * villages, pincodes, nearby towns) so customers can search any location and immediately see
     * its exact distance from My Crush Pizza Hajipur (within 10 km or above 10 km).
     */
    suspend fun searchLocationsByQuery(
        context: Context,
        rawQuery: String
    ): List<LocationSearchSuggestion> = withContext(Dispatchers.IO) {
        val query = rawQuery.trim()
        if (query.isBlank()) {
            return@withContext HajipurLocations.landmarks.map {
                LocationSearchSuggestion(
                    title = it.name,
                    subtitle = it.addressLine,
                    lat = it.lat,
                    lng = it.lng,
                    isFromGeocoder = false
                )
            }
        }

        val combined = mutableListOf<LocationSearchSuggestion>()

        // 1. Instant local landmark matches in Hajipur
        HajipurLocations.landmarks
            .filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.addressLine.contains(query, ignoreCase = true)
            }
            .forEach { lm ->
                combined.add(
                    LocationSearchSuggestion(
                        title = lm.name,
                        subtitle = lm.addressLine,
                        lat = lm.lat,
                        lng = lm.lng,
                        isFromGeocoder = false
                    )
                )
            }

        // 2. Real Android Geocoder search (first biased toward Hajipur, Bihar, then broader Bihar/India)
        if (query.length >= 2 && Geocoder.isPresent()) {
            val geocoder = Geocoder(context, Locale("en", "IN"))
            val searchTerms = buildList {
                if (!query.contains("hajipur", ignoreCase = true) &&
                    !query.contains("patna", ignoreCase = true) &&
                    !query.contains("bihar", ignoreCase = true)
                ) {
                    add("$query, Hajipur, Bihar, India")
                }
                add("$query, Bihar, India")
            }

            for (term in searchTerms) {
                val results = try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(term, 5).orEmpty()
                } catch (_: Exception) {
                    emptyList()
                }

                for (addr in results) {
                    if (!addr.hasLatitude() || !addr.hasLongitude()) continue
                    val lat = addr.latitude
                    val lng = addr.longitude
                    val duplicate = combined.any {
                        abs(it.lat - lat) < 0.0012 && abs(it.lng - lng) < 0.0012
                    }
                    if (!duplicate) {
                        val title = extractShortAreaName(addr).ifBlank { query }
                        val subtitle = formatAddressLine(addr).ifBlank { "$title, Bihar, India" }
                        combined.add(
                            LocationSearchSuggestion(
                                title = title,
                                subtitle = subtitle,
                                lat = lat,
                                lng = lng,
                                isFromGeocoder = true
                            )
                        )
                    }
                }
                if (combined.size >= 8) break
            }
        }

        combined.sortedWith(
            compareByDescending<LocationSearchSuggestion> { it.isWithin10KmRange }
                .thenBy { it.distanceKm }
        )
    }

    private fun extractShortAreaName(address: Address): String {
        val parts = listOfNotNull(
            address.featureName?.takeIf { it.isNotBlank() && !it.all { ch -> ch.isDigit() } },
            address.subLocality?.takeIf { it.isNotBlank() },
            address.thoroughfare?.takeIf { it.isNotBlank() },
            address.locality?.takeIf { it.isNotBlank() }
        ).distinct()
        return parts.take(2).joinToString(", ").ifBlank {
            address.getAddressLine(0)?.substringBefore(",")?.trim().orEmpty()
        }
    }

    private fun formatAddressLine(address: Address): String {
        val fullLine = address.getAddressLine(0)?.trim()
        if (!fullLine.isNullOrBlank()) return fullLine
        return listOfNotNull(
            address.featureName?.takeIf { it.isNotBlank() },
            address.thoroughfare?.takeIf { it.isNotBlank() },
            address.subLocality?.takeIf { it.isNotBlank() },
            address.locality?.takeIf { it.isNotBlank() },
            address.adminArea?.takeIf { it.isNotBlank() },
            address.postalCode?.takeIf { it.isNotBlank() }
        ).distinct().joinToString(", ")
    }

    @SuppressLint("MissingPermission")
    fun streamLiveDeliveryGps(
        context: Context,
        intervalMs: Long = 5000L
    ): Flow<Pair<Double, Double>> = callbackFlow {
        if (!hasLocationPermission(context)) {
            close(SecurityException("Location permission required for live delivery GPS streaming"))
            return@callbackFlow
        }
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(2500L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val isNearHajipur = loc.latitude in 25.55..25.85 && loc.longitude in 85.05..85.38
                val lat = if (isNearHajipur) loc.latitude else HajipurLocations.KITCHEN_LAT
                val lng = if (isNearHajipur) loc.longitude else HajipurLocations.KITCHEN_LNG
                trySend(lat to lng)
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }
}
