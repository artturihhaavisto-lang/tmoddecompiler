# OpenJugg

The Juggernaut Method — automated, local, and open source.

## What It Is

The [Juggernaut Method](https://www.jtsstrength.com/juggernaut-method/) is one of the most respected periodization systems in powerlifting, developed by Chad Wesley Smith. The problem: running it correctly requires manually tracking waves, calculating weights, managing phase transitions, and adjusting for fatigue. Most people just wing it.

OpenJugg implements the full Juggernaut methodology as software. You enter your maxes and available training days, and it generates your entire program — every session, every set, every weight. Everything the method prescribes, handled automatically. No spreadsheets, no subscription, runs entirely on your device.

## What It Automates

The Juggernaut Method has a lot of moving parts. OpenJugg handles all of them:

- **Phase structure** — Hypertrophy → Strength → Peaking → Deload, sequenced and timed automatically based on your experience level and goal
- **Wave progression** — Rep targets, RPE, and intensity advance week-to-week within each phase without you touching a spreadsheet
- **Weight prescription** — Every working set has a suggested weight calculated from your maxes and the day's RPE target
- **Exercise selection** — Accessories are chosen based on your stated weak points in each lift (e.g. quad weakness → more front squat, hip flexor work)
- **Autoregulation** — Log your readiness (sleep, soreness, nutrition) and the session adjusts difficulty accordingly
- **Meet prep** — Give it a competition date and it works the periodization backward so you peak at the right time
- **3–6 day splits** — Frequency and lift distribution adapt to however many days you have available

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
