# Dog Breed Quiz App

An Android application that helps users learn and identify dog breeds through an engaging quiz experience.

## Features

### 🎯 Core Functionality
- **Interactive Quiz**: Multiple choice questions with high-quality dog breed photos
- **Positive Reinforcement**: Celebration animations and achievements for correct answers
- **Educational Feedback**: Learn from mistakes with breed comparisons and fun facts
- **Progress Tracking**: Level system, experience points, and accuracy statistics
- **Achievement System**: Unlock badges and track learning milestones

### 🎨 User Experience
- **Modern UI**: Material Design 3 with Jetpack Compose
- **Smooth Animations**: Engaging transitions and celebration effects
- **Accessibility**: Full screen reader support and high contrast compliance
- **Responsive Design**: Optimized for different screen sizes

## Key Updates (September 2025)

### 🚀 Latest Stable Versions
This project has been updated to use the latest stable versions of all major Android development libraries:

- **Android Gradle Plugin**: 8.7.0 (latest stable)
- **Kotlin**: 2.0.20 (major version upgrade)
- **Jetpack Compose BOM**: 2025.08.00 (Compose 1.9)
- **Hilt**: 2.57.1 with KSP support (faster builds)
- **Android API Level**: 35 (Android 15 support)
- **Gradle**: 8.10.2 (latest stable)
- **Java**: 11 (upgraded from Java 8)

### ⚡ Performance Improvements
- **KSP Migration**: Switched from KAPT to KSP for up to 2x faster annotation processing
- **Compose 1.9**: Latest UI toolkit with improved performance and new features
- **Kotlin 2.0**: New K2 compiler with better performance and type inference

### 🌐 Real API Integration
- **Dynamic Breed Loading**: Fetches 50+ real dog breeds from dog.ceo API
- **Real Dog Photos**: High-quality images from the dog.ceo database
- **Offline Support**: Graceful fallback when network is unavailable
- **Smart Caching**: 30-minute in-memory caching for optimal performance



- **Language**: Kotlin 2.0.20
- **UI Framework**: Jetpack Compose (BOM 2025.08.00)
- **Architecture**: MVVM with ViewModels
- **Dependency Injection**: Hilt 2.57.1 with KSP
- **Navigation**: Navigation Compose 2.7.7
- **Image Loading**: Coil 2.7.0
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0
- **State Management**: Compose State and StateFlow
- **Material Design**: Material 3
- **API Integration**: dog.ceo API for real dog breed data

## Project Structure

