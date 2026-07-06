# Connection "Player2 Disconnected" Fix

## Problem
When clicking on Player2's device to connect, immediately got "Player2 disconnected" message.

## Root Causes Found

### 1. Server Socket Being Closed Prematurely
**Problem**: When Player1 clicked to connect, it closed its own server socket
```kotlin
// OLD CODE - WRONG
serverSocket?.close()  // This prevented Player2 from connecting back!
serverSocket = null
```

**Fix**: Keep server socket open on both phones
```kotlin
// NEW CODE - CORRECT
// DON'T close server socket - both phones need to be listening
```

### 2. Connection Failure Triggering Disconnect Callback
**Problem**: When connection failed, it called `disconnect()` which triggered `onDisconnected` callback
- User saw "Player2 disconnected" even though connection never succeeded
- Confusing message for failed connection attempts

**Fix**: Separate callbacks for failed connection vs disconnection
```kotlin
// NEW: Connection failed callback (never connected)
var onConnectionFailed: ((String) -> Unit)? = null

// EXISTING: Disconnection callback (was connected, now lost)
var onDisconnected: (() -> Unit)? = null
```

### 3. No Fallback Connection Method
**Problem**: Standard RFCOMM connection fails on some Android devices

**Fix**: Added fallback method using reflection
```kotlin
try {
    // Try standard method
    clientSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
    clientSocket?.connect()
} catch (e: IOException) {
    // Fallback method (works better on some devices)
    val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
    clientSocket = method.invoke(device, 1) as BluetoothSocket
    clientSocket?.connect()
}
```

## What Changed

### Player2ConnectionManager.kt
1. ✅ Removed `serverSocket?.close()` from connectToPlayer()
2. ✅ Added fallback connection method with reflection
3. ✅ Added `onConnectionFailed` callback
4. ✅ Changed error handling to NOT call disconnect() on connection failure
5. ✅ Made handleServerConnection async with proper coroutine scope
6. ✅ Added detailed error logging

### AvatarFragmentWithout.kt
1. ✅ Added `onConnectionFailed` callback handler
2. ✅ Shows "Connection failed: {reason}" instead of "Player2 disconnected"
3. ✅ Keeps callbacks separate: failed connection ≠ disconnection

## Expected Behavior Now

### Scenario 1: Connection Fails
**Before**: "Player2 disconnected"
**Now**: "Connection failed: {specific error reason}"

### Scenario 2: Connection Succeeds Then Drops
**Before**: "Player2 disconnected" 
**Now**: "Player2 disconnected" (correct message for this case)

### Scenario 3: Both Phones Keep Server Sockets Open
**Before**: One phone closed its server socket → couldn't receive connections
**Now**: Both keep server sockets → either can initiate connection

## Testing Steps

### Test 1: Verify Connection Works
1. Phone 1: Go to Player1&Player2 mode
2. Phone 2: Go to Player1&Player2 mode
3. Phone 1: Tap Phone 2's name
4. **Expected logs**:
   ```
   Player2Connection: Attempting to connect to Phone2
   Player2Connection: Connected to Phone2  
   Player2Connection: Sent message: CONN_REQ
   ```
5. Phone 2: Should see dialog
6. **If connection fails**, check logs for specific error

### Test 2: Verify Error Messages
If connection still fails, you'll now see:
- "Connection failed: {specific error}" instead of "Player2 disconnected"
- Error details in logs for debugging

### Common Connection Errors

**"Connection refused"**
- Other device not in server mode yet
- Solution: Ensure both phones selected Player1&Player2 mode

**"Host is down"**  
- Bluetooth not enabled on other device
- Solution: Enable Bluetooth

**"Connection timeout"**
- Devices too far apart
- Other device's Bluetooth is busy
- Solution: Move phones closer, disable other Bluetooth connections

**"Service discovery failed"**
- UUID mismatch
- Other device not advertising service
- Solution: Restart both apps, ensure both in Player1&Player2 mode

## Debug with ADB

```bash
# Filter for connection logs
adb logcat | grep -E "Player2Connection|AvatarFragment"

# Look for these key messages:
# - "Attempting to connect to..."
# - "Connected to..." (success)
# - "Connection failed: ..." (failure with reason)
# - "Fallback method..." (if standard method failed)
```

## What to Check If Still Not Working

1. **Both phones in Player1&Player2 mode?**
   - Look for "Server socket started" log on BOTH phones

2. **Permissions granted?**
   - BLUETOOTH_SCAN
   - BLUETOOTH_CONNECT
   - BLUETOOTH_ADVERTISE
   - ACCESS_FINE_LOCATION

3. **Bluetooth enabled on both?**
   ```bash
   adb shell settings get global bluetooth_on
   # Should return: 1
   ```

4. **Check exact error message**
   - Connection failed: {ERROR} will tell you exactly what's wrong
   - Share the error message for specific help

5. **Try discoverable mode**
   - Settings → Bluetooth → Make phone discoverable
   - Try connecting again

## Summary

✅ **Fixed**: "Player2 disconnected" showing for failed connection attempts
✅ **Added**: Proper "Connection failed: {reason}" messages  
✅ **Fixed**: Server socket being closed prematurely
✅ **Added**: Fallback connection method for compatibility
✅ **Improved**: Error logging for debugging

The connection should now work better, and if it doesn't, you'll see exactly WHY it's failing instead of a confusing "disconnected" message.
