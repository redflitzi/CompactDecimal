plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    kotlin("multiplatform") version "2.2.21" apply false
    // alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}
