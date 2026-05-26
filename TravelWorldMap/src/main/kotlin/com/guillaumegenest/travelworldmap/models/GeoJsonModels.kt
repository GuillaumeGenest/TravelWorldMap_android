package com.guillaumegenest.travelworldmap.models

import androidx.annotation.Keep
import org.json.JSONArray

/**
 * Root GeoJSON FeatureCollection structure.
 *
 * This represents the top-level object in a GeoJSON file containing a collection
 * of geographic features. Equivalent to `GeoJSONFeatureCollection` in the iOS version.
 *
 * @property type The GeoJSON object type, always "FeatureCollection".
 * @property features List of individual geographic features (countries).
 */
@Keep
internal data class GeoJsonFeatureCollection(
    val type: String,
    val features: List<GeoJsonFeature>
)

/**
 * Individual GeoJSON Feature representing a country.
 *
 * Each feature contains metadata (properties) and geographic data (geometry).
 * Equivalent to `GeoJSONFeature` in the iOS version.
 *
 * @property type The GeoJSON object type, always "Feature".
 * @property properties Metadata about the country (name, ISO codes).
 * @property geometry Geographic boundary data (polygons).
 */
@Keep
internal data class GeoJsonFeature(
    val type: String,
    val properties: GeoJsonProperties,
    val geometry: GeoJsonGeometry
)

/**
 * Feature properties containing country metadata.
 *
 * Equivalent to `GeoJSONProperties` in the iOS version.
 *
 * @property name The country name (may be null in some datasets).
 * @property isoA2 ISO 3166-1 Alpha-2 country code (2 letters, e.g., "FR", "US").
 *                 Parsed from JSON field "ISO3166-1-Alpha-2".
 * @property isoA3 ISO 3166-1 Alpha-3 country code (3 letters, e.g., "FRA", "USA").
 *                 Parsed from JSON field "ISO3166-1-Alpha-3".
 */
@Keep
internal data class GeoJsonProperties(
    val name: String?,
    val isoA2: String,
    val isoA3: String
)

/**
 * Geometry data representing country boundaries.
 *
 * This class handles both Polygon and MultiPolygon geometry types.
 * The coordinates are stored as raw JSONArray to support custom parsing logic,
 * similar to iOS's custom Decodable implementation.
 *
 * Equivalent to `GeoJSONGeometry` in the iOS version with custom decoding.
 *
 * @property type The geometry type: "Polygon" or "MultiPolygon".
 * @property coordinates Raw coordinate data as JSONArray. Structure varies by type:
 *                       - Polygon: [[[lon, lat], [lon, lat], ...]]
 *                       - MultiPolygon: [[[[lon, lat], ...]], [[[lon, lat], ...]]]
 */
@Keep
internal data class GeoJsonGeometry(
    val type: String,
    val coordinates: JSONArray
)
