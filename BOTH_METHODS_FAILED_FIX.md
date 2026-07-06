# "Both Connection Methods Failed" - FIXED

## The Problem
Error message: "Connection failed: Connection failed: Both connection methods failed"

This means **both phones tried to connect to each other, but neither successfully accepted the connection as a server**.

## Root Causes

### 1. Server Only Accepted One Connection Then Stopped
**Problem**: After `accept()` returned one connection, the server stopped listening
```kotlin
// OLD - WRONG
val socket = serverSocket?.accept()
// After this, server stops - won't accept more connections
```

**Fix**: Server keeps running in a loop
```kotlin
// NEW - CORRECT  
while (serverSocket != null && !isConnected) {
    val socket = serverSocket?.accept()
    // Keep accepting...
}
```

### 2. Security Mode Mismatch
**Problem**: Client used secure connection, server used secure connection, but they couldn't find each other
- Pairing dialogs blocking connection
- Extra authentication steps failing silently

**Fix**: Use **insecure** RFCOMM connection (no pairing required)
```kotlin
// Server
listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)

// Client  
createInsecureRfcommSocketToServiceRecord(SERVICE_UUID)
```

**Note**: "Insecure" means no system pairing dialog, but data is still encrypted by Bluetooth itself. Safe for local P2P connections.

### 3. Wrong Connection Order
**Problem**: Client tried:
1. Secure connection (requires pairing)
2. Reflection fallback

**Fix**: Try in this order:
1. **Insecure** connection (matches server)
2. Secure connection (fallback)
3. Reflection fallback (last resort)

## Changes Made

### Player2ConnectionManager.kt

#### 1. startServerMode() - Keep Server Running
```kotlin
// NEW: Server keeps accepting connections
while (serverSocket != null && !isConnected) {
    try {
        val socket = serverSocket?.accept()
        socket?.let {
            handleServerConnection(it)
        }
    } catch (e: IOException) {
        // Handle error
    }
}
```

#### 2. Use Insecure RFCOMM
```kotlin
// Server
serverSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
    SERVICE_NAME,
    SERVICE_UUID
)

// Client
clientSocket = device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID)
```

#### 3. Better Error Messages
```kotlin
onConnectionFailed?.invoke(
    "Cannot reach ${device.name}. Make sure both phones are in Player1&Player2 mode."
)
```

#### 4. Prevent Multiple Servers
```kotlin
if (serverSocket != null) {
    Log.d(TAG, "Server already running")
    return
}
```

## How It Works Now

### Connection Flow:
```
Phone 1                          Phone 2
--------                         --------
[Start server]                   [Start server]
[Server listening...]            [Server listening...]
[Tap Phone 2's name]            
[Try insecure connect] -------->  [Accept connection]
[Send CONN_REQ]       -------->  [Receive CONN_REQ]
[Wait for accept]               [Show dialog]
                     <--------  [User taps Accept]
                     <--------  [Send CONN_ACCEPT]
[Receive CONN_ACCEPT]
✅ CONNECTED                      ✅ CONNECTED
```

## Testing Steps

### 1. Verify Server Starts on Both Phones
```bash
adb logcat | grep "Server socket started"
```
**Expected on BOTH phones**:
```
Player2Connection: Server socket started (insecure mode), listening on UUID: 00001101-...
Player2Connection: Waiting for incoming connection...
```

### 2. Attempt Connection
Phone 1: Tap Phone 2's name

**Expected logs on Phone 1**:
```
AvatarFragment: Initiating connection to Phone2
Player2Connection: Attempting to connect to Phone2 (XX:XX:XX:XX:XX:XX)
Player2Connection: Trying insecure RFCOMM connection with UUID: 00001101-...
Player2Connection: Client socket created, attempting connect...
Player2Connection: Connection successful!
Player2Connection: Successfully connected to Phone2, sending connection request...
```

**Expected logs on Phone 2**:
```
Player2Connection: Incoming connection received from Phone1
Player2Connection: Server accepted connection from Phone1
Player2Connection: Received message from client: CONN_REQ
AvatarFragment: Received connection request from Phone1
```

### 3. Verify Dialog Appears on Phone 2
- Dialog title: "Confirm Connection"
- Message: "{Phone1 name} wants to connect for training"
- Buttons: "Cancel" and "Accept"

### 4. Accept Connection
Phone 2: Tap "Accept"

**Expected on both phones**:
- Green dot appears
- Toast: "Connected to {device}"
- START button enabled

## If Still Failing

### Check 1: Both phones in Player1&Player2 mode?
```bash
# Both phones should show:
Player2Connection: Server socket started (insecure mode)
Player2Connection: Waiting for incoming connection...
```

### Check 2: Bluetooth permissions granted?
```bash
adb shell dumpsys package com.kayakpro.erg | grep BLUETOOTH
```
Should show:
- BLUETOOTH_SCAN: granted=true
- BLUETOOTH_CONNECT: granted=true
- BLUETOOTH_ADVERTISE: granted=true

### Check 3: Check exact error in logs
```bash
adb logcat | grep -E "Player2Connection|AvatarFragment"
```

Look for specific error like:
- "Connection refused" → Other phone not in server mode
- "Service discovery failed" → UUID mismatch (shouldn't happen now)
- "Host is down" → Bluetooth off or phone too far
- "Connection timeout" → Other phone not responding

### Check 4: Try in this order
1. Close app on BOTH phones
2. Reopen app on BOTH phones
3. **Phone 2 first**: Go to Avatar Training → Player1&Player2
4. **Wait 2 seconds** (let server start)
5. **Phone 1**: Go to Avatar Training → Player1&Player2
6. **Wait 2 seconds** (let server start)
7. **Phone 1**: Tap Phone 2's name

### Check 5: Distance
- Phones must be within 10 meters
- Move them closer (1-2 meters) for testing

### Check 6: Clear Bluetooth cache
```bash
# On each phone
adb shell pm clear com.android.bluetooth
# Then restart Bluetooth
```

## Key Differences from Before

| Issue | Before | Now |
|-------|--------|-----|
| Server lifetime | Single connection | Keeps accepting |
| Security mode | Secure (requires pairing) | Insecure (no pairing) |
| Connection methods | 2 attempts | 3 attempts (insecure first) |
| Error message | "Both methods failed" | "Cannot reach X. Make sure..." |
| Multiple servers | Could start multiple | Prevented |
| Logging | Minimal | Detailed at every step |

## Why Insecure Mode?

**Q**: Is insecure RFCOMM safe?
**A**: Yes, for this use case:
- "Insecure" only means no Android pairing dialog
- Bluetooth itself still encrypts the data
- We're not transmitting sensitive data (just training stats)
- Connection is temporary (only during training)
- Both phones must be in Player1&Player2 mode (user intent clear)

**Benefit**: No pairing popups, faster connection, works reliably across all Android versions

## Summary

✅ **Fixed**: Server now keeps accepting connections
✅ **Fixed**: Using insecure RFCOMM (no pairing required)
✅ **Fixed**: Connection tries insecure first (matches server)
✅ **Fixed**: Better error messages
✅ **Added**: Prevents starting multiple servers
✅ **Added**: Detailed logging at every step

**The connection should now work!** If it still fails, the logs will show exactly which step is failing and why.
