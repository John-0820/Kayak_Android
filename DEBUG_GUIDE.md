# Player1 & Player2 Debug Guide

## Changes Made to Fix Runtime Issues

### 1. Fixed Permission Check in Player2ConnectionManager
**Problem**: Lambda permission check wasn't working correctly
**Solution**: Changed to direct Context-based permission checks

**Before**:
```kotlin
fun startServerMode(permissionCheck: () -> Boolean)
fun connectToPlayer(device: BluetoothDevice, permissionCheck: () -> Boolean)
```

**After**:
```kotlin
fun startServerMode(context: Context)
fun connectToPlayer(device: BluetoothDevice, context: Context)
```

### 2. Added Comprehensive Logging
Added log statements at every critical point to track execution:

**Connection Flow Logs**:
```
TAG: Player2Connection
- "Server socket started, waiting for connection..."
- "Attempting to connect to {device} ({address})"
- "Connected to {device}"
- "Server accepted connection from {device}"
```

**Training Start Logs**:
```
TAG: AvatarFragment
- "startPlayer1Player2Training called"
- "Connection verified, preparing training data..."
- "Making API call for training setup..."
- "Sending training start command to Player2..."
- "Navigating to training screen..."
- "Received training start command, navigating to training..."
```

### 3. Added START Button State Management
**Problem**: START button was always enabled, even when not connected
**Solution**: Disable START button until connection established

```kotlin
// In init() - initially disable in Player2 mode
if (!isPlayerBotMode) {
    binding.btnStart.isEnabled = false
    binding.btnStart.alpha = 0.5f
}

// In onConnected callback - enable after connection
binding.btnStart.isEnabled = true
binding.btnStart.alpha = 1.0f

// In onDisconnected callback - disable again
binding.btnStart.isEnabled = false
binding.btnStart.alpha = 0.5f
```

### 4. Enhanced Error Messages
Added user-friendly toast messages with more context:
- "Bluetooth permission required" (when permission missing)
- "Please connect to Player2 first" (when trying to start without connection)
- Connection status updates with device names

## How to Debug on Device

### Step 1: Enable USB Debugging
1. Go to Settings → About Phone
2. Tap "Build Number" 7 times to enable Developer Options
3. Go to Settings → Developer Options
4. Enable "USB Debugging"
5. Enable "Stay Awake" (keeps screen on while charging)

### Step 2: Connect Phone and View Logs
```bash
# Connect phone via USB
adb devices

# View live logs (filter by tags)
adb logcat | grep -E "Player2Connection|AvatarFragment|Player2Data"

# Or view all logs
adb logcat
```

### Step 3: Test Connection Flow

#### On Phone 1 (Player1):
1. Open app
2. Navigate to Avatar Training
3. Tap "Player1 & Player2"
4. **Check logs for**:
   ```
   Player2Connection: Server socket started, waiting for connection...
   ```
5. Tap Phone 2's name in list
6. **Check logs for**:
   ```
   AvatarFragment: Initiating connection to {device}
   Player2Connection: Attempting to connect to {device}
   Player2Connection: Connected to {device}
   AvatarFragment: Connection established successfully
   ```
7. **Verify UI**:
   - Green dot appears next to Phone 2
   - Toast: "Connected to {device}"
   - START button enabled (not grayed out)

#### On Phone 2 (Player2):
1. Open app
2. Navigate to Avatar Training
3. Tap "Player1 & Player2"
4. **Check logs for**:
   ```
   Player2Connection: Server socket started, waiting for connection...
   ```
5. Wait for Phone 1 to connect
6. **Check logs for**:
   ```
   Player2Connection: Server accepted connection from {device}
   AvatarFragment: Received connection request from {device}
   ```
7. **Verify UI**:
   - Dialog appears: "{Phone 1} wants to connect for training"
   - Buttons: "Cancel" and "Accept"
8. Tap "Accept"
9. **Check logs for**:
   ```
   AvatarFragment: Connection established successfully
   ```
10. **Verify UI**:
    - Green dot appears next to Phone 1
    - Toast: "Connected! Waiting for Player1 to start training..."

