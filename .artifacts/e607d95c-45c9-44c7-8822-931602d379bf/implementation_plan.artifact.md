# Phase 1: Authentication Implementation Plan

This plan outlines the steps to implement a solid authentication foundation for the Tradenetwork application using Firebase, Jetpack Compose, and MVVM.

## User Review Required

> [!IMPORTANT]
> This phase requires manual Firebase project setup in the Google Cloud/Firebase Console. I will provide instructions, but you must download and place the `google-services.json` file yourself.

## Proposed Changes

### Configuration & Dependencies

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/gradle/libs.versions.toml)
Add Firebase, Navigation, and Lifecycle dependencies.

#### [MODIFY] [build.gradle.kts (Project)](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/build.gradle.kts)
Add Google Services plugin.

#### [MODIFY] [build.gradle.kts (App)](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/build.gradle.kts)
Apply Google Services plugin and add dependencies.

---

### Data Layer

#### [NEW] [User.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/data/model/User.kt)
Define the `User` data class for Firestore.

#### [NEW] [AuthRepository.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/data/repository/AuthRepository.kt)
Interface for login, register, logout, and password reset.

#### [NEW] [AuthRepositoryImpl.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/data/repository/AuthRepositoryImpl.kt)
Firebase implementation of the repository.

---

### UI & Navigation Layer

#### [NEW] [Screen.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/navigation/Screen.kt)
Define navigation routes (Splash, Login, Register, ForgotPassword, Home).

#### [NEW] [NavGraph.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/navigation/NavGraph.kt)
Compose Navigation setup with auth state guarding.

#### [NEW] [SplashScreen.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/ui/screens/splash/SplashScreen.kt)
Initial auth check and redirection.

#### [NEW] [LoginScreen.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/ui/screens/login/LoginScreen.kt)
Login form with validation and error handling.

#### [NEW] [RegisterScreen.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/ui/screens/register/RegisterScreen.kt)
Registration form with Firestore user creation.

#### [NEW] [ForgotPasswordScreen.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/ui/screens/forgotpassword/ForgotPasswordScreen.kt)
Password recovery screen.

#### [NEW] [HomeScreen.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/ui/screens/home/HomeScreen.kt)
Placeholder home screen with Logout button.

---

### Logic Layer (ViewModels)

#### [NEW] [AuthViewModel.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/viewmodel/AuthViewModel.kt)
Handles UI state and interactions for Login, Register, and ForgotPassword.

#### [NEW] [SplashViewModel.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/viewmodel/SplashViewModel.kt)
Checks current auth session.

---

### Main Activity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/MainActivity.kt)
Initialize Navigation and set up the content.

## Verification Plan

### Automated Tests
*   Run the app and verify navigation flows.
*   (Optional) Unit tests for `AuthRepositoryImpl` using a mock Firebase instance if time permits, but manual verification is primary here.

### Manual Verification
1.  **Firebase Setup**: Verify `google-services.json` is in `app/`.
2.  **Splash Screen**: Verify it redirects to Login (first run) or Home (if logged in).
3.  **Registration**:
    *   Test validation (empty name, invalid email, password mismatch).
    *   Verify successful registration creates a Firebase User AND a Firestore document.
4.  **Login**:
    *   Test invalid credentials.
    *   Verify successful login redirects to Home.
5.  **Password Recovery**: Verify email is sent.
6.  **Logout**: Verify it returns to Login.
7.  **Persistence**: Close and reopen app to verify session persistence.
