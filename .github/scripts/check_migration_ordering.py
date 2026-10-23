#!/usr/bin/env python3
"""Validate that database migration versions are ordered and complete.

Two counters advance independently and both files are append-only:

  * sormas-backend/src/main/resources/sql/sormas_schema.sql adds one
    "INSERT INTO schema_version (...) VALUES (N, ...)" per server migration.
  * sormas-app/.../backend/common/DatabaseHelper.java bumps DATABASE_VERSION
    and extends the onUpgrade() case ladder for the mobile database.

The script fails when a version is duplicated, out of order, missing from the
onUpgrade() ladder, or when a branch lowers or drops versions that already
exist on the base branch (the signature of a renumbering after a rebase).
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

SCHEMA_FILE = "sormas-backend/src/main/resources/sql/sormas_schema.sql"
DATABASE_HELPER = (
    "sormas-app/app/src/main/java/de/symeda/sormas/app/backend/common/"
    "DatabaseHelper.java"
)

SCHEMA_VERSION_RE = re.compile(
    r"INSERT\s+INTO\s+schema_version\s*\([^)]*\)\s*VALUES\s*\(\s*(\d+)\s*,",
    re.IGNORECASE,
)
DATABASE_VERSION_RE = re.compile(r"\bDATABASE_VERSION\s*=\s*(\d+)\s*;")
CASE_RE = re.compile(r"\bcase\s+(\d+)\s*:")
ON_UPGRADE_RE = re.compile(r"\bvoid\s+onUpgrade\s*\([^)]*\)[^{;]*\{")


@dataclass(frozen=True)
class Problem:
    """A single migration problem, reported as a GitHub Actions annotation."""

    file: str
    line: int | None
    message: str

    def annotation(self) -> str:
        """Render the problem as a GitHub Actions error annotation."""
        if not self.file:
            return f"::error::{self.message}"
        location = f"file={self.file}"
        if self.line:
            location += f",line={self.line}"
        return f"::error {location}::{self.message}"


def _blank(chars: list[str], start: int, end: int) -> None:
    """Replace characters with spaces, keeping line breaks intact."""
    for position in range(start, end):
        if chars[position] != "\n":
            chars[position] = " "


def _end_of_block_comment(text: str, index: int) -> int:
    end = text.find("*/", index + 2)
    return len(text) if end == -1 else end + 2


def _end_of_line_comment(text: str, index: int) -> int:
    end = text.find("\n", index)
    return len(text) if end == -1 else end


def _end_of_literal(text: str, index: int, quote: str, *, doubled: bool) -> int:
    """Return the offset just past a string or character literal."""
    index += 1
    while index < len(text):
        char = text[index]
        if not doubled and char == "\\":
            index += 2
            continue
        if char == quote:
            if doubled and index + 1 < len(text) and text[index + 1] == quote:
                index += 2
                continue
            return index + 1
        index += 1
    return len(text)


def strip_markup(text: str, *, sql: bool) -> str:
    """Blank comments (and Java literals) while preserving length and newlines.

    Keeps offsets aligned with the original file, so a match offset still maps
    to the correct line number.
    """
    chars = list(text)
    index = 0

    while index < len(text):
        char = text[index]
        following = text[index + 1] if index + 1 < len(text) else ""
        starts_line_comment = (
            char == "-" and following == "-" if sql else char == "/" and following == "/"
        )

        if starts_line_comment:
            end = _end_of_line_comment(text, index)
            _blank(chars, index, end)
            index = end
        elif char == "/" and following == "*":
            end = _end_of_block_comment(text, index)
            _blank(chars, index, end)
            index = end
        elif sql and char == "'":
            index = _end_of_literal(text, index, "'", doubled=True)
        elif not sql and char in "\"'":
            end = _end_of_literal(text, index, char, doubled=False)
            _blank(chars, index, end)
            index = end
        else:
            index += 1

    return "".join(chars)


def line_of(text: str, offset: int) -> int:
    """Return the 1-based line number that contains the given offset."""
    return text.count("\n", 0, offset) + 1


def parse_schema_versions(text: str) -> list[tuple[int, int]]:
    """Return (version, line) for every schema_version insert."""
    cleaned = strip_markup(text, sql=True)
    return [
        (int(match.group(1)), line_of(cleaned, match.start()))
        for match in SCHEMA_VERSION_RE.finditer(cleaned)
    ]


def find_on_upgrade_body(cleaned: str) -> tuple[int, int] | None:
    """Return the start and end offsets of the onUpgrade() body."""
    match = ON_UPGRADE_RE.search(cleaned)
    if not match:
        return None
    start = match.end() - 1
    depth = 0
    for index in range(start, len(cleaned)):
        if cleaned[index] == "{":
            depth += 1
        elif cleaned[index] == "}":
            depth -= 1
            if depth == 0:
                return start, index + 1
    return None


def parse_upgrade_cases(cleaned: str) -> list[tuple[int, int]] | None:
    """Return (case version, line) for the onUpgrade() ladder, or None if the method is missing."""
    body = find_on_upgrade_body(cleaned)
    if body is None:
        return None
    start, end = body
    return [
        (int(match.group(1)), line_of(cleaned, start + match.start()))
        for match in CASE_RE.finditer(cleaned, start, end)
    ]


def check_strictly_ascending(
    entries: list[tuple[int, int]], file: str, label: str, problems: list[Problem]
) -> None:
    """Report duplicated or out-of-order versions."""
    first_seen: dict[int, int] = {}
    for version, line in entries:
        if version in first_seen:
            problems.append(
                Problem(
                    file,
                    line,
                    f"duplicate {label} {version}; it is already declared at "
                    f"line {first_seen[version]}",
                )
            )
        else:
            first_seen[version] = line

    for (previous_version, previous_line), (version, line) in zip(entries, entries[1:]):
        if version < previous_version:
            problems.append(
                Problem(
                    file,
                    line,
                    f"{label} {version} is out of order; it must be greater than "
                    f"{previous_version} (line {previous_line})",
                )
            )


def check_upgrade_coverage(
    cases: list[tuple[int, int]],
    database_version: int,
    version_line: int | None,
    file: str,
    problems: list[Problem],
) -> None:
    """Report versions missing from the onUpgrade() ladder."""
    versions = [version for version, _ in cases]
    if not versions:
        problems.append(Problem(file, version_line, "onUpgrade() has no version cases"))
        return

    lowest = min(versions)
    highest = max(versions)
    if highest > database_version - 1:
        problems.append(
            Problem(
                file,
                version_line,
                f"onUpgrade() handles version {highest} but DATABASE_VERSION is "
                f"{database_version}; the ladder must stop at {database_version - 1}",
            )
        )

    known = set(versions)
    missing = [version for version in range(lowest, database_version) if version not in known]
    if missing:
        preview = ", ".join(str(version) for version in missing[:10])
        if len(missing) > 10:
            preview += ", ..."
        problems.append(
            Problem(
                file,
                version_line,
                f"onUpgrade() does not handle version(s) {preview}; DATABASE_VERSION is "
                f"{database_version}, so every version from {lowest} to "
                f"{database_version - 1} needs a case",
            )
        )


def check_against_base(
    branch_entries: list[tuple[int, int]],
    base_text: str,
    file: str,
    label: str,
    problems: list[Problem],
) -> None:
    """Compare the entries with the base branch's entries."""
    if label == "schema_version":
        base_entries = parse_schema_versions(base_text)
    else:
        base_entries = parse_upgrade_cases(strip_markup(base_text, sql=False)) or []

    if not base_entries or not branch_entries:
        return

    branch_versions = [version for version, _ in branch_entries]
    base_versions = [version for version, _ in base_entries]
    branch_highest = max(branch_versions)
    base_highest = max(base_versions)
    highest_line = next(line for version, line in branch_entries if version == branch_highest)

    if branch_highest < base_highest:
        problems.append(
            Problem(
                file,
                highest_line,
                f"highest {label} {branch_highest} is lower than {base_highest} on the "
                f"base branch; the branch is behind or renumbered a migration",
            )
        )

    missing = sorted(set(base_versions) - set(branch_versions))
    if missing:
        preview = ", ".join(str(version) for version in missing[:10])
        if len(missing) > 10:
            preview += ", ..."
        problems.append(
            Problem(
                file,
                None,
                f"{len(missing)} {label}(s) from the base branch are missing: {preview}; "
                f"migrations are append-only",
            )
        )


