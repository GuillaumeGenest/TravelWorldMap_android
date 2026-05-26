package com.guillaumegenest.travelworldmap.models

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Represents a country with its geographic data.
 *
 * This data class holds country metadata and geographic boundary information.
 * It provides lazy-computed properties for performance optimization and utility
 * methods for filtering polygons based on map visibility.
 *
 * Equivalent to `Country.swift` in the iOS version.
 *
 * @property id Unique identifier for the country (typically the ISO A2 code).
 * @property name The country's name.
 * @property isoA2 ISO 3166-1 Alpha-2 country code (2 letters, e.g., "FR", "US").
 * @property isoA3 ISO 3166-1 Alpha-3 country code (3 letters, e.g., "FRA", "USA").
 * @property coordinates Raw coordinate data in GeoJSON format: [[[lon, lat]]].
 *                       Normalized to always be a list of polygons, whether the
 *                       original geometry was Polygon or MultiPolygon.
 */
data class Country(
    val id: String,
    val name: String,
    val isoA2: String,
    val isoA3: String,
    val coordinates: List<List<List<DoubleArray>>>
) {
    /**
     * Lazy-computed polygons converted to LatLng format.
     *
     * This property converts the raw coordinate data from GeoJSON format
     * ([longitude, latitude]) to Google Maps LatLng objects (latitude, longitude).
     * Only the outer ring of each polygon is extracted.
     *
     * The computation is performed lazily on first access and cached afterward,
     * matching the iOS `lazy var polygons` behavior.
     *
     * @return A list of polygons, where each polygon is a list of LatLng points.
     */
    val polygons: List<List<LatLng>> by lazy {
        coordinates.map { polygon ->
            // Extract only the outer ring (first element)
            // Inner rings represent holes and are ignored
            polygon.firstOrNull()?.map { coord ->
                // GeoJSON uses [longitude, latitude]
                // LatLng expects (latitude, longitude)
                LatLng(coord[1], coord[0])
            } ?: emptyList()
        }
    }

    /**
     * The number of polygons in this country's geometry.
     *
     * This is useful for countries with non-contiguous territories (islands, exclaves).
     * Equivalent to iOS `polygonCount` computed property.
     *
     * @return The count of polygons.
     */
    val polygonCount: Int
        get() = coordinates.size

    /**
     * Returns the indices of polygons that fall within the visible map bounds.
     *
     * This method is used for rendering optimization: only polygons within the
     * current map viewport need to be drawn. It checks if any point of a polygon
     * intersects with the provided bounds.
     *
     * Equivalent to iOS `getVisiblePolygonIndices(in:)` method.
     *
     * @param bounds The visible map region as a LatLngBounds.
     * @return A list of polygon indices that are visible within the bounds.
     */
    fun getVisiblePolygonIndices(bounds: LatLngBounds): List<Int> {
        return polygons.indices.filter { index ->
            polygons[index].any { point ->
                bounds.contains(point)
            }
        }
    }

    /**
     * Creates a new Country instance containing only the specified polygons.
     *
     * This method is useful for creating a filtered copy of a country that contains
     * only the polygons visible in the current map region, reducing rendering overhead.
     * The id, name, and ISO codes are preserved.
     *
     * Equivalent to iOS `withFilteredPolygons(_:)` method.
     *
     * @param indices List of polygon indices to include in the new instance.
     * @return A new Country instance with filtered coordinates.
     */
    fun withFilteredPolygons(indices: List<Int>): Country {
        val filteredCoordinates = indices.mapNotNull { index ->
            coordinates.getOrNull(index)
        }
        return copy(coordinates = filteredCoordinates)
    }
}
