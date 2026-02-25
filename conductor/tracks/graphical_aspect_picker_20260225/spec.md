# Specification: Graphical Aspect Picker (Wind Rose)

## Overview
This track aims to replace the text-based lists and selectors for terrain aspects (e.g., N, NE, E, etc.) with a graphical "wind rose" or circle-based UI component. This component will be used in both the `PointInfoCard` (to display affected aspects) and the custom mode aspect picker (to select aspects).

## Functional Requirements
- **Wind Rose UI:** Create a circular UI element divided into 8 sectors (N, NE, E, SE, S, SW, W, NW).
- **PointInfoCard Integration:** Replace the existing text labels for aspects in the `PointInfoCard` with this wind rose. Affected aspects should be highlighted/filled.
- **Custom Mode Aspect Picker:** Use the same wind rose component for selecting aspects. Users can toggle sectors by clicking or tapping them.
- **Visual Feedback:** Active sectors must be clearly distinguished (e.g., solid fill or highlight) and should include labels (e.g., "N", "NE") inside the sectors.

## Non-Functional Requirements
- **Consistency:** The visual design must align with Material Design 3 and the existing app's aesthetic (map-centric, immersive).
- **Responsive:** The component should scale appropriately for different screen sizes and orientations.

## Acceptance Criteria
- [ ] Wind rose component is implemented with 8 interactive sectors.
- [ ] `PointInfoCard` displays the wind rose showing affected aspects from avalanche data.
- [ ] Custom mode aspect picker allows users to toggle 8 directions individually.
- [ ] Visual labels (N, NE, etc.) are visible within the sectors.
- [ ] Toggling a sector in custom mode updates the map visualization accordingly.

## Out of Scope
- Supporting more than 8 sectors (e.g., 16-sector rose).
- Range-based dragging for selection.
