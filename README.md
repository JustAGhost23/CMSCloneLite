# CMSCloneLite
### A Lite version of the cruX BPHC CMS App, made for round 3 of cruX inductions. Built using Jetpack Compose, Viewmodels and Firebase as a BaaS.

---

#### Features

* Google OAuth for students and Regular Email-Password Auth for the admin.
  * Implemented One-Tap Sign In/Sign Up to obtain their OAuth 2.0 credentials and IdToken, and use it to login with Firebase Google OAuth.
  * Used Firebase Email-Password Auth for the admin.

* On logging in as admin: 
  * Admin can create courses with course name, instructor name and lecture timings.
  * Admin can also edit and delete these courses.
  * Admin can also send announcements(with attachments) in each course, and a notification will be sent to users who have enrolled in that particular course.
  * Course data is stored on Firebase Firestore.

* On logging in as user:
  * User can enroll in courses from the "All Courses" Screen. These courses will then be displayed in "My Courses Screen"
  * User can also unenroll from each course, or unenroll from all courses at once.
  * User can create calendar events for classes in their course. (Implemented with Calendar Provider API)
  
* Push notifications are implemented with Firebase Cloud Messaging.

* Attachments for Announcements are stored using Firebase Cloud Storage

* Toggle switch for dark mode

---

#### Installation Methods

1. **Clone the Project:**
    1. Run ` git clone https://github.com/JustAGhost23/CMSCloneLite ` in terminal.
    2. Android Studio -> File -> Open
    3. Connect your Android Device and Run the program (Developer Mode of the Device should be enabled).
    
2. **Run the Release APK**
    1. Click [here](https://github.com/JustAGhost23/CMSCloneLite/releases/tag/v1.0.1) to download the release APK.
    
---

#### Screenshots
| Login | Profile | My Courses |
| --- | --- | --- |
| <img src="https://imgur.com/12IwtnW.png" width="200" height="400"/> | <img src="https://imgur.com/UtRXe0V.png" width="200" height="400"/> | <img src="https://imgur.com/2J3sRYC.png" width="200" height="400"/> |

| All Courses | Edit Course | Announcements |
| --- | --- | --- |
| <img src="https://imgur.com/34AuDjc.png" width="200" height="400"/> | <img src="https://imgur.com/QdFX3XM.png" width="200" height="400"/> | <img src="https://imgur.com/kQC283t.png" width="200" height="400"/> |

| Course View (as admin) | Course View (as user) | Course View (after enrolling) |
| --- | --- | --- |
| <img src="https://imgur.com/oAKaEvK.png" width="200" height="400"/> | <img src="https://imgur.com/9DC4SfX.png" width="200" height="400"/> | <img src="https://imgur.com/fm6iHID.png" width="200" height="400"/> |
