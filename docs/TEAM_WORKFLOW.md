# Team workflow — a step by step guide

For engineers joining the testing and developer workflow team. It assumes you know git but have not worked in a fork before.

If you want the reasoning behind any of this, the readme explains why. This page is just what to type.

> **Note:** this describes the arrangement we are moving to. `main` does not exist yet and `development` still carries our work. Until those are set up, ask before starting — some steps will not apply.

## The short version

There are two projects and three kinds of branch.

The **APMIS project** (`AFG-Polio-Data/APMIS-Project`) is the real one. We cannot write to it. We can only offer it changes, which their developers accept or refuse.

**Our fork** (`Iyanuu/APMIS-Project`) is where we work. Inside it:

| Branch | What it is | Do you edit it? |
|---|---|---|
| `development` | a copy of the APMIS project | **never** |
| `main` | our workspace, where our work lands | only through a pull request |
| `test/...`, `ci/...` | your piece of work | yes, this is where you work |

Everything you do starts by branching from `development` — the copy. That one habit is what makes your work possible to offer to the APMIS team later.

---

## 1. One time setup

Clone our fork:

```bash
git clone git@github.com:Iyanuu/APMIS-Project.git
cd APMIS-Project
```

Add the APMIS project as a second remote so you can pull their changes down. We call it `apmis`:

```bash
git remote add apmis https://github.com/AFG-Polio-Data/APMIS-Project.git
```

Check it looks right:

```bash
git remote -v
# origin  git@github.com:Iyanuu/APMIS-Project.git       (ours, you push here)
# apmis   https://github.com/AFG-Polio-Data/...          (theirs, read only)
```

You push to `origin`. You never push to `apmis`.

---

## 2. Before starting anything, update the copy

Do this at the start of every piece of work. It takes seconds.

```bash
git switch development
git fetch apmis
git merge --ff-only apmis/development
git push origin development
```

`--ff-only` means "only move forward, never create a merge". Because nobody ever edits `development`, this always works. **If it ever refuses, stop and ask** — it means something was committed to the copy by mistake, and that needs sorting out rather than forcing.

---

## 3. Start your branch

From `development`, which you have just updated:

```bash
git switch -c test/25-version-matrix development
```

Name it after what it is, with the issue number:

| Prefix | For |
|---|---|
| `test/` | tests |
| `ci/` | workflows, checks, pipeline |
| `fix/` | defects |
| `docs/` | documentation |
| `chore/` | dependencies, tooling |

**Do not start from `main`.** If you do, your branch picks up everything the team has ever done, and it can no longer be offered to the APMIS developers on its own.

---

## 4. Do the work and commit

```bash
git add <the files you changed>
git commit
```

Write the message as `type(scope): summary`, then explain **why** underneath:

```text
test(api): cover the version compatibility rules

isCompatibleToApi decides whether a phone in the field is allowed to sync
at all, and had no tests. Covers the boundaries either side of the minimum
version, since that is where a wrong answer locks every device out.
```

The diff already shows what you changed. What it cannot show is what you ruled out, or which failure you were chasing. Put that in the message.

---

## 5. Push and open a pull request into `main`

```bash
git push -u origin test/25-version-matrix
gh pr create --base main
```

Then:

- Wait for the checks. If one is red, read it before asking — the failure usually says what is wrong.
- If a check is red for a reason unrelated to your change, say so in the description rather than leaving the reviewer to work it out.
- Link the issue it closes.

Once it is approved and merged, your work is part of what the team has built.

---

## 6. Offer the same branch to the APMIS team

Only after step 5 is merged and you are happy with it. Same branch, different destination:

```bash
gh pr create --repo AFG-Polio-Data/APMIS-Project \
             --base development \
             --head Iyanuu:test/25-version-matrix
```

Keep it to one thing. The APMIS developers are being asked to take changes they did not write, so a pull request doing one clear thing is far more likely to be accepted than one doing five.

**Do not delete your branch** until both pull requests are closed.

---

## 7. When their changes come back

Once the APMIS developers merge your pull request, your change becomes part of their project. It comes back to us next time anyone runs step 2. Nothing extra to do.

---

## The one rule

**Never merge `main` into your branch.**

The moment you do, your branch stops containing only your change and starts carrying the team's whole history. It can no longer be offered to the APMIS team. This has already happened once here and the work had to be redone.

---

## Common situations

### My branch has gone stale and I need the latest changes

Rebase onto the copy — do not merge:

```bash
git switch development
git fetch apmis && git merge --ff-only apmis/development && git push origin development

git switch test/25-version-matrix
git rebase development
git push --force-with-lease
```

`--force-with-lease` rewrites your branch but refuses if someone else has pushed to it in the meantime. Never use plain `--force`.

If the rebase hits conflicts and you are unsure, stop with `git rebase --abort` and ask. Nothing is lost by aborting.

### I accidentally merged `main` into my branch

Tell someone rather than trying to fix it quietly. If the merge is the most recent thing you did and you have not pushed:

```bash
git reset --hard HEAD~1
```

If you have pushed, or other commits came after, ask — untangling it is easy with help and easy to make worse alone.

### My change needs something we have not offered yet

Start from that earlier branch instead of from `development`, and wait until it has been accepted before offering yours.

This is not a problem to work around. It means your change cannot stand on its own yet, which is worth knowing.

### A check is failing and I cannot tell why

- `mvn verify` — the failure is usually near the end of the log, under `BUILD FAILURE`. Look for the module name.
- `android build and unit tests` — this builds `sormas-api` with Maven first, so a failure here is often not about the app at all.
- `Lint Code Base` — names the file and line.

If a check is red on `development` too, it is not your change. Say so and carry on.

### I do not know whether something should be offered to the APMIS team

Ask. Some of our work is only useful to us and is meant to stay on `main`. There is no harm in work that never leaves the fork.

---

## What "done" means

Merged into `main`, the checks proved it, and anything it uncovered is either fixed or written up as an issue.

Not "it works on my machine".
