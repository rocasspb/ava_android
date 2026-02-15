# Implementation Plan: Detailed Avalanche Data in PointInfoCard

## Phase 1: Data Extraction & ViewModel Logic
- [x] Task: Update `MainViewModel` to expose selected point avalanche data [a5fae79]
    - [x] Add state for the currently selected map point and its associated avalanche info.
    - [x] Implement logic to filter `AvalancheData` based on the coordinates' elevation and aspect.
- [ ] Task: Conductor - User Manual Verification 'Data Extraction & ViewModel Logic' (Protocol in workflow.md)

## Phase 2: UI Implementation
- [ ] Task: Create/Update `PointInfoCard` Compose component
    - [ ] Design the layout to show danger level and problem icons/text. Use Icons from https://www.avalanches.org/wp-content/uploads/2022/09/Icons-Avalanche-Danger-Level-Dry-Snow-EAWS.zip
    - [ ] Ensure high contrast and minimalist design.
- [ ] Task: Integrate `PointInfoCard` into `MainActivity` (Compose)
    - [ ] Show the card as an overlay when a point is selected.
    - [ ] Add a close button or tap-to-dismiss functionality.
- [ ] Task: Conductor - User Manual Verification 'UI Implementation' (Protocol in workflow.md)

## Phase 3: Verification
- [ ] Task: Add unit tests for avalanche data filtering logic in `MainViewModel`.
- [ ] Task: Verify UI behavior with various avalanche bulletin scenarios.
- [ ] Task: Conductor - User Manual Verification 'Verification' (Protocol in workflow.md)
