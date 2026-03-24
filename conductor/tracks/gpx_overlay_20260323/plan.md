# Implementation Plan: GPX Route Overlay

## Phase 1: Foundation & Data Layer [checkpoint: f68e7a4]
- [x] Task: Define GPX Data Models (`GpxTrack`, `GpxPoint`, `GpxMetadata`) (8fd83d4)
- [x] Task: Implement `GpxParser` using `XmlPullParser` to extract coordinates, name, distance, and elevation (1ffd864)
- [x] Task: Create `GpxRepository` to handle saving/loading GPX files in internal storage (7da2bd6)
- [x] Task: Conductor - User Manual Verification 'Foundation & Data Layer' (f68e7a4)

## Phase 2: Logic & ViewModel Integration [checkpoint: e6666c9]
- [x] Task: Update `MainViewModel` to manage the list of loaded GPX tracks and selection state (ef9070a)
- [x] Task: Implement `importGpx` and `deleteGpx` logic in `MainViewModel` (fa251e4)
- [x] Task: Add Intent Filter to `AndroidManifest.xml` and handle external GPX files in `MainActivity` (27b98a1)
- [x] Task: Conductor - User Manual Verification 'Logic & ViewModel Integration' (e6666c9)

## Phase 3: Map Visualization [checkpoint: 64304ed]
- [x] Task: Create a `GpxOverlay` component to render routes on the Mapbox map (4a19481)
- [x] Task: Integrate `GpxOverlay` into the `MainScreen` map view (4a19481)
- [x] Task: Implement hit-testing or click handlers for selecting GPX routes on the map (4a19481)
- [x] Task: Conductor - User Manual Verification 'Map Visualization' (64304ed)

## Phase 4: UI Components & Final Integration
- [x] Task: Add the GPX Floating Action Button (FAB) to the main screen (0cfdf75)
- [x] Task: Implement the GPX detail card (showing distance and elevation) and delete functionality (149bf6c)
- [ ] Task: Ensure imported routes are automatically reloaded on app start
- [ ] Task: Conductor - User Manual Verification 'UI Components & Final Integration' (Protocol in workflow.md)
