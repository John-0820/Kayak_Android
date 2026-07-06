# Bluetooth Connection Fixes

## Issues Fixed

### 1. Connect Machine Page (BluetoothScreen) - Can't connect kayak console devices
**Problem**: The Bluetooth filter was too restrictive, requiring exact keyword matches. Some kayak console devices might have variations in their names.

**Solution**: 
- Expanded the kayak device filter to include more variations: "erg", "rowing"
- Changed from "Unknown" fallback to empty string to avoid false matches
- Made the filter logic clearer with explicit boolean check

**File**: `BluetoothScreen.kt`

**Keywords now accepted**:
- kayak
- gem
- kp
- ergometer
- kayakpro
- erg (NEW)
- rowing (NEW)

### 2. Avatar Start Page - Player1 & Player2 Mode - Show ALL nearby phones
**Problem**: The filter was TOO restrictive, only showing devices with specific phone brand names. Many Bluetooth phones don't broadcast manufacturer names.

**Solution**: 
- **REMOVED** the restrictive filter that required specific brand names
- Now shows **ALL Bluetooth devices** EXCEPT:
  - Kayak machines (kayak, gem, kp, ergometer, kayakpro)
  - This phone itself (filters out own MAC address)
  - Devices with no name or "Unknown" name
- This allows ANY phone/tablet to appear in the list for Player2 connection

**File**: `AvatarFragmentWithout.kt`

**New Logic**:
```
SHOW device IF:
  ✅ Has a valid name (not blank, not "Unknown")
  ✅ Is NOT a kayak machine
  ✅ Is NOT this phone itself
```

## Summary of Changes

### BluetoothScreen.kt (Connect Machine page)
```kotlin
// OLD - Too restrictive
if (!nameLower.contains("kayak") && 
    !nameLower.contains("gem") && 
    !nameLower.contains("kp") &&
    !nameLower.contains("ergometer") &&
    !nameLower.contains("kayakpro")) {
    return  // Skip
}

// NEW - More inclusive for kayak devices
val isKayakDevice = nameLower.contains("kayak") || 
                    nameLower.contains("gem") || 
                    nameLower.contains("kp") ||
                    nameLower.contains("ergometer") ||
                    nameLower.contains("kayakpro") ||
                    nameLower.contains("erg") ||
                    nameLower.contains("rowing")

if (!isKayakDevice) {
    return  // Skip
}
```

### AvatarFragmentWithout.kt (Avatar Start - Player2 mode)
```kotlin
// OLD - Only showed devices with specific brand names
val isLikelyPhone = nameLower.contains("phone") ||
                   nameLower.contains("iphone") ||
                   nameLower.contains("samsung") ||
                   // ... 20+ brand checks ...

if (isLikelyPhone) {
    // Add device
}

// NEW - Show ALL devices except kayak machines and self
if (nameLower.contains("kayak") || 
    nameLower.contains("gem") || 
    nameLower.contains("kp") ||
    nameLower.contains("ergometer") ||
    nameLower.contains("kayakpro")) {
    return  // Skip kayak machines
}

val currentDeviceAddress = bluetoothAdapter?.address
if (device.address == currentDeviceAddress) {
    return  // Skip this phone itself
}

// Add ALL other devices
phoneDevices.add(BtRow(device = device, isConnected = false))
```

## Testing Instructions

### Test 1: Connect Machine Page
1. Open "CONNECTING DEVICES" page
2. Click refresh
3. **Expected**: Should see kayak console/ergometer devices
4. **Should NOT see**: Regular phones, tablets, headphones
5. Click a kayak device to connect
6. **Expected**: Should connect successfully with green dot indicator

### Test 2: Avatar Start - Player2 Mode
1. Open Avatar Training page
2. Click "Player1 & Player2" tab
3. Have another phone nearby with Bluetooth enabled and discoverable
4. Click the scan button
5. **Expected**: Should see ALL nearby phones/tablets in the list
6. **Should NOT see**: 
   - Kayak machines
   - This phone itself
   - "Unknown" devices
   - Devices with no name
7. Click a phone in the list
8. **Expected**: Both phones should show pairing dialog
9. Accept pairing on both phones
10. **Expected**: Green dot appears next to connected device

## What Works Now

✅ Connect Machine page shows kayak consoles with expanded keywords
✅ Avatar Player2 mode shows ALL nearby phones except:
   - Kayak machines
   - This phone itself  
   - Unknown/unnamed devices
✅ Pairing request shows on both phones when connection initiated
✅ Green dot indicator for connected/paired devices
✅ Already paired devices appear immediately in the list
✅ 10-second scan for new devices

## What Still Needs Implementation

⚠️ **Real-time data sync** between Player1 and Player2 phones during training
- Currently, selecting a device and starting training works
- But Player2's stats won't update in real-time during the race
- Need to implement Bluetooth socket connection for bidirectional data exchange
- Need to implement protocol for syncing distance, pace, stroke rate, etc.

## Technical Notes

- **bluetoothAdapter?.address** returns this device's MAC address for self-filtering
- **createBond()** initiates the Android system pairing dialog
- **bondedDevices** returns already paired devices from system
- Scan runs for 10 seconds before stopping automatically
- Lottie animation (pluse.json) shows during scan
