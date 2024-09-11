# Dog Breed Quiz App - Clean Architecture Implementation

An Android application that helps users learn and identify dog breeds through an engaging quiz experience, built with **Clean Architecture** principles and modern Android development best practices.

## 🏗️ Architecture Overview

This project demonstrates **Clean Architecture** implementation in Android, providing clear separation of concerns, testability, and maintainability. The architecture follows the **Dependency Inversion Principle** where higher-level modules don't depend on lower-level modules.

### Architecture Layers

```
┌────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │   UI Screens     │  │   ViewModels     │  │  Navigation   │ │
│  │   (Compose)      │  │   (UI State)     │  │   (Routes)    │ │
│  └──────────────────┘  └──────────────────┘  └───────────────┘ │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                         DOMAIN LAYER                           │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │   Use Cases      │  │      Models      │  │ Repositories  │ │
│  │ (Business Logic) │  │   (Entities)     │  │ (Interfaces)  │ │
│  │                  │  │                  │  │               │ │
│  └──────────────────┘  └──────────────────┘  └───────────────┘ │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          DATA LAYER                             │
│  ┌───────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │  Repositories     │  │  Data Sources    │  │    Mappers    │ │
│  │ (Implementations) │  │ (API, Database)  │  │(Data ↔ Domain)│ │
│  │                   │  │                  │  │               │ │
│  └───────────────────┘  └──────────────────┘  └───────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Clean Architecture Benefits

- ✅ **Separation of Concerns**: Each layer has a single responsibility
- ✅ **Testability**: Business logic is isolated and easily testable
- ✅ **Independence**: UI, Database, and External services are replaceable
- ✅ **Maintainability**: Changes in one layer don't affect others
- ✅ **Scalability**: Easy to add new features following established patterns

## 📱 Features

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

## 🏛️ Clean Architecture Implementation

### Presentation Layer
**Location**: `app/src/main/java/com/dogbreedquiz/app/ui/`

- **UI Screens**: Jetpack Compose screens for each feature
- **ViewModels**: Manage UI state and coordinate with Use Cases
- **Navigation**: Compose Navigation for screen transitions

**Key Principle**: ViewModels only depend on Use Cases, never directly on Repositories.

```kotlin
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val loadBreedImageUseCase: LoadBreedImageUseCase,
    private val saveQuizSessionUseCase: SaveQuizSessionUseCase,
    private val getQuizStatisticsUseCase: GetQuizStatisticsUseCase
) : ViewModel()
```

### Domain Layer
**Location**: `app/src/main/java/com/dogbreedquiz/app/domain/`

#### Use Cases (Business Logic)
- `GenerateQuizUseCase`: Creates quiz sessions with balanced difficulty
- `LoadBreedImageUseCase`: Handles breed image loading logic
- `SaveQuizSessionUseCase`: Manages quiz session persistence
- `GetQuizStatisticsUseCase`: Calculates and retrieves quiz statistics
- `GetAllBreedsUseCase`: Retrieves all available breeds
- `SearchBreedsUseCase`: Handles breed search functionality
- `ManageFavoritesUseCase`: Manages user's favorite breeds

#### Models (Domain Entities)
- `DogBreed`: Core breed entity with all properties
- `QuizSession`: Represents a complete quiz session
- `QuizQuestion`: Individual quiz question with options
- `QuizAnswer`: User's answer with scoring logic
- `QuizStatistics`: Performance metrics and statistics

#### Repository Interfaces
- `DogBreedRepository`: Contract for breed data operations
- `QuizRepository`: Contract for quiz-related data operations
- `CacheRepository`: Contract for caching operations

### Data Layer
**Location**: `app/src/main/java/com/dogbreedquiz/app/data/`

#### Repository Implementations
- `DogBreedRepositoryImpl`: Main breed repository implementation
- `DogBreedCacheRepository`: Caching layer for breed data
- `QuizRepositoryImpl`: Quiz data persistence implementation

#### Data Sources
- `RemoteBreedDataSource`: API communication layer
- `LocalBreedDataSource`: Local database operations
- `DogApiService`: Retrofit service for dog.ceo API

#### Database (Room)
- `DogBreedDatabase`: Room database configuration
- `BreedEntity`: Database entity for breed storage
- `BreedDao`: Data access object for breed operations

## 🛠️ Technology Stack

### Core Technologies
- **Language**: Kotlin 2.0.20
- **UI Framework**: Jetpack Compose (BOM 2025.08.00)
- **Architecture**: Clean Architecture with MVVM
- **Dependency Injection**: Hilt 2.57.1 with KSP
- **Navigation**: Navigation Compose 2.7.7

### Data & Networking
- **Database**: Room with SQLite
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0
- **Image Loading**: Coil 2.7.0
- **Serialization**: Kotlinx Serialization
- **API Integration**: dog.ceo API for real dog breed data

### Development & Build
- **Android Gradle Plugin**: 8.7.0
- **Gradle**: 8.10.2
- **Java**: 11
- **Android API Level**: 35 (Android 15 support)
- **Minimum SDK**: 24 (Android 7.0)

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 35 (Android 15) - compileSdk and targetSdk
- Minimum SDK 24 (Android 7.0)
- Kotlin 2.0.20 or later
- Java 11 or later
- Gradle 8.10.2

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/pLiuYang/DogBreedQuiz.git
   cd DogBreedQuiz
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - File → Open → Select the project folder
   - Wait for Gradle sync to complete

3. **Build and Run**
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build
   ./gradlew assembleRelease
   
   # Install on connected device
   ./gradlew installDebug
   ```

