plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.rocasspb.avaawaand.shared"
        compileSdk = 36
        minSdk = 33
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

// WASM NPM Package is generated at:
// shared/build/dist/wasmJs/productionLibrary (for production)
// shared/build/dist/wasmJs/developmentLibrary (for development)

val isWindows = System.getProperty("os.name").lowercase().contains("win")

tasks.register<Exec>("publishToLocalNpm") {
    group = "publishing"
    description = "Publishes the development WASM library to local npm link"
    dependsOn("wasmJsBrowserDevelopmentLibraryDistribution")
    
    val distDir = layout.buildDirectory.dir("dist/wasmJs/developmentLibrary").get().asFile
    workingDir = distDir
    commandLine = if (isWindows) listOf("cmd", "/c", "npm link") else listOf("npm", "link")
}

tasks.register<Exec>("linkToWeb") {
    group = "publishing"
    description = "Links the published WASM library to the web project"
    dependsOn("publishToLocalNpm")
    
    workingDir = file("${project.rootDir}/web")
    commandLine = if (isWindows) listOf("cmd", "/c", "npm link AvaAwaAnd-shared") else listOf("npm", "link", "AvaAwaAnd-shared")
}
