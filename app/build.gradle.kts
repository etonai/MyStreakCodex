import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.pseddev.mystreak"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pseddev.mystreak"
        minSdk = 24
        targetSdk = 36
        versionCode = 23
        versionName = "0.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            buildConfigField("boolean", "IS_PRO_VERSION", "false")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_PRO_VERSION", "false")
            // Use release signing when keystore is configured, otherwise fall back to debug
            signingConfig = if (signingConfigs.getByName("release").storeFile != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug") // Fallback for development
            }
        }
        create("pro") {
            versionNameSuffix = "-pro"
            isDebuggable = false
            buildConfigField("boolean", "IS_PRO_VERSION", "true")
            buildConfigField("boolean", "DEBUG", "false")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing when keystore is configured, otherwise fall back to debug
            signingConfig = if (signingConfigs.getByName("release").storeFile != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug") // Fallback for development
            }
        }
    }

    // Disable baseline profiles and art profiles completely
    androidComponents {
        onVariants { variant ->
            variant.packaging.dex.useLegacyPackaging = true
        }
    }

    // Disable ART profile compilation
    aaptOptions {
        noCompress += listOf("tflite", "lite")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }


    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                // Disable baseline profiles to fix installation issues
                "META-INF/com/android/build/gradle/app-metadata.properties",
                "baseline-prof.txt",
                "baseline.prof",
                "baseline.profm",
                "**/baseline.prof*",
                "META-INF/com.android.tools/r8-from-*/baseline.prof",
                "META-INF/com.android.tools/proguard/baseline.prof",
                // Disable dex metadata to fix file lock issues
                "**/dex-metadata.json",
                "**/dex-metadata-map.properties",
                "META-INF/com.android.tools/r8-from-*/**",
                "**/art-profile.dex",
                "**/art-profile.txt"
            )
        }
    }

    // Custom APK naming
    applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                outputFileName = "MyStreak_${defaultConfig.versionName}.apk"
            }
        }
    }
}

dependencies {

    // Import the Firebase BoM (use latest stable version)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Analytics with Kotlin extensions
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase Crashlytics with Kotlin extensions
    implementation("com.google.firebase:firebase-crashlytics-ktx")


    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // CSV handling
    implementation(libs.opencsv)

    // ViewPager2
    implementation(libs.androidx.viewpager2)

    // Google Drive API
    implementation(libs.play.services.auth)
    implementation(libs.google.api.client.android)
    implementation(libs.google.http.client.android)
    implementation(libs.google.api.services.drive)

    // JSON serialization
    implementation(libs.gson)

    // Modern calendar view with date coloring support
    implementation("com.github.kizitonwose:CalendarView:2.4.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
