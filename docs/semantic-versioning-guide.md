# Semantic Versioning Guide for WigAI

This project uses [Semantic Versioning](https://semver.org/) with automated releases based on conventional commits.

## Commit Message Format

Use the [Conventional Commits](https://conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

## Commit Types and Version Impact

### PATCH Releases (0.2.0 → 0.2.1)
Bug fixes that don't break existing functionality:

```bash
git commit -m "fix: resolve null pointer exception in device controller"
git commit -m "fix(mcp): handle connection timeout gracefully"
git commit -m "fix(transport): correct play/pause state synchronization"
```

### MINOR Releases (0.2.0 → 0.3.0)
New features that are backward compatible:

```bash
git commit -m "feat: add support for scene banking operations"
git commit -m "feat(mcp): implement new device parameter tool"
git commit -m "feat(ui): add configuration panel for MCP settings"
```

### MAJOR Releases (0.2.0 → 1.0.0)
Breaking changes that require user intervention:

```bash
git commit -m "feat!: redesign MCP tool interface for better performance"

# Or with explicit breaking change footer:
git commit -m "feat: change device API structure

BREAKING CHANGE: Device.getParameter() now returns Optional<Parameter> instead of Parameter"
```

## Common Commit Types

- `fix:` - Bug fixes
- `feat:` - New features
- `docs:` - Documentation changes
- `style:` - Code style changes (formatting, etc.)
- `refactor:` - Code refactoring without feature changes
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks, dependency updates
- `perf:` - Performance improvements
- `ci:` - CI/CD configuration changes

## Scopes (Optional)

Use scopes to indicate the area of change:

- `mcp` - MCP server related changes
- `transport` - Transport control functionality
- `device` - Device parameter management
- `clip` - Clip and scene management
- `config` - Configuration management
- `ui` - User interface changes
- `api` - API changes

## Examples for WigAI

```bash
# Bug fixes (PATCH)
git commit -m "fix(mcp): resolve server startup race condition"
git commit -m "fix(device): handle missing parameter gracefully"

# New features (MINOR)
git commit -m "feat(transport): add support for tempo changes"
git commit -m "feat(mcp): implement clip launch tool"

# Breaking changes (MAJOR)
git commit -m "feat(api)!: change MCP tool response format"
git commit -m "feat: restructure extension initialization

BREAKING CHANGE: WigAIExtension constructor now requires McpServerManager parameter"
```

## Release Process

1. **Push to main branch** - Triggers automatic analysis
2. **Semantic Release analyzes commits** - Determines version bump type
3. **Version update** - Updates `build.gradle.kts` automatically
4. **Changelog generation** - Updates `CHANGELOG.md`
5. **GitHub release** - Creates release with artifacts
6. **Git tag** - Tags the release (e.g., `v0.3.0`)

## No Release Scenarios

These commit types will NOT trigger a release:

```bash
git commit -m "docs: update README with new features"
git commit -m "style: fix code formatting"
git commit -m "ci: update GitHub Actions workflow"
git commit -m "chore: update dependencies"
```

## Manual Release

If needed, you can manually trigger a release using GitHub Actions workflow dispatch, but it's recommended to use the commit-based approach for consistency.