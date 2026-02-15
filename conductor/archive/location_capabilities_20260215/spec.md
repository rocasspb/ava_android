# Spec: Add Location Capabilities to the Map

Integrate real-time location tracking and display into the map view using the Mapbox Compose SDK.

## Overview
This track will enable users to see their current position on the map and easily center the view on themselves. This is a critical feature for backcountry navigation and situational awareness.

## Functional Requirements
- **Location Display:** Enable the Mapbox "Location Puck" (2D or 3D depending on default SDK settings) to show the user's current coordinates and bearing.
- **Permission Management:** Implement a standard Android permission request flow for `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` upon app launch.
- **"My Location" Button:** 
    - Add a new Floating Action Button (FAB) using Material 3 design.
    - When clicked, the map camera should smoothly animate (fly-to) to the user's current location at an appropriate zoom level.
- **State Integration:** Bind the location state to the existing `MapViewportState` used in `MainActivity`.

## Non-Functional Requirements
- **UX:** Camera transitions should be smooth and non-disruptive.
- **Consistency:** The "My Location" button should follow the existing UI style (colors, padding, elevations) of the other map controls.

## Acceptance Criteria
- [ ] App prompts for location permissions on first launch.
- [ ] User's location is accurately represented by a blue dot/puck on the map.
- [ ] Clicking the "My Location" FAB centers the map on the user.
- [ ] The feature handles cases where GPS is disabled or permission is denied gracefully.

## Out of Scope
- Background location tracking (tracking while the app is closed/backgrounded).
- Turn-by-turn navigation or route recording.
