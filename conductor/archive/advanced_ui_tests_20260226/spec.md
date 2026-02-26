# Track Overview
Implement advanced UI interaction tests for AvaAwaAnd. These tests will verify that user interactions with Floating Action Buttons (FABs), Mode Selection, and Custom Mode controls correctly trigger the application's core logic and update the UI state. Tests will use a mocked Map SDK to ensure focus remains on app-specific behavior.

# Functional Requirements
1.  **MainScreen FAB Interactions:**
    *   **My Location:** Verify clicking the button attempts to transition the map viewport to follow the user's location.
    *   **2D/3D Toggle:** Verify clicking toggles the pitch between 0 and 60 degrees and updates the button label ("2D" vs "3D").
    *   **Style Switcher:** Verify clicking toggles the map style (e.g., Outdoors vs Satellite) in the `MainViewModel`.
2.  **Mode Selection Integration:**
    *   Verify that clicking different mode icons (Bulletin, Risk, Custom, Off) updates the `visualizationMode` in `MainViewModel`.
    *   Ensure the selection panel UI highlights the active mode.
3.  **Custom Mode Deep-Dive:**
    *   **Elevation/Slope Sliders:** Verify that interacting with sliders updates `customModeParams` and triggers a recalculation of avalanche rules.
    *   **WindRose Aspect Selection:** Verify that clicking sectors updates the selected aspects in the configuration.
4.  **End-to-End Verification:**
    *   Assert that UI interactions lead to observable state changes in the ViewModel and repository mocks.
    *   Verify that the "loading" indicator appears when logic-heavy operations (like raster generation) are triggered.

# Non-Functional Requirements
-   **Stability:** Tests must be deterministic and avoid flakiness related to asynchronous UI updates.
-   **Isolation:** Use `FakeMainRepository` and mock Mapbox dependencies to ensure tests run reliably without network or real Map SDK overhead.

# Acceptance Criteria
-   Comprehensive instrumented test suite using `ComposeTestRule`.
-   100% pass rate for all new interaction tests on a standard emulator.
-   Tests verify the chain of events: User Action -> ViewModel Update -> Logic Trigger -> UI Reflection.

# Out of Scope
-   Verifying the actual visual rendering of Mapbox layers (only state changes are verified).
-   Performance benchmarking of UI interactions.
