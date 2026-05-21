# ⚠️ Build Cache Issue - Solution

## The Problem

You're seeing errors in the **generated** `PlaybackSessionDao_Impl.java` file. This file is automatically created by Room's annotation processor and should not be edited directly.

The generated code shows **old/stale method names** like:
- `deleteSessionSync()` 
- `clearActiveFlagsSync()`

But your source DAO (`PlaybackSessionDao.kt`) only has:
- `delete()`
- `clearActiveFlagsExcept()`

**This means the generated code is out of sync with the source.**

## Why This Happens

Gradle caches generated files. When you make changes to the source code, sometimes the generated files aren't properly regenerated.

## The Solution

### Option 1: Clean Build (BEST)

Delete the entire build directory and rebuild:

```bash
cd /home/keen-alise/AndroidStudioProjects/Musicc

# Delete build cache
rm -rf app/build

# Clean rebuild
./gradlew clean :app:assembleDebug
```

This will:
1. ✅ Delete all cached generated files
2. ✅ Force Room to regenerate everything from your source DAO
3. ✅ Regenerate with correct method names
4. ✅ Build succeeds

### Option 2: If Option 1 Doesn't Work

If you still see errors after clean build, manually delete these folders and rebuild:

```bash
rm -rf ~/.gradle/caches
rm -rf app/build
./gradlew clean :app:assembleDebug
```

### Option 3: Android Studio GUI

1. Go to **Build** → **Clean Project**
2. Go to **Build** → **Rebuild Project**

## What's Currently Wrong

**Source Code** (Correct ✅)
```kotlin
@Query("DELETE FROM playback_sessions WHERE id = :sessionId")
suspend fun delete(sessionId: Long)
```

[//]: # (**Generated Code** &#40;Wrong ❌&#41;)

[//]: # (```java)

[//]: # (public void deleteSessionSync&#40;final long sessionId&#41; { ... })

[//]: # (```)

The mismatch causes compilation errors because the interface contract doesn't match the implementation.

## Why Rebuild Fixes It

When you rebuild:
1. Room re-reads your `PlaybackSessionDao.kt` source
2. Room sees: `delete()`, `clearActiveFlagsExcept()`, `insert()`, `update()`
3. Room regenerates `PlaybackSessionDao_Impl.java` with matching method signatures
4. Generated code matches your DAO interface
5. ✅ Build succeeds

## Files to Check

Your source DAO files are **correct**:

- ✅ `/app/src/main/java/com/example/musicc/data/room/PlaybackSessionDao.kt`
- ✅ `/app/src/main/java/com/example/musicc/data/room/QueueItemDao.kt`

The generated files are **stale/cached** (will be fixed on rebuild):

- ❌ `/app/build/generated/source/kapt/debug/com/example/musicc/data/room/PlaybackSessionDao_Impl.java`

## Step-by-Step Fix

```bash
# 1. Navigate to project
cd /home/keen-alise/AndroidStudioProjects/Musicc

# 2. Remove build cache completely
rm -rf app/build

# 3. Clean and rebuild
./gradlew clean :app:assembleDebug

# Expected output:
# BUILD SUCCESSFUL ✅
```

## After Rebuild

The generated `PlaybackSessionDao_Impl.java` will have correct methods:

```java
@Override
public java.lang.Object insert(@org.jetbrains.annotations.NotNull() 
    com.example.musicc.data.room.PlaybackSessionEntity session,
    @org.jetbrains.annotations.NotNull() 
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
    // Correct implementation
}

@Override
public java.lang.Object delete(final long sessionId,
    @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
    // Correct implementation
}
```

## Do NOT Edit Generated Files

**Important:** Never edit files in `/app/build/generated/`. They're auto-generated and will be overwritten on rebuild.

## Verify Fix

After rebuild, check:
1. ✅ No compilation errors
2. ✅ `PlaybackSessionDao_Impl.java` methods match your interface
3. ✅ `build` folder has updated generated files
4. ✅ App builds successfully

---

**TL;DR:** Run `./gradlew clean :app:assembleDebug` to fix stale generated code. 🎉

