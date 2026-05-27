# TravelWorldMap Android

[![Maven Central](https://img.shields.io/maven-central/v/io.github.guillaumegenest/travelworldmap-android.svg)](https://central.sonatype.com/artifact/io.github.guillaumegenest/travelworldmap-android)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)

An Android library for displaying an interactive world map with highlighted visited countries. Built with Jetpack Compose and Google Maps.

> **iOS Version:** This is the Android equivalent of the [TravelWorldMap Swift Package](https://github.com/GuillaumeGenest/TravelWorldMap)

## Features

- **Interactive World Map** powered by Google Maps
- **Customizable Colors** for visited and unvisited countries
- **ISO 3166-1 Alpha-2** country code support (e.g., "FR", "US", "CA")
- **Lightweight** - minimal dependencies
- **Jetpack Compose** native API
- **Easy Integration** - just add your Google Maps API key
- **Polygon & MultiPolygon** support for complex country shapes

## Requirements

- Android 8.0 (API 26) or higher
- Jetpack Compose
- Google Maps API key

## Installation

Add the library to your module `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.guillaumegenest:travelworldmap-android:0.0.1")
}
```

The library is published on Maven Central, which is already included by default in Android projects.

## Setup

### 1. Get a Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select an existing one)
3. Enable **Maps SDK for Android**
4. Navigate to **Credentials** and create an API key
5. **Restrict the key** to your app's package name for security

### 2. Add API Key to AndroidManifest.xml

Add the following inside the `<application>` tag in your `AndroidManifest.xml`:

```xml
<application>
    <!-- ... other configuration ... -->
    
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_GOOGLE_MAPS_API_KEY" />
        
</application>
```

**Security Best Practice:** Don't commit your API key directly. Use `local.properties`:

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        // Read from local.properties
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        
        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("maps.api.key", "")
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

```properties
# local.properties (add to .gitignore)
maps.api.key=AIzaSyC...
```

## Usage

### Basic Example

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.guillaumegenest.travelworldmap.WorldMapView

@Composable
fun MyTravelScreen() {
    WorldMapView(
        visitedCountries = listOf("FR", "US", "CA", "JP", "AU")
    )
}
```

### Custom Colors

```kotlin
WorldMapView(
    visitedCountries = listOf("FR", "ES", "IT", "DE"),
    visitedColor = Color(0xFF4CAF50),      // Green
    unvisitedColor = Color(0xFFBDBDBD)     // Light Gray
)
```

### With Modifier

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

WorldMapView(
    visitedCountries = listOf("FR", "US", "CA"),
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
)
```

## API Reference

### WorldMapView

```kotlin
@Composable
fun WorldMapView(
    visitedCountries: List<String>,
    visitedColor: Color = Color.Blue,
    unvisitedColor: Color = Color.Gray,
    modifier: Modifier = Modifier
)
```

#### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `visitedCountries` | `List<String>` | **Required** | List of ISO 3166-1 Alpha-2 country codes (e.g., `["FR", "US"]`) |
| `visitedColor` | `Color` | `Color.Blue` | Color for visited countries |
| `unvisitedColor` | `Color` | `Color.Gray` | Color for unvisited countries |
| `modifier` | `Modifier` | `Modifier` | Modifier for the map container |

### Supported Country Codes

Uses **ISO 3166-1 Alpha-2** standard (2-letter codes):

- France: `"FR"`
- United States: `"US"`
- Canada: `"CA"`
- Japan: `"JP"`
- Australia: `"AU"`
- United Kingdom: `"GB"`
- Germany: `"DE"`
- ... and all other countries

[Full list of country codes](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)

## Examples

### Travel Tracker App

```kotlin
@Composable
fun TravelTrackerScreen(viewModel: TravelViewModel) {
    val visitedCountries by viewModel.visitedCountries.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "You've visited ${visitedCountries.size} countries!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        WorldMapView(
            visitedCountries = visitedCountries,
            visitedColor = Color(0xFF2196F3),
            unvisitedColor = Color(0xFFE0E0E0),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
    }
}
```

### Dynamic Theme Colors

```kotlin
@Composable
fun ThemedWorldMap(visitedCountries: List<String>) {
    val visitedColor = MaterialTheme.colorScheme.primary
    val unvisitedColor = MaterialTheme.colorScheme.surfaceVariant
    
    WorldMapView(
        visitedCountries = visitedCountries,
        visitedColor = visitedColor,
        unvisitedColor = unvisitedColor
    )
}
```

## Troubleshooting

### "Google Maps API Key Missing!" error

Make sure you've added the API key to your `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY" />
```

### Map shows blank/gray screen

1. Check that you've enabled **Maps SDK for Android** in Google Cloud Console
2. Verify your API key is correct
3. Check API key restrictions (package name, Android app restrictions)
4. Check Logcat for Google Maps errors

### Country not highlighting

Ensure you're using the correct **ISO 3166-1 Alpha-2** code (2 letters, uppercase).
Example: Use `"FR"` for France, not `"FRA"` or `"France"`.

## Architecture

```
TravelWorldMap/
├── models/
│   ├── Country.kt              # Business model with polygon data
│   └── GeoJsonModels.kt        # GeoJSON parsing models
├── services/
│   └── CountryDataLoader.kt    # Async GeoJSON loader
├── internal/
│   └── ApiKeyValidator.kt      # API key validation
└── WorldMapView.kt             # Public Compose API
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Credits

- **iOS Version:** [TravelWorldMap Swift Package](https://github.com/GuillaumeGenest/TravelWorldMap)
- **GeoJSON Data:** [Natural Earth](https://www.naturalearthdata.com/)
- **Maps SDK:** [Google Maps Platform](https://developers.google.com/maps)

## Contact

Guillaume Genest - [@GuillaumeGenest](https://github.com/GuillaumeGenest)

---

Made with love for travelers
