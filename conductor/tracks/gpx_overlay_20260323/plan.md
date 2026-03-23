# Implementation Plan: GPX Route Overlay

## Phase 1: Foundation & Data Layer [checkpoint: f68e7a4]
- [x] Task: Define GPX Data Models (`GpxTrack`, `GpxPoint`, `GpxMetadata`) (8fd83d4)
- [x] Task: Implement `GpxParser` using `XmlPullParser` to extract coordinates, name, distance, and elevation (1ffd864)
- [x] Task: Create `GpxRepository` to handle saving/loading GPX files in internal storage (7da2bd6)
- [ ] Task: Conductor - User Manual Verification 'Foundation & Data Layer' (Protocol in workflow.md)

## Phase 2: Logic & ViewModel Integration
- [ ] Task: Update `MainViewModel` to manage the list of loaded GPX tracks and selection state
- [ ] Task: Implement `importGpx` and `deleteGpx` logic in `MainViewModel`
- [ ] Task: Add Intent Filter to `AndroidManifest.xml` and handle external GPX files in `MainActivity`
- [ ] Task: Conductor - User Manual Verification 'Logic & ViewModel Integration' (Protocol in workflow.md)

## Phase 3: Map Visualization
- [ ] Task: Create a `GpxOverlay` component to render routes on the Mapbox map
- [ ] Task: Integrate `GpxOverlay` into the `MainScreen` map view
- [ ] Task: Implement hit-testing or click handlers for selecting GPX routes on the map
- [ ] Task: Conductor - User Manual Verification 'Map Visualization' (Protocol in workflow.md)

## Phase 4: UI Components & Final Integration
- [ ] Task: Add the GPX Floating Action Button (FAB) to the main screen
- [ ] Task: Implement the GPX detail card (showing distance and elevation) and delete functionality
- [ ] Task: Ensure imported routes are automatically reloaded on app start
- [ ] Task: Conductor - User Manual Verification 'UI Components & Final Integration' (Protocol in workflow.md)
