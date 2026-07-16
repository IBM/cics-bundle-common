# Releasing

## Prerequisites

- Write access to this repository
- The current version on `main` must be a `-SNAPSHOT` version

## Steps

1. **Verify downstream projects** — before releasing, confirm the changes work correctly with [cics-bundle-maven](https://github.com/IBM/cics-bundle-maven) and [cics-bundle-gradle](https://github.com/IBM/cics-bundle-gradle) by updating their dependency to the current SNAPSHOT and running their builds locally.

2. **Trigger the release workflow** — go to Actions → Prepare Release → Run workflow, or run:
   ```bash
   gh workflow run prepare-release.yml
   ```
   This opens a PR that strips `-SNAPSHOT` from `pom.xml` (e.g. `2.0.5-SNAPSHOT → 2.0.5`).

3. **Review and merge the PR** — merging triggers the existing deploy to OSSRH via `maven-build.yml`.

4. **Publish to Maven Central** — a maintainer with Sonatype publishing permission must log in to [central.sonatype.com/publishing](https://central.sonatype.com/publishing) and publish the staged artifact.

5. **Automation takes over** — once the release PR is merged, `post-release.yml` automatically:
   - Creates a GitHub Release tagged `vX.Y.Z` with generated release notes
   - Opens a PR bumping to the next patch SNAPSHOT (e.g. `2.0.6-SNAPSHOT`)

6. **Merge the next-dev PR** — no rush, merge when ready.
