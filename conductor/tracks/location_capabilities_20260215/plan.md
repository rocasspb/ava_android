# Plan: Add Location Capabilities to the Map

Integrate real-time location tracking and display into the map view using the Mapbox Compose SDK.

## Phase 1: Android Permissions Setup
- [x] Task: Update `AndroidManifest.xml` to include `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` permissions. (Already present)
- [x] Task: Implement a standard `ActivityResultLauncher` in `MainActivity` to request location permissions upon launch. (Implemented in MainScreen)
- [x] Task: Conductor - User Manual Verification 'Phase 1: Permissions' (Protocol in workflow.md)

## Phase 2: Enable Location Puck in Mapbox
- [x] Task: Configure the `MapboxMap` composable to enable the `LocationComponent`.
- [x] Task: Use `LocationPuck2D` (or 3D) within the `MapboxMap` content to display the user's position.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Location Display' (Protocol in workflow.md)

## Phase 3: "My Location" Button and Camera Interaction
- [x] Task: Add a new `FloatingActionButton` to the `MainScreen` UI for the "My Location" action.
- [x] Task: Implement the "fly-to" logic using `mapViewportState.transitionToFollowPuckState()`.
- [x] Task: Ensure the button is only active/visible when permissions are granted.
- [x] Task: Conductor - User Manual Verification 'Phase 3: My Location Button' (Protocol in workflow.md)

## Phase 4: Final Verification
- [x] Task: Run automated tests to ensure no regressions in existing map or viewmodel logic.
- [x] Task: Perform manual verification of the end-to-end flow (launch -> permission -> display -> center).
- [x] Task: Conductor - User Manual Verification 'Phase 4: Final Verification' (Protocol in workflow.md)

## Phase: Review Fixes
- [x] Task: Apply review suggestions b381831
