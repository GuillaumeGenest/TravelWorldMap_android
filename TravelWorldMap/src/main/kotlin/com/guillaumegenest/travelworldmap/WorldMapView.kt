package com.guillaumegenest.travelworldmap

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.math.min
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.guillaumegenest.travelworldmap.internal.ApiKeyValidator
import com.guillaumegenest.travelworldmap.internal.Logger
import com.guillaumegenest.travelworldmap.models.Country
import com.guillaumegenest.travelworldmap.services.CountryDataLoader

/**
 * Displays an interactive world map with visited countries highlighted using ISO codes.
 *
 * This composable uses Google Maps to render a world map where countries can be
 * colored based on whether they have been visited. Countries are identified using
 * ISO 3166-1 Alpha-2 codes (e.g., "FR" for France, "US" for United States).
 *
 * ## Requirements
 * Before using this composable, you must configure a Google Maps API key in your
 * AndroidManifest.xml:
 * ```xml
 * <application>
 *     <meta-data
 *         android:name="com.google.android.geo.API_KEY"
 *         android:value="YOUR_GOOGLE_MAPS_API_KEY" />
 * </application>
 * ```
 *
 * ## Example Usage
 * ```kotlin
 * WorldMapView(
 *     visitedCountries = listOf("FR", "US", "CA", "JP"),
 *     visitedColor = Color.Blue,
 *     unvisitedColor = Color.Gray
 * )
 * ```
 *
 * @param visitedCountries List of ISO 3166-1 Alpha-2 country codes representing
 *                         visited countries (e.g., ["FR", "US", "CA"]).
 * @param visitedColor Color used to fill visited countries. Default is [Color.Blue].
 * @param unvisitedColor Color used to fill unvisited countries. Default is [Color.Gray].
 * @param strokeColor Color for country borders. Default is [Color.White].
 * @param strokeWidth Width of country borders in pixels. Default is 0.5f.
 * @param maxPointsPerPolygon Maximum points per polygon for simplification. Default is 200.
 *                            Lower values improve performance but may reduce accuracy.
 * @param enableRegionOptimization Enable viewport culling to render only visible countries.
 *                                 Default is true. Significantly improves performance on zoom.
 * @param initialCameraPosition Starting camera position. Default is [CameraPosition.world].
 * @param modifier Modifier for the map container.
 *
 * @throws IllegalStateException if Google Maps API key is not configured in AndroidManifest.xml
 *
 * @see [ISO 3166-1 Alpha-2 country codes](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)
 * @see WorldMapView(visitedCountryNames, ...) for using country names instead of codes
 * @see WorldMapView(visitedColor, ...) for displaying a blank map
 */
@Composable
fun WorldMapView(
    visitedCountries: List<String>,
    visitedColor: Color = Color.Blue,
    unvisitedColor: Color = Color.Gray,
    strokeColor: Color = Color.White,
    strokeWidth: Float = 0.5f,
    maxPointsPerPolygon: Int = 200,
    enableRegionOptimization: Boolean = true,
    initialCameraPosition: CameraPosition = CameraPositions.world,
    modifier: Modifier = Modifier
) {
    WorldMapViewInternal(
        visitedCountryCodes = visitedCountries,
        visitedColor = visitedColor,
        unvisitedColor = unvisitedColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        maxPointsPerPolygon = maxPointsPerPolygon,
        enableRegionOptimization = enableRegionOptimization,
        initialCameraPosition = initialCameraPosition,
        modifier = modifier
    )
}

