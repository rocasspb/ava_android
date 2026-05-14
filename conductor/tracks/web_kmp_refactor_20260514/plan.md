# Implementation Plan - Refactor Web App to use KMP Logic

## Phase 1: Wasm Artifact Integration [checkpoint: 33fc042]
- [x] Task: Create a dedicated directory for KMP artifacts in the web project (e.g., `web/src/kmp/`). 9195417
- [x] Task: Copy the generated Wasm/JS files from `shared/build/dist/wasmJs/productionLibrary/` to the new directory. 9195417
- [x] Task: Implement a Wasm loader utility in `web/src/services/WasmLoader.ts` using dynamic `import()`. 19603b2
- [x] Task: Verify the Wasm module can be successfully initialized and its exports are accessible. 19603b2
- [x] Task: Conductor - User Manual Verification 'Wasm Artifact Integration' (Protocol in workflow.md) 33fc042

## Phase 2: Geometry and Terrain Utils Refactor
- [ ] Task: Refactor `web/src/utils/geometry.ts` to delegate `isPointInPolygon` and `isPointInMultiPolygon` to KMP `GeometryUtils`.
- [ ] Task: Refactor `web/src/utils/geo-utils.ts` to delegate `calculateTerrainMetrics` to KMP `TerrainUtils`.
- [ ] Task: Ensure the bridge correctly handles coordinate formats (e.g., `[lng, lat]` vs `Point(x, y)`).
- [ ] Task: Verify that point-in-polygon and slope/aspect calculations match previous results via manual spot checks.
- [ ] Task: Conductor - User Manual Verification 'Geometry and Terrain Utils Refactor' (Protocol in workflow.md)

## Phase 3: Core Logic and Renderer Refactor
- [ ] Task: Adapt `web/src/utils/data-processing.ts` to transform CAAML data into KMP `GenerationRule` and `ElevationBand` objects.
- [ ] Task: Implement a bridge for `ElevationQuery` that allows KMP logic to call the existing `TerrainProvider.getElevation` in the web app.
- [ ] Task: Refactor `web/src/components/CanvasRenderer.ts` to replace the nested loops with a call to KMP `RasterGenerator.generateRaster`.
- [ ] Task: Verify that the generated raster correctly displays avalanche danger and custom mode overlays on the map.
- [ ] Task: Conductor - User Manual Verification 'Core Logic and Renderer Refactor' (Protocol in workflow.md)

## Phase 4: Cleanup and Finalization
- [ ] Task: Remove redundant TypeScript implementations from `web/src/utils/` if no longer used.
- [ ] Task: Perform a final pass on code style, ensuring all public Wasm-bridging functions are well-documented.
- [ ] Task: Conduct a full end-to-end verification of all application features (Bulletin, Risk, Custom, Point Info).
- [ ] Task: Conductor - User Manual Verification 'Cleanup and Finalization' (Protocol in workflow.md)
