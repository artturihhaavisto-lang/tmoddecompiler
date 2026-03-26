import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

kotlin {

    // ─── Targets ─────────────────────────────────────────
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    // ─── Source Sets ─────────────────────────────────────
    sourceSets {

        // ── Common ───────────────────────────────────────
        commonMain.dependencies {
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            // Date/Time
            implementation(libs.kotlinx.datetime)
            // SQLDelight runtime + coroutines
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            // Koin
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.core)
        }

        // ── Android ──────────────────────────────────────
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
        }

        // ── iOS ──────────────────────────────────────────
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }

        // ── Desktop (JVM / Linux / macOS / Windows) ──────
        val desktopMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.jvm)
            }
        }
    }
}

// ─── Android Config ──────────────────────────────────────
android {
    namespace = "com.openjugg.shared"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// ─── SQLDelight Config ───────────────────────────────────
sqldelight {
    databases {
        create("OpenJuggDatabase") {
            packageName.set("com.openjugg.db")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}
