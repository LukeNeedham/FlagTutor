import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    // Explicit because the manual dependsOn edges below disable the implicit default template.
    applyDefaultHierarchyTemplate()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.room.ktx)
            implementation(libs.datastore.preferences)
        }
        // Room has no web support, so the persistence code lives in this source set, shared only
        // by Android and iOS; the web target supplies its own FlagAttemptRepository.
        val roomMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)
            }
        }
        androidMain.get().dependsOn(roomMain)
        iosMain.get().dependsOn(roomMain)
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
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.lifecycle.kmp)
        }
    }
}

android {
    namespace = "com.flagtutor.app"
    compileSdk = 36

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
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    // Room's KSP processor only generates Java stubs by default; Kotlin/Native targets
    // (iOS) can't consume those, so Kotlin sources are required for multiplatform.
    arg("room.generateKotlin", "true")
}

// ─── Asset generation tasks ──────────────────────────────────────────────────

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

/**
 * Builds files/country_data.json from Wikipedia: the ISO 3166-1 country list, each country's article,
 * its flag article, the flag image (downloaded into files/flags) and a short text on the flag's symbolism.
 * Needs network access to en.wikipedia.org and thumb.wikimedia.org (where flag images are served from).
 * Which countries have no official flag, and any flag article that cannot be found by its title, are
 * listed in scripts/country_data_overrides.json. The symbolism texts are written by hand and kept by the task.
 *
 *   ./gradlew generateCountryData                         rebuild everything
 *   ./gradlew generateCountryData -PflagCodes=af,sy       only download the images for these alpha-2 codes
 *   ./gradlew generateCountryData -PskipDownloads         rebuild the data file without downloading images
 *   ./gradlew generateCountryData -PpruneFlags            after downloading every flag, delete flag PNGs the data file no longer lists
 *
 * The images come from Wikimedia Commons; the file each one was taken from is recorded in the data file.
 * Flag colours are derived from the images at build time (generateFlagColors), so nothing else needs regenerating.
 */
tasks.register<BuildCountryDataTask>("generateCountryData") {
    description = "Builds country_data.json and downloads each country's flag from Wikipedia."
    group = "setup"
    val files = layout.projectDirectory.dir("src/commonMain/composeResources/files")
    overridesFile.set(rootProject.layout.projectDirectory.file("scripts/country_data_overrides.json"))
    dataFile.set(files.file("country_data.json"))
    flagsDir.set(files.dir("flags"))
    thumbnailWidth.set(320)
    onlyCodes.set(providers.gradleProperty("flagCodes").orElse(""))
    skipDownloads.set(providers.gradleProperty("skipDownloads").isPresent)
    pruneFlags.set(providers.gradleProperty("pruneFlags").isPresent)
}

// ─── Flag colour extraction ──────────────────────────────────────────────────

// Dominant colours are computed once at build time and shipped as a compose resource, so no
// platform needs its own image-decoding implementation.
val generateFlagColors = tasks.register<GenerateFlagColorsTask>("generateFlagColors") {
    description = "Extracts each flag's dominant colours into a compose resource."
    group = "build"
    flagsDir.set(layout.projectDirectory.dir("src/commonMain/composeResources/files/flags"))
    outputDir.set(layout.buildDirectory.dir("generated/flagColors"))
}

// Groups of countries sharing a pixel-identical flag, so the game never offers them as options together.
val generateIdenticalFlags = tasks.register<GenerateIdenticalFlagsTask>("generateIdenticalFlags") {
    description = "Lists groups of countries that use identical flags."
    group = "build"
    flagsDir.set(layout.projectDirectory.dir("src/commonMain/composeResources/files/flags"))
    outputDir.set(layout.buildDirectory.dir("generated/identicalFlags"))
}

// customDirectory replaces commonMain's default composeResources directory rather than adding to
// it, so the generated file is merged with the checked-in resources into one directory.
val mergedCommonResources = tasks.register<Sync>("mergeCommonResources") {
    from(layout.projectDirectory.dir("src/commonMain/composeResources"))
    from(generateFlagColors.flatMap { it.outputDir })
    from(generateIdenticalFlags.flatMap { it.outputDir })
    into(layout.buildDirectory.dir("generated/commonComposeResources"))
}

compose.resources {
    // Pinned so the generated package doesn't change with the Gradle root project name.
    packageOfResClass = "flagtutor.composeapp.generated.resources"
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = mergedCommonResources.map { it.destinationDir }.let { provider ->
            layout.dir(provider)
        },
    )
}
