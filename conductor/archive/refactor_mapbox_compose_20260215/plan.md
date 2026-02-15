# Plan: Refactor MainActivity to Mapbox Compose SDK

Refactor the map implementation to use the official Mapbox Maps Compose SDK for a more declarative and idiomatic integration.

## Phase 1: Preparation and Environment Verification [checkpoint: 45ddb0c]
- [x] Task: Verify Mapbox Compose SDK dependency and availability.
- [x] Task: Conduct a brief research on `MapboxMap` composable and `MapViewportState` usage in version 11.x.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Preparation' (Protocol in workflow.md)

## Phase 2: Refactor Camera and State Management [checkpoint: e8d63bc]
- [x] Task: Update `MainViewModel` to expose camera state in a format compatible with `MapViewportState` if necessary.
- [x] Task: Implement `rememberMapViewportState` in `MainActivity` and bind it to the initial camera position from ViewModel.
- [x] Task: Implement persistence logic for camera state using `MapViewportState` callbacks or observation.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Camera State' (Protocol in workflow.md)

## Phase 3: Implement Declarative MapboxMap
- [x] Task: Replace `AndroidView` with `MapboxMap` in `MapContent`.
- [x] Task: Configure map settings (gestures, compass, logo, attribution) using the `MapSettings` and `MapboxMap` parameters.
- [x] Task: Implement map event handlers (click and long click) using `onMapClickListener` and `onMapLongClickListener`.
- [x] Task: Conductor - User Manual Verification 'Phase 3: MapboxMap Integration' (Protocol in workflow.md)

## Phase 4: Refactor Raster Overlay to Declarative DSL
- [x] Task: Implement the dynamic Mapbox Style using the `MapboxStyle` and `StyleContent` DSL.
- [x] Task: Refactor `overlayRaster` logic to update state variables that drive the declarative `ImageSource` and `RasterLayer`.
- [x] Task: Ensure Terrain and Globe projection are correctly applied via the DSL.
- [x] Task: Conductor - User Manual Verification 'Phase 4: Raster Overlay' (Protocol in workflow.md)

## Phase 5: Verification and Finalization
- [x] Task: Run automated tests to ensure no regressions in ViewModel or Logic.
- [x] Task: Perform manual verification of all features (mode switching, point info, style toggle).
- [x] Task: Conductor - User Manual Verification 'Phase 5: Final Verification' (Protocol in workflow.md)

## Phase: Review Fixes
- [x] Task: Apply review suggestions 617879a
