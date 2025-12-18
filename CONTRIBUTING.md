# Contributing to WigAI

Thank you for your interest in contributing to WigAI! This guide will help you understand our development workflow and contribution process.

## Getting Started

1. **Fork the repository** and clone your fork locally
2. **Install dependencies**: JDK 21 and Gradle 8+
3. **Build the project**: `./gradlew build`
4. **Run tests**: `./gradlew test`

## Development Workflow

This project uses a cycle-based development workflow with branch protection. Please read the following documents:

### Branch Naming and PR Targeting

See [docs/engineering/git-workflow.md](docs/engineering/git-workflow.md) for:
- Branch naming conventions (`develop/cycle-*`, `analysis/*`, `planning/*`, `solutioning/*`, `implementation/*`, `docs/*`, `hotfix/*`)
- PR targeting rules (all PRs target `develop/cycle-*`, not `main`)
- Merge strategies and protection rules

### CI/CD Workflows

See [.github/workflows/README.md](.github/workflows/README.md) for:
- Workflow architecture and execution strategy
- PR validation process (automated tests and builds)
- Release automation (semantic versioning with Nyx)
- Path filters and optimization details

## Making Changes

### 1. Create a Working Branch

Branch from the current cycle's integration branch (e.g., `develop/cycle-2`). For code changes, use an `implementation/*` branch:

```bash
git checkout develop/cycle-2
git pull
git checkout -b implementation/123-your-feature-name
```

### 2. Make Your Changes

- Write clear, focused commits following [Conventional Commits](https://www.conventionalcommits.org/)
- Include tests for new functionality
- Ensure code follows existing patterns (see `docs/reference/operational-guidelines.md`)

### 3. Test Locally

```bash
# Run tests
./gradlew test

# Build extension
./gradlew build

# Test in Bitwig Studio
cp build/extensions/WigAI.bwextension ~/Documents/Bitwig\ Studio/Extensions/
```

### 4. Submit a Pull Request

```bash
git push -u origin implementation/123-your-feature-name
```

Then create a PR targeting the current `develop/cycle-*` branch (NOT `main`).

**Your PR will automatically trigger:**
- ✅ Branch naming validation
- ✅ Automated tests
- ✅ Build verification
- ✅ Artifact generation

## Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/) for automatic semantic versioning:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat:` - New feature (minor version bump)
- `fix:` - Bug fix (patch version bump)
- `docs:` - Documentation only
- `style:` - Code style changes (formatting, etc.)
- `refactor:` - Code refactoring
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

**Breaking changes:** Add `!` after type or include `BREAKING CHANGE:` in footer (major version bump)

**Examples:**
```bash
feat(transport): add loop toggle support
fix(parameters): handle null device gracefully
docs: update API reference with new endpoints
feat!: redesign MCP tool interface
```

## Code Review Process

1. Automated checks must pass (tests, build, branch policy)
2. Code review and approval required (solo developers can self-approve)
3. Merge to `develop/cycle-*` branch
4. Changes are included in the next cycle release to `main`

## What Gets Tested

Our CI/CD automatically runs tests for **code changes only**. The following changes skip automated testing:

- Documentation changes (`docs/**`, `**.md`)
- Configuration files (`.bmad/**`, `.claude/**`, `.continue/**`, `.github/**`, `.roo/**`)
- Non-code files (`.gitignore`, `LICENSE`)

This saves resources while still validating branch naming conventions.

## Release Process

Releases are fully automated:

1. PRs are validated and merged to `develop/cycle-*`
2. When a cycle completes, `develop/cycle-*` is merged to `main`
3. GitHub Actions automatically:
   - Determines semantic version based on commits
   - Builds the extension
   - Creates a GitHub release with changelog
   - Attaches the `.bwextension` file

No manual intervention needed!

## Questions or Issues?

- **Questions**: Open a [GitHub Discussion](https://github.com/jmeachum/WigAI/discussions)
- **Bugs**: Open a [GitHub Issue](https://github.com/jmeachum/WigAI/issues)
- **Security**: See SECURITY.md (if present) or contact maintainers directly

## Development Method

This project is developed using the [BMAD v2 method](https://github.com/bmadcode/BMAD-METHOD) with AI-assisted development. Files in `.bmad/`, `.claude/`, and related directories support this methodology.

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