def read_base_file(repo_root: Path, ref: str, relative_path: str) -> str:
    """Read a file from the base ref through git."""
    result = subprocess.run(
        ["git", "-C", str(repo_root), "show", f"{ref}:{relative_path}"],
        capture_output=True,
        encoding="utf-8",
        errors="replace",
        check=False,
    )
    if result.returncode != 0:
        raise RuntimeError(f"cannot read {relative_path} at {ref}: {result.stderr.strip()}")
    return result.stdout


def check_schema(schema_text: str, problems: list[Problem]) -> list[tuple[int, int]]:
    """Validate the schema_version inserts and return the parsed entries."""
    entries = parse_schema_versions(schema_text)
    if not entries:
        problems.append(
            Problem(SCHEMA_FILE, None, "no 'INSERT INTO schema_version' statement found")
        )
        return entries

    check_strictly_ascending(entries, SCHEMA_FILE, "schema_version", problems)
    highest = max(version for version, _ in entries)
    print(f"{SCHEMA_FILE}: {len(entries)} schema_version entries, highest {highest}")
    return entries


def check_database_helper(helper_text: str, problems: list[Problem]) -> list[tuple[int, int]]:
    """Validate DATABASE_VERSION and the onUpgrade() ladder, returning the case entries."""
    cleaned = strip_markup(helper_text, sql=False)
    version_matches = list(DATABASE_VERSION_RE.finditer(cleaned))
    cases = parse_upgrade_cases(cleaned)

    if len(version_matches) != 1:
        line = line_of(cleaned, version_matches[0].start()) if version_matches else None
        problems.append(
            Problem(
                DATABASE_HELPER,
                line,
                f"expected exactly one 'DATABASE_VERSION = <number>' declaration, "
                f"found {len(version_matches)}",
            )
        )
        return cases or []

    if cases is None:
        problems.append(Problem(DATABASE_HELPER, None, "onUpgrade() method not found"))
        return []

    database_version = int(version_matches[0].group(1))
    version_line = line_of(cleaned, version_matches[0].start())
    check_strictly_ascending(cases, DATABASE_HELPER, "onUpgrade() case", problems)
    check_upgrade_coverage(cases, database_version, version_line, DATABASE_HELPER, problems)
    lowest = min(version for version, _ in cases)
    highest = max(version for version, _ in cases)
    print(
        f"{DATABASE_HELPER}: DATABASE_VERSION {database_version}, "
        f"onUpgrade() covers {lowest}..{highest}"
    )
    return cases


