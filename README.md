
# Logion

Logion is a minimal, dark-themed Android app for AI-powered voice notes and reminders. Capture, transcribe, summarize, and manage your notes with advanced AI and seamless reminders—all in a beautiful, modern UI.

## Features

- **Voice Capture**: Record audio notes, transcribed and summarized using OpenAI Whisper and GPT.
- **Text Notes**: Manually add and edit notes.
- **Edit Titles**: Rename AI-generated notes.
- **Voice Commands**: Ask for reminders, summaries, or search notes using natural language.
- **Notes Management**: Search, favorite, cross-link, archive, and mark notes as done.
- **Reminders & Notifications**: Schedule reminders with push notifications and quick actions.
- **Read Aloud**: Listen to your notes with built-in text-to-speech.
- **Offline Sync**: Capture notes offline and sync when online.

## UI & Design

- Minimal, dark theme: #121212 background, #1F1F1F/#222222 panels, #BB86FC/#03DAC6 accents, #FFC107 highlights
- Modern Compose UI with smooth navigation and animations
- Roboto/Inter/Google Sans typography

## Tech Stack

- **Jetpack Compose** for UI
- **Room** for local database
- **WorkManager** for reminders/notifications
- **MediaRecorder** for audio capture
- **Retrofit/OkHttp** for OpenAI API
- **TextToSpeech** for read aloud
- **Kotlin** (100%)

## Setup & Usage

1. Clone the repo and open in Android Studio.
2. Build the project (Kotlin, Gradle).
3. On first launch, enter your OpenAI API key (required for transcription and AI features).
4. Start recording, add notes, and set reminders!

## Security

- API key is stored securely in SharedPreferences and never hardcoded.

## Roadmap

- [ ] Backend sync for cloud backup
- [ ] More advanced AI commands
- [ ] UI polish and onboarding

---

**Logion** — Your personal AI voice notes and reminders assistant.
