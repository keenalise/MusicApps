# Architecture Diagrams - Sessions Feature

## 1. System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      ANDROID DEVICE                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   UI Layer (Compose)                      │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │                                                            │   │
│  │  ┌─────────────────┐  ┌──────────────────┐              │   │
│  │  │ SessionsScreen  │  │ SessionCard      │              │   │
│  │  │  - Create UI    │  │ - Display state  │              │   │
│  │  │  - Edit UI      │  │ - Active badge   │              │   │
│  │  │  - Delete UI    │  │ - Edit/Delete    │              │   │
│  │  │  - List         │  │   buttons        │              │   │
│  │  └────────┬────────┘  └────────┬─────────┘              │   │
│  │           │                    │                         │   │
│  │           └────────┬───────────┘                         │   │
│  │                    │                                     │   │
│  │           Observes StateFlows                           │   │
│  │                    │                                     │   │
│  │  ┌────────────────▼──────────────────┐                  │   │
│  │  │    Compose Navigation             │                  │   │
│  │  │  ┌─────────────────────────────┐  │                  │   │
│  │  │  │  SessionsScreen Route       │  │                  │   │
│  │  │  │  - "sessions"               │  │                  │   │
│  │  │  │  - QueueMusic icon          │  │                  │   │
│  │  │  └─────────────────────────────┘  │                  │   │
│  │  └────────────────┬───────────────────┘                  │   │
│  │                   │                                     │   │
│  └───────────────────┼─────────────────────────────────────┘   │
│                      │                                          │
├──────────────────────┼──────────────────────────────────────────┤
│                      │                                          │
│  ┌──────────────────▼──────────────────┐                       │
│  │    ViewModel Layer (MVVM)            │                       │
│  ├──────────────────────────────────────┤                       │
│  │                                      │                       │
│  │  SessionManagementViewModel          │                       │
│  │  ┌──────────────────────────────┐    │                       │
│  │  │ StateFlows (UI State)        │    │                       │
│  │  │ - allSessions                │    │                       │
│  │  │ - activeSession              │    │                       │
│  │  │ - isCreatingSession          │    │                       │
│  │  │ - errorMessage               │    │                       │
│  │  └──────────────────────────────┘    │                       │
│  │  ┌──────────────────────────────┐    │                       │
│  │  │ Functions                    │    │                       │
│  │  │ - createNewSession()         │    │                       │
│  │  │ - switchToSession()          │    │                       │
│  │  │ - deleteSession()            │    │                       │
│  │  │ - renameSession()            │    │                       │
│  │  │ - updateSessionQueue()       │    │                       │
│  │  │ - clearError()               │    │                       │
│  │  └──────────┬───────────────────┘    │                       │
│  │             │                        │                       │
│  │  ┌──────────▼───────────────────┐    │                       │
│  │  │ Database Reference           │    │                       │
│  │  │ - AppDatabase singleton      │    │                       │
│  │  │ - Coroutine scope            │    │                       │
│  │  └──────────────────────────────┘    │                       │
│  │                                      │                       │
│  └──────────────────┬───────────────────┘                       │
│                     │                                           │
├─────────────────────┼───────────────────────────────────────────┤
│                     │                                           │
│  ┌──────────────────▼──────────────────┐                        │
│  │     Data Access Layer (DAO)          │                        │
│  ├──────────────────────────────────────┤                        │
│  │                                      │                        │
│  │  PlaybackSessionDao                  │                        │
│  │  ├─ observeAll() → Flow<List>        │                        │
│  │  ├─ observeById() → Flow<Entity?>    │                        │
│  │  ├─ insert(suspend) → Long           │                        │
│  │  ├─ update(suspend)                  │                        │
│  │  ├─ delete(suspend)                  │                        │
│  │  └─ clearActiveFlagsExcept(suspend)  │                        │
│  │                                      │                        │
│  │  QueueItemDao                        │                        │
│  │  ├─ observeQueue() → Flow<List>      │                        │
│  │  ├─ insertAll(suspend) → List<Long>  │                        │
│  │  ├─ clearQueue(suspend)              │                        │
│  │  └─ replaceQueue(suspend)            │                        │
│  │                                      │                        │
│  └──────────────────┬───────────────────┘                        │
│                     │                                           │
├─────────────────────┼───────────────────────────────────────────┤
│                     │                                           │
│  ┌──────────────────▼──────────────────┐                        │
│  │    Entity Layer (Room Entities)      │                        │
│  ├──────────────────────────────────────┤                        │
│  │                                      │                        │
│  │  PlaybackSessionEntity               │                        │
│  │  ├─ id: Long (PK)                    │                        │
│  │  ├─ title: String                    │                        │
│  │  ├─ createdAt: Long                  │                        │
│  │  ├─ updatedAt: Long                  │                        │
│  │  ├─ currentIndex: Int                │                        │
│  │  ├─ lastPositionMs: Long             │                        │
│  │  ├─ playbackState: Int               │                        │
│  │  ├─ repeatMode: Int                  │                        │
│  │  ├─ shuffleModeEnabled: Boolean      │                        │
│  │  └─ isActive: Boolean                │                        │
│  │                                      │                        │
│  │  QueueItemEntity                     │                        │
│  │  ├─ id: Long (PK)                    │                        │
│  │  ├─ sessionId: Long (FK)             │                        │
│  │  ├─ mediaId: String                  │                        │
│  │  ├─ uri: String                      │                        │
│  │  ├─ title: String                    │                        │
│  │  ├─ artist: String                   │                        │
│  │  ├─ album: String                    │                        │
│  │  ├─ durationMs: Long                 │                        │
│  │  ├─ positionInQueue: Int             │                        │
│  │  └─ extrasJson: String               │                        │
│  │                                      │                        │
│  └──────────────────┬───────────────────┘                        │
│                     │                                           │
├─────────────────────┼───────────────────────────────────────────┤
│                     │                                           │
│  ┌──────────────────▼──────────────────┐                        │
│  │   Storage Layer (SQLite Database)    │                        │
│  ├──────────────────────────────────────┤                        │
│  │                                      │                        │
│  │  musicc_database (SQLite)            │                        │
│  │  ├─ playback_sessions table          │                        │
│  │  │  ├─ INDEX: is_active              │                        │
│  │  │  └─ INDEX: updated_at             │                        │
│  │  │                                  │                        │
│  │  └─ queue_items table                │                        │
│  │     ├─ FK: session_id (CASCADE)      │                        │
│  │     ├─ INDEX: session_id             │                        │
│  │     └─ UNIQUE: session_id +          │                        │
│  │        positionInQueue               │                        │
│  │                                      │                        │
│  └──────────────────────────────────────┘                        │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## 2. Data Flow - Creating a Session

