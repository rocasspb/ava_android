# Specification: Switch LiveData to Flows

## Overview
The goal of this track is to replace all usages of `LiveData` and `MutableLiveData` in the `MainViewModel` with Kotlin `Flows`, specifically `StateFlow` for state and `SharedFlow` for events.

## Rationale
- **Kotlin-First:** Flows are part of the Kotlin Coroutines library, making them more idiomatic for Kotlin-based projects.
- **Improved Testing:** Flows are easier to test in isolation without needing `InstantTaskExecutorRule`.
- **Better Coroutine Integration:** Flows provide seamless integration with other coroutine operators and scopes.
- **Modern Standards:** Kotlin Flows are the current standard for reactive programming in Android, replacing LiveData in many modern architectures.

## Technical Details

### ViewModel Changes
- Replace `MutableLiveData<T>` with `MutableStateFlow<T>`.
- Replace `LiveData<T>` with `StateFlow<T>`.
- For events (like errors), consider using `MutableSharedFlow<T>`.
- Update all assignments from `.value = x` or `.postValue(x)` to `.value = x` (for `StateFlow`) or `.emit(x)` (for `SharedFlow`).
- Use `StateFlow`'s `.update { ... }` when thread-safe atomic updates are needed.

### UI (Compose) Changes
- Replace `observeAsState()` with `collectAsStateWithLifecycle()`.
- Ensure `androidx.lifecycle:lifecycle-runtime-compose` dependency is available.

### Repository Changes
- If repositories are returning `LiveData` (though it seems they return direct values currently), they should be updated to return `Flow` if appropriate.

## Success Criteria
- All `LiveData` references removed from `MainViewModel`.
- App functionality remains identical.
- All unit tests pass (and are updated to work with Flows).
- No memory leaks or regression in reactive updates.
