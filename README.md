# TRACK and SCAN
Map Tracking and Bluetooth devices scan. <br/>

## Includes
- Main activity with two Fragments: Map and Bluetooth. <br/>
- Navigation graph. <br/>
- Data base: SQL Lite. <br/>
- Map: Google Maps. <br/>
- Background service. <br/>
- Design pattern: MVVM <br/>

## Fragments Description:
- Map: <br/>
Running a background service(even if the app is closed). <br/>
Tracking user's current location by google location service every 1 minute. <br/>
Saves the last 20 locations in Data base. <br/>
Displays the locations on a Google map. <br/>

- Bluetooth: <br/>
Scans for Bluetooth devices and shows it in a Recyclerview. <br/>
Ability to filter by MAC address. <br/>

## App created with:
* Android Studio, Kotlin.
* Android version: 13
* SDK version: 33