```
User Interface
     │
     │ User taps (+) button
     ▼
CreateSessionDialog
     │
     │ User enters name, taps Create
     ▼
SessionsScreen
     │
     │ Calls viewModel.createNewSession(name)
     ▼
SessionManagementViewModel
     │
     │ 1. Create PlaybackSessionEntity
     │ 2. Launch coroutine in viewModelScope
     ▼
Database Access Layer
     │
     │ Call PlaybackSessionDao.insert(entity)
     ▼
Room Database
     │
     │ INSERT INTO playback_sessions VALUES (...)
     ▼
SQLite Storage
     │
     │ Row created with auto-generated ID
     ▼
Room Emits Update
     │
     │ Flow updates with new list
     ▼
ViewModel Updates StateFlow
     │
     │ _allSessions.value = newList
     ▼
SessionsScreen Recomposes
     │
     │ Observes allSessions StateFlow
     │ New session appears in list
     ▼
User Sees New Session
```

## 3. Data Flow - Switching Sessions

```
SessionsScreen (User selects session)
     │
     │ Calls viewModel.switchToSession(sessionId)
     ▼
SessionManagementViewModel
     │
     ├─ 1. Get current active session
     │      └─ _activeSession.value
     │
     ├─ 2. Save current session state
     │      └─ db.playbackSessionDao().update(currentSession)
     │
     ├─ 3. Clear all active flags
     │      └─ db.playbackSessionDao().clearActiveFlagsExcept(sessionId)
     │
     ├─ 4. Get target session
     │      └─ db.playbackSessionDao().observeById(sessionId).first()
     │
     └─ 5. Mark target as active
        └─ db.playbackSessionDao().update(updatedSession)
            └─ isActive = true
                └─ updatedAt = now()
     ▼
Database Updates
     │
     │ All Flows emit new data
     ▼
ViewModel StateFlows Update
     │
     ├─ _allSessions updates
     └─ _activeSession updates
     ▼
SessionsScreen Recomposes
     │
     │ Active badge moves to new session
     │ Old session shows no badge
     ▼
User Sees Active Session Changed
```

## 4. Component Relationship Diagram

