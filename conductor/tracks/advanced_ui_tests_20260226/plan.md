# Implementation Plan: Advanced UI Interaction Tests

## Phase 1: Test Infrastructure & FABs [checkpoint: 4efbf2a]
- [x] Task: Create `InteractionTests.kt` and set up the test environment with a mocked Map viewport and `FakeMainRepository`. 4d47744
- [x] Task: Implement `testLocationFabInteraction`: Verify clicking the location FAB triggers the viewport transition logic. 4d47744
- [x] Task: Implement `testPitchToggleInteraction`: Verify the 2D/3D toggle updates pitch state and button text. 4d47744
- [x] Task: Implement `testStyleToggleInteraction`: Verify the style FAB updates the map style in the ViewModel. 4d47744
- [x] Task: Conductor - User Manual Verification 'Phase 1: Test Infrastructure & FABs' (Protocol in workflow.md)

## Phase 2: Mode Selection & State Reflection [checkpoint: a691d36]
- [x] Task: Implement `testModeSwitching`: Verify that selecting different modes in the panel updates `visualizationMode`. 377b118
- [x] Task: Implement `testModeUIState`: Ensure the active mode is visually highlighted in the `ModeSelectionPanel`. 377b118
- [x] Task: Implement `testPanelVisibility`: Verify the panel opens and closes correctly upon interaction. 377b118
- [x] Task: Conductor - User Manual Verification 'Phase 2: Mode Selection & State Reflection' (Protocol in workflow.md)

## Phase 3: Custom Mode & End-to-End Logic
- [ ] Task: Implement `testCustomModeSliders`: Verify that dragging elevation/slope sliders updates `customModeParams`.
- [ ] Task: Implement `testCustomAspectSelection`: Verify that clicking WindRose sectors in the custom panel updates selected aspects.
- [ ] Task: Implement `testLogicTriggering`: Verify that changing parameters triggers avalanche rules recalculation and shows the loading state.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Custom Mode & End-to-End Logic' (Protocol in workflow.md)

## Phase 4: Final Validation & Integration
- [ ] Task: Run all unit and instrumented tests to ensure no regressions.
- [ ] Task: Update the `workflow.md` if any specific environment variables or setup steps are needed for advanced UI tests.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Final Validation & Integration' (Protocol in workflow.md)
