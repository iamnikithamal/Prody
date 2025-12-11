# Prody - Personal Development & Wellness Application

A modern, feature-rich Android application built with **Jetpack Compose** and **Kotlin** that helps users track their personal development, wellness activities, and goals with AI-powered assistance.

## Overview

Prody is a production-grade Android application designed to support personal growth and daily wellness practices. It integrates with **Google Gemini AI** to provide personalized guidance, while maintaining a clean architecture with proper separation of concerns.

### Key Features

- **AI-Powered Buddha Buddy**: Real-time conversations with Google Gemini AI for personalized guidance and wellness support
- **Digital Journal**: Seamless journal entry experience with local storage and data persistence
- **Vocabulary Learning**: Curated vocabulary lessons and tracking with spaced repetition principles
- **Daily Challenges**: Daily tasks and challenges to encourage consistent personal development
- **Future Self Visualization**: Goal setting and future self projection features
- **User Progress Tracking**: Comprehensive statistics and progress analytics
- **Home Widget**: Quick-access home screen widget for daily interactions
- **Notification System**: Smart notifications with action support for user engagement
- **Offline Support**: Full functionality without internet (AI features require connectivity)

## Technical Stack

### Architecture & Frameworks
- **UI Framework**: Jetpack Compose (modern declarative UI)
- **Architecture Pattern**: MVVM with Repository Pattern
- **Navigation**: Navigation Compose
- **Dependency Injection**: Lazy initialization in ProdiApplication

### Data & Storage
- **Database**: Room with SQLite
- **Data Store**: Encrypted preferences using DataStore
- **Serialization**: Kotlin Serialization

### AI & External Services
- **AI Integration**: Google Generative AI (Gemini)
- **HTTP Client**: Retrofit2

### Background & Scheduling
- **Background Tasks**: WorkManager
- **App Widgets**: Glance (Material 3 Widgets)

### Utilities
- **Logging**: Timber
- **Image Loading**: Coil
- **Coroutines**: Kotlin Coroutines for async operations

### Build System
- **Build Tool**: Gradle 8.9 with Kotlin DSL
- **AGP Version**: 8.7.2
- **Min SDK**: 24
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Java/Kotlin**: Java 17, Kotlin 2.0.21

## Project Structure

```
prody/
├── app/
│   ├── src/main/
│   │   ├── java/com/prody/prashant/
│   │   │   ├── ai/                 # Google Gemini AI integration
│   │   │   ├── data/
│   │   │   │   ├── local/          # Room entities, DAOs, database
│   │   │   │   └── repository/     # Data layer repositories
│   │   │   ├── navigation/         # Navigation graph and structure
│   │   │   ├── notification/       # Notification handlers
│   │   │   ├── ui/
│   │   │   │   ├── screens/        # Compose UI screens (11+ features)
│   │   │   │   ├── theme/          # Compose theme configuration
│   │   │   │   └── utils/          # UI utilities (haptic feedback, etc)
│   │   │   ├── widget/             # Home screen widget
│   │   │   ├── MainActivity.kt     # Main entry point
│   │   │   └── ProdiApplication.kt # Application lifecycle
│   │   ├── res/                    # Android resources (XML, strings, etc)
│   │   └── AndroidManifest.xml     # Manifest with permissions
│   ├── build.gradle.kts            # App module build configuration
│   └── proguard-rules.pro          # ProGuard/R8 obfuscation rules
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar      # Gradle wrapper executable
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml          # Dependency version catalog
├── .github/
│   └── workflows/
│       └── android.yml             # GitHub Actions CI/CD pipeline
├── keystore/                       # Release keystore (gitignored)
├── build.gradle.kts                # Root build script
├── settings.gradle.kts             # Gradle settings
├── gradle.properties               # Gradle properties
├── gradlew                         # Linux/macOS wrapper
├── gradlew.bat                     # Windows wrapper
└── README.md                       # This file
```

## Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Android Studio**: Latest version with Android SDK
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 15 (API 35)
- **Gradle**: 8.9+ (included via wrapper)

### Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd prody
   ```

2. **Build the Project**
   ```bash
   ./gradlew build
   ```

3. **Run on Emulator/Device**
   ```bash
   ./gradlew installDebug
   ```

4. **Build Release APK**
   ```bash
   ./gradlew assembleRelease
   ```

### Configuration

#### API Keys & Credentials

**Gemini AI API Key**: Required for AI-powered Buddha Buddy feature
- Obtain from [Google AI Studio](https://aistudio.google.com)
- Stored securely in DataStore (user preference)
- Can be configured from app settings

**Release Signing**: For production builds

See [CI/CD Configuration](#cicd-pipeline) section for secure credential management.

## Configuration & Environment Setup

### Local Development

For local development with release signing:

1. Create `local.properties`:
   ```properties
   PRODY_KEYSTORE_FILE=../keystore/prody-release.jks
   PRODY_KEYSTORE_PASSWORD=your_password
   PRODY_KEY_PASSWORD=your_password
   PRODY_KEY_ALIAS=prody
   ```

2. Ensure keystore exists at `keystore/prody-release.jks`

### Build Properties

Key properties in `gradle.properties`:
- `org.gradle.jvmargs`: JVM memory and encoding settings
- `org.gradle.parallel`: Parallel build execution
- `org.gradle.caching`: Build cache for faster incremental builds
- `android.useAndroidX`: Use AndroidX libraries
- `android.nonTransitiveRClass`: R class optimization

## CI/CD Pipeline

### GitHub Actions Workflow

The project includes an automated GitHub Actions CI/CD pipeline (`.github/workflows/android.yml`):

**Triggered on**:
- Push to app/, gradle/, and build configuration files
- Manual workflow dispatch

**Pipeline Steps**:
1. Checkout repository
2. Setup JDK 17 (Temurin)
3. Configure Gradle caching
4. Make gradlew executable
5. Setup release keystore from secrets
6. Build release APK
7. Generate ProGuard mapping file
8. Upload artifacts with 30-day retention

### Secrets Configuration

**Required GitHub Secrets** for release builds:
- `PRODY_KEYSTORE_BASE64`: Base64-encoded keystore file
- `PRODY_KEYSTORE_PASSWORD`: Keystore password
- `PRODY_KEY_PASSWORD`: Key password

**To setup secrets**:

1. Generate keystore (if not already created):
   ```bash
   keytool -genkeypair -v -storetype PKCS12 \
     -keystore prody-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias prody \
     -dname "CN=Prody, OU=Development, O=Prody, L=Unknown, ST=Unknown, C=US"
   ```

2. Encode keystore to Base64:
   ```bash
   base64 -w 0 prody-release.jks | xclip -selection clipboard
   ```

3. Add to GitHub repository secrets:
   - Navigate to: Settings → Secrets and variables → Actions
   - Create `PRODY_KEYSTORE_BASE64` with the Base64 content
   - Create `PRODY_KEYSTORE_PASSWORD` with the password
   - Create `PRODY_KEY_PASSWORD` with the key password

## Database Schema

The application uses Room for local data persistence with the following entities:

- **JournalEntity**: User journal entries
- **DailyChallengeEntity**: Daily challenge tracking
- **VocabularyEntity**: Vocabulary words and definitions
- **FutureSelfEntity**: Future self goals and visions
- **UserProgressEntity**: User achievement tracking
- **Additional entities**: Support core features

All entities use Type Converters for complex data serialization with Kotlin Serialization.

## ProGuard/R8 Configuration

The app uses ProGuard rules for code obfuscation and shrinking in release builds:

**Key Rules**:
- Preserves Room entities from obfuscation
- Keeps Kotlin serialization metadata
- Maintains Gemini AI client classes
- Configures Compose framework exceptions

**ProGuard Rules File**: `app/proguard-rules.pro`

## Permissions

**Declared Permissions**:
- `INTERNET`: Gemini AI API communication
- `ACCESS_NETWORK_STATE`: Network availability checking
- `POST_NOTIFICATIONS`: Android 13+ notification support
- `RECEIVE_BOOT_COMPLETED`: Device boot notifications
- `SCHEDULE_EXACT_ALARM`: Precise task scheduling
- `USE_EXACT_ALARM`: Exact alarm functionality
- `VIBRATE`: Haptic feedback

## Known Issues & Solutions

### Gradle Wrapper

The gradle wrapper has been fixed to resolve the `ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain` error by adding the missing `gradle-wrapper.jar` file.

### Security Improvements

Recent security enhancements:
- ✅ Removed hardcoded credentials from gradle.properties
- ✅ Moved signing credentials to CI/CD secrets
- ✅ Updated GitHub Actions to use Base64-encoded keystore
- ✅ Fixed ProGuard rules entity path (entities → entity)

## Development Best Practices

1. **Kotlin Code Style**: Official style enforced (`kotlin.code.style=official`)
2. **Coroutines**: Use for all async operations
3. **Room Database**: All database access through DAOs
4. **State Management**: ViewModel for screen state
5. **Logging**: Use Timber for all logging
6. **Error Handling**: Proper exception handling in repositories

## Testing

To run tests:
```bash
./gradlew test
```

## Build Output

**Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
**Release APK**: `app/build/outputs/apk/release/app-release.apk`
**ProGuard Mapping**: `app/build/outputs/mapping/release/mapping.txt`

## Performance Optimization

The app implements several optimizations:

- **Build Cache**: Enabled for faster incremental builds
- **ProGuard**: Code shrinking and obfuscation in release builds
- **Resource Shrinking**: Unused resources removed in release
- **Parallel Gradle**: Enabled for faster compilation
- **Lazy Initialization**: Services initialized on-demand

## Contributing

1. Create a feature branch from `main`
2. Follow Kotlin code style guidelines
3. Ensure all tests pass: `./gradlew test`
4. Submit pull request with clear description

## Troubleshooting

### Build Failures

**Gradle Wrapper Issues**:
```bash
# Make gradlew executable
chmod +x ./gradlew

# Clear gradle cache
./gradlew clean
```

**Memory Issues**:
```bash
# Increase heap size in gradle.properties
org.gradle.jvmargs=-Xmx3072m -Dfile.encoding=UTF-8
```

### Runtime Issues

**AI Features Not Working**:
- Verify Gemini API key is configured
- Check network connectivity
- Ensure API key has required permissions

**Database Errors**:
- Clear app data: `adb shell pm clear com.prody.prashant`
- Reinstall application

## License

[Add your license here]

## Contact & Support

For issues, feature requests, or support:
- GitHub Issues: [Create an issue](../../issues)
- Email: [support email if applicable]

---

**Last Updated**: December 2025
**Version**: 1.0.0
**Maintained By**: Prody Development Team
