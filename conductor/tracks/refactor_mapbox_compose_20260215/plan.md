# Plan: Refactor MainActivity to Mapbox Compose SDK

Refactor the map implementation to use the official Mapbox Maps Compose SDK for a more declarative and idiomatic integration.

## Phase 1: Preparation and Environment Verification
- [ ] Task: Verify Mapbox Compose SDK dependency and availability.
- [ ] Task: Conduct a brief research on `MapboxMap` composable and `MapViewportState` usage in version 11.x.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Preparation' (Protocol in workflow.md)

## Phase 2: Refactor Camera and State Management
- [ ] Task: Update `MainViewModel` to expose camera state in a format compatible with `MapViewportState` if necessary.
- [ ] Task: Implement `rememberMapViewportState` in `MainActivity` and bind it to the initial camera position from ViewModel.
- [ ] Task: Implement persistence logic for camera state using `MapViewportState` callbacks or observation.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Camera State' (Protocol in workflow.md)

## Phase 3: Implement Declarative MapboxMap
- [ ] Task: Replace `AndroidView` with `MapboxMap` in `MapContent`.
- [ ] Task: Configure map settings (gestures, compass, logo, attribution) using the `MapSettings` and `MapboxMap` parameters.
- [ ] Task: Implement map event handlers (click and long click) using `onMapClickListener` and `onMapLongClickListener`.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: MapboxMap Integration' (Protocol in workflow.md)

## Phase 4: Refactor Raster Overlay to Declarative DSL
- [ ] Task: Implement the dynamic Mapbox Style using the `MapboxStyle` and `StyleContent` DSL.
- [ ] Task: Refactor `overlayRaster` logic to update state variables that drive the declarative `ImageSource` and `RasterLayer`.
- [ ] Task: Ensure Terrain and Globe projection are correctly applied via the DSL.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Raster Overlay' (Protocol in workflow.md)

## Phase 5: Verification and Finalization
- [ ] Task: Run automated tests to ensure no regressions in ViewModel or Logic.
- [ ] Task: Perform manual verification of all features (mode switching, point info, style toggle).
- [ ] Task: Conductor - User Manual Verification 'Phase 5: Final Verification' (Protocol in workflow.md)
