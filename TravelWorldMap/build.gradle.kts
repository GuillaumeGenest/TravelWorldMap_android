plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp").version("0.0.8")
    // Kotlin is now built-in with AGP 9.0+
}

android {
    namespace = "com.guillaumegenest.travelworldmap"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    buildFeatures {
        compose = true
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // kotlinOptions removed: AGP 9.0+ uses targetCompatibility automatically

    // Publishing configuration for AGP 8.0+
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Google Maps Compose (api: exposed in public API)
    api(libs.maps.compose)
    api(libs.play.services.maps)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Maven publication configuration for Maven Central
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "io.github.guillaumegenest"
                artifactId = "travelworldmap-android"
                version = "0.0.1"

                pom {
                    name.set("TravelWorldMap Android")
                    description.set("An Android library for displaying an interactive world map with highlighted visited countries")
                    url.set("https://github.com/GuillaumeGenest/TravelWorldMap_android")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("guillaumegenest")
                            name.set("Guillaume Genest")
                            email.set("guillaume-genest2@hotmail.fr")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/GuillaumeGenest/TravelWorldMap_android.git")
                        developerConnection.set("scm:git:ssh://github.com/GuillaumeGenest/TravelWorldMap_android.git")
                        url.set("https://github.com/GuillaumeGenest/TravelWorldMap_android")
                    }
                }
            }
        }

    }

    // Signing configuration
    signing {
        sign(publishing.publications["release"])
    }
}

// Central Portal Publisher configuration
nmcp {
    publishAllPublications {
        username = project.findProperty("centralPortalUsername") as String?
        password = project.findProperty("centralPortalPassword") as String?
        publicationType = "USER_MANAGED"
    }
}