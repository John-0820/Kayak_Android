# Kayak Console Connection Fix

## Problems Fixed

### 1. Scan Time Too Short
**Before**: 3 seconds
**Now**: 10 seconds
**Why**: Kayak devices don't always advertise immediately

### 2. Not Showing Paired Devices
**Before**: Only showed devices found during scan
**Now**: Shows already-paired kayak devices immediately
**Why**: If kayak was paired before, it should appear instantly

### 3. Filter Too Restrictive
**Before**: Only checked for: kayak, gem, kp, ergometer, kayakpro, erg, rowing
**Now**: Added: paddle, concept2, c2, pm5
**Why**: Different kayak brands use different naming

### 4. No Debug Logging
**Before**: No way to see what devices were found
**Now**: Logs every device found
**Why**: Can see exactly what the scan is finding

## How to Debug

### Step 1: Check Logs While Scanning
```bash
adb logcat | grep "BluetoothScan"
```

You'll see:
```
BluetoothScan: Found device: Galaxy S21 (XX:XX:XX:XX:XX:XX)
BluetoothScan: Skipping non-kayak device: Galaxy S21
BluetoothScan: Found device: PM5-12345 (YY:YY:YY:YY:YY:YY)
BluetoothScan: ✓ Kayak device found: PM5-12345
```

### Step 2: If Kayak Not Appearing

**Check if it's advertising:**
```bash
adb logcat | grep "Found device"
```

**If you see your kayak name in the "Found device" logs but it says "Skipping":**
- The filter doesn't match your kayak's name
- Tell me the exact device name from the log
- I'll add it to the filter

**If you DON'T see your kayak name at all:**
- Kayak device is not advertising via Bluetooth
- Check kayak is powered on
- Check kayak Bluetooth is enabled
- Move phone very close to kayak (1 meter)
- Try pairing in phone settings first:
  ```
  Settings → Bluetooth → Scan for devices
  (If kayak appears here, it will appear in app)
  ```

### Step 3: Check Bonded Devices
```bash
adb shell dumpsys bluetooth_manager | grep -A 5 "Bonded devices"
```

If your kayak is in this list, the app should show it immediately.

## Testing Steps

### Test 1: Fresh Start (Not Paired)
1. Open app
2. Go to "CONNECTING DEVICES" page
3. Tap refresh button
4. **Watch logs**:
   ```bash
   adb logcat | grep "BluetoothScan"
   ```
5. **Wait 10 seconds** (full scan duration)
6. Check if kayak device appears in list

### Test 2: Already Paired
1. Go to phone Settings → Bluetooth
2. Pair with kayak device manually
3. Open app
4. Go to "CONNECTING DEVICES" page
5. **Kayak should appear immediately** (no scan needed)

### Test 3: Connection
1. Find kayak device in list
2. Tap device name
3. Should connect and show green dot

## Supported Kayak Device Names

The filter now accepts device names containing ANY of these keywords (case-insensitive):

- `kayak`
- `gem`
- `kp`
- `ergometer`
- `kayakpro`
- `erg`
- `rowing`
- `paddle`
- `concept2` (added)
- `c2` (added)
- `pm5` (added - Performance Monitor 5)

**Examples of matching names**:
- "KayakPro-GEM-1234" ✓
- "PM5-56789" ✓
- "Concept2-PM5" ✓
- "Ergometer-BLE" ✓
- "GEM-Rowing" ✓

## If Your Kayak Still Doesn't Appear

### Option 1: Add Your Device Name to Filter

1. Run this to see ALL devices:
   ```bash
   adb logcat | grep "Found device:"
   ```

2. Find your kayak device name in the output

3. Tell me the EXACT name (e.g., "WaterRower-12345")

4. I'll add it to the filter

### Option 2: Temporarily Show ALL Devices (For Testing)

If you want to test with ALL Bluetooth devices visible (not just kayaks):

1. Open `BluetoothScreen.kt`
2. Find this code:
   ```kotlin
   if (!isKayakDevice) {
       Log.d("BluetoothScan", "Skipping non-kayak device: $deviceName")
       return  // Skip non-kayak devices
   }
   ```
3. Comment it out:
   ```kotlin
   // if (!isKayakDevice) {
   //     Log.d("BluetoothScan", "Skipping non-kayak device: $deviceName")
   //     return  // Skip non-kayak devices
   // }
   ```
4. Rebuild APK
5. Now ALL devices with names will appear

### Option 3: Manual Pairing First

1. Settings → Bluetooth → Scan
2. Find kayak device
3. Tap to pair
4. Accept pairing on both devices
5. Open app → CONNECTING DEVICES
6. Kayak should now appear in list

## Summary of Changes

### scanForDevices()
```kotlin
// OLD
- Scan for 3 seconds
- Don't show bonded devices
- Start scanning

// NEW
+ Show bonded kayak devices first (immediate)
+ Scan for 10 seconds
+ Start scanning
```

### leScanCallback
```kotlin
// OLD
- Basic filter (7 keywords)
- No logging

// NEW
+ Log ALL devices found
+ Extended filter (11 keywords including PM5, Concept2)
+ Log which devices are skipped
+ Log which kayak devices are found
```

## What You'll See Now

### On "CONNECTING DEVICES" Page:
1. **Immediate**: Any already-paired kayak devices
2. **After 0-10s**: New kayak devices found during scan
3. **In logs**: Every device found and why it was accepted/skipped

### In Logcat:
```
BluetoothScan: Found device: Galaxy Watch (XX:XX:XX:XX:XX:XX)
BluetoothScan: Skipping non-kayak device: Galaxy Watch
BluetoothScan: Found device: PM5-12345 (YY:YY:YY:YY:YY:YY)
BluetoothScan: ✓ Kayak device found: PM5-12345
```

## Why It Works Now

✅ **10 second scan** - More time to find devices
✅ **Shows paired devices immediately** - No waiting if already paired
✅ **Extended filter** - Covers more kayak device names
✅ **Full logging** - Can see exactly what's happening
✅ **Lists bonded devices first** - Better user experience

**If your kayak console still doesn't appear, share the logcat output showing "Found device:" messages and I'll add your device to the filter!**
