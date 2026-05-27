package com.guillaumegenest.travelworldmap.services

import android.content.Context
import com.guillaumegenest.travelworldmap.internal.Logger
import com.guillaumegenest.travelworldmap.models.Country
import com.guillaumegenest.travelworldmap.models.GeoJsonFeature
import com.guillaumegenest.travelworldmap.models.GeoJsonGeometry
import com.guillaumegenest.travelworldmap.models.GeoJsonProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Singleton service for loading and parsing country data from GeoJSON resources.
 *
 * This object manages the loading of geographic country data from the bundled
 * countries.json file. It caches the parsed data after the first load for performance.
 *
 * Equivalent to `CountryDataLoader.swift` in the iOS version.
 *
 * Key differences from iOS:
 * - iOS loads synchronously in the initializer
 * - Android loads asynchronously with suspend functions
 * - iOS uses JSONDecoder with Codable; Android uses manual JSONObject parsing
 */
internal object CountryDataLoader {

    private var _countries: List<Country>? = null

    /**
     * Gets a country by its name (case-insensitive).
     *
     * This method searches through the cached countries list to find a country
     * whose name matches the provided string. If countries are not yet loaded,
     * returns null.
     *
     * Equivalent to iOS's `getCountry(byName:)` method.
     *
     * @param byName The country name to search for (e.g., "France", "United States").
     * @return The matching [Country] object, or null if not found or not loaded.
     */
    fun getCountry(byName: String): Country? {
        return _countries?.firstOrNull {
            it.name.equals(byName, ignoreCase = true)
        }
    }

    /**
     * Returns all cached countries.
     *
     * If countries have not been loaded yet via [loadCountries], returns an empty list.
     * Equivalent to iOS's `getAllCountries()` method.
     *
     * @return List of all cached countries, or empty list if not loaded.
     */
    fun getAllCountries(): List<Country> {
        return _countries ?: emptyList()
    }

    /**
     * Loads and parses all countries from the resources/countries.json file.
     *
     * This function reads the GeoJSON file from assets, parses it, and converts
     * each feature into a [Country] object. The result is cached after the first
     * successful load.
     *
     * Equivalent to iOS's `let countries` property that loads in the initializer.
     *
     * @param context Android context for accessing assets.
     * @return A list of [Country] objects representing all countries in the dataset.
     * @throws java.io.IOException if the file cannot be read.
     * @throws org.json.JSONException if the JSON structure is invalid.
     */
    suspend fun loadCountries(context: Context): List<Country> {
        // Return cached data if available (like iOS singleton behavior)
        _countries?.let {
            Logger.dataLoader.debug("DATALOADER | Cache hit - ${it.size} countries already loaded")
            return it
        }

        Logger.dataLoader.info("DATALOADER | Loading GeoJSON - start")
        val startTime = System.currentTimeMillis()

        return withContext(Dispatchers.IO) {
            // Read the GeoJSON file from assets/resources/
            val jsonString = context.assets
                .open("resources/countries.json")
                .bufferedReader()
                .use { it.readText() }

            Logger.dataLoader.debug("DATALOADER | File read - ${jsonString.length} characters")

            // Parse GeoJSON into feature objects
            val features = parseGeoJson(jsonString)
            Logger.dataLoader.debug("DATALOADER | Parsing completed - ${features.size} features")

            // Convert each feature to a Country
            val countries = features.map { feature ->
                Country(
                    id = feature.properties.isoA2,
                    name = feature.properties.name ?: "",
                    isoA2 = feature.properties.isoA2,
                    isoA3 = feature.properties.isoA3,
                    coordinates = parseGeometry(feature.geometry)
                )
            }

            val duration = System.currentTimeMillis() - startTime
            Logger.dataLoader.info("DATALOADER | Loading completed - ${countries.size} countries in ${duration}ms")

            // Cache the result
            _countries = countries
            countries
        }
    }

