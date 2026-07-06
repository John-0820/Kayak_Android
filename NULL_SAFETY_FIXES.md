# Null Safety Fixes for Bluetooth Device Names

## Issue
When clicking the "Allow" button to accept Bluetooth connections, the app crashed with the error message "Home screen device is connect with null" because Bluetooth device names can be null.

## Root Cause
Bluetooth devices can have null names in several scenarios:
1. Device doesn't broadcast its name
2. Device name is not yet discovered during scanning
3. Privacy features on some devices hide the actual name
4. Device is in a state where name is not available

## Files Fixed

### 1. HomeScreen.kt
**Location:** `/app/src/main/java/com/kayakpro/erg/ui/login/activity/HomeScreen.kt`

**Fix:** Changed line 87-88
```kotlin
// Before
"Home screen Device connected with ${bluetoothDeviceModel.name}"

// After
val deviceName = bluetoothDeviceModel.name ?: "Unknown Device"
"Device connected with $deviceName"
```

### 2. AvatarFragmentWithout.kt
**Location:** `/app/src/main/java/com/kayakpro/erg/ui/login/fragments/AvatarFragmentWithout.kt`

**Fixes:**
- Line 273: Added null safety for connected device name in `onConnected` callback
- Line 275: Added null safety in `onConnectionRequest` callback  
- Line 352-353: Fixed dialog message to use safe device name
- Line 635-641: Fixed Toast messages in `clickItem` method

### 3. SelectMachine.kt
**Location:** `/app/src/main/java/com/kayakpro/erg/ui/login/activity/SelectMachine.kt`

**Fix:** Changed line 56
```kotlin
// Before
"Device connected with ${bluetoothDeviceModel.name}"

// After
val deviceName = bluetoothDeviceModel.name ?: "Unknown Device"
"Device connected with $deviceName"
```

### 4. Player2ConnectionManager.kt
**Location:** `/app/src/main/java/com/kayakpro/erg/bluetooth/Player2ConnectionManager.kt`

**Fixes:**
- Line 114: Server connection log message
- Line 155: Connection attempt log message
- Line 163: Pairing message
- Line 267: Connection failed log message
- Line 287-291: Error messages
- Line 318: Server accepted connection log message

## Testing Recommendations

1. **Test with devices that have no name:** Try connecting to Bluetooth devices that don't broadcast their names
2. **Test connection flow:** Test the full Player1 & Player2 connection flow
3. **Test error scenarios:** Test connection failures, timeouts, and refused connections
4. **Test edge cases:** Test with devices in various states (bonding, connected, disconnected)

## Prevention

To prevent similar issues in the future:
1. Always use the Elvis operator (`?:`) when accessing Bluetooth device names
2. Provide fallback text like "Unknown Device" for null names
3. Test with real devices that may have null or empty names
4. Use consistent null handling patterns across the codebase

## Example Pattern

```kotlin
// Good pattern
val deviceName = device.name ?: "Unknown Device"
Toast.makeText(context, "Connected to $deviceName", Toast.LENGTH_SHORT).show()

// Bad pattern (can crash)
Toast.makeText(context, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
```
