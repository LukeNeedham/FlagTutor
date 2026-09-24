import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.room.runtime)
            implementation(libs.room.ktx)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
        }
    }
}

android {
    namespace = "com.flagtutor.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.flagtutor.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        getByName("debug") {
            // Checked-in keystore with fixed credentials so debug builds are reproducible
            // across machines and CI, rather than relying on each user's local ~/.android/debug.keystore.
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// ─── Asset generation tasks ──────────────────────────────────────────────────

/**
 * Downloads country flag SVGs from hampusborgos/country-flags on GitHub and converts them
 * to 320px-wide PNGs. Requires Python 3 with cairosvg: pip3 install cairosvg
 * Only needed if flag images need to be regenerated; they are already committed to the repo.
 */
tasks.register("downloadFlags") {
    description = "Downloads and converts country flag images from GitHub into compose resources."
    group = "setup"
    doLast {
        val script = rootProject.file("scripts/download_flags.py")
        exec {
            commandLine("python3", script.absolutePath)
        }
    }
}

/**
 * Downloads each country's globe/orthographic locator map from its Wikipedia infobox using
 * download_wikipedia_maps.py. Run once after checkout: ./gradlew downloadWikipediaMaps
 * These images are sourced from Wikimedia Commons; see the in-app credits screen for attribution.
 */
tasks.register("downloadWikipediaMaps") {
    description = "Downloads globe/orthographic map images for every country from Wikipedia."
    group = "setup"
    inputs.file("src/commonMain/composeResources/files/wikipedia_links.json")
    outputs.dir("src/commonMain/composeResources/files/maps")
    doLast {
        val script = rootProject.file("scripts/download_wikipedia_maps.py")
        exec {
            commandLine("python3", script.absolutePath)
        }
    }
}