```
                    MainActivity
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
    MusicApp        SessionVM      SessionsScreen
        │           (created)
        │                │
        ├────────────┐   │
        │            │   │
        ▼            ▼   ▼
    NavHost ◄──── Observer ◄──── StateFlows
        │                │
        ├────────┐       │
        │        │       │
        ▼        ▼       ▼
     Routes  Dialogs  CardList
        │      │        │
        ├─────┴────────┤
        │              │
        ▼              ▼
  HomeScreen    Database (Room)
  SearchScreen      │
  LibraryScreen     ├─ AppDatabase
  SessionsScreen    │  ├─ PlaybackSessionDao
                    │  └─ QueueItemDao
                    │
                    ▼
              SQLite Database
              ├─ playback_sessions
              └─ queue_items
```

## 5. State Management Flow

```
User Action
     │
     │ (Create, Switch, Edit, Delete)
     ▼
SessionManagementViewModel
     │
     ├─ Input Validation
     │
     ├─ Error Handling (try-catch)
     │
     ├─ Database Operation (suspend)
     │      └─ DAO method (insert/update/delete)
     │
     ├─ StateFlow Update
     │      └─ _allSessions.value = ...
     │      └─ _activeSession.value = ...
     │      └─ _isCreatingSession.value = ...
     │      └─ _errorMessage.value = ...
     │
     └─ Completion
     ▼
SessionsScreen Collects
     │
     ├─ collectAsStateWithLifecycle()
     │
     └─ Recomposes with new state
     ▼
UI Updates
     │
     └─ User sees changes
```

## 6. Database Schema

```
┌─────────────────────────────────────────────────────┐
│         playback_sessions table                      │
├────────────────────────┬──────────────────────────────┤
│ Column                 │ Type                         │
├────────────────────────┼──────────────────────────────┤
│ id (PK)                │ LONG (auto-increment)        │
│ title                  │ TEXT                         │
│ created_at             │ LONG                         │
│ updated_at             │ LONG (indexed)               │
│ current_index          │ INT                          │
│ last_position_ms       │ LONG                         │
│ playback_state         │ INT                          │
│ repeat_mode            │ INT                          │
│ shuffle_mode_enabled   │ BOOLEAN                      │
│ is_active              │ BOOLEAN (indexed)            │
└────────────────────────┴──────────────────────────────┘
           │
           │ Foreign Key Relation
           │
           ▼
┌─────────────────────────────────────────────────────┐
│           queue_items table                          │
├────────────────────────┬──────────────────────────────┤
│ Column                 │ Type                         │
├────────────────────────┼──────────────────────────────┤
│ id (PK)                │ LONG (auto-increment)        │
│ session_id (FK)        │ LONG (indexed, CASCADE)      │
│ media_id               │ TEXT                         │
│ uri                    │ TEXT                         │
│ title                  │ TEXT                         │
│ artist                 │ TEXT                         │
│ album                  │ TEXT                         │
│ duration_ms            │ LONG                         │
│ position_in_queue      │ INT (unique with session_id) │
│ extras_json            │ TEXT                         │
└────────────────────────┴──────────────────────────────┘
```

## 7. Error Handling Flow

```
Try Block
     │
     ├─ Database Operation
     │
     ▼
Catch Block (Exception)
     │
     ├─ Log Error: Log.e(...)
     │
     ├─ Update ErrorMessage StateFlow
     │      └─ _errorMessage.value = message
     │
     └─ SessionsScreen Observes
            │
            ├─ Renders Error Card
            │
            └─ User can dismiss
                   │
                   └─ viewModel.clearError()
                      └─ _errorMessage.value = null
```

## 8. Coroutine & Scope Hierarchy

```
Activity Lifecycle
     │
     └─ ViewModel.viewModelScope
            │
            ├─ createNewSession()
            │      └─ viewModelScope.launch { ... }
            │             └─ db operation (suspend)
            │
            ├─ switchToSession()
            │      └─ viewModelScope.launch { ... }
            │             └─ db operations (suspend)
            │
            ├─ deleteSession()
            │      └─ viewModelScope.launch { ... }
            │             └─ db operation (suspend)
            │
            └─ init block
                   └─ viewModelScope.launch { ... }
                      └─ db.observeAll().collect { ... }
                         (runs until ViewModel cleared)
```

---

**These diagrams show:**
1. System architecture from UI to database
2. Data flow for creating sessions
3. Data flow for switching sessions
4. Component relationships
5. State management lifecycle
6. Database schema with relations
7. Error handling flow
8. Coroutine scope hierarchy

All of these work together to provide seamless multi-session playback!

