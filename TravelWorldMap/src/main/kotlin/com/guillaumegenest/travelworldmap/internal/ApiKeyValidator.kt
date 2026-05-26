package com.guillaumegenest.travelworldmap.internal

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Internal utility to validate Google Maps API key configuration.
 *
 * This validator ensures that the consuming app has properly configured
 * a Google Maps API key in their AndroidManifest.xml before attempting
 * to render the world map.
 */
internal object ApiKeyValidator {

    private const val API_KEY_META_DATA = "com.google.android.geo.API_KEY"
    private const val TAG = "TravelWorldMap"

    /**
     * Reads the Google Maps API key from AndroidManifest.xml meta-data.
     *
     * The key is expected to be declared as:
     * ```xml
     * <meta-data
     *     android:name="com.google.android.geo.API_KEY"
     *     android:value="YOUR_API_KEY" />
     * ```
     *
     * @param context Android context to access PackageManager
     * @return The API key if found, null otherwise
     */
    fun getApiKey(context: Context): String? {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString(API_KEY_META_DATA)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read API key from manifest", e)
            null
        }
    }

    /**
     * Validates that a Google Maps API key is properly configured.
     *
     * This method checks:
     * 1. That the API key exists in AndroidManifest.xml
     * 2. That the key is not blank
     *
     * @param context Android context to access PackageManager
     * @throws IllegalStateException if no API key is found or if it's blank
     */
    fun validateApiKey(context: Context) {
        val apiKey = getApiKey(context)

        if (apiKey.isNullOrBlank()) {
            throw IllegalStateException(
                """
                ╔═══════════════════════════════════════════════════════════════╗
                ║  Google Maps API Key Missing!                                  ║
                ╚═══════════════════════════════════════════════════════════════╝

                TravelWorldMap requires a Google Maps API key to function.

                Please add the following to your AndroidManifest.xml:

                <application>
                    <meta-data
                        android:name="com.google.android.geo.API_KEY"
                        android:value="YOUR_GOOGLE_MAPS_API_KEY" />
                </application>

                📖 How to get an API key:
                   1. Go to https://console.cloud.google.com/
                   2. Create a new project (or select existing)
                   3. Enable "Maps SDK for Android"
                   4. Go to "Credentials" and create an API key
                   5. Restrict the key to your app's package name

                📚 Full documentation:
                   https://github.com/GuillaumeGenest/TravelWorldMap-Android#setup
                """.trimIndent()
            )
        }
    }
}
