# Player1 & Player2 Mode - Integration Guide

## Current Status
✅ UI Implementation Complete
✅ Bluetooth Phone Discovery Complete
✅ Device Selection Complete
✅ Confirmation Dialog Complete
⚠️ Player2 Connection Logic - Needs Implementation
⚠️ Real-time Data Sync - Needs Implementation
⚠️ Training Screen Update - Needs Implementation

## What's Already Working

1. **Tab Switching**: Users can toggle between "Player & Bot" and "Player1 & Player2"
2. **UI Adaptation**: Correct UI elements show/hide based on selected mode
3. **Phone Discovery**: Bluetooth scanning finds nearby Android devices
4. **Device Selection**: User can select Player2's phone from the list
5. **Confirmation**: Dialog asks user to confirm before starting training
6. **Navigation**: App navigates to training screen with proper flags

## What Needs to Be Implemented

### 1. Bluetooth Connection Establishment
Currently, we only **discover** phones. You need to **connect** to them.

**Location**: After user confirms in `showConnectionConfirmationDialog()`

**Required Implementation**:
```kotlin
// In showConnectionConfirmationDialog(), replace the TODO section:

btnStartTraining.setOnClickListener {
    dialog.dismiss()
    
    // Step 1: Connect to Player2's device via Bluetooth
    connectToPlayer2Device(selectedPlayer2Device!!)
    
    // Step 2: Wait for connection establishment
    // Step 3: Send training parameters to Player2
    // Step 4: Receive confirmation from Player2
    // Step 5: Navigate to training screen
}

private fun connectToPlayer2Device(device: BluetoothDevice) {
    // Use BluetoothSocket or BluetoothGatt for connection
    // You may need to define a custom UUID for your app
    
    val uuid = UUID.fromString("YOUR-CUSTOM-UUID-HERE")
    
    try {
        val socket = device.createRfcommSocketToServiceRecord(uuid)
        socket.connect()
        
        // Connection established
        // Now send training parameters
        sendTrainingParametersToPlayer2(socket)
        
    } catch (e: IOException) {
        // Connection failed
        showError("Failed to connect to Player2")
    }
}
```

### 2. Define Communication Protocol

You need a protocol for Player1 and Player2 to communicate.

**Suggested Message Types**:
```kotlin
sealed class Player2Message {
    data class TrainingInvite(
        val timeOrDistance: Boolean,
        val amount: String,
        val machine: String
    ) : Player2Message()
    
    object InviteAccepted : Player2Message()
    object InviteRejected : Player2Message()
    
    data class TrainingData(
        val distance: String,
        val elapsedTime: String,
        val pace: String,
        val power: String,
        val calories: String,
        val heartRate: String,
        val strokeRate: String
    ) : Player2Message()
    
    object TrainingFinished : Player2Message()
}
```

### 3. Sync Training Parameters

**On Player1's Device** (Host):
```kotlin
private fun sendTrainingParametersToPlayer2(socket: BluetoothSocket) {
    val outputStream = socket.outputStream
    
    val params = JSONObject().apply {
        put("timeOrDistance", if (time != "Select") "time" else "distance")
        put("amount", if (time != "Select") time else distance)
        put("machine", sp!!.getSelectedMachine())
    }
    
    outputStream.write(params.toString().toByteArray())
    outputStream.flush()
}
```

**On Player2's Device** (Join):
- Need to create a "waiting for invitation" state
- Receive training parameters from Player1
- Show acceptance dialog
- Send confirmation back to Player1

### 4. Real-time Data Sharing During Training

During the workout, both players need to share their metrics.

