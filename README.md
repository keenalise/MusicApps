# Musicc - Local Music Player with Spotify UI

A fully functional Android music player app that plays MP3 files from your phone, featuring a beautiful Spotify-inspired UI built with Jetpack Compose.

## ✨ Key Features

### 🎵 **Real Music Playback**
- **Scans all MP3 files** from your device storage
- **ExoPlayer integration** for high-quality audio playback  
- **Background playback** with proper media controls
- **Play, pause, skip** functionality
- **Seek bar** to jump to any position in the track
- **Shuffle and repeat modes**
- **Album art** automatically loaded from MP3 metadata

### 🎨 **Spotify-like UI**
- **Dark Theme**: Beautiful dark color scheme with Spotify green (#1DB954) accents
- **Modern Material Design 3**: Latest components and theming
- **Bottom Navigation**: Easy navigation between Home, Search, and Library
- **Smooth animations** and transitions
- **Album artwork** displayed throughout the app

### 🏠 **Home Screen**
- **Displays all your MP3 files** automatically
- **Recently played grid** (2-column layout)
- **All songs list** with artist, duration, and album art
- **Albums section** - songs grouped by album
- **Loading indicator** while scanning device
- **Empty state** message if no songs found

### 🎵 **Full Player Screen**
- **Large album artwork** from MP3 metadata
- **Real-time progress bar** synced with playback
- **Full playback controls**:
  - Large Play/Pause button
  - Skip to next/previous track
  - Shuffle mode toggle (highlighted when active)
  - Repeat modes: Off → All → One (highlighted when active)
  - Interactive seek bar
- **Live position updates** every second
- **Song metadata** - title, artist, album
- **Favorite button** (UI ready)

### 🎯 **Mini Player**
- **Persistent bottom bar** when music is playing
- **Album art thumbnail**
- **Song title and artist**
- **Play/Pause button** (synced with playback state)
- **Favorite button** (UI ready)
- **Tap anywhere** to expand to full player

### 🔍 **Search & Library**
- Search screen with genre categories
- Library screen with playlists
- Ready for future enhancements

## 📱 Permissions

The app requests:
- **READ_MEDIA_AUDIO** (Android 13+) or **READ_EXTERNAL_STORAGE** (Android 12 and below)
- Required to scan and play MP3 files on your device

## 🏗️ Architecture

### **MVVM Pattern**
- **ViewModel**: `MusicViewModel` - Manages app state and business logic
- **Repository**: `MusicRepository` - Scans device for MP3 files
- **Service**: `MusicPlayerService` - Handles ExoPlayer audio playback
- **Models**: Clean data models for Song and Playlist

### **Modern Android Stack**
- **Jetpack Compose**: Declarative UI
- **StateFlow**: Reactive state management  
- **Coroutines**: Asynchronous operations
- **ExoPlayer (Media3)**: Professional audio playback
- **Coil**: Image loading for album art
- **Navigation Compose**: Type-safe navigation

## 📁 Project Structure

```
app/src/main/java/com/example/musicc/
├── MainActivity.kt                    # Main app with permission handling
├── model/
│   └── Song.kt                       # Data models (Song, Playlist)
├── repository/
│   └── MusicRepository.kt            # MediaStore scanner
├── service/
│   └── MusicPlayerService.kt         # ExoPlayer wrapper
├── viewmodel/
│   └── MusicViewModel.kt             # State management
├── ui/
│   ├── navigation/
│   │   └── Screen.kt                 # Navigation routes
│   ├── screens/
│   │   ├── HomeScreen.kt            # Shows all MP3 files
│   │   ├── SearchScreen.kt          # Search interface
│   │   ├── LibraryScreen.kt         # Library management
│   │   └── PlayerScreen.kt          # Full player with controls
│   └── theme/
│       ├── Color.kt                  # Spotify colors
│       ├── Theme.kt                  # Material theme
│       └── Type.kt                   # Typography
```

## 🚀 How to Run

1. **Open in Android Studio**
2. **Sync Gradle** dependencies
3. **Run on device or emulator** (API 24+)
4. **Grant storage permission** when prompted
5. **App will scan for MP3 files** automatically
6. **Tap any song** to start playing!

## 🎼 How It Works

1. **Startup**: App requests storage permission
2. **Scanning**: Uses MediaStore to find all MP3 files
3. **Display**: Shows songs with metadata (title, artist, album, duration)
4. **Playback**: Tapping a song initializes ExoPlayer with entire playlist
5. **Controls**: Play, pause, skip, shuffle, repeat all work in real-time
6. **Navigation**: Mini player stays visible while browsing

## 📦 Dependencies

```gradle
// Media playback
implementation("androidx.media3:media3-exoplayer:1.4.1")
implementation("androidx.media3:media3-ui:1.4.1")
implementation("androidx.media3:media3-common:1.4.1")

// Image loading
implementation("io.coil-kt:coil-compose:2.7.0")

// ViewModel & Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

// Navigation & UI
implementation("androidx.navigation:navigation-compose:2.8.5")
implementation("androidx.compose.material:material-icons-extended:1.7.5")
```

## 🎨 Color Scheme

- **Spotify Green**: `#1DB954` - Primary accent
- **Spotify Black**: `#121212` - Background
- **Spotify Dark Gray**: `#181818` - Surface
- **Spotify Gray**: `#282828` - Cards
- **Spotify Light Gray**: `#B3B3B3` - Secondary text
- **White**: `#FFFFFF` - Primary text

## ✅ What Works

- ✅ Scans all MP3 files from device
- ✅ Displays album art from metadata
- ✅ Real audio playback with ExoPlayer
- ✅ Play/Pause/Skip controls
- ✅ Progress bar with seek
- ✅ Shuffle mode
- ✅ Repeat modes (Off, All, One)
- ✅ Mini player with playback state
- ✅ Beautiful Spotify-inspired UI
- ✅ Permission handling
- ✅ Loading states
- ✅ Empty states

## 🔮 Future Enhancements

- [ ] Search functionality (scan results)
- [ ] Playlist creation and management
- [ ] Favorites/Liked songs storage
- [ ] Sort options (name, artist, date added)
- [ ] Filter by artist/album
- [ ] Queue management
- [ ] Lock screen controls
- [ ] Notification media controls
- [ ] Equalizer
- [ ] Lyrics display
- [ ] Sleep timer
- [ ] Recent history tracking

## 📱 Tested On

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Permissions**: Handles Android 13+ granular media permissions

## 🎉 Ready to Use!

This is a **fully functional music player** that will:
1. Find all your MP3 files
2. Display them with a beautiful UI
3. Play them with professional quality
4. Let you control playback with all standard features

Just install and enjoy your music! 🎵

## License

Demo project showcasing modern Android development with Jetpack Compose.

