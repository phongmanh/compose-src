# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app built with Jetpack Compose, MVVM, and Hilt. It talks to the GAMAN "ichi" backend. `applicationId`/`namespace` is `com.liam.compose`.

Multi-module Gradle build:
- `:app` — application module; app bootstrap, bottom-nav shell, cross-module DI wiring.
- `:core:navigation` — reusable, app-agnostic Navigation 3 machinery.
- `:core:networking` — Retrofit/OkHttp stack, interceptors, error mapping, repository base + result types.
- `:core:datastore` — Proto DataStore persistence (session/token/user prefs).
- `:core:designsystem` — Compose theme (`JetpackComposeTheme`) + Material 3, `api`-exposed to consumers.
- `:feature:auth` — login, change-password, gateway client-credentials token.
- `:feature:home` — home screen.
- `:feature:settings` — settings screen (placeholder-ish).

Plus a `build-logic` composite build that supplies the shared convention plugins (see below).

Toolchain: Gradle 9.4.1, JDK 21 (via Foojay toolchain), AGP 9.2.1, Kotlin 2.2.10, Compose BOM 2025.12.00. `compileSdk 37`, `minSdk 24`, `targetSdk 36`. Java 11 source/target. Configuration cache is enabled.

## Commands

```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build + install on connected device/emulator
./gradlew test                   # all JVM unit tests (src/test)
./gradlew testDebugUnitTest      # unit tests for the debug variant
./gradlew connectedAndroidTest   # instrumented tests (src/androidTest, needs a device)
./gradlew lint                   # Android lint

# scope a task to one module
./gradlew :core:datastore:testDebugUnitTest
./gradlew :app:assembleDebug

# single unit test class / method
./gradlew testDebugUnitTest --tests "com.liam.compose.ExampleUnitTest"
./gradlew testDebugUnitTest --tests "com.liam.compose.ExampleUnitTest.addition_isCorrect"
```

Dependencies are managed through the version catalog at `gradle/libs.versions.toml` — add/upgrade libraries there, not inline in a module's `build.gradle.kts`.

## Architecture

### Build logic — convention plugins (`build-logic`)
Shared Gradle setup lives in the `build-logic` composite build and is applied by id, never duplicated inline:
- `jetpackcompose.android.application` / `jetpackcompose.android.library` — Android baseline (compileSdk 37, minSdk 24, Java 11, test deps).
- `jetpackcompose.android.compose` — applies the Compose compiler **and** kotlinx.serialization plugins, turns on `buildFeatures.compose`, and wires the Compose BOM + baseline UI/tooling deps. **Every Compose module needs this**; every module here also uses `@Serializable` nav keys, which is why serialization is bundled in. (Missing the compose plugin → obscure Kotlin internal compiler error; missing serialization → runtime `SerializationException` when the nav back stack is saved.)
- `jetpackcompose.android.hilt` — Hilt + hilt-compiler (KSP).

`COMPILE_SDK`/`MIN_SDK`/`JAVA_VERSION` and the `libs` catalog accessor live in `build-logic/.../ProjectExtensions.kt`. Copy an existing module's `plugins { }` block as the template for a new one.

### Module dependency graph
- `:core:networking`, `:core:navigation`, `:core:designsystem` — leaf modules, no internal deps.
- `:feature:auth` → `:core:networking`, `:core:navigation`.
- `:core:datastore` → `:feature:auth`, `:core:networking` — note this core module depends on a **feature** module (it needs the auth session/user types and fulfils the auth + networking token contracts). Not a strict layering; keep the arrow in mind before adding deps.
- `:feature:home` / `:feature:settings` → `:core:{networking,navigation,datastore}` + `:feature:auth`.
- `:app` → every core + feature module.

