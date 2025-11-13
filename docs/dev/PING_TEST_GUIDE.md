# Ping Testing Guide

## How to Verify the Ping Feature Works

Follow these steps to test and verify the ping implementation:

---

## Step 1: Rebuild the Server

First, rebuild the server with the updated ping handling code:

```powershell
cd server
mvn clean compile
```

**Expected Output:**

```
[INFO] BUILD SUCCESS
```

---

## Step 2: Start the Server

Start the game server:

```powershell
cd server
mvn exec:java
```

**Expected Output:**

```
╔════════════════════════════════════════════════════════════╗
║      🎮 Tank Game Server - Pure Java Network Programming   ║
╠════════════════════════════════════════════════════════════╣
...
╚════════════════════════════════════════════════════════════╝

Enter winning score (default 10): [Press Enter]

[SERVER] Waiting for connections...
```

---

## Step 3: Start the Client

In a **NEW terminal window**, start the client web server:

```powershell
cd client
python -m http.server 3000
```

**Expected Output:**

```
Serving HTTP on :: port 3000 (http://[::]:3000/) ...
```

---

## Step 4: Open Browser and Connect

1. Open your browser (Chrome, Firefox, Edge)
2. Navigate to: `http://localhost:3000`
3. **Open Browser Console** (Press F12, then click "Console" tab)
4. Fill in the form:
   - Server address: `localhost`
   - Callsign: `TestPlayer`
5. Click **"Deploy to Battle"**
6. Click **"Join Game"** in the lobby

---

## Step 5: Verify Ping is Working

### Visual Check (In Game)

Look at the **top-left corner** of the screen. You should see:

```
❤️ 100    🎯 0    💀 0    📶 [XX] ms
```

The ping value `[XX]` should:

- ✅ Update every 2 seconds
- ✅ Show a number (e.g., 5, 15, 45)
- ✅ Change color based on value:
  - **Green** if < 50ms
  - **Orange** if 50-100ms
  - **Red** if > 100ms

### Browser Console Check

In the browser console (F12), you should see repeating logs every 2 seconds:

```
[PING] Starting ping monitoring...
[PING] Sending ping at: 1699901234567
[PING] Received pong! RTT: 15 ms
[PING] Sending ping at: 1699901236570
[PING] Received pong! RTT: 12 ms
[PING] Sending ping at: 1699901238575
[PING] Received pong! RTT: 18 ms
```

**What to look for:**

- ✅ `[PING] Starting ping monitoring...` appears once when joining
- ✅ `[PING] Sending ping...` appears every 2 seconds
- ✅ `[PING] Received pong! RTT: X ms` appears after each ping
- ✅ RTT values are reasonable (typically 1-50ms on localhost)

### Server Console Check

In the server terminal, you should see repeating logs:

```
[PING] Received ping from abcd1234, timestamp: 1699901234567
[PING] Sent pong to abcd1234
[PING] Received ping from abcd1234, timestamp: 1699901236570
[PING] Sent pong to abcd1234
```

**What to look for:**

- ✅ `[PING] Received ping...` appears every 2 seconds
- ✅ `[PING] Sent pong...` appears immediately after
- ✅ Timestamp values match those sent from client

---

## Step 6: Test Different Scenarios

### Test 1: Multiple Players

1. Open a second browser window (or incognito mode)
2. Connect with a different name
3. Both players should see their own ping values
4. Server should show ping logs for both players

### Test 2: Network Simulation

To test different ping colors, you can temporarily modify the client code:

Open `client/js/managers/network-manager.js` and change line in `handlePong()`:

```javascript
// Original
this.currentPing = Date.now() - sentTime;

// Test high ping (orange)
this.currentPing = 75;

// Test very high ping (red)
this.currentPing = 150;
```

Refresh the browser to see different colors.

**Remember to change it back to:**

```javascript
this.currentPing = Date.now() - sentTime;
```

### Test 3: Disconnect/Reconnect

1. Stop the server (Ctrl+C)
2. Watch the browser - connection should be lost
3. Restart the server
4. Ping should resume automatically (if auto-reconnect works)

---

## Troubleshooting

### Problem: Ping shows "0 ms" and never changes

**Possible Causes:**

1. ❌ Ping monitoring not started
2. ❌ Server not handling ping messages
3. ❌ Client not receiving pong responses

**Solutions:**

1. Check browser console for `[PING] Starting ping monitoring...`
2. Check server console for `[PING] Received ping...`
3. Verify no JavaScript errors in browser console (F12)

---

### Problem: Ping shows but doesn't update

**Check:**

1. Browser console - are new pings being sent?
2. Server console - are pings being received?
3. Browser console - are pongs being received?

**Debug:**
Look for any error messages in either console.

---

### Problem: Server shows "BUILD FAILURE"

**Solution:**

```powershell
cd server
mvn clean
mvn compile
```

If still failing, check for Java syntax errors in `ClientHandler.java`.

---

### Problem: Ping indicator not visible

**Check:**

1. Is the game connected? (Should see game canvas, not join screen)
2. Open browser console and type: `document.getElementById('ping-text')`
   - Should return an element, not `null`
3. Inspect element (Right-click on health stats, choose "Inspect")
   - Look for `<span id="ping-text">`

---

## Expected Results Summary

| Check                    | Expected Result           |
| ------------------------ | ------------------------- |
| Ping visible in UI       | ✅ Yes, top-left corner   |
| Ping value updates       | ✅ Every 2 seconds        |
| Color changes with value | ✅ Green/Orange/Red       |
| Browser console logs     | ✅ Ping send/receive logs |
| Server console logs      | ✅ Ping receive/send logs |
| Works on localhost       | ✅ 1-50ms typical         |
| Multiple players         | ✅ Each sees own ping     |

---

## Quick Verification Command

Run this in the browser console while in-game:

```javascript
// Check if ping monitoring is active
console.log(
  "Ping monitoring active:",
  window.gameClient?.networkManager?.pingInterval !== null
);

// Get current ping value
console.log(
  "Current ping:",
  window.gameClient?.networkManager?.getPing(),
  "ms"
);

// Check last ping timestamp
console.log(
  "Last ping sent at:",
  window.gameClient?.networkManager?.lastPingTimestamp
);
```

**Note:** This assumes the gameClient is accessible globally. If not, just check the console logs.

---

## Screenshot Verification

Take a screenshot of the game showing:

1. Top-left corner with ping indicator visible
2. Browser console with ping logs
3. Server terminal with ping logs

This proves the feature is working end-to-end!

---

## Performance Check

The ping feature should have **minimal impact**:

- Network: ~50 bytes per 2 seconds (negligible)
- CPU: <0.1% on both client and server
- Memory: ~100 bytes for timestamps
- No gameplay lag or stuttering

If you notice performance issues, check:

1. Other programs consuming bandwidth
2. Server under heavy load (many players)
3. Computer running other intensive tasks

---

## Success Criteria

✅ Ping value displays in top-left  
✅ Updates every 2 seconds  
✅ Shows realistic values (1-50ms localhost, 10-100ms remote)  
✅ Color-codes correctly  
✅ Browser logs show ping/pong  
✅ Server logs show ping/pong  
✅ No errors in console  
✅ No gameplay disruption

If all checkmarks pass: **Ping feature is working correctly!** 🎉
