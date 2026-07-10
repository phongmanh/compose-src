# Proto DataStore Setup — Step-by-Step Guide

This guide walks through how **Proto DataStore** was added to this app to persist
`AuthModel` (the OAuth token) and `UserModel` (the logged-in user), replacing the
old Preferences DataStore for that data. It's written for someone who hasn't set
up Proto DataStore before.

If you just want to know *where things are*, jump to
[File map](#file-map-what-lives-where). If you want to understand *why* each
piece exists, read the steps in order.

## Why Proto DataStore instead of Preferences DataStore?

The app already had a Preferences DataStore (`datastore/AppDataStore.kt`) storing
loose, stringly-typed keys like `stringPreferencesKey("access_token")`. That
works, but:

- There's no schema — nothing stops you from writing a token where a boolean
  was expected, and every read needs a fallback value (`?: ""`).
- Nested/structured data (like "the user has a name, a role, *and* a token
  that expires at some time") gets awkward — you end up with a pile of flat
  keys instead of one coherent object.

**Proto DataStore** stores a strongly-typed object (generated from a
`.proto` schema) instead of loose keys. You get compile-time safety and a
single source of truth for what "the user's stored data" looks like.

## Step 1 — Add the Gradle plugin and dependencies

Two things had to be true before any `.proto` file could be compiled:

1. The **protobuf Gradle plugin** applied to `app/build.gradle.kts`.
2. The **DataStore** and **protobuf-kotlin-lite** libraries on the classpath.

Version catalog (`gradle/libs.versions.toml`):

```toml
[versions]
dataStore = "1.2.1"
protobuf = "0.10.0"
protobufKotlinLite = "4.32.1"

[libraries]
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "dataStore" }
datastore = { group = "androidx.datastore", name = "datastore", version.ref = "dataStore" }
protobuf-kotlin-lite = { group = "com.google.protobuf", name = "protobuf-kotlin-lite", version.ref = "protobufKotlinLite" }
protobuf-protoc = { group = "com.google.protobuf", name = "protoc", version.ref = "protobufKotlinLite" }

[plugins]
protobuf = { id = "com.google.protobuf", version.ref = "protobuf" }
```

`app/build.gradle.kts`:

```kotlin
plugins {
    // ...existing plugins...
    alias(libs.plugins.protobuf)
}

dependencies {
    // ...existing deps...
    implementation(libs.datastore.preferences) // used by the unrelated dark-mode setting
    implementation(libs.datastore)             // core DataStore (needed for Proto DataStore)
    implementation(libs.protobuf.kotlin.lite)  // generated proto message runtime
}

protobuf {
    protoc {
        // NOTE: must call .get() — libs.protobuf.protoc is a Gradle Provider,
        // and Provider.toString() does NOT resolve to "group:artifact:version".
        // Forgetting .get() produces a broken artifact coordinate at protoc-resolution time.
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") { option("lite") } // required base codegen for protobuf-kotlin-lite
                create("kotlin")                  // gives us the nice `.copy { }` / DSL builders
            }
        }
    }
}
```

**Gotcha we hit:** `libs.protobuf.protoc` from the version catalog is a
`Provider<MinimalExternalModuleDependency>`. Calling `.toString()` directly on
it (without `.get()`) doesn't throw — it silently produces a garbage string
like `map(valueof(DependencyValueSource))`, and protoc resolution fails with a
confusing "could not find" Maven error. Always call `.get()` first.

## Step 2 — Write the `.proto` schema

Proto files live under `app/src/main/proto/` (this is the default source dir
the protobuf Gradle plugin looks for — no config needed for the path itself).

**`app/src/main/proto/user_prefs.proto`:**

```proto
syntax = "proto3";

option java_package = "com.liam.compose.core.datastore";
option java_multiple_files = true;
option java_outer_classname = "UserPrefsProto";

// Mirrors com.liam.compose.features.auth.data.model.AuthModel
message AuthProto {
  string access_token = 1;
  int32 access_token_life_time = 2;
  // Epoch millis; 0 = unset (proto3 has no null).
  int64 access_token_expire_date = 3;
}

// Mirrors com.liam.compose.features.auth.data.model.UserModel
message UserProto {
  string user_name = 1;
  string full_name = 2;
  string role = 3;
}

// Root document persisted by the Proto DataStore (user_prefs.pb).
message UserPreferences {
  AuthProto auth = 1;
  UserProto user = 2;
}
```

A few things worth knowing if you're new to protobuf:

- **Field numbers** (`= 1`, `= 2`, ...) are part of the wire format, not just
  cosmetic — once shipped, don't reuse or renumber them. Add new fields with
  new numbers.
- **proto3 has no `null`.** Every scalar has a default: `""` for `string`, `0`
  for numeric types. "Not set" and "set to empty/zero" look the same on the
  wire — that's why the mapping code in Step 5 treats `""`/`0` as "absent".
- `java_multiple_files = true` + `java_outer_classname` gives us one file per
  message (`AuthProto.java`, `UserProto.java`, `UserPreferences.java`) instead
  of everything nested inside one big outer class.
- One root message (`UserPreferences`) wraps the others because Proto
  DataStore persists **one object per file** — you can't have two independent
  top-level messages in the same `.pb` file, so everything you want to store
  together needs to nest under a single root.

Running any Gradle task now generates, from this file:

- `AuthProto`, `UserProto`, `UserPreferences` (plus `*OrBuilder` interfaces) —
  the Java protobuf-lite message classes.
- `AuthProtoKt.kt`, `UserProtoKt.kt`, `UserPreferencesKt.kt` — Kotlin DSL
  wrappers giving you `authProto { ... }` builder syntax and `.copy { }` for
  immutable updates.

Generated into `app/build/generated/java/generateDebugProto/{java,kotlin}/...`
— **never hand-edit these**, they're regenerated on every build.

## Step 3 — Fix Kotlin/KSP visibility of the generated code (the tricky part)

This is the step most tutorials skip, and the one that actually broke the
build here. The protobuf Gradle plugin registers its output as an **AGP
Java-typed generated source directory**. Plain `javac`/the Android Java
compiler picks it up automatically — but:

- **Kotlin's compiler doesn't automatically look there**, so `UserPreferences`
  etc. aren't resolvable from Kotlin files.
- **KSP (which powers Hilt's annotation processing) doesn't either.** A Hilt
  `@Provides` method returning `DataStore<UserPreferences>` failed with
  `[ksp] ... 'UserPreferences' could not be resolved` — even though the class
  physically existed in `build/generated/...`.

The fix, in `app/build.gradle.kts`:

```kotlin
android {
    // ...

    // The protobuf plugin emits its Java + Kotlin output as an AGP *java*-typed
    // generated source dir, which Kotlin/KSP (Hilt) can't resolve. Expose the whole
    // generated tree (java/ + kotlin/) to the Kotlin source set of each variant.
    sourceSets {
        named("debug") {
            kotlin.srcDir("build/generated/java/generateDebugProto")
        }
        named("release") {
            kotlin.srcDir("build/generated/java/generateReleaseProto")
        }
    }
}

// Ensure proto classes are generated before Kotlin compilation / KSP annotation
// processing runs for each main variant (registered lazily so it applies whenever
// the ksp*/compile* tasks are created).
tasks.matching { it.name.matches(Regex("^(ksp|compile)(Debug|Release)Kotlin$")) }
    .configureEach {
        val variant = name.removePrefix("ksp").removePrefix("compile").removeSuffix("Kotlin")
        dependsOn("generate${variant}Proto")
    }
```

Two separate problems, two separate fixes:

1. **`kotlin.srcDir(...)`** tells the Kotlin compiler "also treat this
   directory as Kotlin/Java sources." We had to use a **plain path string**
   (`"build/generated/java/generateDebugProto"`), not a Gradle `Provider` /
   `layout.buildDirectory.dir(...)` — AGP 9's source set API explicitly
   rejects `Provider` instances here with *"You cannot add Provider instances
   to the Android SourceSet API."*
2. **`tasks.matching { }.configureEach { dependsOn(...) }`** forces Gradle to
   run `generateDebugProto`/`generateReleaseProto` *before*
   `kspDebugKotlin`/`compileDebugKotlin` (and the release equivalents). Without
   this, Gradle might run KSP before the proto sources exist for that build,
   or run it against a stale copy. It's written with `tasks.matching` (a lazy,
   name-pattern match) rather than `tasks.named("kspDebugKotlin")` because at
   the point this block evaluates, those tasks haven't been created by the
   Kotlin plugin yet — matching against future task names avoids "task not
   found" errors.

If you ever see `could not be resolved` for a generated proto type, or a KSP
error that a type is missing, this step is almost always why — check that
both the `srcDir` and the `dependsOn` wiring are in place for the variant
you're building.

## Step 4 — Write the `Serializer`

Proto DataStore needs a `Serializer<T>` telling it how to read/write your
message type to/from bytes on disk.

**`datastore/UserPreferencesSerializer.kt`:**

```kotlin
object UserPreferencesSerializer : Serializer<UserPreferences> {

    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserPreferences =
        try {
            UserPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read UserPreferences proto.", exception)
        }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) = t.writeTo(output)
}
```

- `defaultValue` is what you get on first-ever launch (no file on disk yet) —
  protobuf gives you `getDefaultInstance()` for free, so every field is at its
  proto3 default.
- Wrapping parse failures in `CorruptionException` is a DataStore convention:
  it signals "the file on disk is unreadable," and DataStore will fall back to
  `defaultValue` instead of crashing the app.

## Step 5 — Write the repository (proto ↔ domain model mapping)

The rest of the app should never see `AuthProto`/`UserProto` directly — it
should keep using the existing `AuthModel`/`UserModel` domain classes. That
mapping (and all DataStore access) is centralized in one repository.

**`datastore/UserPreferencesRepository.kt`** (trimmed):

```kotlin
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) {
    val authModel: Flow<AuthModel> = dataStore.data.map { it.auth.toAuthModel() }
    val userModel: Flow<UserModel> = dataStore.data.map { it.user.toUserModel() }

    suspend fun accessToken(): String = dataStore.data.first().auth.accessToken

    suspend fun saveAuth(authModel: AuthModel) {
        dataStore.updateData { prefs -> prefs.copy { auth = authModel.toProto() } }
    }

    suspend fun saveUser(userModel: UserModel) {
        dataStore.updateData { prefs -> prefs.copy { user = userModel.toProto() } }
    }

    suspend fun clear() {
        dataStore.updateData { UserPreferences.getDefaultInstance() }
    }
}

// proto3 has no null: "" / 0 means "unset" on the way out of proto,
// and going back in, null domain values become "" / 0.
private fun AuthProto.toAuthModel(): AuthModel = AuthModel(
    accessToken = accessToken.ifEmpty { null },
    accessTokenLifeTime = accessTokenLifeTime.takeIf { it != 0 },
    accessTokenExpireDate = accessTokenExpireDate.takeIf { it != 0L }?.let(::Date)
)

private fun AuthModel.toProto(): AuthProto = authProto {
    accessToken = this@toProto.accessToken.orEmpty()
    accessTokenLifeTime = this@toProto.accessTokenLifeTime ?: 0
    accessTokenExpireDate = this@toProto.accessTokenExpireDate?.time ?: 0L
}
// (UserProto <-> UserModel mapping follows the same pattern)
```

Key ideas:

- **`dataStore.data`** is a `Flow<UserPreferences>` — it emits the current
  value immediately, then again every time it changes. We `.map` it into
  domain-model flows so ViewModels never import proto types.
- **`updateData { }`** is how you *write*. It's transactional — you receive
  the current value and return the new one, and DataStore handles
  concurrent-write safety for you. `prefs.copy { auth = ... }` uses the
  generated Kotlin DSL (`UserPreferencesKt.Dsl`) so you never hand-build a
  `Builder`.
- **`accessToken()`** is a one-shot suspend read (`.data.first()`) for
  callers that just need the current value once (see Step 7 — the OkHttp
  interceptor) rather than observing a `Flow`.
- proto3's "no null" rule means the mapping functions are the only place that
  translates between "empty string/zero" (proto) and "actually absent"
  (domain nullable). Keep that translation *only* here — don't leak `""` vs
  `null` ambiguity into ViewModels.

## Step 6 — Leave a `migrations` extension point for the future

There's no real legacy data to carry over here — the old Preferences-based
token (`datastore/AppDataStore.kt`'s `userDataStore` / `ACCESS_TOKEN`) was
never actually read by anything besides this setup, so there's nothing worth
migrating *from*. Rather than write a migration that copies data nobody has,
`DataStoreFactory.create` is given an explicit empty list — with a comment
marking it as the spot to add a real one later:

```kotlin
migrations = emptyList<DataMigration<UserPreferences>>(),
```

Why keep this instead of just omitting the parameter (it defaults to empty
anyway)? Because **this is the one deliberate extension point for future
schema changes**, and it's worth being explicit about where it lives:

`DataMigration<T>` is DataStore's mechanism for one-time, run-once-then-forget
upgrades — not just Preferences→Proto, but *any* future breaking change to
`UserPreferences` itself (e.g. splitting the file, renumbering/removing a
field, merging in data from some other store). It has three lifecycle
methods, called automatically by DataStore the first time it opens the file:

1. **`shouldMigrate(currentData)`** — cheap check: "is there something to
   bring over, and have we not already done it?" Return `false` once it's
   done so this never re-runs.
2. **`migrate(currentData)`** — do the actual transform, returning the new
   value. Runs inside the same transactional machinery as `updateData`.
3. **`cleanUp()`** — runs after `migrate` succeeds; delete/clear whatever the
   old source was so `shouldMigrate` stays cheap and `false` afterward.

**When you'll actually need one:** most schema growth (adding a field) needs
*no* migration at all — proto3's wire format already handles that, see
[Quick checklist](#quick-checklist-for-adding-a-new-field-later). Reach for a
real `DataMigration<UserPreferences>` only for something that isn't
backward-compatible on its own (e.g. importing data from a different store,
or reshaping the schema in a way old bytes can't parse into on their own).

## Step 7 — Provide the `DataStore<UserPreferences>` via Hilt

**`di/DataStoreModule.kt`:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<UserPreferences> = DataStoreFactory.create(
        serializer = UserPreferencesSerializer,
        // No legacy data to migrate today — this is the extension point for a future
        // breaking change to the UserPreferences schema (see Step 6).
        migrations = emptyList<DataMigration<UserPreferences>>(),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.dataStoreFile("user_prefs.pb") }
    )
}
```

- `DataStoreFactory.create` (not the `by dataStore(...)` Kotlin property
  delegate you might see in simpler tutorials) is used here because we need to
  pass `migrations` — the delegate form doesn't expose that parameter as
  conveniently for Proto DataStore.
- `@Singleton` is essential: DataStore must be a single instance for a given
  file across the whole app, or you risk multiple in-memory copies going out
  of sync / file-lock contention.
- `context.dataStoreFile("user_prefs.pb")` — DataStore's own helper for
  "put this file in the app's standard DataStore directory." `.pb` is just a
  conventional extension for a serialized protobuf binary; DataStore doesn't
  care what you name it.
- The `CoroutineScope(Dispatchers.IO + SupervisorJob())` is the scope
  DataStore uses internally for its read/write operations — `SupervisorJob`
  so one failed operation doesn't cancel the whole DataStore.

Because this is a Hilt `@Provides` method, any `@Inject` constructor
anywhere in the app (ViewModel, repository, interceptor, ...) can now just ask
for `UserPreferencesRepository` and get a correctly-wired singleton — no
manual wiring at call sites.

## Step 8 — Use it from the rest of the app

Three call sites currently consume `UserPreferencesRepository`:

**a) Save the auth token after the startup token fetch**
(`entry/MainViewModel.kt`):

```kotlin
class MainViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getToken(AuthPostRequest(...))
            response.getOrNull()?.data?.let { userPreferencesRepository.saveAuth(it) }
            // ...
        }
    }
}
```

**b) Save the user on login / clear everything on logout**
(`features/auth/ui/viewmodel/AuthViewModel.kt`):

```kotlin
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    fun login(username: String, password: String) {
        viewModelScope.launch {
            authRepository.auth(...)
                .onSuccess { data ->
                    _currentUser = parseUserModel(data)
                    _currentUser?.let { userPreferencesRepository.saveUser(it) }
                    // ...
                }
        }
    }

    fun logout() {
        // ...
        viewModelScope.launch { userPreferencesRepository.clear() }
    }
}
```

**c) Read the token for outgoing network requests**
(`data/remote/TokenInterceptor.kt`):

```kotlin
class TokenInterceptor @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getToken()
        val requestWithToken = if (token.isNotEmpty()) {
            chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            chain.request().newBuilder().header("gm-gateway-key", getGatewayKey()).build()
        }
        return chain.proceed(requestWithToken)
    }

    // OkHttp interceptors are synchronous, so bridge the suspend DataStore read.
    // DataStore serves subsequent reads from its in-memory cache, so this is cheap.
    private fun getToken(): String = runBlocking { userPreferencesRepository.accessToken() }
}
```

**Why `runBlocking` here specifically:** OkHttp's `Interceptor.intercept` is a
plain synchronous function — it's called from OkHttp's own dispatcher thread,
not a coroutine. There's no `suspend` version of this API to hook into, so
bridging with `runBlocking` is the standard, accepted pattern for this exact
situation. It's safe here because:

- It never runs on the main thread (OkHttp calls interceptors from its
  background dispatcher).
- After the very first read, DataStore serves `.data` from an in-memory cache
  (no disk I/O), so the blocked time per request is negligible.

If this ever needs to become fully non-blocking, the alternative is to keep a
`@Volatile` in-memory copy of the token updated by collecting `authModel` once
at app startup, and have the interceptor read that field directly. Worth
revisiting if profiling ever shows this as a bottleneck — not needed today.

## File map — what lives where

```
app/src/main/proto/user_prefs.proto                                  ← schema (Step 2)