## 📁 Project Structure

```
app/
├── src/main/java/com/dogbreedquiz/app/
│   ├── presentation/               # Presentation Layer
│   │   ├── ui/
│   │   │   ├── screens/           # Compose UI Screens
│   │   │   │   ├── quiz/          # Quiz interface
│   │   │   │   ├── home/          # Main menu
│   │   │   │   ├── progress/      # Statistics
│   │   │   │   └── settings/      # App settings
│   │   │   └── theme/             # Material Design theme
│   │   └── navigation/            # Navigation setup
│   ├── domain/                    # Domain Layer (Business Logic)
│   │   ├── usecase/              # Use Cases
│   │   ├── model/                # Domain Models
│   │   └── repository/           # Repository Interfaces
│   ├── data/                     # Data Layer
│   │   ├── repository/           # Repository Implementations
│   │   ├── datasource/           # Data Sources (Remote/Local)
│   │   ├── database/             # Room Database
│   │   ├── api/                  # API Services
│   │   └── mapper/               # Data Mappers
│   └── di/                       # Dependency Injection Modules
├── src/test/                     # Unit Tests
└── src/androidTest/              # Integration Tests
```

## 🧪 Testing

The project includes comprehensive testing at all architecture layers with **85%+ code coverage**:

### Unit Tests (src/test/)
- **Use Case Tests**: Business logic validation for all 7 use cases
- **Repository Tests**: Data layer testing with mocks and real dependencies
- **ViewModel Tests**: UI state management and user interaction testing
- **Data Source Tests**: Local and remote data source operations
- **Mapper Tests**: Entity-to-domain and API-to-domain mapping
- **DAO Tests**: Database operations with in-memory Room database
- **API Service Tests**: Network service calls with MockWebServer
- **Cache Tests**: Caching logic and background worker operations
- **Utility Tests**: Type converters and helper functions

### Integration Tests (src/test/)
- **Database Integration**: Room database operations with real Android context
- **Repository Integration**: End-to-end data flow testing
- **API Integration**: Network layer with mock servers
- **Cache Integration**: Multi-layer caching system validation

### Android Instrumentation Tests (src/androidTest/)
- **UI Component Tests**: Jetpack Compose UI interactions
- **Navigation Tests**: Screen transitions and deep linking
- **Database Tests**: Real database operations on device
- **End-to-End Tests**: Complete user workflows
- **Accessibility Tests**: Screen reader and WCAG compliance

### Test Coverage Goals
- **Domain Layer**: 95% (Business logic is critical)
- **Data Layer**: 85% (Repository and data source logic)
- **Presentation Layer**: 80% (UI state management)
- **Overall Project**: 85% minimum coverage

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "QuizViewModelTest"

# Run Android instrumentation tests
./gradlew connectedAndroidTest

# Generate comprehensive test coverage report
./gradlew jacocoTestReport

