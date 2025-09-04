# Logion App Permissions

This document lists all the permissions required by the Logion app and explains why each permission is needed.

## Required Permissions

### 🌐 Network & Internet Access
- **`INTERNET`** - Required for:
  - OpenAI API calls (transcription, summarization, text processing)
  - Web page content fetching
  - YouTube video information retrieval
  - All AI processing features

- **`ACCESS_NETWORK_STATE`** - Required for:
  - Checking network connectivity before making API calls
  - Handling offline scenarios gracefully

### 🎙️ Audio Permissions
- **`RECORD_AUDIO`** - Required for:
  - Voice recording functionality (main feature)
  - Audio transcription processing
  - Audio file upload and processing

- **`MODIFY_AUDIO_SETTINGS`** - Required for:
  - Optimizing audio recording quality
  - Managing audio focus during recording

### 📷 Camera Permissions
- **`CAMERA`** - Required for:
  - Taking pictures for OCR text extraction
  - Image capture functionality
  - Visual content processing

### 📁 Storage & File Access Permissions

#### For Android 13+ (API 33+) - Granular Media Permissions
- **`READ_MEDIA_IMAGES`** - Required for:
  - Accessing images from gallery for OCR processing
  - Image upload and analysis features

- **`READ_MEDIA_AUDIO`** - Required for:
  - Accessing audio files from device storage
  - Audio file upload and transcription

- **`READ_MEDIA_VIDEO`** - Required for:
  - Future video processing capabilities
  - Media file analysis

#### For Android 10-12 (API 29-32)
- **`READ_EXTERNAL_STORAGE`** - Required for:
  - Accessing all media files (images, audio, documents)
  - File upload functionality across all features

#### For Android 9 and below (API 28-)
- **`WRITE_EXTERNAL_STORAGE`** (maxSdkVersion="28") - Required for:
  - Saving temporary files during processing
  - Audio recording file storage

### 📄 Document Access
- **`MANAGE_DOCUMENTS`** - Required for:
  - PDF document upload and processing
  - Word document analysis
  - File picker functionality

### ⚡ System Permissions
- **`WAKE_LOCK`** - Required for:
  - Keeping device awake during long audio recordings
  - Background processing of large files
  - Preventing interruption during AI processing

- **`VIBRATE`** - Required for:
  - Haptic feedback during interactions
  - Recording start/stop notifications
  - User interface feedback

## Hardware Features (Optional)

- **`android.hardware.camera`** (required="false") - For:
  - Camera functionality on devices that support it
  - Graceful degradation on devices without cameras

- **`android.hardware.microphone`** (required="false") - For:
  - Audio recording on supported devices
  - Fallback behavior for devices without microphones

- **`android.hardware.touchscreen`** (required="false") - For:
  - Touch interface support
  - Alternative input method compatibility

## Feature-Permission Matrix

| Feature | Required Permissions |
|---------|---------------------|
| Voice Recording | `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `WAKE_LOCK` |
| Image OCR | `CAMERA`, `READ_MEDIA_IMAGES`, `INTERNET` |
| Audio Upload | `READ_MEDIA_AUDIO`, `INTERNET` |
| Document Upload | `MANAGE_DOCUMENTS`, `READ_EXTERNAL_STORAGE`, `INTERNET` |
| Web Page Processing | `INTERNET`, `ACCESS_NETWORK_STATE` |
| Video URL Processing | `INTERNET`, `ACCESS_NETWORK_STATE` |
| File Storage | `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` |
| Background Processing | `WAKE_LOCK` |
| User Feedback | `VIBRATE` |

## Permission Request Strategy

The app now requests permissions comprehensively with user-friendly explanations:

1. **On App Launch**:
   - Shows a beautiful permission rationale dialog explaining why each permission is needed
   - Users can choose to grant permissions immediately or defer for later
   - Version-aware permission requests (different permissions for different Android versions)

2. **Comprehensive Permission Set** (requested on first launch):
   - `RECORD_AUDIO` - for voice recording functionality
   - `CAMERA` - for photo capture and OCR processing  
   - `READ_MEDIA_IMAGES` (Android 13+) or `READ_EXTERNAL_STORAGE` (older versions) - for image access
   - `READ_MEDIA_AUDIO` (Android 13+) - for audio file access
   - `READ_MEDIA_VIDEO` (Android 13+) - for future video capabilities
   - `WRITE_EXTERNAL_STORAGE` (Android 9 and below only) - for temporary file storage
   - `MODIFY_AUDIO_SETTINGS` - for optimal audio recording quality
   - `VIBRATE` - for haptic feedback

3. **Runtime Permission Checks**:
   - Individual screens (like ImageCaptureScreen) perform additional permission checks
   - Graceful fallback when permissions are denied
   - Clear UI messaging about missing permissions

4. **User Experience**:
   - Beautiful permission rationale dialog with icons and explanations
   - Privacy protection notice
   - Non-blocking approach - users can still use available features
   - Option to request permissions later when needed

## Privacy Considerations

- **No sensitive data storage**: All processing is done through secure API calls
- **Temporary files**: Audio/image files are only temporarily stored for processing
- **No background recording**: Audio recording only happens when explicitly started by user
- **No location data**: App does not access or store location information
- **No contacts access**: App does not access user's contacts or personal information

## Version Compatibility

The permission model is designed to work across different Android versions:

- **Android 13+ (API 33+)**: Uses granular media permissions
- **Android 10-12 (API 29-32)**: Uses scoped storage with READ_EXTERNAL_STORAGE
- **Android 9- (API 28-)**: Uses traditional storage permissions with WRITE_EXTERNAL_STORAGE

All permissions are requested at runtime with proper user consent dialogs.
