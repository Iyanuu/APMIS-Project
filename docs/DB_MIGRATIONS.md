# Database Migrations

The server and the mobile app keep separate migration counters. Both are append-only, and the
`Migration Ordering` workflow verifies them on every pull request.

| Counter | File | Rule |
| --- | --- | --- |
| `schema_version` | `sormas-backend/src/main/resources/sql/sormas_schema.sql` | One `INSERT INTO schema_version (version_number, comment) VALUES (N, ...)` per migration, strictly ascending and without duplicates. |
| `DATABASE_VERSION` | `sormas-app/app/src/main/java/de/symeda/sormas/app/backend/common/DatabaseHelper.java` | Bump the constant and add the matching `case N:` to `onUpgrade()`. The case ladder must cover every version from its lowest case up to `DATABASE_VERSION - 1`. |

## Adding a migration

1. Append the new SQL or the new `case N:` block; never edit or renumber an existing version.
2. Use the next free number: the current highest version plus one.
3. If a rebase or a concurrent change occupies that number, renumber **your** unmerged migration to
   the next free number. Existing versions never move.
4. When touching `DatabaseHelper.java`, bump `DATABASE_VERSION` in the same change that adds the
   new `case` label.

## What CI checks

`Migration Ordering` runs on every pull request and fails when:

* a `schema_version` or `onUpgrade()` case number is duplicated (the message names the version and
  both lines);
* a version appears out of order (for example a renumbered entry after a rebase);
* the `onUpgrade()` ladder has a gap between its lowest case and `DATABASE_VERSION - 1` (the
  message lists the missing versions);
* the branch's highest version is lower than on the target branch, or a version that exists on the
  target branch disappeared.

Failures are reported as annotations on the offending file and line.

## Running the check locally

```bash
python3 .github/scripts/check_migration_ordering.py
python3 .github/scripts/check_migration_ordering.py --base-ref origin/development
```
