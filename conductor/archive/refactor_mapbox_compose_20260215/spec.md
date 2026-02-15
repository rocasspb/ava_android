# Spec: Refactor MainActivity to Mapbox Compose SDK

Refactor the `MainActivity` and `MapContent` implementation to use the official Mapbox Maps Compose SDK, replacing the manual `AndroidView` wrapper with the idiomatic `MapboxMap` composable.

## Overview
The current implementation of Mapbox in `MainActivity` uses a legacy `AndroidView` approach. This track will modernize the map integration by adopting the Mapbox Compose SDK, which provides a more declarative and lifecycle-aware way to manage the map within a Jetpack Compose environment.

## Functional Requirements
- Replace `AndroidView` with the `MapboxMap` composable from `com.mapbox.maps.extension.compose`.
- Use `rememberMapViewportState` to manage and synchronize the map's camera position with the `MainViewModel`.
- Implement map styling (Terrain, Projection, etc.) using the Mapbox Compose DSL.
- Refactor the avalanche raster overlay logic to use declarative `ImageSource` and `RasterLayer` within the `MapboxMap` content block.
- Ensure map events (clicks, long clicks) are handled via the `MapboxMap` composable's parameters or state.

## Non-Functional Requirements
- Maintain existing performance for raster generation and overlay updates.
- Adhere to the design principles of the project (Map-centric, Material 3).
- Improve code readability and maintainability by removing imperative style management where possible.

## Acceptance Criteria
- [ ] The app launches and displays the Mapbox map correctly.
- [ ] The camera position is restored from saved preferences on startup.
- [ ] Switching between "Bulletin", "Risk", and "Custom" modes correctly updates the avalanche overlay.
- [ ] Long-pressing on the map still displays the `PointInfoCard` with correct data.
- [ ] The map style (Outdoors/Satellite) can still be toggled.
- [ ] All existing automated tests pass, and new tests are added for the refactored components if necessary.

## Out of Scope
- Adding new avalanche visualization logic or changing the `RasterGenerator`.
- Modifying the `MainViewModel` business logic unless required for Compose integration.