**Implementation Approach**:
```kotlin
// In AvatarTraining fragment or wherever training happens:

private fun shareMyTrainingData() {
    val currentData = JSONObject().apply {
        put("distance", myDistance)
        put("time", myTime)
        put("pace", myPace)
        put("power", myPower)
        put("calories", myCalories)
        put("heartRate", myHeartRate)
        put("strokeRate", myStrokeRate)
    }
    
    // Send to Player2
    player2Socket?.outputStream?.write(currentData.toString().toByteArray())
}

private fun receivePlayer2Data() {
    // Listen for incoming data from Player2
    val inputStream = player2Socket?.inputStream
    
    // Parse received data and update Player2's UI metrics
    val data = JSONObject(receivedString)
    updatePlayer2UI(
        distance = data.getString("distance"),
        time = data.getString("time"),
        // ... etc
    )
}
```

### 5. Update Avatar Training Screen

The training screen needs to show **both players** side-by-side (as in your screenshot).

**Current**: Shows Player vs Bot (single kayak on left, bot kayak on right)
**Needed**: Show Player1 (left) and Player2 (right) with their real-time data

**Files to Modify**:
- `fragment_avatar_training.xml` (or whatever the training screen layout is)
- `AvatarTraining.kt` (or corresponding fragment/activity)

**Changes Needed**:
```xml
<!-- Add separate metric displays for Player2 -->
<LinearLayout
    android:id="@+id/player2_metrics"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_alignParentEnd="true"
    android:orientation="vertical">
    
    <TextView android:id="@+id/player2_distance" ... />
    <TextView android:id="@+id/player2_time" ... />
    <TextView android:id="@+id/player2_calories" ... />
    <TextView android:id="@+id/player2_bpm" ... />
    <TextView android:id="@+id/player2_watts" ... />
</LinearLayout>
```

### 6. Handle Connection Loss

What happens if Bluetooth connection drops during training?

**Recommended Approach**:
- Monitor connection state
- Show warning if connection lost
- Option to continue solo or end training
- Auto-save progress

```kotlin
private fun monitorConnection() {
    // Periodically check if socket is still connected
    handler.postDelayed({
        if (player2Socket?.isConnected == false) {
            onConnectionLost()
        }
        monitorConnection()
    }, 1000)
}

private fun onConnectionLost() {
    showDialog("Connection to Player2 lost. Continue training solo?")
    // Handle user choice
}
```

## Recommended Implementation Order

1. **First**: Set up basic Bluetooth socket connection
2. **Second**: Implement training parameter sync (one-time at start)
3. **Third**: Test connection and parameter sharing
4. **Fourth**: Implement real-time data sharing during training
5. **Fifth**: Update training screen UI for dual player display
6. **Sixth**: Add connection monitoring and error handling

## Testing Recommendations

1. **Test with 2 physical devices** - Bluetooth LE can't be fully tested on emulator
2. **Test connection stability** - Walk away with one device, see what happens
3. **Test data sync frequency** - Don't overwhelm Bluetooth with too frequent updates
4. **Test edge cases**:
   - One player finishes before the other
   - Connection drops mid-training
   - One device runs out of battery
   - App goes to background on one device

## Additional Considerations

### Server-based Alternative
Instead of direct Bluetooth P2P, you could use a server-based approach:
- Both players connect to your backend server
- Server coordinates the session
- More reliable but requires internet

### Security
- Add pairing/authentication before allowing connection
- Encrypt sensitive training data
- Validate data received from other player

### UX Improvements
- Show "Searching for Player2..." animation
- Show Player2's profile picture if available
- Add voice chat capability (advanced)
- Post-training comparison screen

## Questions to Consider

1. **Who controls the training session?**
   - Can only Player1 (host) end the session?
   - Or can either player end it?

2. **What if players have different machines selected?**
   - Allow mismatched machines?
   - Force both to use same machine?

3. **Leaderboards/History?**
   - Save P2P training sessions separately?
   - Show comparison stats after training?

4. **Rematch feature?**
   - Quick "Play Again" button after session ends?

---

**Note**: The UI and discovery logic is already complete. The main work remaining is implementing the Bluetooth connection, data synchronization, and updating the training screen to display both players' data in real-time.
