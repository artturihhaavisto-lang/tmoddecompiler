# OpenJugg

A cross-platform powerlifting training program generator for Android, iOS, and Desktop.

## Features

- **Intelligent Program Generation**: Generates periodized training programs (hypertrophy → strength → peaking → deload cycles)
- **Exercise Library**: 100+ exercises with weak point targeting
- **Auto-Regulation**: Real-time RPE-based load adjustments during workouts
- **Readiness Scoring**: Pre-workout assessment with dynamic adjustments
- **Session Logging**: Track sets, reps, weights, and RPE in real-time
- **Cross-Platform**: Single codebase for Android, iOS, Linux, macOS, and Windows

## Architecture

**Backend (100% shared code)**
- Domain models (pure Kotlin)
- 9 advanced algorithms (1RM estimation, volume calculation, periodization, etc.)
- SQLDelight database (type-safe queries)
- Koin dependency injection

**Platforms**
- `shared/`: Multiplatform Kotlin code
- `androidApp/`: Android shell
- `desktopApp/`: Compose Desktop (Linux/macOS/Windows)
- `iosApp/`: iOS shell (Xcode)

## Building

### Prerequisites
- Kotlin 2.0+
- Android SDK (for Android builds)
- Xcode (for iOS builds)

### Android
```bash
./gradlew androidApp:build
```

### Desktop
```bash
./gradlew desktopApp:run
```

### iOS
```bash
xed iosApp/
# Build from Xcode
```

## Project Structure

```
shared/src/
├── commonMain/
│   ├── domain/
│   │   ├── model/        # Data classes (UserProfile, Exercise, Program, etc.)
│   │   ├── engine/       # 9 algorithms for program generation
│   │   ├── repository/   # Repository interfaces
│   │   └── usecase/      # High-level business logic
│   ├── data/
│   │   ├── db/           # SQLDelight mappers, serializers
│   │   ├── repository/   # Repository implementations
│   │   └── seed/         # Exercise library seed data
│   ├── di/               # Koin modules
│   └── sqldelight/       # 7 .sq schema files
├── androidMain/
├── iosMain/
└── desktopMain/          # Platform-specific drivers
```

## Key Algorithms

1. **OneRepMaxEstimator** - Calculate 1RM from rep max + RPE
2. **VolumeCalculator** - MEV/MRV for periodization phases
3. **PeriodizationPlanner** - 4-phase cycles (hypertrophy→strength→peaking→deload)
4. **FrequencyDistributor** - Optimal training split (3/4/5/6 days/week)
5. **ExerciseSelector** - Intelligent variation selection based on weak points
6. **RpeAutoRegulator** - Intra-session load adjustments
7. **ReadinessAdjuster** - Pre-workout scoring + session modifications
8. **LoadProgressionManager** - Automatic intensity/volume progression
9. **ProgramGenerator** - Master orchestrator for full program generation

## Development

Run all tests:
```bash
./gradlew commonTest
```

Format code:
```bash
./gradlew ktlintFormat
```

## Database

SQLDelight auto-generates type-safe Kotlin code from `.sq` files.

Schema locations:
- `shared/src/commonMain/sqldelight/com/openjugg/db/*.sq`

Drivers:
- **Android**: AndroidSqliteDriver (uses app Context)
- **iOS**: NativeSqliteDriver (uses native SQLite)
- **Desktop**: JdbcSqliteDriver (stores at `~/.openjugg/openjugg.db`)

## Next Steps

- [ ] Compose Multiplatform UI (shared across all platforms)
- [ ] Cloud sync (future)
- [ ] AI-powered weak point analysis (future)

## License

Unlicensed