### Step 4: Test Training Start

#### On Phone 1 (Player1):
1. Select Time (e.g., 5m 0s) or Distance
2. Tap START button
3. **Check logs for**:
   ```
   AvatarFragment: startPlayer1Player2Training called
   AvatarFragment: Connection verified, preparing training data...
   AvatarFragment: Making API call for training setup...
   AvatarFragment: Sending training start command to Player2...
   Player2Connection: Sent training start command
   AvatarFragment: Navigating to training screen...
   ```
4. **Verify**: Phone 1 navigates to "STARTING IN" countdown

#### On Phone 2 (Player2):
**WITHOUT tapping START button**, check logs for:
```
Player2Connection: Received message: TRAINING_START|...
AvatarFragment: Received training start command, navigating to training...
```
**Verify**: Phone 2 automatically navigates to "STARTING IN" countdown

### Step 5: Test Real-Time Data Sync

#### During Training:
1. Both phones should be in training screen
2. **Check logs on Phone 1**:
   ```
   Player2Data: Updated opponent stats: distance={value}, pace={value}
   ```
3. **Check logs on Phone 2**:
   ```
   Player2Data: Updated opponent stats: distance={value}, pace={value}
   ```
4. **Verify UI on both phones**:
   - LEFT side: Shows own kayak data (updates from own console)
   - RIGHT side: Shows opponent's data (updates every ~1 second)

## Common Issues and Solutions

### Issue 1: "Server socket started" but no connection
**Symptoms**: Log shows server started, but connection never happens
**Causes**:
- Bluetooth not enabled on one phone
- Permissions not granted
- Phones too far apart (>10 meters)
- One phone not in Player1&Player2 mode

**Solution**:
```bash
# Check Bluetooth status
adb shell dumpsys bluetooth_manager | grep "enabled"

# Check permissions
adb shell dumpsys package com.kayakpro.erg | grep "BLUETOOTH"
```

### Issue 2: Connection request dialog doesn't appear
**Symptoms**: Phone 1 connects, but Phone 2 shows nothing
**Causes**:
- Server socket not accepting connection
- Callback not registered properly
- UI thread issue

**Check logs for**:
```
Player2Connection: Server accepted connection from {device}
AvatarFragment: Received connection request from {device}
```

**If missing**, server accept failed. Check:
- BLUETOOTH_CONNECT permission
- Server socket initialized correctly

### Issue 3: START button grayed out after connection
**Symptoms**: Green dot shows, but START button still disabled
**Causes**:
- onConnected callback not fired
- UI not updated on main thread

**Check logs for**:
```
AvatarFragment: Connection established successfully
```

**If missing**, connection succeeded but callback didn't fire.

### Issue 4: Player2 doesn't auto-navigate when Player1 starts
**Symptoms**: Player1 navigates, Player2 stays on same screen
**Causes**:
- TRAINING_START message not sent
- TRAINING_START message not received
- Callback not registered

**Check logs on Player1**:
```
AvatarFragment: Sending training start command to Player2...
Player2Connection: Sent training start command
```

**Check logs on Player2**:
```
Player2Connection: Received message: TRAINING_START|...
AvatarFragment: Received training start command, navigating to training...
```

**If Player1 logs are OK but Player2 logs missing**:
- Check Bluetooth connection still active
- Check for IOException in logs
- Try reconnecting

### Issue 5: Opponent data not showing on right side
**Symptoms**: Left side updates, right side stays at 0
**Causes**:
- TRAINING_DATA messages not being sent
- Callback not registered
- Bluetooth transmission errors

**Check logs**:
```
Player2Data: Updated opponent stats: distance={value}, pace={value}
```

**If missing**, data not received. Check:
- Connection still active (`Player2ConnectionManager.isConnected = true`)
- `sendTrainingData()` being called every second
- No IOException in logs

## Manual Testing Checklist

