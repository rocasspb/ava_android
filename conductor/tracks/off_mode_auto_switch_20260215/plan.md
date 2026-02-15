# Plan: Add "Off" Mode and Auto-Switching Visualization

Implement a new "Off" visualization mode and add logic to automatically switch between modes based on the map's zoom level.

## Phase 1: Logic and Data Updates
- [ ] Task: Add `OFF` to `VisualizationMode` enum.
- [ ] Task: Update `MainViewModel.calculateRules()` to handle the `OFF` mode (clear rules and stop jobs).
- [ ] Task: Implement zoom-based mode switching logic in `MainViewModel`.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Core Logic' (Protocol in workflow.md)

## Phase 2: UI Integration
- [ ] Task: Add "Off" icon to resources (if not already available, use `ic_close` or similar).
- [ ] Task: Add `mode_off` string to `strings.xml`.
- [ ] Task: Update `ModeSelectionPanel.kt` to include the "Off" option.
- [ ] Task: Bind the "Off" mode to the FAB icon logic in `MainActivity`.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: UI Integration' (Protocol in workflow.md)

## Phase 3: Map Interaction
- [ ] Task: Ensure the `MapContent` in `MainActivity` correctly handles the removal of the overlay when switching to `OFF`.
- [ ] Task: Connect map zoom changes to the `MainViewModel` auto-switching logic.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Map Interaction' (Protocol in workflow.md)

## Phase 4: Final Verification
- [ ] Task: Run unit tests for `MainViewModel` to verify auto-switching and "Off" mode logic.
- [ ] Task: Perform manual verification of zoom-based transitions and "Off" mode functionality.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Final Verification' (Protocol in workflow.md)
