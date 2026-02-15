# Plan: Add Location Capabilities to the Map

Integrate real-time location tracking and display into the map view using the Mapbox Compose SDK.

## Phase 1: Android Permissions Setup
- [ ] Task: Update `AndroidManifest.xml` to include `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` permissions.
- [ ] Task: Implement a standard `ActivityResultLauncher` in `MainActivity` to request location permissions upon launch.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Permissions' (Protocol in workflow.md)

## Phase 2: Enable Location Puck in Mapbox
- [ ] Task: Configure the `MapboxMap` composable to enable the `LocationComponent`.
- [ ] Task: Use `LocationPuck2D` (or 3D) within the `MapboxMap` content to display the user's position.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Location Display' (Protocol in workflow.md)

## Phase 3: "My Location" Button and Camera Interaction
- [ ] Task: Add a new `FloatingActionButton` to the `MainScreen` UI for the "My Location" action.
- [ ] Task: Implement the "fly-to" logic using `mapViewportState.transitionToFollowPuck()` or `mapViewportState.flyTo()`.
- [ ] Task: Ensure the button is only active/visible when permissions are granted.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: My Location Button' (Protocol in workflow.md)

## Phase 4: Final Verification
- [ ] Task: Run automated tests to ensure no regressions in existing map or viewmodel logic.
- [ ] Task: Perform manual verification of the end-to-end flow (launch -> permission -> display -> center).
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Final Verification' (Protocol in workflow.md)
