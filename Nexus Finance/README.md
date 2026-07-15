# Nexus Finance

Nexus Finance is an offline-first Android personal finance tracker built for a university software development course. The app helps users record income and expenses, monitor monthly category budgets, review cashflow analytics, generate optional AI finance reports, and create optional personal backups with Google/Gmail sign-in.

The project uses the standard Android XML/View stack with Kotlin, ViewBinding, Material Components, and SQLite. It does not require cloud login for normal use.

## Main Features

- Add, edit, and delete income or expense transactions.
- Filter transactions by All, Today, Week, Month, or Year.
- Set a user-selected default filter.
- Create, edit, and delete monthly category budgets.
- View budget progress, remaining budget, and overspending states.
- View dashboard analytics such as balance, income, expense, savings rate, top category, daily spend pace, and cashflow trend.
- View category spending breakdown with custom visual components.
- Store preferred currency locally.
- Store a private OpenRouter API key locally for optional AI reports.
- Generate optional AI financial intelligence reports using OpenRouter model `openrouter/free`.
- Sign in with Gmail/Google only when the user wants backup.
- Back up and restore transactions, budgets, and currency settings through Google Drive app data.

## Technology Stack

- Language: Kotlin
- UI: Android XML layouts with ViewBinding
- Design: Material Components / Material 3 theme
- Local database: SQLite through `SQLiteOpenHelper`
- Async work: Kotlin Coroutines
- Authentication: Google Sign-In
- Backup storage: Google Drive `appDataFolder`
- AI provider: OpenRouter chat completions API
- Tests: JUnit and Android instrumented tests

## Project Structure

```text
app/src/main/java/com/sezanx/nexusfinance/
  MainActivity.kt                         Main screen, dialogs, backup, AI, UI orchestration
  data/
    FinanceDatabaseHelper.kt              SQLite schema and CRUD
    FinanceRepository.kt                  Repository layer for app data
    FinanceAnalytics.kt                   Analytics and budget calculations
    FinanceBackupCodec.kt                 Backup JSON encode/decode
    DriveBackupClient.kt                  Google Drive app data upload/download
  model/
    FinanceModels.kt                      Data models and enums
  ui/
    TransactionAdapter.kt                 Transaction RecyclerView adapter
    BudgetAdapter.kt                      Budget RecyclerView adapter
    CategoryTotalAdapter.kt               Category breakdown adapter
    CashflowTrendView.kt                  Custom cashflow chart view
    CategoryRingView.kt                   Custom category ring view
    MoneyFormatter.kt                     Currency formatting helper
app/src/main/res/layout/                  XML screens, dialogs, and list items
app/src/test/                             Unit tests
app/src/androidTest/                      Instrumented database tests
docs/PROJECT_DOCUMENTATION.md             Full course documentation
```

## Setup

1. Open the project in Android Studio.
2. Make sure the Android SDK path exists in `local.properties`.
3. Sync Gradle.
4. Run the `app` configuration on an emulator or Android device.

Optional OpenRouter API key:

```properties
OPENROUTER_API_KEY=your_key_here
```

The user can also add the API key inside the app from Settings. In-app keys stay on the device and are not included in backup files.

Optional Google backup setup:

1. Create an Android OAuth client in Google Cloud.
2. Use package name `com.sezanx.nexusfinance`.
3. Add the SHA-1 certificate fingerprint for the debug or release keystore being used.
4. Enable the Google Drive API.
5. The app requests only the Drive app data scope.

## Build and Test

PowerShell:

```powershell
$env:GRADLE_USER_HOME = "D:\Android-Apps-DEv\NexusFinance\.gradle-user-home"
$env:ANDROID_USER_HOME = "D:\Android-Apps-DEv\NexusFinance\.android-user-home"
.\gradlew.bat --no-daemon "-Dkotlin.compiler.execution.strategy=in-process" :app:assembleDebug
.\gradlew.bat --no-daemon "-Dkotlin.compiler.execution.strategy=in-process" :app:testDebugUnitTest :app:lintDebug
```

Instrumented tests require an emulator or connected Android device:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Documentation

The full university-style project documentation is available at:

- `docs/PROJECT_DOCUMENTATION.md`

