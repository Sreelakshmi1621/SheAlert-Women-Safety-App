# 🚨 SheAlert - Trusted Check-In Based Women Safety Mobile Application

SheAlert is an Android-based women safety application designed to enhance personal security through trusted contacts, SOS alerts, safety check-ins, location sharing, and route awareness. The application helps users stay connected with trusted contacts and enables rapid emergency assistance during critical situations.

---

## 📱 Overview

SheAlert combines emergency communication, trusted contact management, safety monitoring, and location-based assistance into a single mobile application.

Users can schedule safety check-ins, receive reminders, manage trusted contacts, view alert history, and automatically trigger SOS alerts if they fail to confirm their safety within a grace period.

---

## ✨ Features

### 🔐 User Authentication
- User Registration and Login
- Email Verification
- Password Reset
- Firebase Authentication Integration

### 🚨 SOS Emergency System
- Manual SOS activation
- 5-second confirmation countdown before sending SOS
- Emergency SMS alerts to trusted contacts
- Location sharing through Google Maps links
- Battery status included in SOS alerts
- SOS alert history storage
- Manual and Automatic SOS support

### ⏰ Trusted Check-In System
- Schedule expected arrival times
- Countdown timer monitoring
- Check-in expiry notifications
- 30-second grace period after missed check-in
- Automatic SOS escalation if the user does not respond

### 👥 Trusted Contacts Management
- Add trusted contacts
- Manage emergency contact information
- Notify multiple contacts simultaneously during emergencies

### 📍 Location Services
- Real-time location access
- Google Maps integration
- Location sharing during SOS alerts

### 🛣️ Safe Route Awareness
- Route viewing using Google Maps
- Risk-aware navigation support
- Route assistance features

### 🔔 Notifications & Alerts
- Check-in reminders
- Safety notifications
- SOS notifications
- Notification history management
- Alert history tracking

### 📊 Alert History
- View previous SOS activations
- Track alert type (Manual / Auto)
- View alert timestamps
- View stored location and battery information

---

## 🛠️ Technologies Used

### Programming Language
- Java

### Mobile Development
- Android Studio
- Android SDK

### Backend & Cloud Services
- Firebase Authentication
- Firebase Realtime Database

### APIs & Services
- Google Maps API
- Google Location Services
- SMS Manager API
- Android Notification System
- AlarmManager

---

## 📂 Project Structure

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/shealert/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── test/
├── build.gradle.kts
└── proguard-rules.pro
```

---

## 📊 Core Modules

- Authentication Module
- Dashboard Module
- Trusted Contacts Module
- SOS Management Module
- Check-In Monitoring Module
- Notification Management Module
- Alert History Module
- Safe Route Module
- User Profile Module

---

## 🚀 How It Works

### Normal Check-In Flow

1. User schedules an expected arrival time.
2. Check-in countdown begins.
3. User confirms safety before timer expires.
4. Check-in is completed successfully.

### Missed Check-In Flow

1. Check-in timer expires.
2. User receives a high-priority notification.
3. A 30-second grace countdown begins.
4. User can confirm safety during the grace period.
5. If there is no response, an automatic SOS alert is triggered.

### SOS Flow

1. User activates SOS manually or through automatic escalation.
2. Current location is retrieved.
3. Emergency SMS messages are sent to trusted contacts.
4. Alert details are stored in Firebase.
5. Alert appears in alert history.

---

## 🎯 Purpose

The objective of SheAlert is to provide a practical and reliable personal safety solution that enables users to:

- Stay connected with trusted contacts
- Share emergency information quickly
- Monitor safety through check-ins
- Automatically escalate emergencies when required
- Improve response time during critical situations

---

## 🚀 Setup Instructions

### 1️⃣ Clone Repository

```bash
git clone https://github.com/Sreelakshmi1621/SheAlert-Women-Safety-App.git
```

### 2️⃣ Open in Android Studio

Open the cloned project in Android Studio.

### 3️⃣ Configure Firebase

Create your own Firebase project and place:

```text
app/google-services.json
```

inside the app directory.

This file is intentionally excluded from the repository.

### 4️⃣ Configure Google Maps API

Add your own Google Maps API key in:

```properties
gradle.properties
```

Example:

```properties
MAPS_API_KEY=YOUR_API_KEY
```

### 5️⃣ Sync Gradle

Sync the project after adding Firebase and Maps configuration.

### 6️⃣ Run Application

Run the application on:

- Android Emulator
- Physical Android Device

---

## 🔒 Security Notes

The following files are intentionally excluded from the repository:

```text
app/google-services.json
gradle.properties
```

These files may contain Firebase credentials and API keys and should never be committed to GitHub.

---

## 🌟 Future Enhancements

- Push Notification Integration
- Live Location Tracking During Active SOS
- Emergency Voice Recording
- Emergency Escalation to Authorities
- Offline Emergency Support
- Advanced Risk Awareness Features

