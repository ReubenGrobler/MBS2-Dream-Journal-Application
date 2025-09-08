# Dream Journal Mobile Application v1.0.0
## Developed by Reuben Grobler

This is a proof of concept for a mobile application developed for the course ___Mobile Software Engineering 2___ for ___IU University of Applied Sciences___.
This app is exclusively built using the Java programming language. As such, ***it is only supported on Android version 8.0 ("Oreo") or higher.***

## How to Install the Application:

There are two ways to install this application: building from source, or by installing the APK included in the project.

### Building from Source

1. Clone the repo
```
git clone https://github.com/ReubenGrobler/MBS2-Dream-Journal-Application.git
cd MBS2-Dream-Journal-Application
```
2. Open the project within Android Studio
3. Build the project (this should happen automatically)
4. Create a virtual device running Android 8.0 or higher
5. Run `MainActivity.java`

### Launching the APK

1. Download `Dream_Journal_Application.apk` from this repository
2. Transfer the APK to your mobile device
3. Open the APK and start installation
4. Click on the installed app to run it

## Running the Unit Tests

### Running the Unit Tests

In order to run the unit tests included within the project, it is necessary to first build the project from source. As such, it is necessary to follow until step 4 within the installation guide for building the project from source first. Assuming that this has been done, the following should be done to run the unit tests:
1. Locate `ManagingJournalEntriesTest.java`
2. Right-click on the class and select `"Run 'ManagingJournalEntriesTest.java'"`. Alternatively, open the file directly and press the green arrow button at the top of the screen.
3. 8 tests are included within this file to test several functions and methods regarding processing data, with all tests passing.

Optionally, it is possible to run tests via command line. To do so, it is necessary to first have Java JDK 11 (or higher) installed. Assuming that Windows is the operating system used:
1. Open Command Prompt and navigate to the directory of the project. An example path is `C:\Users\your_username\AndroidStudioProjects\DreamJournalApplication`
2. Run `gradlew.bat test`. All tests will then be executed.

### Disclaimer
There is no default test cases included with the project, meaning that the user starts with a blank slate. However, by using recognisable components and icons as found in mainstream journalling applications, the app should be intuitive enough for the average user to navigate their way across the UI.
