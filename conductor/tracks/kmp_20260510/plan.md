# Implementation Plan: KMP Core Migration

This plan outlines the migration of core data models and business logic to a Kotlin Multiplatform shared module, focusing on platform independence and cross-platform verification (Android & Web).

## Phase 1: Shared Module Infrastructure [checkpoint: a7fc33f]
- [x] Task: Create KMP `shared` module with Android and Web (JS/Wasm) targets.
- [x] Task: Configure shared dependencies in `libs.versions.toml` and `shared/build.gradle.kts`.
- [x] Task: Verify shared module build for both targets.
- [x] Task: Conductor - User Manual Verification 'Shared Module Infrastructure' (Protocol in workflow.md)

## Phase 2: Data Model Migration [checkpoint: 1e8ae77]
- [x] Task: Migrate `AvalancheData`, `Region`, `GpxData`, and related constants to `commonMain`.
- [x] Task: Update Android application code to use the new shared models.
- [x] Task: Write tests in `commonTest` to verify model serialization/deserialization if applicable.
- [x] Task: Conductor - User Manual Verification 'Data Model Migration' (Protocol in workflow.md)

## Phase 3: Logic Refactoring (Decoupling Bitmap) [checkpoint: 1e8ae77]
- [x] Task: Refactor `RasterGenerator` to remove dependency on `android.graphics.Bitmap`.
- [x] Task: Create a platform-agnostic representation for the generated terrain overlay (e.g., `ColorBuffer` or `ByteArray`).
- [x] Task: Update Android implementation of `RasterGenerator` usage to convert the shared representation back to `Bitmap` for Mapbox.
- [x] Task: Conductor - User Manual Verification 'Logic Refactoring' (Protocol in workflow.md)

## Phase 4: Business Logic Migration & Cross-Platform Testing [checkpoint: 1e8ae77]
- [x] Task: Move `AvalancheLogic`, `GeometryUtils`, `TerrainUtils`, and refactored `RasterGenerator` to `commonMain`.
- [x] Task: Migrate existing unit tests for this logic to `commonTest`.
- [x] Task: Execute and pass all unit tests on the Android target.
- [x] Task: Execute and pass all unit tests on the Web (JS/Wasm) target.
- [x] Task: Conductor - User Manual Verification 'Business Logic Migration & Cross-Platform Testing' (Protocol in workflow.md)

## Phase 5: Final Android Integration & Cleanup
- [x] Task: Ensure the Android app compiles and runs correctly with all core logic moved to the `shared` module.
- [x] Task: Perform manual smoke tests of all visualization modes (Bulletin, Risk, Custom) in the Android app.
- [x] Task: Conductor - User Manual Verification 'Final Android Integration & Cleanup' (Protocol in workflow.md)
