# Spec: 2D/3D Map View Toggle

Add a new Floating Action Button (FAB) to toggle the map camera pitch between a flat 2D view and a tilted 3D view.

## Overview
This feature enhances terrain visualization by allowing users to quickly switch between a top-down navigation view (2D) and a perspective view (3D) that highlights the 3D terrain and avalanche risk layers.

## Functional Requirements
- **Toggle Button:**
    - Add a new `SmallFloatingActionButton` (or standard FAB) to the `MainScreen` UI.
    - Position: **Just above the "Switch Map Style" FAB** in the bottom-right corner stack.
    - Label: Displays "3D" when the map is currently flat (pitch = 0) and "2D" when the map is tilted (pitch > 0).
- **Pitch Management:**
    - On click (when label is "3D"): Smoothly animate the map camera to a pitch of 45 degrees.
    - On click (when label is "2D"): Smoothly animate the map camera back to a pitch of 0 degrees.
- **State Integration:**
    - Use the current `MapViewportState.cameraState.pitch` to determine the button's label and action.
    - Ensure the animation uses `MapViewportState.easeTo` or `flyTo` to preserve smooth UX.

## Non-Functional Requirements
- **Responsiveness:** The button label should update immediately as the user manually tilts the map.
- **Consistency:** Use Material Design 3 components and match the styling (colors, elevation) of the existing map buttons.

## Acceptance Criteria
- [ ] A new button appears in the map UI stack, located above the Map Style button.
- [ ] Button says "3D" when the map is at 0 pitch.
- [ ] Clicking "3D" tilts the map to 45 degrees with a smooth animation.
- [ ] Button says "2D" when the map is tilted (even if tilted manually).
- [ ] Clicking "2D" resets the map to 0 pitch with a smooth animation.

## Out of Scope
- Configurable tilt angles (permanently fixed to 45 degrees).
- Automatically tilting based on zoom level.
