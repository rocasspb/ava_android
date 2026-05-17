# Implementation Plan: RasterGenerator Optimization

## Phase 1: Setup Pure Kotlin Graphics Dependency [checkpoint: 701c0b5]
- [x] Task: Research and add pure Kotlin 2D graphics library 4d7a5f8
    - [ ] Identify a lightweight, KMP-compatible 2D graphics library (e.g., KorIM or Skia via Compose Multiplatform `org.jetbrains.compose.ui:ui-graphics`).
    - [ ] Update `shared/build.gradle.kts` to include the dependency in `commonMain`.
    - [ ] Sync Gradle and verify project builds successfully across Android and WasmJS.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Setup Pure Kotlin Graphics Dependency' (Protocol in workflow.md) 701c0b5

## Phase 2: Create Offscreen Rendering Abstraction [checkpoint: 8736ee3]
- [x] Task: Define Rendering Interface daf96f5
    - [ ] Create an interface (e.g., `OffscreenRenderer`) in `RasterGenerator.kt` or a new file to abstract drawing operations (create buffer, draw polygon with color, get pixels).
- [x] Task: Implement KMP Renderer 953693b
    - [ ] Create an implementation of `OffscreenRenderer` using the chosen pure Kotlin graphics library.
    - [ ] Write unit tests for `OffscreenRenderer` to ensure polygons are drawn correctly and colors (bitmasks) accumulate properly via bitwise OR blending.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Create Offscreen Rendering Abstraction' (Protocol in workflow.md) 8736ee3

## Phase 3: Integrate Offscreen Rendering into RasterGenerator [checkpoint: f746dc8]
- [x] Task: Refactor RasterGenerator (TDD Red Phase) 20eff31
    - [ ] Update/add unit tests for `RasterGenerator` to assert that overlapping rules produce the expected output.
- [x] Task: Refactor RasterGenerator (TDD Green Phase) fff9814
    - [ ] Modify `RasterGenerator.generateRaster` to instantiate the `OffscreenRenderer`.
    - [ ] Assign unique bits to each rule up to 32 rules.
    - [ ] Draw all `filteredRules` geometries into the offscreen buffer using their assigned bitmask colors.
    - [ ] Iterate through the resulting pixel buffer. For each pixel, read the integer color.
    - [ ] Determine intersecting rules from the integer bitmask.
    - [ ] Apply elevation, slope, and aspect logic (existing logic) to determine the final pixel color.
- [x] Task: Fallback Implementation 05b328c
    - [ ] Implement chunking or multiple passes if `filteredRules.size > 32`.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Integrate Offscreen Rendering into RasterGenerator' (Protocol in workflow.md) f746dc8

## Phase 4: Final Testing and Cleanup
- [ ] Task: Run full test suite
    - [ ] Ensure all unit and UI tests pass.
- [ ] Task: Performance benchmarking
    - [ ] Verify logs indicate a significant reduction in `generateRaster` execution time.
- [ ] Task: Clean up dead code
    - [ ] Remove any obsolete geometric iteration logic from `RasterGenerator`.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Final Testing and Cleanup' (Protocol in workflow.md)