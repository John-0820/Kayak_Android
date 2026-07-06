# "Permission Denied" Error - FIXED

## The Problem
When tapping "Tap to Scan" button, you got "permission denied" error and couldn't see kayak console list.

## Root Cause
The app wasn't requesting Bluetooth permissions from the user at runtime. On Android 6+, apps must request dangerous permissions at runtime even if they're in the manifest.

## The Fix

### Changed onCreate() in BluetoothScreen.kt
```kotlin
// OLD - Just started scanning without checking permissions
if (checkBluetoothSupport()) {
    scanForDevices()
}

// NEW - Check permissions FIRST, request if needed
if (!isBluetoothPermissionsGranted()) {
    Log.d("BluetoothScreen", "Permissions not granted, requesting...")
    checkBluetoothPermissions()  // Shows permission dialog
} else if (checkBluetoothSupport()) {
    Log.d("BluetoothScreen", "Permissions granted, starting scan...")
    scanForDevices()
}
```

## What Happens Now

### First Time Opening "CONNECTING DEVICES" Page

1. App checks if permissions are granted
2. **If NO**: Shows Android permission dialog
   ```
   Allow KayakPro to:
   - Find, connect to, and determine the position of nearby devices?
   [Deny] [Allow]
   ```
3. User taps **[Allow]**
4. App automatically starts scanning
5. Kayak devices appear in list

### Subsequent Times
- Permissions already granted
- Scanning starts immediately
- No permission dialog

## Testing Steps

### Step 1: Uninstall Old Version (Important!)
```bash
adb uninstall com.kayakpro.erg
```
**Why**: Old version has wrong permission state cached

### Step 2: Install New APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Open App and Go to Connect Machine
1. Login to app
2. Go to "CONNECTING DEVICES" page
3. **EXPECT**: Permission dialog appears
4. Tap **[Allow]** for ALL permissions
5. **EXPECT**: Scanning starts automatically
6. **EXPECT**: Paired kayak devices appear immediately
7. **EXPECT**: New kayak devices appear within 10 seconds

### Step 4: Verify in Settings
After granting permissions:
```
Settings → Apps → KayakPro → Permissions

Should show:
✓ Location - Allowed
✓ Nearby devices - Allowed
```

## If Permission Dialog Doesn't Appear

### Option 1: Clear App Data
```bash
adb shell pm clear com.kayakpro.erg
```
Then open app again - permission dialog will appear

### Option 2: Manually Grant Permissions
```bash
adb shell pm grant com.kayakpro.erg android.permission.BLUETOOTH_SCAN
adb shell pm grant com.kayakpro.erg android.permission.BLUETOOTH_CONNECT
adb shell pm grant com.kayakpro.erg android.permission.BLUETOOTH_ADVERTISE
adb shell pm grant com.kayakpro.erg android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.kayakpro.erg android.permission.ACCESS_COARSE_LOCATION
```

### Option 3: Grant in Settings
1. Settings → Apps → KayakPro
2. Permissions
3. Location → Allow
4. Nearby devices → Allow

## Required Permissions

The app needs these permissions for Bluetooth scanning:

### Android 12+ (API 31+)
- `BLUETOOTH_SCAN` - To scan for devices
- `BLUETOOTH_CONNECT` - To connect to devices  
- `BLUETOOTH_ADVERTISE` - To make device discoverable
- `ACCESS_FINE_LOCATION` - Required for Bluetooth scanning

### Android 11 and below (API 30-)
- `BLUETOOTH` - Basic Bluetooth
- `BLUETOOTH_ADMIN` - Bluetooth management
- `ACCESS_FINE_LOCATION` - Required for Bluetooth scanning
- `ACCESS_COARSE_LOCATION` - Location access

All these are already in AndroidManifest.xml ✓

## What the User Sees

### Permission Dialog Text
```
Allow KayakPro to find, connect to, and determine 
the position of nearby devices?

This permission lets the app:
- Scan for Bluetooth devices
- Connect to devices like kayak consoles
- Make your device visible to other devices

[Deny]  [Allow]
```

**User MUST tap [Allow]** or scanning won't work.

## Troubleshooting

### "Permission denied" still appears
**Check**:
```bash
adb logcat | grep "BluetoothScreen"
```

Look for:
```
BluetoothScreen: Permissions not granted, requesting...
```

If you see this but no permission dialog:
- App was denied previously
- User must grant manually in Settings

### Kayak devices don't appear after granting permission
**Check logs**:
```bash
adb logcat | grep "BluetoothScan"
```

Should see:
```
BluetoothScan: Found device: PM5-12345
BluetoothScan: ✓ Kayak device found: PM5-12345
```

If no devices found:
- Wait full 10 seconds
- Kayak device might not be advertising
- Check kayak is powered on
- Move phone closer to kayak

### "Permissions denied" toast appears
User tapped [Deny] on permission dialog.

**Solution**:
1. Settings → Apps → KayakPro → Permissions
2. Grant all permissions manually
3. Reopen "CONNECTING DEVICES" page

## For Deployment

### Tell Users:
```
IMPORTANT: Grant Permissions

When you first open the "CONNECTING DEVICES" page, 
Android will ask for Bluetooth permissions.

You MUST tap [Allow] or the app cannot scan for 
kayak console devices.

If you accidentally tap [Deny]:
1. Go to Settings → Apps → KayakPro → Permissions
2. Enable "Location" and "Nearby devices"
3. Return to app
```

## Summary of Changes

✅ **Check permissions before scanning** - No more "permission denied"
✅ **Request permissions on first use** - User sees permission dialog
✅ **Auto-start scan after grant** - Seamless experience
✅ **Clear logging** - Can debug permission issues

**The "permission denied" error is now fixed. Users will see permission dialog on first use and must grant permissions.**
