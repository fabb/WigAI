# CI/CD (GitHub Actions)

WigAI uses GitHub Actions for PR validation, branch policy enforcement, and release publishing.

## Workflows

- `/.github/workflows/pr-validation.yml`: Runs tests/build for PRs (skips for docs-only changes).
- `/.github/workflows/build-and-test.yml`: Reusable workflow that runs `./gradlew test` then builds `WigAI.bwextension`.
- `/.github/workflows/release.yml`: Publishes a GitHub Release on `main` (Nyx-driven semantic versioning).
- `/.github/workflows/branch-policy.yml`: Enforces branch naming/PR targeting rules.

## Local equivalents

Run the same steps locally as CI:

- Full CI mirror: `./scripts/ci-local.sh`
- Selective (like PR validation filter): `./scripts/test-changed.sh`

## Artifacts

On failures, CI uploads Gradle test reports:

- `build/reports/tests/test/`
- `build/test-results/test/`

PR validation also uploads the built extension artifact (`build/extensions/WigAI.bwextension`) for convenience.
