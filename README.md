# LocalMind (Liome)

An Android app for running an AI assistant entirely on your device. No API calls, no account, no internet required once the model is downloaded.

The app is written in Kotlin with Jetpack Compose. Inference runs through a native library (`app/src/main/cpp`) that loads GGUF models locally.

## About

I wanted a chatbot that works with no connection and no account, where the model actually runs on the phone. That's the idea behind Liome. It's still early and I'm working on it in the open. I'm Saddek, a developer in Algeria. More of my projects are at [saddexrnx.github.io](https://saddexrnx.github.io), and I'm on [Telegram](https://t.me/Saddex_x).

## What it does

- Downloads GGUF models and runs them on-device through a JNI bridge to a local inference engine
- Profiles the device hardware and recommends models that will actually run on it
- Keeps conversations in a local Room database
- Markdown rendering in chat, with themes you can switch
- Screens for chat, model management, onboarding, performance, and privacy

Everything stays on the phone. There is no server and no remote inference.

## How it fits together

```
app/src/main/java/com/example/
  engine/    GGUF parser, local inference engine, native bridge
  model/     model catalog, download manager, recommendation logic
  data/      Room database, DAOs, conversation and message entities
  device/    hardware profiling
  ui/        Compose screens (chat, models, onboarding, performance, privacy)
app/src/main/cpp/
  localmind_jni.cpp   native inference bridge
  CMakeLists.txt
```

## Build

This is a standard Gradle project. Open it in Android Studio or build from the command line:

```sh
./gradlew assembleDebug
```

Requires the Android SDK and NDK (the native bridge is compiled with CMake).

## Status

Work in progress. The model catalog and recommendation engine are the parts most likely to change.

## Contact

Built by Saddex. Portfolio: https://saddexrnx.github.io/. Telegram: [@Saddex_x](https://t.me/Saddex_x).
