# Live Character OS — 1.0.1

Mobile-first Live2D-like character runtime foundation for Android. The project is designed around a real-time simulation core, layered character rendering, Android interaction, persistent overlay, screen-perception permission flow, and secure provider settings.

## Scope

- Native Android/Kotlin application.
- Real-time Canvas frame loop with bounded frame delta.
- 2-bone arm IK with reach constraints and smoothed target following.
- Idle breathing and secondary motion.
- Layered procedural renderer: shadow, body, outfit, head, face, hair, accessory, effect.
- Persistent screen overlay using a foreground service.
- AccessibilityService gesture bridge with finite-coordinate and duration validation.
- MediaProjection permission entry point.
- Android Keystore-backed AES/GCM storage for provider API keys.
- Mobile-first settings UI.
- Pure Kotlin engine verification harness.
- GitHub Actions Android debug APK pipeline.

## Architecture

```text
CharacterProfile / CharacterAssetSet
              ↓
       CharacterEngine
   simulation + IK + motion
              ↓
        CharacterPose
              ↓
       CharacterRenderer
              ↓
       CharacterCanvasView
              ↓
 MainActivity / Overlay Service

AccessibilityService → InteractionPlanner → Android gestures
MediaProjection permission → ScreenPerceptionController
SecureSettingsStore → Android Keystore → provider secrets
```

## Safety boundaries

The application does not silently capture the screen, dispatch gestures, or store provider credentials in plaintext. Screen perception requires explicit MediaProjection consent. Android interaction requires the system Accessibility permission. Overlay requires the system overlay permission.

## Build

A full Android SDK/Gradle toolchain is required. From the project root:

```bash
gradle :app:assembleDebug
```

GitHub Actions also builds `app-debug.apk` automatically on pushes to `main`.

## Engine verification

Run `bash verify-engine.sh` with Kotlin installed. The harness checks finite pose output, IK reach enforcement, invalid-target rejection, and target clearing.

## Version

`1.0.1` — Android runtime foundation plus automated APK build pipeline.