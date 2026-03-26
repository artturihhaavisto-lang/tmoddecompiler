import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.openjugg.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "OpenJugg"
            packageVersion = "1.0.0"
            linux { iconFile.set(project.file("icon.png")) }
        }
    }
}