```
app/
├── src/main/
│   ├── java/com/dogbreedquiz/app/
│   │   ├── data/
│   │   │   ├── model/          # Data classes (DogBreed, QuizSession, etc.)
│   │   │   └── repository/     # Data repository with sample breeds
│   │   ├── navigation/         # Navigation setup
│   │   ├── ui/
│   │   │   ├── screens/        # All UI screens
│   │   │   │   ├── onboarding/ # Welcome and tutorial screens
│   │   │   │   ├── home/       # Main menu screen
│   │   │   │   ├── quiz/       # Quiz interface and completion
│   │   │   │   ├── feedback/   # Answer feedback screens
│   │   │   │   ├── progress/   # Statistics and progress tracking
│   │   │   │   ├── achievements/ # Achievement system
│   │   │   │   └── settings/   # App settings
│   │   │   └── theme/          # Material Design theme
│   │   └── MainActivity.kt     # Main activity
│   └── res/
│       ├── values/             # Strings, colors, themes
│       ├── drawable/           # App icons and graphics
│       └── mipmap/             # Launcher icons
└── build.gradle               # App dependencies
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 35 (Android 15) - compileSdk and targetSdk
- Minimum SDK 24 (Android 7.0)
- Kotlin 2.0.20 or later
- Java 11 or later
- Gradle 8.10.2

### Installation

1. **Clone or Download** this project
2. **Open Android Studio**
3. **Import Project**: File → Open → Select `DogBreedQuizApp` folder
4. **Sync Project**: Let Android Studio sync Gradle files
5. **Run**: Click the Run button or press Shift+F10

### Building the App

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Key Components

### 1. Quiz System
- **QuizViewModel**: Manages quiz state and logic
- **QuizScreen**: Main quiz interface with photo and multiple choice
- **Question Generation**: Random breed selection with balanced difficulty

### 2. Feedback System
- **CorrectAnswerScreen**: Celebration with breed facts and animations
- **IncorrectAnswerScreen**: Educational comparison and encouragement

### 3. Progress Tracking
- **Level System**: XP-based progression with meaningful rewards
- **Statistics**: Accuracy tracking, streaks, and performance metrics
- **Breed Mastery**: Individual breed learning progress

### 4. User Interface
- **Material Design 3**: Modern, accessible design system
- **Compose Animations**: Smooth transitions and celebrations
- **Responsive Layout**: Adapts to different screen sizes

## Customization

### Adding New Breeds
Edit `DogBreedRepository.kt` to add new dog breeds:

```kotlin
DogBreed(
    id = "new_breed_id",
    name = "New Breed Name",
    imageUrl = "https://example.com/image.jpg",
    description = "Breed description",
    funFact = "Interesting fact about the breed",
    origin = "Country of origin",
    size = DogBreed.Size.MEDIUM,
    temperament = listOf("Trait1", "Trait2"),
    lifeSpan = "10-12 years",
    difficulty = DogBreed.Difficulty.BEGINNER
)
```

### Modifying UI Colors
Update colors in `app/src/main/res/values/colors.xml` or `Color.kt`:

```kotlin
val PrimaryBlue = Color(0xFF4A90E2)
val SuccessGreen = Color(0xFF7ED321)
```

### Changing Difficulty Settings
Modify quiz generation in `DogBreedRepository.generateQuizSession()`:

```kotlin
// Adjust question count, difficulty distribution, etc.
```

## Design System

### Colors
- **Primary**: Blue (#4A90E2) - Main actions and selections
- **Success**: Green (#7ED321) - Correct answers and achievements
- **Warning**: Orange (#F5A623) - Hints and caution states
- **Error**: Red (#D0021B) - Incorrect answers

### Typography
- **Roboto Font Family** (Android default)
- **8px Grid System** for consistent spacing
- **Accessibility**: Minimum 4.5:1 contrast ratio

### Components
- **Cards**: Rounded corners (8-16px radius)
- **Buttons**: 48px minimum height for accessibility
- **Images**: 4:3 aspect ratio for dog photos

## Performance

- **Image Loading**: Optimized with Coil library
- **Memory Management**: Efficient Compose state handling
- **Animations**: 60fps performance target
- **Offline Support**: All core functionality works offline

## Accessibility

- **Screen Readers**: Full VoiceOver/TalkBack support
- **High Contrast**: WCAG AA compliance
- **Touch Targets**: Minimum 48dp size
- **Focus Management**: Proper keyboard navigation

## Testing

The project includes:
- **Unit Tests**: Business logic validation
- **UI Tests**: Compose testing framework
- **Integration Tests**: End-to-end user flows

Run tests with:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Future Enhancements

- **Camera Integration**: Real-time breed identification
- **Social Features**: Share scores and compete with friends
- **Advanced Breeds**: Rare and mixed breed identification
- **Offline Mode**: Download breed packs for offline use
- **Multiple Languages**: Internationalization support

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is created for educational purposes. Dog breed information and images are used under fair use for educational content.

## Acknowledgments

- **UI/UX Design**: Based on comprehensive design specifications
- **Dog Breed Data**: Curated from reliable breed information sources
- **Images**: High-quality breed photos from Unsplash
- **Material Design**: Google's Material Design 3 guidelines

---

**Ready to test your dog breed knowledge?** Import this project into Android Studio and start learning! 🐕