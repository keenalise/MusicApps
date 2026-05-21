# Quick Start - Testing Sessions Feature

## 🎬 Quick Test Scenario

### Setup (First Time)
1. Open Android Studio
2. Clean and rebuild the project:
   ```bash
   ./gradlew clean :app:assembleDebug
   ```
3. Run the app on an emulator or device

### Test Workflow

#### Step 1: Create First Session
- Navigate to **Sessions** tab (bottom navigation)
- Tap the **+** (plus) button
- Enter name: "Workout Mix"
- Tap **Create**
- ✅ Session appears in the list

#### Step 2: Add Music to First Session
- Go back to **Home** tab
- Tap a song to play it
- Player opens showing the song
- Note the current position in the mini-player or full player
- ✅ Music is playing in "Workout Mix" session

#### Step 3: Create Second Session
- Go to **Sessions** tab
- Tap **+**
- Enter name: "Sleep Sounds"
- Tap **Create**
- ✅ You now have two sessions

#### Step 4: Switch to Second Session
- Tap on "Sleep Sounds" session in the list
- ✅ Current session state from "Workout Mix" is saved
- ✅ "Sleep Sounds" is marked as "Active" (green badge)

#### Step 5: Play Different Music
- Go to **Home** tab
- Tap a different song
- Let it play for a bit
- Note the playback position
- ✅ Playing in "Sleep Sounds" session

#### Step 6: Switch Back to First Session
- Go to **Sessions** tab
- Tap "Workout Mix"
- ✅ "Workout Mix" is now marked as "Active"
- Go to **Home** → play or check player
- ✅ The original song is restored with saved position

#### Step 7: Rename a Session
- Go to **Sessions** tab
- Tap the **edit** icon (pencil) on "Sleep Sounds"
- Change name to "Meditation"
- Tap **Save**
- ✅ Session name updated

#### Step 8: Delete a Session
- Go to **Sessions** tab
- Tap the **delete** icon (trash) on a session
- ✅ Session is removed from the list

## 📊 Verification Checklist

### Database Operations
- [ ] Sessions are created and visible in list
- [ ] Session creation shows proper timestamp
- [ ] Session deletion removes item from list
- [ ] Session renaming updates display name

### State Management
- [ ] Active session has green "Active" badge
- [ ] Only one session is marked as active at a time
- [ ] Session list updates in real-time

### UI/UX
- [ ] Dialogs appear and close properly
- [ ] Error messages (if any) display clearly
- [ ] No crashes when switching between tabs
- [ ] Navigation works smoothly

### Data Persistence (App Restart)
1. Create 2-3 sessions
2. Close app completely
3. Reopen app
4. ✅ All sessions should still be there (persisted in database)

## 🐛 Troubleshooting

### Sessions not showing up?
- Check if database is initialized: `AppDatabase.getDatabase(context)`
- Verify Room dependency in build.gradle.kts
- Check Logcat for Room/database errors

### Can't create sessions?
- Ensure SessionManagementViewModel is properly instantiated
- Check if database access is happening on the correct coroutine scope
- Verify PlaybackSessionDao has suspend keyword on insert()

### Session switching not working?
- Verify `switchToSession()` is being called with correct sessionId
- Check if clearActiveFlagsExcept() is updating database correctly
- Verify Flow collection in ViewModel init block

### UI not updating after actions?
- Ensure StateFlow is being collected with `collectAsStateWithLifecycle()`
- Verify Flow observers in ViewModel.init are running
- Check if database updates are triggering Flow emissions

## 📱 What to Expect

### First Launch
- Empty Sessions list with "+" button
- Can create first session immediately

### After Creating Sessions
- List shows all created sessions with timestamps
- One session marked as "Active"

### Visual Design
- Dark theme matching Spotify aesthetic
- Green accent color (SpotifyGreen) for active session and buttons
- Cards with rounded corners for each session
- Action buttons (edit/delete) aligned right

### Functionality
- Smooth transitions between screens
- Real-time list updates
- Proper error handling with dismissible messages
- Natural flow between creating, editing, deleting sessions

## 🎯 Success Criteria

✅ **If you can do all of this, the feature is working:**
1. Create multiple sessions
2. Switch between them
3. See state changes reflected (Active badge)
4. Edit session names
5. Delete sessions
6. Data persists after app restart
7. No crashes or errors
8. UI updates smoothly

---

## 📞 Support

If you encounter any issues:
1. Check Logcat for error messages
2. Verify all files are created in correct locations
3. Do a clean build: `./gradlew clean :app:assembleDebug`
4. Check the SESSIONS_IMPLEMENTATION.md for architecture details

**Happy testing! 🎉**

