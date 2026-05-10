# Implementation Plan: KMP Core Migration

This plan outlines the migration of core data models and business logic to a Kotlin Multiplatform shared module, focusing on platform independence and cross-platform verification (Android & Web).

## Phase 1: Shared Module Infrastructure [checkpoint: a7fc33f]
- [x] Task: Create KMP `shared` module with Android and Web (JS/Wasm) targets.
- [x] Task: Configure shared dependencies in `libs.versions.toml` and `shared/build.gradle.kts`.
- [x] Task: Verify shared module build for both targets.
- [x] Task: Conductor - User Manual Verification 'Shared Module Infrastructure' (Protocol in workflow.md)

## Phase 2: Data Model Migration
- [~] Task: Migrate `AvalancheData`, `Region`, `GpxData`, and related constants to `commonMain`.
- [ ] Task: Update Android application code to use the new shared models.
- [ ] Task: Write tests in `commonTest` to verify model serialization/deserialization if applicable.
- [ ] Task: Conductor - User Manual Verification 'Data Model Migration' (Protocol in workflow.md)

## Phase 3: Logic Refactoring (Decoupling Bitmap)
- [ ] Task: Refactor `RasterGenerator` to remove dependency on `android.graphics.Bitmap`.
- [ ] Task: Create a platform-agnostic representation for the generated terrain overlay (e.g., `ColorBuffer` or `ByteArray`).
- [ ] Task: Update Android implementation of `RasterGenerator` usage to convert the shared representation back to `Bitmap` for Mapbox.
- [ ] Task: Conductor - User Manual Verification 'Logic Refactoring' (Protocol in workflow.md)

## Phase 4: Business Logic Migration & Cross-Platform Testing
- [ ] Task: Move `AvalancheLogic`, `GeometryUtils`, `TerrainUtils`, and refactored `RasterGenerator` to `commonMain`.
- [ ] Task: Migrate existing unit tests for this logic to `commonTest`.
- [ ] Task: Execute and pass all unit tests on the Android target.
- [ ] Task: Execute and pass all unit tests on the Web (JS/Wasm) target.
- [ ] Task: Conductor - User Manual Verification 'Business Logic Migration & Cross-Platform Testing' (Protocol in workflow.md)

## Phase 5: Final Android Integration & Cleanup
- [ ] Task: Ensure the Android app compiles and runs correctly with all core logic moved to the `shared` module.
- [ ] Task: Perform manual smoke tests of all visualization modes (Bulletin, Risk, Custom) in the Android app.
- [ ] Task: Conductor - User Manual Verification 'Final Android Integration & Cleanup' (Protocol in workflow.md)
