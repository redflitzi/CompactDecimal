import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // alias(libs.plugins.kotlinMultiplatform)
    kotlin("multiplatform") version "2.2.21"
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    // id("org.jetbrains.kotlin.kapt")
}

group = "io.github.redflitzi"
version = "1.0.0"

kotlin {
    jvm()
    androidLibrary {
        namespace = "io.github.redflitzi.compactdecimals"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    /*
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

    wasmJs {
        browser()
        // ...
        binaries.executable()
    }
    */
    js {
        browser {
        }
        binaries.executable()
    }


    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        /*
        val wasmJsMain by getting {
            dependencies {
                // Wasm-specific dependencies
            }
        }
        */

    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "library", version.toString())

    pom {
        name = "CompactDecimals Library"
        description = "Multiplatform CompactDecimals library."
        inceptionYear = "2025"
        url = "https://github.com/redflitzi/CompactDecimals/"
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }
        developers {
            developer {
                id = "XXX"
                name = "YYY"
                url = "ZZZ"
            }
        }
        scm {
            url = "XXX"
            connection = "YYY"
            developerConnection = "ZZZ"
        }
    }
}
