# Bluetooth Permissions Fix for Android 9-14

## Problem
The app was showing "Bluetooth permissions are required to search for other players" and couldn't search for other Bluetooth devices on Android phones (versions 9-14).

## Root Cause
The Bluetooth permissions handling was not properly accounting for the differences between Android versions:
- **Android 12+ (API 31+)**: Requires new runtime permissions (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`)
- **Android 11 and below (API 30 and lower)**: Uses legacy permissions (`BLUETOOTH`, `BLUETOOTH_ADMIN`)

The code was checking for Android 12+ permissions on all Android versions, which caused permission denial on Android 11 and below because those permission constants don't exist on older versions.

## Changes Made

### 1. AndroidManifest.xml
Added proper permission declarations with version-specific attributes:

```xml
<!-- Android 12+ (API 31+) Bluetooth permissions -->
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

<!-- Android 11 and below (API 30 and lower) Bluetooth permissions -->
<uses-permission android:name="android.permission.BLUETOOTH"
    android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"
    android:maxSdkVersion="30" />

<!-- Location permissions needed for Bluetooth scanning on all versions -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 2. AvatarFragmentWithout.kt
- Updated `bluetoothPermissions` to use a `by lazy` block that returns different permissions based on Android version
- Updated `bluetoothPermissionsForAdvertise` similarly
- Updated `scanForPhones()` to check correct permissions based on Android version
- Updated `stopPhoneScanning()` to check correct permissions based on Android version
- Updated `clickItem()` to check correct permissions based on Android version
- Updated `leScanCallback` to check correct permissions based on Android version

### 3. BluetoothScreen.kt
- Updated `bluetoothPermissions` to use a `by lazy` block that returns different permissions based on Android version

### 4. Player2ConnectionManager.kt
- Added `hasBluetoothConnectPermission()` helper function
- Updated `startServerMode()` to use the helper
- Updated `connectToPlayer()` to use the helper

### 5. BluetoothService.kt
- Added `hasBluetoothConnectPermission()` helper function
- Updated all permission checks to use the helper

### 6. HomeScreen.kt
- Added `hasBluetoothConnectPermission()` helper function
- Updated permission checks to use the helper

### 7. SelectMachine.kt
- Added `hasBluetoothConnectPermission()` helper function
- Updated permission checks to use the helper

## Permission Mapping

| Android Version | Scan Permission | Connect Permission | Advertise Permission |
|----------------|-----------------|-------------------|---------------------|
| Android 12+ (API 31+) | BLUETOOTH_SCAN | BLUETOOTH_CONNECT | BLUETOOTH_ADVERTISE |
| Android 11 and below | BLUETOOTH | BLUETOOTH | N/A |

## Testing
After these changes, the app should:
1. Request correct permissions based on the Android version
2. Successfully scan for Bluetooth devices on all Android versions 9-14
3. Allow connecting to other phones in Player1 & Player2 mode
4. No longer show "Bluetooth permissions are required to search for other players" error when permissions are properly granted

## User Instructions
1. Uninstall the old version of the app
2. Install the new version
3. When prompted, grant all Bluetooth permissions
4. The app should now be able to search for other Bluetooth devices
