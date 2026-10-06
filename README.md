# CodeRoute 🚀

### Personalized DSA Learning & Practice Platform

CodeRoute is a full-stack platform designed to help students **learn, practice, and track Data Structures and Algorithms (DSA)** in a structured way.

It combines DSA problem practice, progress tracking, personalized recommendations, a LeetCode profile, and a DSA-focused To-Do system in one platform.

---

## ✨ Features

### 🔐 User Authentication

* User registration and login
* JWT-based authentication
* Secure user account management

### 🧩 DSA Problem Practice

* 170+ DSA problems
* Problems organized by topic
* Difficulty levels:

  * Beginner
  * Intermediate
  * Advanced
* Filter problems by:

  * Topic
  * Difficulty
  * Solved/Unsolved status

### 📊 Learning Progress

* Track problem attempts
* Track topic progress
* Monitor solved problems
* Personalized practice recommendations

### 🎯 Recommended Problems

CodeRoute recommends problems based on the user's practice and learning progress.

This helps users focus on topics where they need more practice.

### 💻 LeetCode Profile

Users can connect their LeetCode profile to their CodeRoute account.

The profile link is available from the user's account section without requiring the user's LeetCode password.

### ✅ DSA To-Do

A dedicated To-Do system for DSA practice.

Users can:

* Add DSA tasks
* Mark tasks as completed
* Delete tasks
* Track completed and remaining tasks
* View overall To-Do progress

---

## 🛠️ Tech Stack

### Frontend

* React
* JavaScript
* CSS
* Vite

### Backend

* Java
* Spring Boot
* Spring Security
* JWT
* Spring Data JPA
* Hibernate

### Database

* PostgreSQL

### Database Migration

* Flyway

### Development Tools

* Git
* GitHub
* Maven

---

## 🏗️ Architecture

```text
┌─────────────────────┐
│   React Frontend    │
│      Vite           │
└──────────┬──────────┘
           │
           │ REST API
           ▼
┌─────────────────────┐
│   Spring Boot API   │
│                     │
│  JWT Authentication │
│  Business Logic     │
│  JPA / Hibernate    │
└──────────┬──────────┘
           │
           │ JDBC
           ▼
┌─────────────────────┐
│     PostgreSQL      │
│                     │
│ Users               │
│ Problems             │
│ Attempts             │
│ Topics               │
│ Progress             │
│ DSA To-Do            │
└─────────────────────┘
```

---

## 📁 Project Structure

```text
CodeRoute - End Term Project Mern/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/coderoute/
│   │       │   ├── auth/
│   │       │   ├── entity/
│   │       │   ├── repository/
│   │       │   ├── service/
│   │       │   ├── problem/
│   │       │   ├── leetcode/
│   │       │   ├── todo/
│   │       │   └── dto/
│   │       │
│   │       └── resources/
│   │           └── db/migration/
│   │
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── auth/
│   │   ├── components/
│   │   ├── pages/
│   │   └── ...
│   │
│   ├── package.json
│   └── vite.config.*
│
└── README.md
```

---

## 🗄️ Database

CodeRoute uses PostgreSQL with Flyway for database version control.

The project includes migrations for:

* Learning schema
* Problem catalog
* Expanded problem catalog
* Topic prerequisite graph
* LeetCode profile
* DSA To-Do

Flyway ensures database changes are applied in the correct order.

---

## 🔑 Authentication Flow

```text
User
  ↓
Sign Up
  ↓
Account Created
  ↓
Login
  ↓
Backend verifies credentials
  ↓
JWT generated
  ↓
Frontend stores authentication state
  ↓
Authenticated API requests
```

Passwords and JWT secrets should be stored using environment variables and should never be committed to GitHub.

---

## 💻 Getting Started

### Prerequisites

Install:

* Java
* PostgreSQL
* Node.js
* Git

---

## 1. Clone the Repository

```bash
git clone https://github.com/angel25bcs10712-stack/Mern---End-Term-Project-Term-5.git
cd Mern---End-Term-Project-Term-5
```

---

## 2. Start PostgreSQL

Make sure PostgreSQL is running and the CodeRoute database exists.

Example:

```text
Database: coderoute
Username: postgres
```

Configure the required database and JWT environment variables locally.

**Never commit `.env` files or real credentials.**

---

## 3. Start Backend

Open a terminal:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### Health Check

```powershell
Invoke-WebRequest http://localhost:8080/api/health -UseBasicParsing
```

Expected response:

```json
{
  "status": "UP"
}
```

---

## 4. Start Frontend

Open a **second terminal**:

```powershell
cd frontend
npm install
npm run dev
```

Then open the URL shown by Vite, normally:

```text
http://localhost:5173
```

---

## 🔒 Environment Variables

Create your local environment configuration separately.

Example:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
FLYWAY_ENABLED=true
```

Do not put actual passwords, JWT secrets, or API keys in this README or GitHub.

---

## 🧪 Testing the Application

After starting both servers, verify:

### Backend

```text
http://localhost:8080/api/health
```

### Frontend

```text
http://localhost:5173
```

Then test:

* User registration
* User login
* Problems page
* Problem filtering
* Problem progress
* Recommended problems
* LeetCode profile
* DSA To-Do
* To-Do progress

---

## 📌 Main User Flow

```text
Create Account
      ↓
Login
      ↓
Dashboard
      ↓
Explore DSA Problems
      ↓
Solve Problems
      ↓
Track Progress
      ↓
Get Recommendations
      ↓
Manage DSA To-Do
      ↓
View LeetCode Profile
```

---

## 🚀 Deployment

The application can be deployed as separate services:

```text
React Frontend
      ↓
Spring Boot Backend
      ↓
Production PostgreSQL
```

The frontend and backend should be configured with the appropriate production environment variables.

Database credentials and JWT secrets must be stored securely in the deployment platform's environment-variable settings.

---

## 🔮 Future Improvements

Possible future improvements include:

* More advanced personalized recommendations
* Detailed learning analytics
* Contest preparation plans
* Better LeetCode synchronization
* Daily DSA learning plans
* Progress streaks
* Email notifications
* Production monitoring

---



## 👨‍💻 Author

**Angel Singh**

---

## ⭐ Project

If you find CodeRoute useful, consider giving the repository a ⭐ on GitHub.
