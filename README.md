# Precision Calc — Senior-Grade Jetpack Compose Calculator with Secure Auth

Precision Calc is a complete, production-ready Android Calculator application built using Kotlin and Jetpack Compose. This app implements an offline-first MVVM architecture featuring Material Design 3, persistent calculation history using Room database, responsive layouts supporting Portrait/Landscape modes, memory registers, a robust math parser with floating-point precision guarantees, and secure local authentication with persistent sessions.

---

## 🔒 Security & Authentication Design (Day 2)

We have implemented **Local Authentication** with secure salted hashing to manage user profiles on-device.

### 🛡️ Local Auth vs. Firebase Authentication (Trade-offs)
- **Offline Autonomy**: Using a local SQLite Room database keeps the application 100% self-contained and offline-first. Users can registers and authenticate without any cell coverage or external network calls.
- **Salted Password Hashing**: To prevent serious security leaks, **plain text passwords are never stored**. On registration, a cryptographically strong 16-byte random **Salt** is generated via `SecureRandom`. The password is then hashed using a standard **SHA-256** message digest mixed with the salt. On login, the entered password is salted and hashed with the saved salt, and then verified against the database.
- **Session Persistence**: Login states are saved in a **Jetpack DataStore Preferences** file rather than lightweight database fields. DataStore performs safe, non-blocking, and thread-safe disk reads/writes on a background Dispatcher.
- **Backstack Safety Gates**: The Calculator screen is protected by dynamic Compose Navigation. On app startup, DataStore is checked asynchronously, redirecting to Login if empty. Upon clicking logout, the persisted preference is cleared, and the backstack is completely popped, preventing any unauthorized back-navigation.

---

## 🚀 Key Features

### 1. Secure Authentication & Session Management
- **Registration**: Allows registration of new local accounts with username validation and password strength validation (minimum 4 characters).
- **Secure Storage**: Offloads registration data directly into a local Room table with password hashes and random salts.
- **Login Verification**: Compares entered credentials against stored hash values safely inside a coroutine background scope.
- **Session Persistence**: Saves the active user state inside Android Preferences DataStore so it survives app restarts.
- **Gated Navigation & Backstack Popping**: Uses Compose Navigation Compose to swap login, register, and workspace views seamlessly. Clears navigation history upon logging out so unauthorized back-navigation is impossible.

### 2. Robust Arithmetic & Parser
- **Mathematical Precedence**: Accurately handles operator precedence (e.g., `5 + 4 × 3` results in `17`, not `27`) using a custom **Recursive Descent Parser**.
- **Edge-Case Protections**: Prevents divide-by-zero crashes gracefully by outputting customized safe error states.
- **Large Number Precision**: Correctly handles floating-point calculations with high decimal accuracy via `BigDecimal` formatting, avoiding trailing floating-point artifacts (e.g., `0.30000000000000004` -> `0.3`).
- **Standard Functions**: Handles addition (`+`), subtraction (`-`), multiplication (`×`), division (`÷`), and percent (`%`).

