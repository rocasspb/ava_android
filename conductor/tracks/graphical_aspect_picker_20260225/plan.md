# Implementation Plan: Graphical Aspect Picker (Wind Rose)

This plan outlines the steps to replace the text-based aspect representation with a graphical wind rose component in `PointInfoCard` and the custom mode aspect picker.

## Phase 1: Research & Component Design (UI/Logic)
- [x] Task: Research existing aspect representation and UI components in the codebase.
- [x] Task: Define the data model for the 8-sector wind rose (mapping directions to sectors).
- [x] Task: Design the UI component in Jetpack Compose (using `Canvas` for drawing the rose).
- [x] Task: Conductor - User Manual Verification 'Phase 1: Research & Component Design' (Protocol in workflow.md)

## Phase 2: Core Component Implementation (Wind Rose)
- [x] Task: Create a reusable `WindRose` Composable that can display and/or select 8 directions.
    - [x] Write unit tests for the `WindRose` state management (toggling sectors, mapping data).
    - [x] Implement the `WindRose` Composable using `Canvas`.
    - [x] Add support for "active" vs "inactive" sector styling.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Core Component Implementation' (Protocol in workflow.md)

## Phase 3: Custom Mode Aspect Picker Integration
- [x] Task: Replace the existing aspect picker in Custom Mode with the new `WindRose` component.
    - [x] Update the aspect selection logic to use the `WindRose` state.
    - [x] Write tests for the updated aspect picker in custom mode.
    - [x] Verify that selecting/deselecting sectors updates the map's filter logic.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Custom Mode Aspect Picker Integration' (Protocol in workflow.md)

## Phase 4: PointInfoCard Integration
- [x] Task: Update `PointInfoCard` to display the `WindRose` instead of text labels for aspects.
    - [x] Write tests for `PointInfoCard` ensuring it correctly maps avalanche data to the `WindRose`.
    - [x] Implement the `WindRose` in `PointInfoCard` (read-only mode).
    - [x] Remove the old text-based aspect labels from `PointInfoCard`.
- [x] Task: Conductor - User Manual Verification 'Phase 4: PointInfoCard Integration' (Protocol in workflow.md)

## Phase 5: Refinement & Cleanup
- [x] Task: Polish the UI (colors, spacing, animations).
    - [x] Adjust colors to match Material 3 theme more closely.
    - [x] Add simple animations for sector selection.
- [x] Task: Final code review and documentation update.
- [x] Task: Conductor - User Manual Verification 'Phase 5: Refinement & Cleanup' (Protocol in workflow.md)
