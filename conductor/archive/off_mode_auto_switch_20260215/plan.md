# Plan: Add "Off" Mode and Auto-Switching Visualization

Implement a new "Off" visualization mode and add logic to automatically switch between modes based on the map's zoom level.

## Phase 1: Logic and Data Updates
- [x] Task: Add `OFF` to `VisualizationMode` enum.
- [x] Task: Update `MainViewModel.calculateRules()` to handle the `OFF` mode (clear rules and stop jobs).
- [x] Task: Implement zoom-based mode switching logic in `MainViewModel`.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Core Logic' (Protocol in workflow.md)

## Phase 2: UI Integration
- [x] Task: Add "Off" icon to resources (if not already available, use `ic_close` or similar).
- [x] Task: Add `mode_off` string to `strings.xml`.
- [x] Task: Update `ModeSelectionPanel.kt` to include the "Off" option.
- [x] Task: Bind the "Off" mode to the FAB icon logic in `MainActivity`.
- [x] Task: Conductor - User Manual Verification 'Phase 2: UI Integration' (Protocol in workflow.md)

## Phase 3: Map Interaction
- [x] Task: Ensure the `MapContent` in `MainActivity` correctly handles the removal of the overlay when switching to `OFF`.
- [x] Task: Connect map zoom changes to the `MainViewModel` auto-switching logic.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Map Interaction' (Protocol in workflow.md)

## Phase 4: Final Verification
- [x] Task: Run unit tests for `MainViewModel` to verify auto-switching and "Off" mode logic.
- [x] Task: Perform manual verification of zoom-based transitions and "Off" mode functionality.
- [x] Task: Conductor - User Manual Verification 'Phase 4: Final Verification' (Protocol in workflow.md)

## Phase: Review Fixes
- [x] Task: Apply review suggestions (Updated zoom threshold to 10.0 in spec, logic, and tests)
