plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.exposed.core)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.time)
            implementation(libs.sqlite.jdbc)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}