package com.guillaumegenest.travelworldmap.internal

import android.util.Log

/**
 * Internal logging utility for TravelWorldMap library.
 *
 * Provides categorized logging similar to iOS OSLog subsystem.
 * All logs are prefixed with "TWM:" (TravelWorldMap) for easy filtering in Logcat.
 *
 * Equivalent to iOS Logger extension with subsystem categories.
 *
 * ## Usage in Logcat
 * Filter by tag:
 * - `TWM:DataLoader` - GeoJSON loading and parsing
 * - `TWM:Rendering` - Map rendering and polygon drawing
 * - `TWM:Performance` - Performance metrics and optimization stats
 * - `TWM:Interaction` - User interactions and camera changes
 *
 * ## Example
 * ```kotlin
 * Logger.performance.info("WORLDMAP | Chargement pays — début")
 * Logger.rendering.debug("WORLDMAP | Simplification polygon — avant=1000 après=200")
 * ```
 */
internal object Logger {

    /**
     * Data loading and parsing operations.
     * Use for GeoJSON loading, country data caching.
     */
    object dataLoader {
        private const val TAG = "TWM:DataLoader"

        fun info(message: String) = Log.i(TAG, message)
        fun debug(message: String) = Log.d(TAG, message)
        fun warn(message: String) = Log.w(TAG, message)
        fun error(message: String, throwable: Throwable? = null) {
            if (throwable != null) Log.e(TAG, message, throwable)
            else Log.e(TAG, message)
        }
    }

    /**
     * Map rendering operations.
     * Use for polygon drawing, simplification, and visual updates.
     */
    object rendering {
        private const val TAG = "TWM:Rendering"

        fun info(message: String) = Log.i(TAG, message)
        fun debug(message: String) = Log.d(TAG, message)
        fun warn(message: String) = Log.w(TAG, message)
        fun error(message: String, throwable: Throwable? = null) {
            if (throwable != null) Log.e(TAG, message, throwable)
            else Log.e(TAG, message)
        }
    }

    /**
     * Performance metrics and optimization.
     * Use for tracking loading times, polygon counts, memory usage.
     */
    object performance {
        private const val TAG = "TWM:Performance"

        fun info(message: String) = Log.i(TAG, message)
        fun debug(message: String) = Log.d(TAG, message)
        fun warn(message: String) = Log.w(TAG, message)
        fun error(message: String, throwable: Throwable? = null) {
            if (throwable != null) Log.e(TAG, message, throwable)
            else Log.e(TAG, message)
        }
    }

    /**
     * User interactions and map events.
     * Use for camera changes, zoom events, user gestures.
     */
    object interaction {
        private const val TAG = "TWM:Interaction"

        fun info(message: String) = Log.i(TAG, message)
        fun debug(message: String) = Log.d(TAG, message)
        fun warn(message: String) = Log.w(TAG, message)
        fun error(message: String, throwable: Throwable? = null) {
            if (throwable != null) Log.e(TAG, message, throwable)
            else Log.e(TAG, message)
        }
    }
}
