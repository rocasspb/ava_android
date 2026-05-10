---
name: new-feature
description: Creates a new feature branch and switches to it. Use when starting a new development task.
---
# New Feature Skill

This skill automates the creation of a git branch for a new feature.

## Workflow

1. **Feature Name**: Ask the user for the name of the feature if not already provided.
2. **Setup**: Run the setup script to create and checkout the branch.
   ```powershell
   powershell -File scripts/setup_branch.ps1 -FeatureName "<feature-name>"
   ```

## Conventions

- **Branch Name**: `feature/<kebab-case-name>`
- **Behavior**: If the branch already exists, it simply checks it out.
