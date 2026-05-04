# Implementation Plan: Switch LiveData to Flows

Migrate the codebase from using Android LiveData to Kotlin Flows for reactive state management.

## Phase 1: Setup and Dependencies [checkpoint: 37bfc87]
- [x] Add `lifecycle-runtime-compose` dependency to `libs.versions.toml` and `app/build.gradle.kts` 291a6c8
- [x] Verify build is still successful 291a6c8
- [x] Ensure existing tests pass 291a6c8
- [x] Task: Conductor - User Manual Verification 'Phase 1: Setup and Dependencies' (Protocol in workflow.md) 37bfc87

## Phase 2: MainViewModel Migration
- [ ] Migrate `mapStyleUrl` to `StateFlow`
- [ ] Migrate `initialCameraPosition` to `StateFlow`
- [ ] Migrate `cameraPosition` to `StateFlow`
- [ ] Migrate `regions` to `StateFlow`
- [ ] Migrate `avalancheData` to `StateFlow`
- [ ] Migrate `error` to `SharedFlow` (or `StateFlow` with null)
- [ ] Migrate `generationRules` to `StateFlow`
- [ ] Migrate `visualizationMode` to `StateFlow`
- [ ] Migrate `customModeParams` to `StateFlow`
- [ ] Migrate `pointInfo` to `StateFlow`
- [ ] Migrate `locationPermissionGranted` to `StateFlow`
- [ ] Migrate `gpxTracks` to `StateFlow`
- [ ] Migrate `selectedGpxTrack` to `StateFlow`
- [ ] Migrate `showDisclaimer` to `StateFlow`
- [ ] Update all methods to use `.value` or `.emit()` instead of LiveData methods
- [ ] Update unit tests in `MainViewModelTest.kt` to handle Flows
- [ ] Task: Conductor - User Manual Verification 'Phase 2: MainViewModel Migration' (Protocol in workflow.md)

## Phase 3: UI Migration (Compose)
- [ ] Update `MainScreen.kt` to use `collectAsStateWithLifecycle()` instead of `observeAsState()`
- [ ] Update `ModeSelectionPanel.kt` to use `collectAsStateWithLifecycle()`
- [ ] Update any other composables observing ViewModel state
- [ ] Verify UI behavior is unchanged
- [ ] Task: Conductor - User Manual Verification 'Phase 3: UI Migration (Compose)' (Protocol in workflow.md)

## Phase 4: Cleanup and Verification
- [ ] Remove `androidx.compose.runtime.livedata` and `androidx.lifecycle.livedata.ktx` if no longer needed
- [ ] Final build and test run
- [ ] Check for any remaining LiveData usage in the project
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Cleanup and Verification' (Protocol in workflow.md)