**Package naming mirrors the module path — follow it for new modules and files**: feature modules use `com.liam.compose.features.<name>` (`features.auth`, `features.home`, `features.settings`), core modules use `com.liam.compose.core.<name>` (`core.navigation`, `core.designsystem`, `core.networking`, `core.datastore`), and `:app` is `com.liam.compose` (bootstrap under `com.liam.compose.entry`). Each module's `namespace` in its `build.gradle.kts` matches its package (and `:core:datastore`'s proto `java_package` matches too).

### Module responsibilities
- **`:app`** (`com.liam.compose`): `entry/` holds `Application` (`@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`), `MainViewModel`, bootstrap state (`AuthState`, `GatewayAuthState`), `AppNavGraph.kt` (the `ReportKey` + `getAppEntries` aggregator), `BottomBar.kt` (`BottomTab` + `AppBottomBar`). Owns `res/xml/network_security_config.xml` and the bundled CA at `res/raw/gamanjsc_ca.pem`. Also owns the cross-module Hilt bindings (via `:core:datastore`, see DI).
- **`:core:navigation`** (`com.liam.compose.core.navigation`): reusable Nav 3 machinery, no feature/DI deps. `api`-exposes the navigation3 libs so consumers get `NavKey`/`NavBackStack` transitively.
- **`:core:networking`** (`com.liam.compose.core.networking`): `NetworkModule` (Retrofit x2, see DI), `TokenInterceptor`, `ITokenProvider`, `ErrorInterceptor`/`ErrorMapper`/`ApiException`, `BaseRepository` + `RepositoryHelper`, and the result/response models. Injects `BuildConfig.GATEWAY_KEY` from `secrets.properties`.
- **`:core:datastore`** (`com.liam.compose.core.datastore`): Proto DataStore (`user_prefs.proto` → `UserPreferences`). `UserPreferencesRepository` is the single impl of **both** `IAuthSessionStore` (auth) and `ITokenProvider` (networking); `AuthBindingsModule` binds them.
- **`:core:designsystem`** (`com.liam.compose.core.designsystem`): `theme/` (`Color`, `Type`, `Theme` → `JetpackComposeTheme`). `api`-exposes `androidx.compose.material3` so consumers get `MaterialTheme`/`colorScheme` transitively. Applied at the app root by `MainActivity`; feature modules can depend on it when they need brand tokens.
- **`:feature:auth`** (`com.liam.compose.features.auth`): `data/{model,remote,repository}`, `gateway/` (`GatewayTokenManager`, `GatewayClientCredentials`), `di/AuthModule`, `navigation/` (`AuthKey`, `authEntries`), `ui/{screens,state,viewmodel}`. Declares the `IAuthSessionStore` contract fulfilled by datastore.
- **`:feature:home`** / **`:feature:settings`**: `navigation/` (`HomeKey`/`SettingKey`, `homeEntries`/`settingsEntries`) + `ui/`. Follow the `data/{model,remote,repository}` + `ui/{screens,state,viewmodel}` layering when a feature grows real data.

### Navigation — Navigation 3 (not classic Jetpack Navigation)
This app uses **androidx.navigation3** (`NavBackStack`, `NavKey`, `NavDisplay`, `entryProvider`). Do not reach for `NavController`/`NavHost`. Nav destinations are `@Serializable sealed interface : NavKey` with `data object`/`data class` keys.

**Each feature owns its own nav keys and entries** in its `navigation/` package: `AuthKey`/`authEntries`, `HomeKey`/`homeEntries`, `SettingKey`/`settingsEntries`. `:app` defines only `ReportKey` (placeholder tab). `entry/AppNavGraph.getAppEntries(backStack)` aggregates all of them into one `EntryProviderScope`.

Generic pieces live in **`:core:navigation`**:
- `AppNavigation(modifier, backStack, entries)` wraps `NavDisplay` with two decorators, in order: saveable-state-holder (so `rememberSaveable` in screens survives process death) then viewmodel-store (scopes ViewModels to the nav entry, not the Activity). It also publishes the active back stack via `LocalNavBackStack` so screens can navigate without threading it through signatures.
- `TabbedNavigator` / `rememberTabbedNavigator(...)` owns one back stack per bottom-nav tab; `select(tab)` switches tabs, or pops the current tab's stack to root when the active tab is re-selected. `NavExtensions.kt` has `navigate()`/`pop()`/`popToRoot()`.

