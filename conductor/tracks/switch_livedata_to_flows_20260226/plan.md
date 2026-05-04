# Implementation Plan: Switch LiveData to Flows

Migrate the codebase from using Android LiveData to Kotlin Flows for reactive state management.

## Phase 1: Setup and Dependencies [checkpoint: 37bfc87]
- [x] Add `lifecycle-runtime-compose` dependency to `libs.versions.toml` and `app/build.gradle.kts` 291a6c8
- [x] Verify build is still successful 291a6c8
- [x] Ensure existing tests pass 291a6c8
- [x] Task: Conductor - User Manual Verification 'Phase 1: Setup and Dependencies' (Protocol in workflow.md) 37bfc87

## Phase 2: MainViewModel Migration [checkpoint: 49444ad]
- [x] Migrate `mapStyleUrl` to `StateFlow` 802d1f4
- [x] Migrate `initialCameraPosition` to `StateFlow` 802d1f4
- [x] Migrate `cameraPosition` to `StateFlow` 802d1f4
- [x] Migrate `regions` to `StateFlow` 802d1f4
- [x] Migrate `avalancheData` to `StateFlow` 802d1f4
- [x] Migrate `error` to `SharedFlow` (or `StateFlow` with null) 802d1f4
- [x] Migrate `generationRules` to `StateFlow` 802d1f4
- [x] Migrate `visualizationMode` to `StateFlow` 802d1f4
- [x] Migrate `customModeParams` to `StateFlow` 802d1f4
- [x] Migrate `pointInfo` to `StateFlow` 802d1f4
- [x] Migrate `locationPermissionGranted` to `StateFlow` 802d1f4
- [x] Migrate `gpxTracks` to `StateFlow` 802d1f4
- [x] Migrate `selectedGpxTrack` to `StateFlow` 802d1f4
- [x] Migrate `showDisclaimer` to `StateFlow` 802d1f4
- [x] Update all methods to use `.value` or `.emit()` instead of LiveData methods 802d1f4
- [x] Update unit tests in `MainViewModelTest.kt` to handle Flows 802d1f4
- [x] Task: Conductor - User Manual Verification 'Phase 2: MainViewModel Migration' (Protocol in workflow.md) 49444ad

## Phase 3: UI Migration (Compose) [checkpoint: 49444ad]
- [x] Update `MainScreen.kt` to use `collectAsStateWithLifecycle()` instead of `observeAsState()` 802d1f4
- [x] Update `ModeSelectionPanel.kt` to use `collectAsStateWithLifecycle()` 802d1f4
- [x] Update any other composables observing ViewModel state 802d1f4
- [x] Verify UI behavior is unchanged 802d1f4
- [x] Task: Conductor - User Manual Verification 'Phase 3: UI Migration (Compose)' (Protocol in workflow.md) 49444ad

## Phase 4: Cleanup and Verification [checkpoint: 3d69b50]
- [x] Remove `androidx.compose.runtime.livedata` and `androidx.lifecycle.livedata.ktx` if no longer needed ee47025
- [x] Final build and test run ee47025
- [x] Check for any remaining LiveData usage in the project ee47025
- [x] Task: Conductor - User Manual Verification 'Phase 4: Cleanup and Verification' (Protocol in workflow.md) 3d69b50
