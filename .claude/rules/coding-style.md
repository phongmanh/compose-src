# Mobile Development Guidelines

Common rules and conventions for all mobile projects (Java / Kotlin).

## 1. Core Principles

- **SOLID**
    - **S** — Single Responsibility: one class, one reason to change (e.g., ViewModel handles UI
      state, Repository handles data).
    - **O** — Open/Closed: extend behavior via interfaces/abstraction, don't modify existing code.
    - **L** — Liskov Substitution: subclasses must be usable wherever the parent is expected.
    - **I** — Interface Segregation: prefer small, focused interfaces over fat ones.
    - **D** — Dependency Inversion: depend on abstractions; inject dependencies (Hilt/Koin/Dagger).
- **DRY** — Don't Repeat Yourself: extract shared logic into utils, extensions, or base classes. But
  avoid premature abstraction.
- **KISS** — Keep It Simple: the simplest solution that works wins.
- **YAGNI** — You Aren't Gonna Need It: don't build for imaginary future requirements.
- **Separation of Concerns** — UI, business logic, and data layers must not leak into each other.

## 2. Architecture

- Use a clear layered architecture: **Presentation → Domain → Data** (Clean Architecture or
  MVVM/MVI).
- UI layer is dumb: render state, forward events. No business logic in
  Activities/Fragments/Composables.
- Single source of truth for state; unidirectional data flow (UDF).
- Repository pattern for data access; abstract remote/local sources behind it.
- Use Dependency Injection everywhere — no manual instantiation of dependencies.

## 3. Kotlin / Java Conventions

- Prefer Kotlin for new code; use `val` over `var`, immutability by default.
- Null safety: avoid `!!`; use `?.`, `?:`, `requireNotNull` with a message.
- Use `data class` for models, `sealed class`/`sealed interface` for state and events.
- Coroutines + Flow for async work; never block the main thread.
- Keep functions small (< 30 lines as a guideline); one function, one job.
- No magic numbers/strings — extract to constants or resources.
- Business logic (ViewModels, UseCases, Repositories) must be unit-testable — this is a design
  constraint.

# 4. Naming Conventions

- Classes/Objects/Interfaces: `PascalCase`
- Functions/variables: `camelCase`
- Constants (`const val`, top-level): `SCREAMING_SNAKE_CASE`
- Composables: `PascalCase`, noun phrases (`ProfileScreen`, not `ShowProfile`)
- UseCases: verb phrase + `UseCase` suffix (`GetUserProfileUseCase`)
- Repositories: noun + `Repository` suffix, interface has no prefix, impl has `Impl` suffix
  (`UserRepository` / `UserRepositoryImpl`)
- Test files: `<ClassUnderTest>Test.kt`
- Resource IDs: `<screen>_<component>_<type>` (e.g. `login_email_input`)

## 5. UI & Resources

- All user-facing strings in `strings.xml` (localization-ready). No hardcoded text.
- Dimensions, colors in resources or design system tokens; support dark mode.
- Handle configuration changes properly — state survives rotation (ViewModel, SavedStateHandle).
- Design for all screen sizes; avoid fixed dimensions where flexible layouts work.
- Design consistently with Material Design / platform guidelines; use system components where
  possible.
- Create common components to reuse as much as possible (buttons, text fields, cards, etc.).
- Modern UI/UX patterns: Compose, MotionLayout, ConstraintLayout, etc. Avoid deprecated APIs.

## 6. Error Handling & State

- Model UI state explicitly: `Loading / Success / Error` (sealed classes).
- Never swallow exceptions silently; log and surface user-friendly messages.
- Handle offline/poor network gracefully — timeouts, retries, cached fallbacks.

## 6. Performance

- No heavy work on the main thread (I/O, parsing, DB).
- Avoid memory leaks: no Activity/Context references in long-lived objects; cancel coroutines with
  lifecycle scopes.
- Lazy-load and paginate lists; use `DiffUtil` / stable keys in Compose.
- Optimize images (proper sizing, caching via Glide/Coil).

## 8. Code Quality & Process

- Enforce static analysis: ktlint/detekt (Kotlin), Lint. Fix warnings, don't suppress blindly.
- Small, focused commits and PRs; meaningful commit messages.
- Code review required before merge; no direct pushes to main.
- Boy Scout Rule: leave the code cleaner than you found it.

