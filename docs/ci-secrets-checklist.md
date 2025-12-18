# CI Secrets Checklist (GitHub Actions)

## Required

- None beyond the default `GITHUB_TOKEN` provided by GitHub Actions.

## Release Publishing

`/.github/workflows/release.yml` uses `GITHUB_TOKEN` to:

- push tags/metadata required by Nyx
- publish GitHub Releases

## Optional (Only If Added Later)

- `SLACK_WEBHOOK_URL` (or similar) for notifications
- Any external service tokens used by future integration tests

