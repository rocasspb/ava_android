# Specification: Detailed Avalanche Data in PointInfoCard

## Overview
Enhance the map interaction by displaying detailed avalanche bulletin data when a user clicks on a specific location. This provides immediate, context-aware safety information.

## User Stories
- As a backcountry skier, I want to click on a point on the map and see the specific avalanche problems (e.g., wind slab, persistent weak layer) affecting that location.
- As a user, I want to see the danger level, elevation range, and aspect relevant to the avalanche problems at my selected point.

## Requirements
- Display a UI card (PointInfoCard) when a map point is selected.
- Extract avalanche data for the selected region, aspect, and elevation.
- Show danger level and specific avalanche problem details.
- Ensure the UI remains map-centric and minimalist.

## Technical Details
- Integrate with existing `AvaAwaService` and `AvalancheLogic`.
- Update `MainViewModel` to handle point selection and data extraction.
- Implement or update the Compose-based `PointInfoCard` component.
