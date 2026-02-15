# Spec: Add "Off" Mode and Auto-Switching Visualization

Introduce a new "Off" mode to the visualization selection and implement automatic switching between modes based on map zoom levels to optimize data presentation.

## Overview
This track adds an "Off" mode for a clean map view and introduces intelligent mode switching. When zooming in, the app transitions from broad regional data (Bulletin) to granular terrain risk (Risk). When zooming out, it reverts to regional data to maintain clarity at scale.

## Functional Requirements
- **Enum Update:** Add `OFF` to the `VisualizationMode` enum.
- **UI Integration:**
    - Add an "Off" option to the `ModeSelectionPanel`.
    - Positioning: Last option in the horizontal row.
    - Icon: Use a "visibility off" icon.
    - Label: "Off" (localized).
- **Map Behavior:**
    - When `OFF` mode is active, remove the avalanche raster overlay from the Mapbox style.
- **Logic Optimization:**
    - Disable raster generation and terrain elevation fetching when in `OFF` mode.
- **Auto-Switching Logic:**
    - **Zoom In:** If current mode is `BULLETIN` and zoom level reaches **14.0 or higher**, automatically switch to `RISK` mode.
    - **Zoom Out:** If current mode is `RISK` or `CUSTOM` and zoom level drops **below 14.0**, automatically switch back to `BULLETIN` mode.
- **User Manual Override:** User manual mode changes do not disable auto-switching; it triggers on every threshold crossing.

## Non-Functional Requirements
- **Performance:** Ensure mode transitions are smooth and do not cause UI stutters.
- **Responsiveness:** The "Off" mode must immediately halt background processing.

## Acceptance Criteria
- [ ] "Off" mode is selectable and removes the overlay.
- [ ] Zooming in to 14.0+ switches `BULLETIN` -> `RISK`.
- [ ] Zooming out below 14.0 switches `RISK`/`CUSTOM` -> `BULLETIN`.
- [ ] No background raster generation occurs in "Off" mode.
- [ ] The default mode remains "Bulletin".

## Out of Scope
- Configurable zoom thresholds for auto-switching.