app/src/main/java/com/liam/compose/
├── datastore/
│   ├── UserPreferencesSerializer.kt   ← Serializer<UserPreferences>   (Step 4)
│   ├── UserPreferencesRepository.kt   ← proto <-> domain model, r/w   (Step 5)
│   └── AppDataStore.kt                ← unrelated dark-mode Preferences store
├── di/
│   └── DataStoreModule.kt             ← Hilt @Provides DataStore<UserPreferences> (Step 7)
├── entry/
│   └── MainViewModel.kt               ← saveAuth() after startup token fetch (Step 8a)
├── features/auth/ui/viewmodel/
│   └── AuthViewModel.kt               ← saveUser() on login, clear() on logout (Step 8b)
└── data/remote/
    └── TokenInterceptor.kt            ← accessToken() read for outgoing requests (Step 8c)

app/build.gradle.kts                   ← plugin, deps, protoc artifact fix,
                                          Kotlin/KSP source-set + task wiring (Steps 1 & 3)
gradle/libs.versions.toml              ← version catalog entries (Step 1)
```

## Quick checklist for adding a new field later

1. Add the field to `user_prefs.proto` with a **new, unused field number**.
2. Rebuild (`./gradlew :app:assembleDebug`) — new getters/setters appear on
   the generated proto classes automatically.
3. Update the mapper functions in `UserPreferencesRepository.kt` (both
   directions) to read/write the new field.
4. If the field replaces something in the *domain* model that's already
   nullable, remember proto3's rule: empty/zero on the wire = "unset" — no
   separate migration is needed for additive fields, since old files simply
   read back with the new field at its default.
