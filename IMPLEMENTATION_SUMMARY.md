# Avatar Training - Player & Bot / Player1 & Player2 Implementation Summary

## Overview
Added functionality to switch between "Player & Bot" and "Player1 & Player2" training modes in the Avatar training page.

## Files Modified

### 1. Layout File: `fragment_avatra_without.xml`
**Changes:**
- Added tabs (buttons) for "Player & Bot" and "Player1 & Player2" selection
- Added Bluetooth device list section (`rl_bluetooth_list`) for Player1 & Player2 mode
- Bluetooth list includes:
  - RecyclerView for displaying nearby phones
  - Refresh button to rescan for devices
  - Uses same styling as the Bluetooth connection page

### 2. Fragment: `AvatarFragmentWithout.kt`
**Added Features:**
- **Tab Switching:**
  - `btn_player_bot` - Activates Player & Bot mode (default)
  - `btn_player_player` - Activates Player1 & Player2 mode
  - Active tab highlighted in green, inactive in gray

- **Player & Bot Mode (Default):**
  - Shows: Time/Distance selectors + "Pace For Avatar partner" dropdown
  - Works exactly as before - training between real player and virtual boat
  - Clicking Start validates inputs and navigates to avatar training

- **Player1 & Player2 Mode:**
  - Shows: Time/Distance selectors + Bluetooth device list
  - Hides: "Pace For Avatar partner" section
  - Bluetooth list shows ONLY Android phones (filters out KayakPro machines)
  - User can select Player2's phone from the list
  - Shows confirmation dialog before starting training

- **Bluetooth Scanning:**
  - Automatically scans for nearby Android phones when Player1 & Player2 mode is selected
  - Filters to show only phones (excludes devices with "kayak", "gem", "kp" in name)
  - Manual refresh button to rescan
  - Scan runs for 5 seconds then stops automatically

- **Confirmation Dialog:**
  - Shows before starting Player1 & Player2 training
  - Displays Player2's device name
  - Options: Cancel or Start training
  - Close button (X) in top right

### 3. New Files Created

#### `PhoneBluetoothAdapter.kt`
- Adapter for displaying phone devices in Bluetooth list
- Uses same item layout as machine Bluetooth list
- Shows device name and connection status
- Handles device selection

#### `dialog_confirm_player_connection.xml`
- Confirmation dialog layout
- Shows selected Player2 device name
- Cancel and Start buttons
- Matches app's design style (dark theme, green accent)

## User Flow

### Player & Bot Mode (Existing Flow - Unchanged):
1. Avatar training page loads with "Player & Bot" selected by default
2. User selects Time OR Distance
3. User selects Pace for Avatar partner
4. User clicks Start
5. Training begins with real player vs virtual boat

### Player1 & Player2 Mode (New Flow):
1. User clicks "Player1 & Player2" tab
2. UI switches to show Bluetooth device list instead of Pace selector
3. App automatically scans for nearby Android phones
4. User selects Player2's phone from the list
5. User clicks Start button
6. Confirmation dialog appears showing Player2's device name
7. User clicks "Start" in dialog
8. Training begins with Player1 vs Player2

## Technical Details

### Bluetooth Scanning
- Uses BluetoothLE scanner (same as machine connection)
- Scans for 5 seconds on tab switch
- Can be manually refreshed with refresh button
- Filters: Excludes devices with "kayak", "gem", or "kp" in name (case-insensitive)
- Permission handling for BLUETOOTH_SCAN, BLUETOOTH_CONNECT, etc.

### State Management
- `isPlayerBotMode` boolean tracks current mode
- `selectedPlayer2Device` stores selected phone
- `phoneDevices` list maintains discovered phones
- Session manager stores training parameters

### Data Flow
When starting Player1 & Player2 training:
- Distance/Time settings are saved to session
- Pace is set to "0" (not used in P2P mode)
- Player2 device address passed to training screen via bundle
- `isPlayer2Mode` flag set to true in bundle

## Next Steps / TODO

The implementation includes a placeholder for the actual Player1 & Player2 connection logic:
```kotlin
// TODO: Implement Player1 & Player2 training start logic
// This would typically involve:
// 1. Establishing connection with Player2's device
// 2. Syncing training parameters  
// 3. Navigating to the training screen
```

To fully implement Player1 & Player2 training, you'll need to:
1. **Establish Bluetooth connection** between phones (not just discovery)
2. **Sync training parameters** (time/distance) between Player1 and Player2
3. **Share real-time training data** during workout (distance, speed, calories, etc.)
4. **Update avatar training screen** to display both players' data side-by-side (as shown in the screenshot)
5. **Handle connection loss** during training

## Testing Checklist

- [ ] Tab switching works correctly
- [ ] Player & Bot mode shows Pace selector
- [ ] Player1 & Player2 mode shows Bluetooth list
- [ ] Bluetooth scan finds nearby phones
- [ ] Refresh button rescans for devices
- [ ] Selecting a phone from list shows selection
- [ ] Start button validation works for both modes
- [ ] Confirmation dialog appears in Player1 & Player2 mode
- [ ] Training starts correctly in both modes
- [ ] Permissions are requested if not granted

## UI/UX Notes

- Tab buttons use same green (#89F05B) as Start button for active state
- Gray background (#4A4A4A) for inactive tabs
- Bluetooth list uses same dark background and styling as connection page
- Confirmation dialog matches app's error dialog style
- All text uses Bricolage Grotesque font for consistency
