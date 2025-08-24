# Geofencing-Based Attendance Management System

This Android application simplifies employee attendance and performance tracking through geofencing and Firebase integration. It offers two user roles — **Admin** and **Employee** — each with a set of dedicated features to streamline attendance management, leave tracking, and performance analysis.

---

## 📱 Features

### Admin Panel:
- **Set Geofence**: Define office location and radius using Google Maps API.
- **Employee Management**: View and manage assigned employees.
- **Leave Management**: Approve or reject employee leave requests.
- **Live Tracking**: View login/logout status within the geofence on a real-time basis.
- **Reports & Analytics**: View attendance and test performance trends via interactive charts (MPAndroidChart).


### Employee Panel:
- **Geofenced Attendance**: Login/Logout allowed only within the admin-set geofence.
- **Leave Requests**: Apply for leave and track approval status.
- **Dashboard**: View test history, attendance status, and performance scores.
- **Live Tracking**: Check geofence status for attendance eligibility.

---

## 🔧 Technologies Used

- **Language**: Java (v17)
- **IDE**: Android Studio
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Geolocation**: Google Maps & Location Services
- **Charts**: MPAndroidChart
- **Cloud Platform**: Firebase Hosting & Firestore

---

## 🚀 How to Run the Project

1. Clone the repository and open it in Android Studio.
2. Connect your Firebase project and replace the `google-services.json`.
3. Enable Firestore and Authentication in Firebase Console.
4. Add Google Maps API key in `AndroidManifest.xml`.
5. Build and run the app on Android 8.0+ with location permissions enabled.

---

## 📌 Permissions Required

- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- INTERNET
- FOREGROUND_SERVICE

---

## 📈 Future Enhancements

- Notification alerts for late logins.
- Exportable reports in CSV/PDF.
- Role-based dashboard enhancements.

---

## 🤝 Contributors  

Developed by final-year Computer Engineering students at **NBN Sinhgad Technical Institute Campus, Pune**  

<a href="https://github.com/mihir-mihir-gawade">
  <img src="https://avatars.githubusercontent.com/mihir-mihir-gawade" width="60px" style="border-radius:50%;" />
</a>
<a href="https://github.com/atharvabharambe">
  <img src="https://avatars.githubusercontent.com/atharvabharambe" width="60px" style="border-radius:50%;" />
</a>
<a href="https://github.com/suyash-username">
  <img src="https://avatars.githubusercontent.com/suyash-username" width="60px" style="border-radius:50%;" />
</a>
<a href="https://github.com/nikhilgandhi08">
  <img src="https://avatars.githubusercontent.com/nikhilgandhi08" width="60px" style="border-radius:50%;" />
</a>

- [Mihir Gawade](https://github.com/mihir-mihir-gawade)  
- [Atharva Bharambe](https://github.com/atharvabharambe)  
- [Suyash Bhanwase](https://github.com/suyash-username)  
- [Nikhil Gandhi](https://github.com/nikhilgandhi08)  




---



