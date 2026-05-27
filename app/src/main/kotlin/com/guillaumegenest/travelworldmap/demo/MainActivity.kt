package com.guillaumegenest.travelworldmap.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.guillaumegenest.travelworldmap.CameraPositions
import com.guillaumegenest.travelworldmap.WorldMapView

/**
 * Demo activity showcasing the TravelWorldMap library.
 *
 * This activity demonstrates how to use the WorldMapView composable
 * to display a world map with highlighted visited countries.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelWorldMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DemoScreen()
                }
            }
        }
    }
}

@Composable
fun TravelWorldMapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        content = content
    )
}

@Composable
fun DemoScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "TravelWorldMap Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Example 1: Using ISO country codes with optimization
        WorldMapView(
            visitedCountries = listOf(
                "FR", // France
                "US", // United States
                "CA", // Canada
                "JP", // Japan
                "AU", // Australia
                "BR", // Brazil
                "DE", // Germany
                "IT", // Italy
                "ES"  // Spain
            ),
            visitedColor = Color(0xFF2196F3), // Blue
            unvisitedColor = Color(0xFFE0E0E0), // Light Gray
            strokeColor = Color.White,
            strokeWidth = 0.5f,
            maxPointsPerPolygon = 200, // Simplification for performance
            enableRegionOptimization = true, // Only render visible countries
            initialCameraPosition = CameraPositions.europe, // Start on Europe
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        // Example 2: Using country names with custom region
        // Uncomment to test:
        /*
        WorldMapView(
            visitedCountryNames = setOf(
                "France",
                "United States",
                "Canada",
                "Japan"
            ),
            visitedColor = Color(0xFF4CAF50), // Green
            unvisitedColor = Color(0xFFE0E0E0),
            strokeColor = Color(0xFF424242), // Dark gray borders
            strokeWidth = 1f, // Thicker borders
            initialCameraPosition = CameraPositions.world, // World view
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
        */

        // Example 3: Blank map for custom country selection UI
        // Uncomment to test:
        /*
        WorldMapView(
            visitedColor = Color(0xFFFF5722), // Deep Orange
            unvisitedColor = Color(0xFFECEFF1), // Blue Gray 50
            strokeColor = Color(0xFF37474F), // Blue Gray 800
            strokeWidth = 0.8f,
            maxPointsPerPolygon = 100, // More aggressive simplification
            initialCameraPosition = CameraPositions.europe,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
        */
    }
}
