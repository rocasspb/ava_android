# Implementation Plan: Graphical Aspect Picker (Wind Rose)

This plan outlines the steps to replace the text-based aspect representation with a graphical wind rose component in `PointInfoCard` and the custom mode aspect picker.

## Phase 1: Research & Component Design (UI/Logic)
- [ ] Task: Research existing aspect representation and UI components in the codebase.
- [ ] Task: Define the data model for the 8-sector wind rose (mapping directions to sectors).
- [ ] Task: Design the UI component in Jetpack Compose (using `Canvas` for drawing the rose).
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Research & Component Design' (Protocol in workflow.md)

## Phase 2: Core Component Implementation (Wind Rose)
- [ ] Task: Create a reusable `WindRose` Composable that can display and/or select 8 directions.
    - [ ] Write unit tests for the `WindRose` state management (toggling sectors, mapping data).
    - [ ] Implement the `WindRose` Composable using `Canvas`.
    - [ ] Add support for "active" vs "inactive" sector styling.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Core Component Implementation' (Protocol in workflow.md)

## Phase 3: Custom Mode Aspect Picker Integration
- [ ] Task: Replace the existing aspect picker in Custom Mode with the new `WindRose` component.
    - [ ] Update the aspect selection logic to use the `WindRose` state.
    - [ ] Write tests for the updated aspect picker in custom mode.
    - [ ] Verify that selecting/deselecting sectors updates the map's filter logic.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Custom Mode Aspect Picker Integration' (Protocol in workflow.md)

## Phase 4: PointInfoCard Integration
- [ ] Task: Update `PointInfoCard` to display the `WindRose` instead of text labels for aspects.
    - [ ] Write tests for `PointInfoCard` ensuring it correctly maps avalanche data to the `WindRose`.
    - [ ] Implement the `WindRose` in `PointInfoCard` (read-only mode).
    - [ ] Remove the old text-based aspect labels from `PointInfoCard`.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: PointInfoCard Integration' (Protocol in workflow.md)

## Phase 5: Refinement & Cleanup
- [ ] Task: Polish the UI (colors, spacing, animations).
- [ ] Task: Final code review and documentation update.
- [ ] Task: Conductor - User Manual Verification 'Phase 5: Refinement & Cleanup' (Protocol in workflow.md)
