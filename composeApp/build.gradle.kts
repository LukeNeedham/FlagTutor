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

/**
 * Replaces the bundled flag images with the lead image of each country's Wikipedia flag article
 * (links in wikipedia_flag_links.json), so flags that have changed are picked up, e.g. Afghanistan.
 * Needs network access to en.wikipedia.org and thumb.wikimedia.org (flag images are served from
 * there; upload.wikimedia.org may also be needed if Wikimedia redirects).
 *
 *   ./gradlew downloadWikipediaFlags                      all countries
 *   ./gradlew downloadWikipediaFlags -PflagCodes=af,sy    only these alpha-2 codes
 *   ./gradlew downloadWikipediaFlags -PdryRun             list what would be downloaded
 *
 * The images come from Wikimedia Commons; the file each flag was taken from is recorded in
 * files/flag_image_sources.json. Flag colours are derived from the images at build time
 * (generateFlagColors), so nothing else needs regenerating.
 */
tasks.register<DownloadWikipediaFlagsTask>("downloadWikipediaFlags") {
    description = "Downloads each country's current flag from its Wikipedia flag article."
    group = "setup"
    val files = layout.projectDirectory.dir("src/commonMain/composeResources/files")
    linksFile.set(files.file("wikipedia_flag_links.json"))
    flagsDir.set(files.dir("flags"))
    sourcesFile.set(files.file("flag_image_sources.json"))
    thumbnailWidth.set(320)
    onlyCodes.set(providers.gradleProperty("flagCodes").orElse(""))
    dryRun.set(providers.gradleProperty("dryRun").isPresent)
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