/**
 * Displays an interactive world map with visited countries highlighted using country names.
 *
 * This overload allows specifying countries by their English names instead of ISO codes.
 * The names are matched case-insensitively against the GeoJSON dataset.
 *
 * Equivalent to iOS `init(visitedCountryNames:...)`.
 *
 * ## Example Usage
 * ```kotlin
 * WorldMapView(
 *     visitedCountryNames = setOf("France", "United States", "Japan"),
 *     visitedColor = Color.Blue,
 *     unvisitedColor = Color.Gray
 * )
 * ```
 *
 * @param visitedCountryNames Set of country names (e.g., setOf("France", "United States")).
 *                            Names are matched case-insensitively.
 * @param visitedColor Color used to fill visited countries. Default is [Color.Blue].
 * @param unvisitedColor Color used to fill unvisited countries. Default is [Color.Gray].
 * @param strokeColor Color for country borders. Default is [Color.White].
 * @param strokeWidth Width of country borders in pixels. Default is 0.5f.
 * @param maxPointsPerPolygon Maximum points per polygon for simplification. Default is 200.
 * @param enableRegionOptimization Enable viewport culling. Default is true.
 * @param initialCameraPosition Starting camera position. Default is [CameraPositions.world].
 * @param modifier Modifier for the map container.
 *
 * @throws IllegalStateException if Google Maps API key is not configured in AndroidManifest.xml
 *
 * @see WorldMapView(visitedCountries, ...) for using ISO codes instead
 */
@Composable
fun WorldMapView(
    visitedCountryNames: Set<String>,
    visitedColor: Color = Color.Blue,
    unvisitedColor: Color = Color.Gray,
    strokeColor: Color = Color.White,
    strokeWidth: Float = 0.5f,
    maxPointsPerPolygon: Int = 200,
    enableRegionOptimization: Boolean = true,
    initialCameraPosition: CameraPosition = CameraPositions.world,
    modifier: Modifier = Modifier
) {
    // Convert country names to ISO codes
    val visitedCodes = remember(visitedCountryNames) {
        visitedCountryNames.mapNotNull { name ->
            CountryDataLoader.getCountry(byName = name)?.isoA2
        }.also { codes ->
            Logger.dataLoader.debug("WORLDMAP | Name to code conversion - input=${visitedCountryNames.size} output=${codes.size}")
        }
    }

    WorldMapViewInternal(
        visitedCountryCodes = visitedCodes,
        visitedColor = visitedColor,
        unvisitedColor = unvisitedColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        maxPointsPerPolygon = maxPointsPerPolygon,
        enableRegionOptimization = enableRegionOptimization,
        initialCameraPosition = initialCameraPosition,
        modifier = modifier
    )
}

/**
 * Displays a blank world map with no countries marked as visited.
 *
 * This overload is useful for displaying an empty map where all countries are shown
 * in the unvisited color. Users can use this as a base to build their own interactive
 * country selection UI.
 *
 * Equivalent to iOS `init(visitedColor:unvisitedColor:...)`.
 *
 * ## Example Usage
 * ```kotlin
 * WorldMapView(
 *     visitedColor = Color.Blue,
 *     unvisitedColor = Color.LightGray
 * )
 * ```
 *
 * @param visitedColor Color that would be used for visited countries. Default is [Color.Blue].
 * @param unvisitedColor Color used to fill all countries. Default is [Color.Gray].
 * @param modifier Modifier for the map container.
 *
 * @throws IllegalStateException if Google Maps API key is not configured in AndroidManifest.xml
 *
 * @see WorldMapView(visitedCountries, ...) for marking countries as visited
 */
@Composable
fun WorldMapView(
    visitedColor: Color = Color.Blue,
    unvisitedColor: Color = Color.Gray,
    strokeColor: Color = Color.White,
    strokeWidth: Float = 0.5f,
    maxPointsPerPolygon: Int = 200,
    enableRegionOptimization: Boolean = true,
    initialCameraPosition: CameraPosition = CameraPositions.world,
    modifier: Modifier = Modifier
) {
    WorldMapViewInternal(
        visitedCountryCodes = emptyList(),
        visitedColor = visitedColor,
        unvisitedColor = unvisitedColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        maxPointsPerPolygon = maxPointsPerPolygon,
        enableRegionOptimization = enableRegionOptimization,
        initialCameraPosition = initialCameraPosition,
        modifier = modifier
    )
}

/**
 * Internal implementation of WorldMapView.
 * This function contains the actual composable logic shared by all public overloads.
 */
