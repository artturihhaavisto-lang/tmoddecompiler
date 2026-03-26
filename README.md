# OpenJugg

AI-driven strength training program generator for powerlifters and strength athletes.

## What It Does

OpenJugg creates personalized, periodized training programs based on your profile, goals, and current maxes. It handles everything from phase structure to weekly volume, exercise selection, and daily load prescription — all grounded in sports science principles like RPE-based autoregulation and volume landmarks (MEV/MAV/MRV).

## Features

- **Program generation** — Full periodized programs (Hypertrophy → Strength → Peaking → Deload) tailored to your experience level and goals
- **Goal types** — Powerlifting, Powerbuilding, or Power Combo
- **Smart exercise selection** — Picks exercises that target your specific weak points in each lift
- **Load progression** — Weekly rep/RPE/intensity prescriptions that progress automatically through each phase
- **1RM estimation** — Epley/Brzycki formulas + RPE chart; reverse-calculates suggested working weights
- **Readiness adjustment** — Modifies session difficulty based on sleep, nutrition, motivation, and soreness
- **Set logging** — Tracks actual weight/reps/RPE and maintains performance history
- **Meet date support** — Structures periodization backward from a competition date
- **3–6 day splits** — Distributes lifts intelligently across your available training days

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin Multiplatform 2.0 |
| UI | Compose Multiplatform 1.6 |
| Database | SQLDelight 2.0 |
| DI | Koin 3.5 |
| Serialization | Kotlinx Serialization |
| Async | Kotlinx Coroutines + Flow |
| Platforms | Android, Desktop (JVM), iOS |

## Getting Started

**Prerequisites:** Java 17+, Android SDK (API 26+) for Android builds, Xcode + macOS for iOS

```bash
# Build everything
./gradlew build

# Run desktop app
./gradlew :desktopApp:run

# Run tests
./gradlew :shared:test

# Build native installers (deb, rpm, dmg, msi)
./gradlew :desktopApp:packageDistribution
```

## Project Structure

```
tmoddecompiler/
├── shared/                         # Shared multiplatform business logic
│   └── src/commonMain/kotlin/com/openjugg/
│       ├── domain/
│       │   ├── engine/             # Core algorithms
│       │   │   ├── ProgramGenerator.kt
│       │   │   ├── PeriodizationPlanner.kt
│       │   │   ├── VolumeCalculator.kt
│       │   │   ├── FrequencyDistributor.kt
│       │   │   ├── ExerciseSelector.kt
│       │   │   ├── LoadProgressionManager.kt
│       │   │   ├── OneRepMaxEstimator.kt
│       │   │   ├── RpeAutoRegulator.kt
│       │   │   └── ReadinessAdjuster.kt
│       │   ├── model/              # Domain models
│       │   ├── usecase/            # Use cases
│       │   └── repository/         # Repository interfaces
│       ├── data/
│       │   ├── repository/         # Repository implementations
│       │   ├── seed/               # Exercise library (30 exercises)
│       │   └── db/                 # SQLDelight + mappers
│       └── di/                     # Koin modules
├── androidApp/                     # Android entry point
└── desktopApp/                     # Desktop entry point
```

## Architecture

Clean Architecture with a shared Kotlin Multiplatform core:

- **Domain layer** — Pure business logic: engines, use cases, repository interfaces
- **Data layer** — SQLDelight-backed repositories, exercise seed data, JSON serializers
- **Presentation layer** — Platform-specific Compose UI (Android + Desktop)

The exercise library covers 10 exercises each for Squat, Bench, and Deadlift, with detailed cues, common mistakes, and weak point tags used during selection.

## License

MIT
