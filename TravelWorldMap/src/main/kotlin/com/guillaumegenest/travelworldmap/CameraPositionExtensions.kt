package com.guillaumegenest.travelworldmap

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

/**
 * Predefined camera positions for common world regions.
 *
 * Provides convenient starting positions for displaying specific regions of the world map.
 * Equivalent to iOS MKCoordinateRegion extensions.
 *
 * ## Usage
 * ```kotlin
 * WorldMapView(
 *     visitedCountries = listOf("FR", "US"),
 *     initialCameraPosition = CameraPositions.europe
 * )
 * ```
 */
object CameraPositions {

    /**
     * Europe region centered on Paris.
     * Default region for the world map.
     */
    val europe: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(48.858370, 2.294481), // Paris, France
        3.5f
    )

    /**
     * World view showing all continents.
     */
    val world: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(0.0, 0.0),
        1.5f
    )

    /**
     * North America region centered on the United States.
     */
    val northAmerica: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(40.0, -100.0),
        3.0f
    )

    /**
     * South America region centered on Brazil.
     */
    val southAmerica: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(-15.0, -60.0),
        3.0f
    )

    /**
     * Asia region centered on China.
     */
    val asia: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(30.0, 100.0),
        3.0f
    )

    /**
     * Africa region centered on the equator.
     */
    val africa: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(0.0, 20.0),
        3.0f
    )

    /**
     * Oceania region centered on Australia.
     */
    val oceania: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(-25.0, 135.0),
        3.5f
    )

    /**
     * France metropolitan region.
     */
    val france: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(46.603354, 2.888334),
        5.5f
    )

    /**
     * United States continental region.
     */
    val usa: CameraPosition = CameraPosition.fromLatLngZoom(
        LatLng(39.8283, -98.5795),
        4.0f
    )
}
