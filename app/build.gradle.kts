plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.qflix.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.qflix.tv"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // If you use TMDB, put: TMDB_API_KEY=xxxx in local.properties (NOT in git)
        val tmdbKey = (project.findProperty("TMDB_API_KEY") as String?) ?: ""
        buildConfigField("String", "TMDB_API_KEY", ""$tmdbKey"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.leanback:leanback:1.0.0")

    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui-leanback:1.3.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("org.json:json:20240303")

    // XML parsing (EPG XMLTV)
    implementation("xmlpull:xmlpull:1.1.3.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
