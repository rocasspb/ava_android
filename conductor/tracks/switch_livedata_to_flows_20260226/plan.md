# Implementation Plan: Switch LiveData to Flows

Migrate the codebase from using Android LiveData to Kotlin Flows for reactive state management.

## Phase 1: Setup and Dependencies
- [ ] Add `lifecycle-runtime-compose` dependency to `libs.versions.toml` and `app/build.gradle.kts`
- [ ] Verify build is still successful
- [ ] Ensure existing tests pass

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

## Phase 3: UI Migration (Compose)
- [ ] Update `MainScreen.kt` to use `collectAsStateWithLifecycle()` instead of `observeAsState()`
- [ ] Update `ModeSelectionPanel.kt` to use `collectAsStateWithLifecycle()`
- [ ] Update any other composables observing ViewModel state
- [ ] Verify UI behavior is unchanged

## Phase 4: Cleanup and Verification
- [ ] Remove `androidx.compose.runtime.livedata` and `androidx.lifecycle.livedata.ktx` if no longer needed
- [ ] Final build and test run
- [ ] Check for any remaining LiveData usage in the project
