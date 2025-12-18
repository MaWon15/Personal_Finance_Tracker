# Personal Finance Tracker (Android)

## Overview
**Personal Finance Tracker** is an Android app that helps users track **income and expenses**, organize spending with **categories**, and view quick insights on a **Dashboard** (balance, recent transactions, and spending breakdown by category with a pie chart).

Built with **Jetpack Compose** and **Room Database**, and secured with **Firebase Email/Password Authentication**.

---

## What This Project Satisfies (Course Requirements)

### 1) Authentication (Firebase Email/Password)
- ✅ Email/Password **Sign Up** and **Login**
- ✅ **Persistent login** (user stays logged in after app restart)
- ✅ **Auth state management** via `AuthViewModel`
- ✅ **Protected routes** (auth graph vs main graph)
- ✅ **Sign out** from Profile/Settings
- ✅ **Validation**
  - Email format validation
  - Password minimum length (≥ 6)
- ✅ **Loading + error handling**
  - Loading indicators during auth actions
  - User-friendly error messages

### 2) Data Persistence (Room Database)
- ✅ Local persistence using **Room**
- ✅ **Two related entities** with relationships:
  - `Transaction`
  - `Category`
- ✅ Full CRUD:
  - Transactions: Create / Read / Update / Delete
  - Categories: Create / Read / Update / Delete
- ✅ Reactive updates using **Flow** (UI updates automatically when DB changes)

### 3) UI with Jetpack Compose
- ✅ Built with **Material 3** components & consistent styling
- ✅ Navigation using **Navigation Component (Compose)**
- ✅ App includes **functional screens (6–8+)**:
  - Login
  - Sign Up
  - Dashboard (Home)
  - Transactions List
  - Add Transaction
  - Edit Transaction
  - Categories Management
  - Profile/Settings (Sign out)

### 4) Architecture
- ✅ **MVVM** separation of concerns
- ✅ **Repository pattern**
- ✅ **StateFlow / Flow** for state management + reactive UI
- ✅ **Coroutines** for async work

---

## Functional Screens (Screenshots)

> Put your screenshots inside: `./screenshots/`  
> Recommended filenames are below (you can rename them, just update the paths).

### 1) Login Screen
![Login Screen](screenshots/01_login.png)

### 2) Sign Up Screen
![Sign Up Screen](screenshots/02_signup.png)

### 3) Dashboard (Balance + Recent + Spending Pie Chart)
![Dashboard Screen](screenshots/03_dashboard.png)

### 4) Transactions List Screen
![Transactions List Screen](screenshots/04_transactions_list.png)

### 5) Add Transaction Screen
![Add Transaction Screen](screenshots/05_add_transaction.png)

### 6) Edit Transaction Screen
![Edit Transaction Screen](screenshots/06_edit_transaction.png)

### 7) Categories Management Screen (CRUD)
![Categories Screen](screenshots/07_categories.png)

### 8) Profile / Settings Screen (Sign Out)
![Profile Screen](screenshots/08_profile.png)

---

## Tech Stack
- **Kotlin**
- **Jetpack Compose (Material 3)**
- **Navigation Compose**
- **Room Database**
- **Firebase Authentication (Email/Password)**
- **Coroutines + Flow/StateFlow**

---

## Getting Started (Local Run)
1. Clone the repo
2. Open in **Android Studio**
3. Create a Firebase project and enable **Email/Password** provider
4. Download `google-services.json` and place it here:
   - `app/google-services.json`
5. Sync Gradle and run the app on an emulator/device

> Note: `google-services.json` is intentionally not committed to GitHub.
