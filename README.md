# TaskSaver - Personal Task & Goal Planner 🚀

TaskSaver is an Android task management application designed to help users structure daily tasks, manage productivity goals, and maintain habit streaks. Built natively in Kotlin using modern Android Architecture Components, Room database persistence, Retrofit API integration, and WorkManager background synchronization.

---

## 📱 Features & Highlights

- **Authentication System**: Secure user registration and login with local session persistence via `SharedPreferences`.
- **Data Isolation**: Clean dashboard per session, reset on logout.
- **Task Management**: Create tasks with titles, descriptions, categories, and high/medium/low priority tags.
- **Priority Filtering**: Dynamic tab layout (`High`, `Medium`, `Low`) for real-time list filtering via RecyclerView.
- **Offline Mode & Sync**: Local storage through Room SQLite database with WorkManager background sync.
- **Productivity Dashboard**: Habit progress indicator and completed task streak counter (`🔥 Tasks Done`).

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Material Design 3, View Binding, XML Layouts
- **Architecture**: MVVM (Model-View-ViewModel), Coroutines, Kotlin Flow
- **Local Database**: Room Persistence Library
- **Networking**: Retrofit, Gson Converter
- **Background Work**: WorkManager
- **Continuous Integration**: GitHub Actions CI/CD pipeline

---

## 🎥 Video Demonstration


## 📄 Submission Checklist

- [x] Complete Kotlin source code pushed to GitHub (no ZIP files)
- [x] In-code documentation and comments added
- [x] Automated testing configured with GitHub Actions (`build.yml`)
- [x] Comprehensive README file uploaded to GitHub
- [x] Voice-over video walkthrough uploaded and linked above
