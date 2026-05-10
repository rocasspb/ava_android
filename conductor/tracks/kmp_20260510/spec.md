# Specification: KMP Core Migration (Simplified)

## Overview
This track focuses on migrating the application's core data models and business logic to a Kotlin Multiplatform (KMP) shared module. The primary objective is to decouple this logic from Android-native dependencies (specifically `Bitmap`) to ensure compatibility with both Android and Web platforms.

## Goals
- Establish a KMP shared module targeting Android and Web.
- Migrate and refactor business logic to be platform-agnostic.
- Eliminate dependencies on Android-specific classes within the core logic.
- Verify cross-platform compatibility through shared unit tests.

## Scope
- **Models & Constants:** Migration of common data classes (e.g., `AvalancheData`, `Region`, `GpxData`).
- **Business Logic:** Porting and refactoring of `AvalancheLogic`, `GeometryUtils`, `TerrainUtils`, and `RasterGenerator`.
- **Refactoring:** Specifically removing `android.graphics.Bitmap` usage from `RasterGenerator`, replacing it with a platform-agnostic representation (e.g., a raw byte array or a custom color buffer).

## Target Platforms
- **Android**
- **Web** (JS or Wasm target)
- *(Note: Structure should remain compatible with future iOS expansion)*

## Acceptance Criteria
- The `shared` module builds successfully for Android and Web.
- All unit tests for the migrated logic pass on both Android and Web targets.
- The Android application functions correctly using the shared logic, with no regressions in terrain visualization.

## Out of Scope
- Networking migration (Retrofit remains in the Android module).
- Persistence migration.
- UI layer migration.
- iOS-specific implementation work.
