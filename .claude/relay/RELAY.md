# Relay runbook

Four subagents, each pinned to the model that suits its stage. They hand work to
each other through files in `.claude/relay/<task-slug>/`, because subagents can't
see each other's context — only files survive the handoff.

| Stage | Agent | Model | Writes |
|---|---|---|---|
| Advise (conditional) | `relay-advisor` | fable | `00-advice.md` |
| Plan | `relay-planner` | opus | `01-plan.md` |
| Implement | `relay-implementer` | sonnet | `02-changes.md` + real edits |
| Review | `relay-reviewer` | opus | `03-review-N.md` |

## Running it

Whole relay, orchestrated for you:

```text
Run the relay for <task>. Gate the advisor first, then plan, implement, review.
Use slug <task-slug>.
```

One stage, pinned explicitly (the @-mention guarantees which agent runs; Claude
still writes the task prompt from your message):

```text
@"relay-planner (agent)" plan <task> in .claude/relay/<task-slug>/
```

Resume where you left off:

```text
Continue the relay in .claude/relay/<task-slug>/ — plan's done, run the implementer.
```

## The advisor gate

Skip the advisor unless the task hits at least one:

- More than one defensible approach with real tradeoffs
- Crosses module boundaries or changes a `:core:*` contract
- Concurrency or state correctness — Flow composition, single-flight refresh, cache coherence
- Expensive to reverse — persisted schema, public API, navigation topology
- Introduces a pattern the codebase doesn't have yet

Routine work goes straight to the planner. The planner escalates if it hits a wall.

## The review loop

`CHANGES_REQUESTED` sends the findings back to the implementer. **Max 2 rounds.**
After round 2, whatever's left comes to you — no round 3, and no downgrading
findings to force an `APPROVED`.

## Setup notes

- `.claude/agents/` is watched live; edits to the agent files land within seconds,
  no restart. **But** if `.claude/agents/` didn't exist when the session started,
  restart Claude Code once so the watcher picks it up.
- `CLAUDE_CODE_SUBAGENT_MODEL` overrides every agent's `model:` field. If all four
  stages seem to run on one model, that variable is why.
- The agents inherit `CLAUDE.md` automatically. Conventions live there, not in the
  agent files — keep it current and all four stages improve at once.
- `.claude/agents/` belongs in git. `.claude/relay/*/` is scratch; gitignore it.