### 3. Scientific & Modifier Functions
- **Trigonometric Functions**: Sine (`sin`), Cosine (`cos`), and Tangent (`tan`) in degrees.
- **Logarithmic Functions**: Common Logarithm (`log`) and Natural Logarithm (`ln`).
- **Mathematical Powers & Constants**: Power raising (`x^y`), squaring (`x^2`), square root (`√`), factorial (`x!`), reciprocal (`1/x`), and mathematical constants (Pi `π` and Euler's constant `e`).
- **Parentheses**: Proper sub-expression support with opening `(` and closing `)` brackets.

### 4. Memory Registers
- Fully functional memory keys including:
  - **MC**: Memory Clear (resets memory register to `0`).
  - **MR**: Memory Recall (loads memory register value to the primary display).
  - **M+**: Memory Add (adds current display value to memory register).
  - **M-**: Memory Subtract (subtracts current display value from memory register).

### 5. Advanced UI & UX Polish
- **Dynamic Orientation Layouts**:
  - **Portrait Mode**: Elegant standard keypad with an expandable scientific utility panel.
  - **Landscape Mode**: Automated, side-by-side view with scientific modifiers on the left and standard keypad keys on the right.
- **Calculation History (Room Database)**: A slide-up history sheet persistence mechanism. Tapping history rows loads past expressions back into the active calculator instance; individual items can be deleted, or all cleared at once.
- **Clipboard Integration**: Click or long-press on the primary display to copy results instantly to the system clipboard, with a visual toast message.
- **Haptic Tactile Feedback**: Integrated haptic feedback on key presses.
- **Visual Polish**: Built entirely on **Material 3 (M3)** with custom animated squircle keys, spring-based key transitions, custom color accents, and full edge-to-edge system bar safe drawing.

---

## 📂 Project Folder Structure

The project has been organized following MVVM and Clean Architecture principles:

```text
/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml          # System and configuration manifest
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── CalculatorDatabase.kt # Main Room database (Stores History & Users)
│   │   │   │   │   ├── UserDao.kt            # Room DAO for User credentials CRUD
│   │   │   │   │   ├── UserEntity.kt         # Secure user credential data model
│   │   │   │   │   ├── SecurityUtils.kt      # Cryptographic salt & SHA-256 hashing helper
│   │   │   │   │   ├── AuthRepository.kt     # Repository abstracting auth from data layer
│   │   │   │   │   ├── HistoryDao.kt         # Dao for history CRUD operations
│   │   │   │   │   ├── HistoryEntity.kt      # Historical record data model
│   │   │   │   │   └── HistoryRepository.kt  # Repository separating DB from VM
│   │   │   │   ├── ui/theme/
│   │   │   │   │   ├── Color.kt             # High-contrast palette constants
│   │   │   │   │   ├── Theme.kt             # Material 3 light/dark mode configuration
│   │   │   │   │   └── Type.kt              # Clean monospace and typography definitions
│   │   │   │   ├── CalculatorButton.kt      # Button action models & reusable key components
│   │   │   │   ├── CalculatorOperation.kt   # Operations enum mapping (+, -, ×, ÷)
│   │   │   │   ├── CalculatorScreen.kt      # Workspace screen with calculation layout & TopAppBar
│   │   │   │   ├── CalculatorState.kt       # State wrapper class (formulas, memory, values)
│   │   │   │   ├── CalculatorViewModel.kt   # MVVM ViewModel state machine for arithmetic
│   │   │   │   ├── AuthState.kt             # Sealed class hierarchy representing Login/Register states
│   │   │   │   ├── AuthViewModel.kt         # ViewModel managing user registration & sessions
│   │   │   │   ├── LoginScreen.kt           # Beautiful M3 login credential collection screen
│   │   │   │   ├── RegisterScreen.kt        # Custom registration layout with password matches
│   │   │   │   ├── MainActivity.kt          # App entrance, Navigation graph & VM assembly
│   │   │   │   └── Utils.kt                 # Custom mathematical expression parser
│   │   └── src/test/java/com/example/
│   │       ├── ExampleUnitTest.kt           # Math and parser logic unit tests
│   │       ├── ExampleRobolectricTest.kt    # Robolectric resource verification
│   │       ├── AuthUnitTest.kt              # Robolectric cryptographic hashing & salt validation
│   │       └── GreetingScreenshotTest.kt    # Roborazzi Visual Regression test
│   └── build.gradle.kts                     # Gradle configuration declaring datastore & navigation
└── metadata.json                            # AI Studio platform identification
```

---

## 🛠️ Step-by-Step Implementation Details

1. **Secure Hashing & Salts (`SecurityUtils.kt`)**: Implemented robust password encryption via cryptographically strong `SecureRandom` bytes. Salts are combined with passwords and hashed via SHA-256 using standard byte streams, preventing database breaches from exposing clear text.
2. **Session Preservation (`AuthRepository.kt`)**: Leveraged Jetpack DataStore Preferences to read and write active login tokens asynchronously and safely. This eliminates block-failures on main threads.
3. **Double-Security Gating (`MainActivity.kt`)**: Enforced a dynamic state-driven navigation architecture. A security interceptor in the `"calculator"` route guarantees instant re-routing back to `"login"` if the active session is terminated or null.
4. **State-Driven Views (`LoginScreen.kt` & `RegisterScreen.kt`)**: Designed clean, responsive entry screens using Outlined Text Fields, password visible-toggling toggles, and stateful error banners matching Material 3 patterns.
5. **Arithmetic state (`CalculatorState.kt` & `CalculatorViewModel.kt`)**: Standard arithmetic state flow.
6. **Room Database integration (`CalculatorDatabase.kt`)**: Integrated multi-entity Room architecture. All changes are validated inside a destructive fallback migration loop.
7. **Verification (`AuthUnitTest.kt`)**: Validates the cryptographic hashing algorithm under Robolectric JVM test runners.

---

## 🧪 Verification & Testing

Our test suite covers all expected mathematics and safety boundaries:
- **`testGenerateSalt_isUniqueAndNotEmpty()`**: Assures that salts are unique, non-empty, and randomized.
- **`testHashPassword_isConsistentAndSecure()`**: Confirms standard secure properties of SHA-256 deterministic hashes (verifies different passwords or salts produce distinct output, and no clear text leaks).
- **`testBasicArithmetic()`**: Addition, subtraction, multiplication, and division.
- **`testOperatorPrecedence()`**: Evaluates nested operations such as multiplication before addition.
- **`testPercentage()`**: Verifies `%` operations.
- **`testScientificFunctions()`**: Verifies trigonometric (in degrees), logarithms, factorials, and powers.
- **`testDivisionByZero()`**: Verifies safety exceptions.
- **`testInvalidInputs()`**: Ensures bad expressions throw appropriate exceptions safely.
