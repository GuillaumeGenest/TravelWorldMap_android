# TravelWorldMap Library - Consumer ProGuard Rules
# These rules are automatically applied to apps that use this library

# Keep data classes annotated with @Keep (GeoJSON models)
# Nécessaire pour le parsing JSON avec réflexion
-keep @androidx.annotation.Keep class * { *; }

# Keep Country model (future API publique)
# Les consommateurs vont utiliser cette classe directement
-keep class com.guillaumegenest.travelworldmap.models.Country { *; }

# Keep Google Maps classes
# Protège LatLng, LatLngBounds utilisés dans l'API publique
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.maps.**

# Keep Kotlin Coroutines internals
# Nécessaire pour Dispatchers.IO et le loading async
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep JSONArray/JSONObject (used for parsing)
-keep class org.json.** { *; }
-dontwarn org.json.**