    /**
     * Parses the root GeoJSON string into a list of features.
     *
     * @param jsonString The raw JSON string from the countries.json file.
     * @return A list of [GeoJsonFeature] objects.
     */
    private fun parseGeoJson(jsonString: String): List<GeoJsonFeature> {
        val geoJson = JSONObject(jsonString)
        val featuresArray = geoJson.getJSONArray("features")

        val features = mutableListOf<GeoJsonFeature>()

        for (i in 0 until featuresArray.length()) {
            val featureJson = featuresArray.getJSONObject(i)
            val propertiesJson = featureJson.getJSONObject("properties")
            val geometryJson = featureJson.getJSONObject("geometry")

            val properties = GeoJsonProperties(
                name = propertiesJson.optString("name", null),
                isoA2 = propertiesJson.getString("ISO3166-1-Alpha-2"),
                isoA3 = propertiesJson.getString("ISO3166-1-Alpha-3")
            )

            val geometry = GeoJsonGeometry(
                type = geometryJson.getString("type"),
                coordinates = geometryJson.getJSONArray("coordinates")
            )

            features.add(
                GeoJsonFeature(
                    type = featureJson.getString("type"),
                    properties = properties,
                    geometry = geometry
                )
            )
        }

        return features
    }

    /**
     * Parses and normalizes geometry coordinates.
     *
     * This function handles both Polygon and MultiPolygon geometries and normalizes
     * them into a consistent format: List<List<List<DoubleArray>>>.
     *
     * Equivalent to iOS's custom Decodable implementation that flattens geometries
     * using flatMap.
     *
     * @param geometry The GeoJSON geometry object.
     * @return Normalized coordinate structure [[[lon, lat]]].
     */
    private fun parseGeometry(geometry: GeoJsonGeometry): List<List<List<DoubleArray>>> {
        return when (geometry.type) {
            "Polygon" -> {
                // A Polygon is a single polygon, wrap it in a list
                listOf(parsePolygonCoordinates(geometry.coordinates))
            }
            "MultiPolygon" -> {
                // A MultiPolygon is multiple polygons
                parseMultiPolygonCoordinates(geometry.coordinates)
            }
            else -> emptyList()
        }
    }

    /**
     * Parses a single Polygon's coordinate rings.
     *
     * A Polygon in GeoJSON consists of an array of linear rings. The first ring
     * is the exterior boundary, and subsequent rings are holes.
     *
     * @param coordinates JSONArray representing a Polygon: [[[lon, lat], ...], ...]
     * @return List of rings, where each ring is a list of coordinate arrays.
     */
    private fun parsePolygonCoordinates(coordinates: JSONArray): List<List<DoubleArray>> {
        val rings = mutableListOf<List<DoubleArray>>()

        for (i in 0 until coordinates.length()) {
            val ring = coordinates.getJSONArray(i)
            val points = mutableListOf<DoubleArray>()

            for (j in 0 until ring.length()) {
                val coord = ring.getJSONArray(j)
                val longitude = coord.getDouble(0)
                val latitude = coord.getDouble(1)
                points.add(doubleArrayOf(longitude, latitude))
            }

            rings.add(points)
        }

        return rings
    }

    /**
     * Parses a MultiPolygon's coordinate structure.
     *
     * A MultiPolygon in GeoJSON is an array of Polygons. This function parses
     * each polygon and returns a flattened list.
     *
     * Equivalent to iOS's flatMap operation on MultiPolygon coordinates.
     *
     * @param coordinates JSONArray representing a MultiPolygon: [[[[lon, lat], ...]], ...]
     * @return List of polygons, where each polygon is a list of rings.
     */
    private fun parseMultiPolygonCoordinates(coordinates: JSONArray): List<List<List<DoubleArray>>> {
        val polygons = mutableListOf<List<List<DoubleArray>>>()

        for (i in 0 until coordinates.length()) {
            val polygonCoords = coordinates.getJSONArray(i)
            polygons.add(parsePolygonCoordinates(polygonCoords))
        }

        return polygons
    }
}
