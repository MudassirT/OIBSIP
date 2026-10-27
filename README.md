# Oasis Infobyte Summer Internship Program (OIBSIP) - Submissions

Welcome to the comprehensive submission repository for the **Oasis Infobyte Summer Internship Program (OIBSIP)**, completed by **Mudassir Ahmed**. This monorepo consolidates all **13 completed internship projects** across three domains: **Web Development & Designing** (Levels 1, 2, and 3), **Android App Development** (Tasks 1, 2, and 4), and **Java Development** (Tasks 1 and 4) into a structured, production-grade workspace.

---

## 👨‍💻 Intern Profile
- **Author:** Mudassir Ahmed
- **LinkedIn Profile:** [https://www.linkedin.com/in/mudassir-ahmed-TODO](https://www.linkedin.com/in/mudassir-ahmed-TODO)
- **Internship Program:** Oasis Infobyte Summer Internship Program (OIBSIP)
- **Tracks Completed:**
  - 🌐 **Web Development & Designing** (Levels 1, 2, and 3 — 8 Projects)
  - 📱 **Android App Development** (Tasks 1, 2, and 4 — 3 Projects)
  - ☕ **Java Development** (Tasks 1 and 4 — 2 Projects)

---

## 📂 Projects Overview

The table below outlines all 13 completed projects, their directory locations, technology stacks, internship tracks, concise descriptions, and placeholders for LinkedIn demonstration videos.

| # | Project Name | Directory Link | Domain / Task | Tech Stack | Description | Demo Video (LinkedIn) |
|---|--------------|----------------|---------------|------------|-------------|-----------------------|
| 1 | **NovaFlow Landing Page** | [WebDev-L1-LandingPage](./WebDev-L1-LandingPage/) | Web Dev: Level 1, Task 1 | HTML5, CSS3, Responsive Design | Modern SaaS landing page for an AI productivity platform featuring interactive product previews and pricing. | `TODO` |
| 2 | **Personal Portfolio** | [WebDev-L1-Portfolio](./WebDev-L1-Portfolio/) | Web Dev: Level 1, Task 2 | HTML5, CSS3, Responsive Layouts | High-impact personal developer portfolio showcasing skills, projects, and an interactive contact interface. | `TODO` |
| 3 | **Temperature Converter** | [WebDev-L1-TemperatureConverter](./WebDev-L1-TemperatureConverter/) | Web Dev: Level 1, Task 3 | HTML5, CSS3, JavaScript (ES6+) | Real-time bi-directional converter supporting Celsius, Fahrenheit, and Kelvin with input validation. | `TODO` |
| 4 | **Interactive Calculator** | [WebDev-L2-Calculator](./WebDev-L2-Calculator/) | Web Dev: Level 2, Task 1 | HTML5, CSS3, JavaScript (ES6+) | Responsive calculator supporting standard arithmetic, chained calculations, and keyboard shortcuts. | `TODO` |
| 5 | **Tribute Page** | [WebDev-L2-TributePage](./WebDev-L2-TributePage/) | Web Dev: Level 2, Task 2 | HTML5, CSS3, Responsive Design | Commemorative tribute page honoring Quaid-e-Azam Muhammad Ali Jinnah with an interactive milestone timeline. | `TODO` |
| 6 | **Task Flow To-Do App** | [WebDev-L2-TodoApp](./WebDev-L2-TodoApp/) | Web Dev: Level 2, Task 3 | HTML5, CSS3, JavaScript, LocalStorage | Client-side task manager with active/completed filters, priority tags, animations, and persistent storage. | `TODO` |
| 7 | **SecureGate Login Auth** | [WebDev-L2-LoginAuth](./WebDev-L2-LoginAuth/) | Web Dev: Level 2, Task 4 | Node.js, Express, EJS, bcryptjs, Express-Session | Production-ready authentication system with password hashing, session management, and protected routes. | `TODO` |
| 8 | **PizzaForge Platform** | [WebDev-L3-PizzaDelivery](./WebDev-L3-PizzaDelivery/) | Web Dev: Level 3, Task 1 | MERN Stack (React, Node.js, Express, MongoDB), Socket.io, Razorpay | Full-stack pizza ordering platform with dynamic custom builder, live order tracking, and admin inventory alerts. | `TODO` |
| 9 | **UnitSwitch Converter** | [Android-Task1-UnitConverter](./Android-Task1-UnitConverter/) | Android Dev: Task 1 | Java, Android SDK, Material 3, Gradle | Native Android multi-unit & temperature converter with live calculation, unit swapping, and absolute-zero boundary protection. | `TODO` |
| 10 | **TaskMaster To-Do & Auth** | [Android-Task2-TodoLoginApp](./Android-Task2-TodoLoginApp/) | Android Dev: Task 2 | Java, Android SDK, Material 3, SQLite, Gradle | Native Android application featuring user registration, secure authentication, and an SQLite-backed task manager. | `TODO` |
| 11 | **BrainBlitz Quiz App** | [Android-Task4-QuizApp](./Android-Task4-QuizApp/) | Android Dev: Task 4 | Java, Android SDK, Material 3, Gradle | Native Android Computer Science & Tech quiz app with 25 curated questions, dynamic option shuffling, and instant feedback. | `TODO` |
| 12 | **RailBook Reservation System** | [Java-Task1-OnlineReservationSystem](./Java-Task1-OnlineReservationSystem/) | Java Dev: Task 1 | Java 17, Swing, SQLite, FlatLaf, Maven | Desktop train reservation system with PBKDF2 authentication, PNR generation, cancellation audit log, and ticket export. | `TODO` |
| 13 | **ExamPortal Examination System** | [Java-Task4-OnlineExaminationSystem](./Java-Task4-OnlineExaminationSystem/) | Java Dev: Task 4 | Java 17, Swing, SQLite, Maven | Robust desktop computer-based examination system with drift-free countdown timer, randomized questions, auto-submit, and analytics. | `TODO` |

---

## 🚀 How to Run the Projects

Each project in this repository is completely self-contained. For detailed installation and execution instructions, please refer to the dedicated `README.md` inside each project's directory:

### 1. Static Web Projects (Level 1: Tasks 1-3, Level 2: Tasks 1-3)
- Navigate into the respective directory:
  ```bash
  cd WebDev-L1-LandingPage # or WebDev-L1-Portfolio, WebDev-L1-TemperatureConverter, WebDev-L2-Calculator, WebDev-L2-TributePage, WebDev-L2-TodoApp
  ```
- Open `index.html` directly in any modern browser, or launch via Live Server / `npx serve`.

### 2. SecureGate Authentication (WebDev-L2-LoginAuth)
- Navigate to `WebDev-L2-LoginAuth/`.
- Copy template configuration: `cp .env.example .env`.
- Install dependencies: `npm install`.
- Start server: `npm start` (runs on `http://localhost:3000`).

### 3. PizzaForge Full-Stack Platform (WebDev-L3-PizzaDelivery)
- Navigate to `WebDev-L3-PizzaDelivery/`.
- Copy `.env.example` to `.env` in both `client/` and `server/` subfolders.
- Install dependencies: `npm run install:all`.
- Start both server and client concurrently: `npm run dev`.

### 4. Native Android Applications (Android-Task1-UnitConverter, Android-Task2-TodoLoginApp, Android-Task4-QuizApp)
- Open Android Studio and choose **Open**.
- Select the desired project folder (e.g. `Android-Task1-UnitConverter`, `Android-Task2-TodoLoginApp`, or `Android-Task4-QuizApp`).
- Allow Gradle to sync dependencies.
- Run on an Android Emulator or physical device (Min SDK 24-26+, Android 8.0+).

### 5. Java Desktop Swing Applications (Java-Task1-OnlineReservationSystem, Java-Task4-OnlineExaminationSystem)
- Ensure Java 17+ and Maven are installed.
- Navigate to the respective project directory:
  ```bash
  cd Java-Task1-OnlineReservationSystem # or Java-Task4-OnlineExaminationSystem
  ```
- Build and run using Maven:
  ```bash
  mvn clean package
  java -jar target/railbook.jar    # For Task 1 (RailBook)
  java -jar target/examportal.jar  # For Task 4 (ExamPortal)
  ```

---

## 🔒 Security & Environment Variables

- **No Secrets Committed:** In accordance with professional DevOps and security standards, live secrets, API credentials, private keys, keystores, and active `.env` files are never tracked or committed to this repository.
- **Environment Configuration:** Each project requiring configuration provides a pre-configured `.env.example` file with documented placeholders.
- **Sensitive Ignored Files:** Root and project-level `.gitignore` rules strictly prevent accidental tracking of `node_modules/`, `build/`, `target/`, `.gradle/`, SQLite `.db` databases, and `.env` files.

---

## 📸 Project Visuals & Screenshots

Every project folder includes a dedicated `screenshots/` directory containing high-resolution interface captures and workflow demonstrations validating design fidelity and functionality.