def compare_with_base(
    repo_root: Path,
    base_ref: str,
    schema_entries: list[tuple[int, int]],
    case_entries: list[tuple[int, int]],
    problems: list[Problem],
) -> None:
    """Compare both counters with the base branch."""
    try:
        base_schema_text = read_base_file(repo_root, base_ref, SCHEMA_FILE)
        base_helper_text = read_base_file(repo_root, base_ref, DATABASE_HELPER)
    except RuntimeError as error:
        problems.append(Problem("", None, str(error)))
        return

    check_against_base(schema_entries, base_schema_text, SCHEMA_FILE, "schema_version", problems)
    if case_entries:
        check_against_base(
            case_entries, base_helper_text, DATABASE_HELPER, "onUpgrade() case", problems
        )
    print(f"compared against base ref {base_ref}")


def main(argv: list[str] | None = None) -> int:
    """Run the migration ordering check and return a process exit code."""
    parser = argparse.ArgumentParser(description="Check database migration ordering.")
    parser.add_argument(
        "--repo-root",
        default=".",
        help="repository root (default: current directory)",
    )
    parser.add_argument(
        "--base-ref",
        default="",
        help="git ref of the target branch, e.g. origin/development",
    )
    arguments = parser.parse_args(argv)

    repo_root = Path(arguments.repo_root).resolve()
    problems: list[Problem] = []

    try:
        schema_text = (repo_root / SCHEMA_FILE).read_text(encoding="utf-8")
        helper_text = (repo_root / DATABASE_HELPER).read_text(encoding="utf-8")
    except OSError as error:
        print(f"::error::{error}")
        return 2

    schema_entries = check_schema(schema_text, problems)
    case_entries = check_database_helper(helper_text, problems)

    if arguments.base_ref:
        compare_with_base(repo_root, arguments.base_ref, schema_entries, case_entries, problems)

    if problems:
        for problem in problems:
            print(problem.annotation())
        print(
            f"\nDatabase migration ordering check failed with {len(problems)} problem(s).",
            file=sys.stderr,
        )
        return 1

    print("Database migration ordering check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
