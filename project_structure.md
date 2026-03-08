# CrowdSenseNet Android Project Structure

```
app/
├── build.gradle.kts                    # App-level build configuration
├── proguard-rules.pro                  # ProGuard configuration
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml         # App manifest
│   │   ├── java/com/example/crowdsensenet/
│   │   │   ├── MainActivity.kt         # Main entry point
│   │   │   │
│   │   │   ├── data/                   # Data layer (Clean Architecture)
│   │   │   │   ├── local/              # Local database (Room)
│   │   │   │   │   ├── AppDatabase.kt  # Room database
│   │   │   │   │   ├── MeasurementDao.kt # Data Access Object
│   │   │   │   │   └── MeasurementEntity.kt # Database entity
│   │   │   │   ├── model/               # Data models
│   │   │   │   │   └── Measurement.kt  # Measurement data model
│   │   │   │   └── remote/             # Remote data (Firebase)
│   │   │   │       ├── FirebaseRepository.kt # Firebase operations
│   │   │   │       └── SyncManager.kt  # Data synchronization
│   │   │   │
│   │   │   ├── service/                # Background services
│   │   │   │   ├── SensingService.kt   # Foreground sensing service
│   │   │   │   └── UploadWorker.kt     # WorkManager upload worker
│   │   │   │
│   │   │   ├── ui/                     # UI layer
│   │   │   │   ├── DashboardActivity.kt # Main dashboard
│   │   │   │   ├── MetricsActivity.kt   # Metrics display
│   │   │   │   ├── SettingsActivity.kt  # App settings
│   │   │   │   └── UploadsActivity.kt   # Upload management
│   │   │   │
│   │   │   └── utils/                  # Utility classes
│   │   │       ├── LocationUtils.kt    # Location utilities
│   │   │       └── NetworkUtils.kt     # Network utilities
│   │   │
│   │   └── res/                        # Android resources
│   │       ├── color/                  # Color definitions
│   │       │   └── primary_colors.xml
│   │       ├── drawable/               # Drawable resources
│   │       │   ├── ic_launcher.xml
│   │       │   ├── ic_sensing.xml
│   │       │   ├── [30+ other drawables]
│   │       ├── layout/                 # XML layouts
│   │       │   ├── activity_dashboard.xml
│   │       │   ├── activity_metrics.xml
│   │       │   ├── activity_settings.xml
│   │       │   ├── activity_uploads.xml
│   │       │   ├── activity_splash.xml
│   │       │   ├── fragment_map.xml
│   │       │   ├── layout_upload_status.xml
│   │       │   ├── item_measurement.xml
│   │       │   └── dialog_permissions.xml
│   │       ├── menu/                   # Menu resources
│   │       │   ├── bottom_navigation.xml
│   │       │   └── settings_menu.xml
│   │       ├── mipmap-*/               # App icons (multiple densities)
│   │       │   ├── hdpi/
│   │       │   ├── mdpi/
│   │       │   ├── xhdpi/
│   │       │   ├── xxhdpi/
│   │       │   ├── xxxhdpi/
│   │       │   └── mipmap-anydpi-v26/
│   │       ├── values/                 # Value resources
│   │       │   ├── strings.xml         # String resources
│   │       │   ├── themes.xml          # App themes
│   │       │   ├── colors.xml          # Color resources
│   │       │   └── dimens.xml          # Dimension resources
│   │       ├── values-night/           # Night mode resources
│   │       │   └── themes.xml
│   │       └── xml/                    # XML configurations
│   │           ├── network_security_config.xml
│   │           └── backup_rules.xml
│   │
│   ├── androidTest/                    # Instrumentation tests
│   │   └── java/com/example/crowdsensenet/
│   │       └── ExampleInstrumentedTest.kt
│   │
│   └── test/                           # Unit tests
│       └── java/com/example/crowdsensenet/
│           └── ExampleUnitTest.kt
│
└── .gitignore                          # Git ignore file
```

## Architecture Overview

### Clean Architecture Layers

1. **UI Layer** (`ui/`)
   - Activities and UI logic
   - ViewModels (if implemented)
   - UI state management

2. **Data Layer** (`data/`)
   - **Local** (`data/local/`) - Room database for offline storage
   - **Remote** (`data/remote/`) - Firebase Firestore integration
   - **Models** (`data/model/`) - Data transfer objects

3. **Domain Layer** (Not yet implemented)
   - Business logic and use cases
   - Repository interfaces

4. **Service Layer** (`service/`)
   - Background services for sensing and uploading
   - WorkManager for scheduled tasks

5. **Utility Layer** (`utils/`)
   - Helper classes for common operations
   - Location and network utilities

### Key Components

- **SensingService**: Foreground service for continuous network monitoring
- **UploadWorker**: Background worker for Firebase synchronization
- **AppDatabase**: Room database for local storage
- **FirebaseRepository**: Firebase Firestore operations
- **SyncManager**: Coordinates data synchronization

### Data Flow

```
UI Activities → ViewModels → Repository → Local Database (Room)
                                    ↓
                              Background Sync (WorkManager)
                                    ↓
                              Remote Database (Firebase)
```

### Dependencies

- **Firebase**: Firestore for cloud storage
- **Room**: Local database for offline-first architecture
- **WorkManager**: Background task scheduling
- **Google Maps**: Location visualization
- **TelephonyManager**: Network signal measurement
- **FusedLocationProvider**: GPS location services
