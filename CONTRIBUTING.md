# Contributing to KiranaFlow

Thank you for helping make KiranaFlow better for small shopkeepers across India.
Please read this document before opening an issue or submitting a pull request.

## Code of Conduct

Be respectful and constructive. We value clear communication in English or Tamil.

## How to Contribute

### Reporting Bugs

Use the **Bug Report** issue template. If the bug involves financial data (incorrect
bill committed, stock off by a unit, day-close duplication), mark the "Financial impact"
checkbox and describe the scenario carefully.

### Suggesting Features

Use the **Feature Request** template. Explain the shopkeeper problem you are solving —
not just the technical solution.

### Submitting Code

1. **Fork and branch** from `main`:

   ```bash
   git checkout -b feat/your-feature-name
   ```

2. **Write tests first** (TDD preferred). Minimum coverage targets:
   - `core:data` — 90 % line coverage (billing logic)
   - `core:domain` — 80 % line coverage
   - New features — at least happy path + two error cases

3. **Follow the code style**:

   ```bash
   ./gradlew ktlintFormat   # auto-fix formatting
   ./gradlew detekt         # static analysis — must be zero violations
   ./gradlew ktlintCheck    # lint — must be zero violations
   ```

4. **Commit messages** must follow [Conventional Commits](https://www.conventionalcommits.org/):

   | Prefix | Use for |
   |--------|---------|
   | `feat:` | New user-visible feature |
   | `fix:` | Bug fix |
   | `refactor:` | Code improvement with no behaviour change |
   | `test:` | Adding or fixing tests |
   | `docs:` | Documentation only |
   | `ci:` | CI/CD workflow changes |
   | `build:` | Build-script changes |
   | `chore:` | Maintenance (dependency bumps, cleanup) |

   Include the affected module scope in parentheses: `fix(billing): prevent double commit on rotate`.

5. **Non-negotiable constraints** (your PR will be rejected if violated):
   - No hardcoded `Color(0x...)` literals outside `core/ui/.../Color.kt`
   - No hardcoded user-visible strings in composables — use string resources
   - No secrets committed (API keys, keystore passwords, `google-services.json`)
   - No commented-out code
   - No TODOs without a linked GitHub issue number: `// TODO(#42): remove after migration`
   - No magic numbers — extract to named constants

6. **Run the full local check** before pushing:

   ```bash
   ./gradlew testDebugUnitTest detekt ktlintCheck
   ```

7. **Open a Pull Request** against `main` using the PR template.

## Project Architecture

See `PROJECT_CONTEXT.md` for the module map, design palette, and architectural decisions.

The **core flow** (Speak → Understand → Confirm → Commit) and the **WAL/Queue billing
engine** are production-critical. Any change to `BillingQueue`, `BillJournal`, or the
Room migrations must include regression tests and a description of WAL-recovery behaviour
in the PR body.

## Database Migrations

Never delete or modify an existing migration file. Add a new numbered migration.
Always add a `MigrationTest` verifying both the happy path and that old data survives.

## Local Setup

```bash
# Prerequisites: Android Studio Hedgehog+, JDK 21, Android SDK platform 35
git clone https://github.com/bowsii/Kirana-Flow.git
cd Kirana-Flow
./gradlew assembleDebug
```

## Questions?

Open a GitHub Discussion or tag `@bowsii` in an issue.
