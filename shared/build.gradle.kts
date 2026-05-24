plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.library()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
        }
        wasmJsMain.dependencies {
        }
    }
}

val webKmpDir = file("${project.rootDir}/web/src/kmp")

tasks.register<Copy>("deliverWasmDev") {
    group = "delivery"
    description = "Delivers development Wasm build to web folder"
    from(tasks.named("wasmJsBrowserDevelopmentLibraryDistribution"))
    into(webKmpDir)
}

tasks.register<Copy>("deliverWasmProd") {
    group = "delivery"
    description = "Delivers production Wasm build to web folder"
    from(tasks.named("wasmJsBrowserProductionLibraryDistribution"))
    into(webKmpDir)
}

tasks.register("deliverWasm") {
    group = "delivery"
    description = "Delivers development Wasm build to web folder (alias for deliverWasmDev)"
    dependsOn("deliverWasmDev")
}

android {
    namespace = "com.rocasspb.avaawaand.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