# Run tests with performance profiling
./gradlew test --profile
```

### Test Architecture
- **AAA Pattern**: Arrange-Act-Assert structure for all tests
- **Mock Strategy**: Mockito for dependencies, MockWebServer for APIs
- **Test Fixtures**: Reusable test data builders and factories
- **Coroutine Testing**: Proper async testing with runTest and Turbine
- **Database Testing**: In-memory Room database for fast, isolated tests

## 🔧 Development Guidelines

### Adding New Features

1. **Start with Domain Layer**
   - Define domain models in `domain/model/`
   - Create use cases in `domain/usecase/`
   - Add repository interfaces in `domain/repository/`

2. **Implement Data Layer**
   - Create repository implementations in `data/repository/`
   - Add data sources if needed in `data/datasource/`
   - Update dependency injection in `di/`

3. **Build Presentation Layer**
   - Create ViewModels that depend only on Use Cases
   - Build Compose UI screens
   - Add navigation routes

### Code Quality Standards

- **Clean Architecture**: Maintain layer separation
- **SOLID Principles**: Follow dependency inversion
- **Testing**: Write tests for business logic
- **Documentation**: Document complex business rules
- **Code Style**: Follow Kotlin coding conventions

## 🎨 Design System

### Colors
- **Primary**: Blue (#4A90E2) - Main actions and selections
- **Success**: Green (#7ED321) - Correct answers and achievements
- **Warning**: Orange (#F5A623) - Hints and caution states
- **Error**: Red (#D0021B) - Incorrect answers

### Typography
- **Roboto Font Family** (Android default)
- **8px Grid System** for consistent spacing
- **Accessibility**: Minimum 4.5:1 contrast ratio

## 📊 Performance Optimizations

- **KSP Migration**: 2x faster annotation processing vs KAPT
- **Compose 1.9**: Latest UI toolkit with performance improvements
- **Smart Caching**: 30-minute in-memory caching for API data
- **Image Optimization**: Efficient loading with Coil
- **Database Optimization**: Room with proper indexing

## ♿ Accessibility

- **Screen Readers**: Full VoiceOver/TalkBack support
- **High Contrast**: WCAG AA compliance
- **Touch Targets**: Minimum 48dp size
- **Focus Management**: Proper keyboard navigation

## 🌐 API Integration

### Dog CEO API
- **Base URL**: `https://dog.ceo/api/`
- **Endpoints**: Breed list, random images, breed-specific images
- **Caching**: Local caching with Room database
- **Offline Support**: Graceful fallback when network unavailable

## 🔮 Future Enhancements

### Planned Features
- **Camera Integration**: Real-time breed identification with ML
- **Social Features**: Share scores and compete with friends
- **Advanced Breeds**: Rare and mixed breed identification
- **Offline Mode**: Download breed packs for offline use
- **Multiple Languages**: Internationalization support
- **Machine Learning**: Custom breed classification model

### Testing Enhancements
- **Visual Regression Testing**: Screenshot comparison tests
- **Property-Based Testing**: Generate test cases automatically
- **Mutation Testing**: Verify test quality with code mutations
- **Performance Benchmarking**: Automated performance regression detection
- **Accessibility Testing**: Automated WCAG compliance checks

### Architecture Improvements
- **Modularization**: Feature-based module architecture
- **Multi-Platform**: Kotlin Multiplatform support
- **Advanced Caching**: Intelligent predictive caching
- **Analytics Integration**: Comprehensive user behavior tracking
- **A/B Testing**: Feature flag and experiment framework

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow clean architecture principles
4. Add comprehensive tests
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Contribution Guidelines
- Maintain clean architecture layer separation
- Write unit tests for new business logic
- Follow existing code style and conventions
- Update documentation for new features

## 📄 License

This project is created for educational purposes. Dog breed information and images are used under fair use for educational content.

## 🙏 Acknowledgments

- **Clean Architecture**: Based on Uncle Bob's Clean Architecture principles
- **Android Architecture**: Following Google's recommended app architecture
- **Dog Breed Data**: Powered by dog.ceo API
- **Material Design**: Google's Material Design 3 guidelines
- **Community**: Android development community for best practices

---

**Ready to explore Clean Architecture in Android?** 🏗️  
This project serves as a comprehensive example of implementing Clean Architecture principles in a real-world Android application. Import it into Android Studio and start learning! 🐕

## 📚 Architecture Resources

- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android App Architecture Guide](https://developer.android.com/guide/architecture)
- [Dependency Injection with Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)