# 🔧 Troubleshooting Guide - Sessions Feature

## Build Issues

### Issue: "Cannot find symbol PlaybackSessionEntity"
**Solution:**
1. Clean build: `./gradlew clean`
2. Ensure Room dependency is in build.gradle.kts
3. Verify kapt plugin is present: `id("kotlin-kapt")`
4. Run: `./gradlew :app:kaptDebugKotlin` to regenerate code

### Issue: "Kapt currently doesn't support language version 2.0+"
**This is a WARNING, not an error. Safe to ignore.**
- Falls back to Kotlin 1.9 automatically
- Doesn't affect functionality

### Issue: "Cannot find setter for field"
**Solution:**
- Ensure all PlaybackSessionEntity fields use `var` (not `val`)
- Check that Kotlin data classes are mutable
- Verify no custom getters/setters are interfering

### Issue: "Not sure how to handle query method's return type"
**Solution:**
- Check DAO methods have proper return types:
  - `insert()` → `Long`
  - `update()` → `Unit` (suspend)
  - `delete()` → `Unit` (suspend)
- Add `suspend` keyword to write operations

---

## Runtime Issues

### Issue: "App crashes on Sessions tab"
**Steps to debug:**
1. Check Logcat for full stack trace
2. Look for these common errors:
   - NullPointerException on sessionViewModel
   - IllegalStateException from Flow operations
   - SQLiteException from database

**Solutions:**
```kotlin
// Ensure ViewModel is instantiated:
val sessionViewModel: SessionManagementViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

// Verify database initialization:
val db = AppDatabase.getDatabase(context)

// Check Flow operations have proper scope:
viewModelScope.launch { /* ... */ }
```

### Issue: "Can't create sessions"
**Checklist:**
- [ ] Is insert() marked as suspend?
- [ ] Is viewModel.createNewSession() being called?
- [ ] Is the database being initialized?
- [ ] Are there any database constraint violations?

**Debug steps:**
```kotlin
// Add logging in ViewModel:
try {
    db.playbackSessionDao().insert(newSession)
    Log.d("SessionVM", "Session inserted successfully")
} catch (e: Exception) {
    Log.e("SessionVM", "Insert failed: ${e.message}", e)
    _errorMessage.value = e.message ?: "Unknown error"
}
```

### Issue: "Sessions aren't showing up in list"
**Verify:**
- [ ] observeAll() Flow is being collected
- [ ] _allSessions StateFlow is being updated
- [ ] SessionsScreen is observing allSessions.collectAsStateWithLifecycle()

**Fix:**
```kotlin
// In init block, ensure this is running:
viewModelScope.launch {
    db.playbackSessionDao().observeAll().collect { sessions ->
        _allSessions.value = sessions
        Log.d("SessionVM", "Updated sessions: ${sessions.size}")
    }
}
```

### Issue: "Can't switch between sessions"
**Debug:**
1. Verify switchToSession() is called with correct sessionId
2. Check if activeSession StateFlow is updating
3. Ensure clearActiveFlagsExcept() completes

**Test code:**
```kotlin
// In SessionsScreen click handler:
Log.d("Sessions", "Switching to session: $sessionId")
sessionViewModel.switchToSession(
    sessionId = sessionId,
    currentSongs = allSongs,
    currentPosition = viewModel.getCurrentPosition(),
    playbackState = if (isPlaying) 2 else 0
)
```

---

## Database Issues

### Issue: "Database locked" error
**Solution:**
- Ensure only one database instance (singleton)
- Don't open database on main thread
- Use suspend functions for all DB operations

### Issue: "Foreign key constraint failed"
**This means:**
- QueueItemEntity.sessionId doesn't match PlaybackSessionEntity.id
- Solution: Ensure sessionId is valid before inserting queue items

### Issue: "UNIQUE constraint failed"
**This means:**
- Duplicate values in unique column (usually positionInQueue)
- Solution: Clear queue before adding new items

---

## UI Issues

### Issue: "Sessions tab not appearing"
**Checklist:**
- [ ] Is Screen.Sessions added to Screen.kt?
- [ ] Is Screen.Sessions in bottomNavItems list?
- [ ] Is SessionsScreen route in NavHost?
- [ ] Is SessionsScreen composable imported?

**Verify in Screen.kt:**
```kotlin
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Sessions : Screen("sessions", "Sessions", Icons.Default.QueueMusic)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Library,
    Screen.Sessions  // ← Must be here
)
```

### Issue: "Dialogs not showing"
**Solution:**
- Ensure state management is correct:
  ```kotlin
  var showCreateDialog by remember { mutableStateOf(false) }
  
  if (showCreateDialog) {
      CreateSessionDialog(
          onDismiss = { showCreateDialog = false },
          onConfirm = { sessionName ->
              viewModel.createNewSession(sessionName)
              showCreateDialog = false
          }
      )
  }
  ```

### Issue: "Active badge not showing"
**Verify:**
- [ ] isActive field is true in database
- [ ] activeSession StateFlow is updated
- [ ] Composable checks: `session.id == activeSession?.id`

