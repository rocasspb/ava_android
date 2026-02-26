# Track Overview
Create a comprehensive UI test harness for the project, covering all main components (MainScreen, PointInfoCard, ModeSelectionPanel, WindRose). The goal is to ensure UI stability and prevent regressions in the core visual elements. This includes updating `workflow.md` with relevant testing commands.

# Functional Requirements
1.  **MainScreen Test Suite:** Create UI tests for the primary screen, ensuring the Mapbox view and essential overlays (like the centered location button) are present and interactive.
2.  **PointInfoCard Test Suite:** Test the display and behavior of the `PointInfoCard`, including the expanded/collapsed states and data presentation.
3.  **ModeSelectionPanel Test Suite:** Test the interaction with the mode selection panel, verifying that different modes (Bulletin, Risk, Custom, Off) can be selected.
4.  **WindRose Test Suite:** Test the graphical aspect picker (WindRose), ensuring sectors can be selected and visual states are correct.
5.  **UI Test Harness Infrastructure:** Establish a reusable base or utility for Compose UI tests in the `androidTest` source set.
6.  **Workflow Update:** Document the commands for running both UI (instrumented) and unit tests in `workflow.md`.

# Non-Functional Requirements
-   **Execution Time:** UI tests should be efficient, though they are instrumented.
-   **Reliability:** Tests must be robust and avoid flakiness (e.g., using `ComposeTestRule` appropriately).
-   **Code Coverage:** Maintain or improve UI-related code coverage.

# Acceptance Criteria
-   UI tests exist for `MainScreen`, `PointInfoCard`, `ModeSelectionPanel`, and `WindRose`.
-   All UI tests pass when run via `./gradlew connectedCheck` (or equivalent).
-   `workflow.md` is updated with clear instructions for running tests.
-   A sample/smoke test for the entire app flow is included.

# Out of Scope
-   Automated screenshot/snapshot testing.
-   Performance/Stress testing of the Mapbox rendering.
