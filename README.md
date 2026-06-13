# GymTrainer

A gym training app for Android built with Kotlin, Jetpack Compose, and Room.

Features:
- Guided Plans: pre-built programs with workouts, daily macros, and meals
- Manual Workout Builder: cascading muscle group -> exercise dropdowns, each exercise
  labeled with the exact sub-muscle it targets (e.g. Incline Dumbbell Press -> Upper Chest)
- Granular progress tracking: every set is logged as exact weight x reps, persisted
  in a local Room database, with last-session reference and full per-exercise history

## Build an APK

1. Install Android Studio (Koala or newer) from https://developer.android.com/studio
2. File > Open... and select this GymTrainer folder. Let Gradle sync finish
   (first sync downloads dependencies; accept any prompt to upgrade the
   Android Gradle Plugin if Studio suggests it).
3. Debug APK for your own device:
   Build > Build App Bundle(s) / APK(s) > Build APK(s)
   Output: app/build/outputs/apk/debug/app-debug.apk
4. Install on a physical device:
   - Enable Developer Options (tap Build Number 7 times in Settings > About phone)
   - Enable USB debugging, plug the phone in, press Run, OR
   - Copy app-debug.apk to the phone and open it (allow "install unknown apps")

Command line alternative:
  ./gradlew assembleDebug        (macOS/Linux)
  gradlew.bat assembleDebug      (Windows)

## Project layout

  app/src/main/java/com/example/gymtrainer/
    data/Entities.kt    Room entities (schema)
    data/Daos.kt        Queries incl. joins for the UI
    data/AppDb.kt       Database singleton + seed data (exercise library, guided plans)
    GymViewModel.kt     State holder bridging Room and Compose
    MainActivity.kt     Navigation + home screen
    ui/BuilderScreen.kt   Manual Workout Builder (cascading dropdowns)
    ui/SessionScreen.kt   Active session set-by-set logging
    ui/ProgressScreen.kt  Per-exercise history / progressive overload
    ui/GuidedPlansScreen.kt
    ui/MyPlansScreen.kt