### Issue: "Buttons not responding"
**Solutions:**
- Add proper click handling
- Ensure onClick lambdas are not null
- Check for event consumption in parent

---

## Data Persistence Issues

### Issue: "Sessions disappear after app restart"
**Debug:**
1. Check if database file exists: `/data/data/com.example.musicc/databases/musicc_database`
2. Verify Room is persisting data
3. Check if migrations are needed

**Fix:**
```kotlin
// Ensure database is initialized before any operations
companionobject {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,  // ← Use applicationContext
                AppDatabase::class.java,
                "musicc_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
```

### Issue: "Data not syncing across screens"
**Solution:**
- Use Flow for cross-screen updates
- Ensure StateFlow is collected with `collectAsStateWithLifecycle()`
- Don't cache data locally without Flow backing

---

## State Management Issues

### Issue: "Error message never clears"
**Solution:**
```kotlin
// Ensure clearError() is called after displaying message
DisposableEffect(errorMessage) {
    if (errorMessage != null) {
        // Auto-dismiss after 3 seconds
        Timer().schedule(3000) {
            viewModel.clearError()
        }
    }
    onDispose {}
}
```

### Issue: "StateFlow not updating UI"
**Verify:**
- [ ] StateFlow is being updated: `_allSessions.value = newList`
- [ ] UI is collecting: `val sessions by viewModel.allSessions.collectAsStateWithLifecycle()`
- [ ] Recomposition is triggered (State changes should trigger it)

---

## Memory Issues

### Issue: "App crashes with OutOfMemory"
**Prevent:**
1. Don't load entire database into memory
2. Use pagination for large lists
3. Implement proper coroutine scoping

```kotlin
// BAD - loads all sessions at once
val allSessions = db.playbackSessionDao().observeAll().first()

// GOOD - streams sessions with Flow
viewModelScope.launch {
    db.playbackSessionDao().observeAll().collect { sessions ->
        _allSessions.value = sessions
    }
}
```

### Issue: "Coroutine scope not cancelled"
**Solution:**
- ViewModel should auto-cancel in onCleared()
- viewModelScope is tied to ViewModel lifecycle
- Never launch with GlobalScope

---

## Logging & Debugging

### Enable Detailed Logging
```kotlin
// Add to SessionManagementViewModel
private fun log(message: String) {
    Log.d("SessionManagementVM", message)
}

// Usage:
log("Creating session: $title")
```

### Monitor Database Operations
```kotlin
// Add to build.gradle.kts for Room
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
```

### Check Database Schema
```bash
# Export Room database schema
./gradlew compileDebugKotlin

# Check generated schema in app/schemas/
```

### View Database Content (Android Studio)
1. Open Android Studio Database Inspector
2. Connect device/emulator
3. Select app in dropdown
4. Browse musicc_database
5. Inspect tables directly

---

## Performance Issues

### Issue: "Slow session switching"
**Optimize:**
1. Add database indices: ✅ Already done
2. Use suspend functions: ✅ Already done
3. Minimize database queries
4. Cache frequently accessed data

### Issue: "UI lag when opening Sessions tab"
**Solution:**
- Sessions list uses LazyColumn ✅
- Each card is a separate composable ✅
- No blocking operations in UI thread ✅

---

## Testing Checklist

### Before Reporting Bugs
- [ ] Did you clean build?
  ```bash
  ./gradlew clean :app:assembleDebug
  ```
- [ ] Did you check Logcat for errors?
- [ ] Did you restart the app?
- [ ] Did you restart Android Studio?
- [ ] Is your device/emulator up to date?
- [ ] Are dependencies updated?

### Quick Verification
```bash
# Check compilation
./gradlew :app:compileDebugKotlin

# Check kapt code generation
./gradlew :app:kaptDebugKotlin

# Full build
./gradlew :app:assembleDebug
```

---

## Getting Help

### Check These Resources
1. **IMPLEMENTATION_SUMMARY.md** - Architecture overview
2. **SESSIONS_QUICK_START.md** - Testing scenarios
3. **Logcat** - Error messages and stack traces
4. **Android Studio Database Inspector** - View database content
5. **Room Database Documentation** - Official docs

### Common Search Terms for Issues
- "Room database crash"
- "Kotlin suspend function error"
- "StateFlow not updating"
- "Foreign key constraint SQLite"
- "Jetpack Compose dialog"

---

## Success Indicators

If you see these, everything is working:
- ✅ No red errors in Logcat
- ✅ Sessions tab appears in navigation
- ✅ Can create sessions
- ✅ Sessions persist after restart
- ✅ Can switch between sessions
- ✅ Active session shows green badge
- ✅ UI updates in real-time

---

## Contact for Support

If you encounter issues not covered here:
1. Check the implementation files for comments
2. Review the error message carefully
3. Search Android documentation
4. Check Room database official docs
5. Review the generated code in /build/generated

---

**Last Updated**: May 21, 2026
**Status**: Comprehensive troubleshooting guide ready

