# Specification: GPX Route Overlay

## Overview
This feature allows users to import, visualize, and manage GPX tracks on the map. It enhances the planning experience by allowing users to see their intended routes alongside real-time avalanche danger and terrain risk data.

## Functional Requirements
- **Import GPX:**
    - Provide a Floating Action Button (FAB) at the bottom-right (above existing map controls) to trigger the system file picker.
    - Support opening GPX files via Android Intent filters (e.g., from a file manager or email).
- **Visualization:**
    - Render GPX tracks as polyline overlays on the Mapbox map.
    - Use a uniform, distinct color (e.g., primary accent color) and standard width for all routes.
    - Support displaying multiple GPX routes simultaneously.
- **Management:**
    - On clicking a GPX route, display a detail card showing:
        - Route name (from GPX metadata).
        - Total distance.
        - Elevation gain/loss.
    - Provide a way to delete an imported route directly from the detail view.
- **Persistence:**
    - Store imported GPX files in the app's internal storage.
    - Load and display saved routes automatically when the app starts.

## Non-Functional Requirements
- **Performance:** Rendering multiple GPX routes should not noticeably degrade map performance or responsiveness.
- **Storage:** Efficiently manage storage by only keeping the necessary GPX data.

## Acceptance Criteria
- [ ] Users can successfully select and import a GPX file via the FAB.
- [ ] Users can open a GPX file with the app from a file manager.
- [ ] Imported GPX routes are visible on the map.
- [ ] Clicking a route displays its metadata (name, distance, elevation).
- [ ] Users can delete a route, and it is removed from both the map and storage.
- [ ] Imported routes persist after app restarts.

## Out of Scope
- Editing GPX track points.
- Real-time navigation along the GPX route.
- Cloud synchronization of GPX files.
