# Plan: 2D/3D Map View Toggle

Add a new Floating Action Button (FAB) to toggle the map camera pitch between a flat 2D view and a tilted 3D view.

## Phase 1: Logic and State Verification
- [ ] Task: Verify the current `MapViewportState` access in `MainActivity.kt`.
- [ ] Task: Conduct a brief research on the most efficient way to animate camera pitch using `MapViewportState.easeTo`.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Logic' (Protocol in workflow.md)

## Phase 2: UI Implementation
- [ ] Task: Implement the new `FloatingActionButton` in the `MainScreen` FAB stack.
- [ ] Task: Position the button just above the "Switch Map Style" button.
- [ ] Task: Bind the button label ("2D" vs "3D") to the `mapViewportState.cameraState.pitch`.
- [ ] Task: Implement the `onClick` logic to animate pitch to 45 or 0.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: UI Implementation' (Protocol in workflow.md)

## Phase 3: Verification and Finalization
- [ ] Task: Perform manual verification of the toggle functionality and animation smoothness.
- [ ] Task: Ensure the label updates correctly when the user tilts the map manually using gestures.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Final Verification' (Protocol in workflow.md)
