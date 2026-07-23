# Codex Project Instructions

This repository also maintains CodeBuddy workspace rules under `.codebuddy/rules/`.
Codex must treat those rules as project instructions and load the relevant rule
files before making changes.

## Rule Loading

- For any task that involves locating modules, page entries, Service/Factory/
  Registry wiring, startup registration, or cross-module implementation mapping,
  read `.codebuddy/rules/项目导航/RULE.mdc` first.
- For any task that involves default page architecture, View, ViewModel,
  Repository, Service factory, new page work, cross-module changes, or refactors,
  read `.codebuddy/rules/通用架构规范/RULE.mdc` before editing.
- For tasks involving `PageWidget`, `DataRepo`, `StructComposePage`, multi-tab,
  sticky headers, floating layers, or other Struct page capabilities, read
  `.codebuddy/rules/Struct结构规范/RULE.mdc` before editing.
- For tasks involving Compose UI, Kuikly Compose DSL, component usage, theme,
  accessibility, or UI implementation, read `.codebuddy/rules/UI规范/RULE.mdc`
  before editing.
- If a task matches multiple categories, read all matching rule files and follow
  the most specific rule for the touched code path.

## Architecture Defaults

- Default non-Struct page work follows MVVM as defined in
  `.codebuddy/rules/通用架构规范/RULE.mdc`.
- Keep dependency direction aligned with the existing module boundaries:
  contract layer (`wsCore`) -> logic modules (`wsDrama`, `wsFeeds`, `wsUser`,
  etc.) -> UI layer (`wsCompose` / consuming UI modules). Do not introduce
  reverse dependencies.
- UI must consume VM interfaces and UI-facing primitive/state properties. Do not
  expose DTO/PB/business models, controllers, mutable flows, sealed UIState, or
  action-dispatch patterns through VM interfaces unless an existing local pattern
  requires it and the rule file permits it.
- Prefer the repository's current successful pattern in the same domain over new
  abstractions. Avoid adding empty Repository/UseCase layers without concrete
  value.

## Verification

- Before claiming a change is complete, make sure the files are actually written
  and run the most relevant compile, test, or static check that is practical for
  the scope.
- If a referenced CodeBuddy rule points to a missing file, continue with the
  available rule content and mention the missing reference in the final response
  when it affects the task.