`MainActivity` builds a `rememberTabbedNavigator` over `BottomTab.entries` and hands `navigator.currentBackStack` to `AppNavigation`. Report is a placeholder screen until a real one exists.

### DI — Hilt, single network module, cross-module bindings
`NetworkModule` (in `:core:networking`) is now a **single** module (the old debug/release split that trusted all SSL certs is gone — there are no build-variant source sets). It provides two Retrofit instances via the `@AuthRetrofit` qualifier: default = main API (`gm-platform-ichi-api.gamanjsc.com/ichi/api/v1.0/`), `@AuthRetrofit` = OAuth/token service (`oauth.gamanjsc.com/gm/api/v1.0/`). Retrofit **services** are provided per feature (`:feature:auth`'s `AuthModule` creates `AuthService` on the default Retrofit and `TokenService` on the `@AuthRetrofit` one).

Contracts are bound across modules by `AuthBindingsModule` (in `:core:datastore`): `UserPreferencesRepository` supplies both `IAuthSessionStore` (auth's persistence contract) and `ITokenProvider` (networking's token contract). `App` is `@HiltAndroidApp`; `MainActivity` is `@AndroidEntryPoint`; ViewModels are `@HiltViewModel`.

### Networking & error handling
Retrofit + OkHttp + Gson. `TokenInterceptor` reads the persisted access token via `ITokenProvider.accessToken()` (DataStore-backed, bridged with `runBlocking` since OkHttp is synchronous). If a token exists it adds `Authorization: Bearer <token>`; **otherwise it falls back to a `gm-gateway-key: <BuildConfig.GATEWAY_KEY>` header** (key injected from `secrets.properties`, Guideline §9). `ErrorMapper` converts any `Throwable` into the typed `ApiException` sealed hierarchy, each case exposing a user-facing `userMessage`. `:app`'s `network_security_config.xml` adds the bundled `res/raw/gamanjsc_ca.pem` CA chain alongside the system trust store for `*.gamanjsc.com`.

### Repositories & result types — note the duplication
Repositories extend `BaseRepository` (`:core:networking`), which offers `safeApiCall*` helpers. Two parallel result models coexist and you must pick one deliberately:
- `kotlin.Result<T>` via `safeApiCall` / `safeApiCallWithErrorBody` (used by `AuthRepository`).
- custom `AppResult<T>` (Loading/Success/Error) via `safeApiCall1` / `safeApiCallWithErrorBody1`, or the `apiCallFlow`/`apiCall` Flow builders in `RepositoryHelper.kt`.

Similarly there are two response envelopes: `ApiResponse<T>` and `AppResponse<T>`. And `GatewayAuthState` is defined **twice** with different shapes — `entry/GatewayAuthState` (app bootstrap: Authenticated/Unauthenticated/Failed) vs `features/auth/ui/state/GatewayAuthState` (login lifecycle: Loading/Success/Unauthorized/Error). There's also `entry/AuthState` (Loading/Authenticated/Unauthenticated). Check the import before using one.

### Startup flow
`MainActivity` installs a splash screen. `MainViewModel` injects `UserPreferencesRepository` (`:core:datastore`) and `GatewayTokenManager` (`:feature:auth`). Its `uiState: StateFlow<AuthState>` is derived from the persisted `userModel` (Authenticated once a `userName` is stored, else Unauthenticated, initial Loading). In `init` it ensures a gateway client-credentials token exists: if the DataStore has none, `GatewayTokenManager.ensureGatewayToken()` calls `AuthRepository.getToken(...)` with `GatewayClientCredentials` and persists the result, driving `gatewayAuthState` (Authenticated/Failed).


## Instruction Index
- Code style & conventions [GUIDE](CODE_GUIDELINES.md)
