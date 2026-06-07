# Specification: RasterGenerator Optimization (Color Buffer Approach)

## Overview
Optimize the `RasterGenerator` in `commonMain` to eliminate heavy `GeometryUtils.isPointInGeometry` calls during rasterization. The new approach will use an offscreen color buffer where geometries are drawn using a pure Kotlin 2D graphics library. Intersecting rules will be managed using a bitmasking strategy within the color buffer.

## Functional Requirements
1. **Offscreen Rendering:** Replace geometric point-in-polygon checks with an offscreen 2D drawing mechanism.
2. **Pure Kotlin Library:** Utilize a pure Kotlin 2D graphics library compatible with KMP (Android and Wasm/JS) to perform the rendering.
3. **Bitmasking Strategy:** 
   - Assign each `GenerationRule` a unique bit (power of 2).
   - Draw geometries such that intersections accumulate the bits (bitwise OR).
   - After rendering, read the pixel's integer value to determine all intersecting rules.
4. **Resolution:** Render the offscreen buffer at a 1:1 resolution matching the target grid points density.
5. **Rule Limit Handling:** Implement handling for cases where there are more than 32 rules (e.g., grouping into multiple passes).

## Non-Functional Requirements
- **Performance:** Significant reduction in `RasterGenerator.generateRaster` execution time.
- **Cross-Platform Compatibility:** The solution must work on both Android and WasmJS targets.

## Acceptance Criteria
- `RasterGenerator` no longer uses `isPointInGeometry` per cell/pixel.
- Visual output matches the previous geometric approach.
- Intersecting regions correctly identify all applicable rules via the bitmask.

## Out of Scope
- Changing the steepness and elevation logic applied after identifying the rule.