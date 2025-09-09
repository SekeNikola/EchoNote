# EchoNote Server Setup

## Prerequisites

1. **Android Studio** with Kotlin support
2. **Ngrok** (optional) - for public internet access
   - Download from: https://ngrok.com/download
   - Install on your computer, not required on Android device

## Setup Instructions

### Android Server

1. **Build and install** the Android app
   - The server starts automatically when the app launches
   - Server runs on port 8080
   - No additional setup required

2. **Find server URLs** in Settings:
   - Open the app → Settings
   - Look for "Server Status" section
   - You'll see local network URL (e.g., `http://192.168.1.100:8080`)
   - If ngrok is available, public URL will also appear

### Web Client

1. **Open** `web-client/index.html` in any web browser

2. **Get server URL** from Android app Settings screen

3. **Enter the URL** in web client connection field

4. **Click Connect** - you should see "Connected" status

## Server Access Options

### Option 1: Local Network (Recommended)
- **Same WiFi network**: Use the local IP shown in Settings
- **Example**: `http://192.168.1.100:8080`
- **Works for**: Devices on same WiFi network

### Option 2: Public Internet (Optional)
- **Install ngrok** on your computer
- **Run**: `ngrok http --url=tcp://your-android-ip:8080` 
- **Use ngrok URL** in web client
- **Works for**: Any device with internet access

## Usage

### Android App
- Server runs automatically in background
- Check Settings → Server Status for URLs
- Server continues running even when app is closed

### Web Client
- Add/edit tasks and notes through web interface
- Changes sync in real-time via WebSocket
- Works from any device that can reach the server

## API Endpoints

- `GET /tasks` - Get all tasks
- `POST /tasks` - Create new task
- `PUT /tasks/{id}` - Update task
- `GET /notes` - Get all notes  
- `POST /notes` - Create new note
- `PUT /notes/{id}` - Update note
- `WebSocket /sync` - Real-time sync

## Troubleshooting

1. **No server URL shown**: Wait a few seconds for server to start
2. **Connection failed**: Check if devices are on same network
3. **Can't access from internet**: Set up ngrok on a computer
4. **Port 8080 busy**: Restart the Android app

## Network Setup Examples

### Same WiFi Network
```
Android Phone (192.168.1.100:8080) ←→ Laptop (192.168.1.101)
Web client connects to: http://192.168.1.100:8080
```

### Internet Access via Computer
```
Android Phone → Computer with ngrok → Internet
Web client connects to: https://abc123.ngrok.io
```