### Connection Phase:
- [ ] Phone 1: Server mode starts (log message)
- [ ] Phone 2: Server mode starts (log message)
- [ ] Phone 1: Sees Phone 2 in Bluetooth list
- [ ] Phone 2: Sees Phone 1 in Bluetooth list
- [ ] Phone 1: Can tap Phone 2's name
- [ ] Phone 1: "Connecting..." toast appears
- [ ] Phone 2: Connection request dialog appears
- [ ] Phone 2: Dialog shows correct device name
- [ ] Phone 2: Can tap "Accept" button
- [ ] Phone 1: Green dot appears
- [ ] Phone 2: Green dot appears
- [ ] Phone 1: "Connected to {device}" toast
- [ ] Phone 2: "Connected! Waiting..." toast
- [ ] Phone 1: START button enabled (not gray)

### Training Start Phase:
- [ ] Phone 1: Can select time or distance
- [ ] Phone 1: Can tap START button
- [ ] Phone 1: Navigates to countdown screen
- [ ] Phone 2: Automatically navigates to countdown screen (without tapping START)
- [ ] Both: Countdown shows simultaneously
- [ ] Both: Enter training screen at same time

### Real-Time Sync Phase:
- [ ] Phone 1: LEFT side updates from Kayak 1
- [ ] Phone 2: LEFT side updates from Kayak 2
- [ ] Phone 1: RIGHT side shows Player2's data
- [ ] Phone 2: RIGHT side shows Player1's data
- [ ] Both: Opponent data updates every ~1 second
- [ ] Both: All metrics sync (distance, pace, stroke rate, etc.)

## Expected Log Sequence

### Successful Connection:
```
Phone 1:
  Player2Connection: Server socket started, waiting for connection...
  AvatarFragment: Initiating connection to Phone2 (XX:XX:XX:XX:XX:XX)
  Player2Connection: Attempting to connect to Phone2
  Player2Connection: Connected to Phone2
  AvatarFragment: Connection established successfully

Phone 2:
  Player2Connection: Server socket started, waiting for connection...
  Player2Connection: Server accepted connection from Phone1
  AvatarFragment: Received connection request from Phone1
  AvatarFragment: Connection established successfully
```

### Successful Training Start:
```
Phone 1:
  AvatarFragment: startPlayer1Player2Training called
  AvatarFragment: Connection verified, preparing training data...
  AvatarFragment: Making API call for training setup...
  AvatarFragment: Sending training start command to Player2...
  Player2Connection: Sent training start command
  AvatarFragment: Navigating to training screen...

Phone 2:
  Player2Connection: Received message: TRAINING_START|index:2|isPlayer2Mode:true|...
  AvatarFragment: Received training start command, navigating to training...
```

### Successful Data Sync:
```
Both Phones (continuous):
  Player2Data: Updated opponent stats: distance=150, pace=120
  Player2Data: Updated opponent stats: distance=155, pace=118
  Player2Data: Updated opponent stats: distance=160, pace=120
  ... (every ~1 second)
```

## If Nothing Works

1. **Clear app data**:
   ```bash
   adb shell pm clear com.kayakpro.erg
   ```

2. **Reinstall app**:
   ```bash
   adb uninstall com.kayakpro.erg
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Check Bluetooth is working**:
   ```bash
   adb shell dumpsys bluetooth_manager
   ```

4. **Grant all permissions manually**:
   ```bash
   adb shell pm grant com.kayakpro.erg android.permission.BLUETOOTH_SCAN
   adb shell pm grant com.kayakpro.erg android.permission.BLUETOOTH_CONNECT
   adb shell pm grant com.kayakpro.erg android.permission.BLUETOOTH_ADVERTISE
   adb shell pm grant com.kayakpro.erg android.permission.ACCESS_FINE_LOCATION
   ```

5. **Capture full log for analysis**:
   ```bash
   adb logcat > logcat_full.txt
   ```
   Then send the file for detailed analysis.

## Summary of Fixes

✅ Fixed permission checks (Context-based instead of lambda)
✅ Added comprehensive logging throughout
✅ Added START button state management
✅ Enhanced error messages with context
✅ All files compile without errors

The implementation is now more robust and debuggable. Follow the debug guide above to identify exactly where the issue is occurring.