@OptIn(FlowPreview::class)
@Composable
private fun WorldMapViewInternal(
    visitedCountryCodes: List<String>,
    visitedColor: Color,
    unvisitedColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    maxPointsPerPolygon: Int,
    enableRegionOptimization: Boolean,
    initialCameraPosition: CameraPosition,
    modifier: Modifier
) {
    val context = LocalContext.current

    // State management
    var allCountries by remember { mutableStateOf<List<Country>>(emptyList()) }
    var visibleCountries by remember { mutableStateOf<List<Country>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentRegion by remember { mutableStateOf<LatLngBounds?>(null) }

    // Load countries data and validate API key
    LaunchedEffect(Unit) {
        try {
            Logger.performance.info("WORLDMAP | Loading countries - start")
            Logger.performance.info("WORLDMAP | Configuration - optimization=$enableRegionOptimization maxPoints=$maxPointsPerPolygon")

            // Validate Google Maps API key
            ApiKeyValidator.validateApiKey(context)

            // Load GeoJSON data
            val loadedCountries = CountryDataLoader.loadCountries(context)
            allCountries = loadedCountries
            visibleCountries = loadedCountries
            isLoading = false

            Logger.performance.info("WORLDMAP | ${loadedCountries.size} countries loaded")

            // Print optimization stats
            printOptimizationStats(loadedCountries, maxPointsPerPolygon)
        } catch (e: Exception) {
            Logger.performance.error("WORLDMAP | Loading error", e)
            error = e.message ?: "Unknown error occurred"
            isLoading = false
        }
    }

    // Update visible countries when region changes
    LaunchedEffect(enableRegionOptimization, currentRegion) {
        if (enableRegionOptimization && currentRegion != null && allCountries.isNotEmpty()) {
            updateVisibleCountries(
                allCountries = allCountries,
                region = currentRegion!!,
                onUpdate = { updated ->
                    visibleCountries = updated
                }
            )
        } else if (!enableRegionOptimization) {
            Logger.performance.debug("WORLDMAP | Optimization disabled - ${allCountries.size} countries displayed")
            visibleCountries = allCountries
        }
    }

    // Render appropriate UI based on state
    when {
        isLoading -> LoadingView(modifier)
        error != null -> ErrorView(
            message = error!!,
            onRetry = {
                error = null
                isLoading = true
            },
            modifier = modifier
        )
        else -> MapView(
            countries = visibleCountries,
            visitedCountries = visitedCountryCodes,
            visitedColor = visitedColor,
            unvisitedColor = unvisitedColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            maxPointsPerPolygon = maxPointsPerPolygon,
            enableRegionOptimization = enableRegionOptimization,
            initialCameraPosition = initialCameraPosition,
            onRegionChange = { region ->
                currentRegion = region
            },
            modifier = modifier
        )
    }
}

/**
 * Displays a loading indicator while country data is being loaded.
 */
@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Displays an error message with a retry button when loading fails.
 */
@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error loading map",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

/**
 * Displays the Google Map with country polygons.
 */
@OptIn(FlowPreview::class)
@Composable
private fun MapView(
    countries: List<Country>,
    visitedCountries: List<String>,
    visitedColor: Color,
    unvisitedColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    maxPointsPerPolygon: Int,
    enableRegionOptimization: Boolean,
    initialCameraPosition: CameraPosition,
    onRegionChange: (LatLngBounds) -> Unit,
    modifier: Modifier = Modifier
) {
    // Initialize camera position
    val cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }

    // Observe camera position changes with debounce
    LaunchedEffect(cameraPositionState, enableRegionOptimization) {
        if (enableRegionOptimization) {
            snapshotFlow { cameraPositionState.position }
                .debounce(300) // Debounce to avoid too many updates
                .collect { position ->
                    val projection = cameraPositionState.projection
                    if (projection != null) {
                        val bounds = projection.visibleRegion.latLngBounds
                        onRegionChange(bounds)
                    }
                }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    ) {
        // Draw each country's polygons
        countries.forEach { country ->
            val fillColor = if (country.isoA2 in visitedCountries) {
                visitedColor.copy(alpha = 0.7f)
            } else {
                unvisitedColor.copy(alpha = 0.7f)
            }

            // Each country may have multiple polygons (for islands, territories, etc.)
            country.polygons.forEach { polygon ->
                if (polygon.isNotEmpty()) {
                    // Simplify polygon if it has too many points
                    val simplifiedPolygon = if (polygon.size > maxPointsPerPolygon) {
                        simplifyPolygon(polygon, maxPointsPerPolygon)
                    } else {
                        polygon
                    }

                    Polygon(
                        points = simplifiedPolygon,
                        fillColor = fillColor,
                        strokeColor = strokeColor.copy(alpha = 0.3f),
                        strokeWidth = strokeWidth
                    )
                }
            }
        }
    }
}

// MARK: - Optimization Functions

/**
 * Simplifies a polygon by reducing the number of points.
 *
 * This function uses a simple sampling algorithm: it takes every nth point
 * from the original polygon, where n = originalSize / maxPoints.
 * The last point is always included to ensure the polygon closes properly.
 *
 * Equivalent to iOS `simplifyPolygon(_:maxPoints:)` method.
 *
 * @param coordinates Original polygon coordinates.
 * @param maxPoints Maximum number of points to keep.
 * @return Simplified polygon with at most maxPoints coordinates.
 */
private fun simplifyPolygon(
    coordinates: List<LatLng>,
    maxPoints: Int
): List<LatLng> {
    if (coordinates.size <= maxPoints) return coordinates

    Logger.rendering.debug("WORLDMAP | Simplifying polygon - before=${coordinates.size} after=$maxPoints")

    val step = coordinates.size / maxPoints
    val result = mutableListOf<LatLng>()

    for (i in coordinates.indices step step) {
        result.add(coordinates[i])
    }

    // Always include the last point to close the polygon
    val last = coordinates.last()
    if (result.last() != last) {
        result.add(last)
    }

    return result
}

/**
 * Updates the list of visible countries based on the current map region.
 *
 * This function filters countries to only include those that have at least
 * one polygon visible within the provided bounds. For each visible country,
 * it further filters to only include the visible polygons.
 *
 * Equivalent to iOS `updateVisibleCountries()` method.
 *
 * @param allCountries Complete list of all countries.
 * @param region Current visible map bounds.
 * @param onUpdate Callback invoked with the filtered list of visible countries.
 */
private fun updateVisibleCountries(
    allCountries: List<Country>,
    region: LatLngBounds,
    onUpdate: (List<Country>) -> Unit
) {
    val visibleCountries = allCountries.mapNotNull { country ->
        val visiblePolygonIndices = country.getVisiblePolygonIndices(region)
        if (visiblePolygonIndices.isEmpty()) {
            null
        } else {
            country.withFilteredPolygons(visiblePolygonIndices)
        }
    }

    Logger.performance.debug("WORLDMAP | Visible countries - total=${allCountries.size} visible=${visibleCountries.size}")
    onUpdate(visibleCountries)
}

/**
 * Prints optimization statistics in debug builds.
 *
 * Logs information about:
 * - Total countries, polygons, and points
 * - Estimated reduction percentage from polygon simplification
 * - Top 5 most complex countries (by point count)
 *
 * Equivalent to iOS `printOptimizationStats(_:)` method.
 *
 * @param countries List of all loaded countries.
 * @param maxPointsPerPolygon Maximum points per polygon (for reduction estimate).
 */
private fun printOptimizationStats(
    countries: List<Country>,
    maxPointsPerPolygon: Int
) {
    val totalPolygons = countries.sumOf { it.polygons.size }
    val totalPoints = countries.sumOf { country ->
        country.polygons.sumOf { it.size }
    }
    val estimatedOptimized = min(totalPoints, totalPolygons * maxPointsPerPolygon)
    val reduction = ((1.0 - estimatedOptimized.toDouble() / totalPoints) * 100).toInt()

    // Stats
    Logger.performance.info(
        "WORLDMAP | Stats - countries=${countries.size} polygons=$totalPolygons " +
        "pointsBefore=$totalPoints pointsAfter=$estimatedOptimized reduction=$reduction%"
    )

    // Top 5 most complex countries
    val top5 = countries
        .map { country ->
            val points = country.polygons.sumOf { it.size }
            Triple(country.name, country.polygons.size, points)
        }
        .sortedByDescending { it.third }
        .take(5)

    top5.forEachIndexed { index, (name, polygonCount, pointCount) ->
        Logger.performance.info(
            "WORLDMAP | Top${index + 1} - $name polygons=$polygonCount points=$pointCount"
        )
    }
}

