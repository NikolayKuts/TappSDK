# Tapp SDK Assignment

Minimal Kotlin Android home screen spin wheel widget with a React Native wrapper.

## Project Structure

- `Android/Library` - Kotlin Android SDK with the home screen widget implementation.
- `Android/app` - native Android demo app that consumes the SDK module.
- `ReactNative/tapp-sdk-react-native` - React Native wrapper library.
- `ReactNative/tapp-sdk-react-native/demo-app` - React Native demo app.
- `ReactNative/tapp-sdk-react-native/tapp-sdk-react-native-0.1.0.tgz` - installable React Native package.
- `Tapp Assignment.txt` - original task specification.

## What Is Implemented

- Remote JSON configuration loading with OkHttp.
- Kotlin Serialization JSON mapping into domain models.
- Last configuration fetch time and cached configuration in `SharedPreferences`.
- Remote image asset download and local file cache.
- Android home screen widget via `AppWidgetProvider` and `RemoteViews`.
- Spin wheel rendering with background, wheel, frame, and clickable spin button.
- Wheel spin animation on widget button tap.
- React Native API exposed as `TappSdk.initialize(configurationUrl)`.
- React Native demo app consuming the wrapper from the local `.tgz` package.

## Run the React Native Demo

The demo app already depends on the local `.tgz` package:

```json
"tapp-sdk-react-native": "file:../tapp-sdk-react-native-0.1.0.tgz"
```

From the repository root:

```sh
cd ReactNative/tapp-sdk-react-native
yarn install
yarn demo android
```

If global `yarn` is unavailable, use the bundled Yarn release:

```sh
node .yarn/releases/yarn-4.11.0.cjs install
node .yarn/releases/yarn-4.11.0.cjs demo android
```

After the app is installed:

1. Open the demo app once so it initializes the SDK.
2. Open the launcher widget picker.
3. Add `TappWidget` to the home screen.
4. Tap the center spin button to start the wheel animation.

## Run the Native Android Demo

From the repository root:

```sh
cd Android
./gradlew :app:installDebug
```

On Windows:

```powershell
cd Android
.\gradlew.bat :app:installDebug
```

## React Native Package

The wrapper package is built as:

```text
ReactNative/tapp-sdk-react-native/tapp-sdk-react-native-0.1.0.tgz
```

It includes the Android SDK AAR under:

```text
ReactNative/tapp-sdk-react-native/android/libs/tapp-sdk-android-0.2.0.aar
```

That means the React Native demo app does not need to publish the Android SDK to Maven Local.

To rebuild the package:

```sh
cd ReactNative/tapp-sdk-react-native
yarn prepare
npm pack --pack-destination .
```

## Configuration and Assets

The React Native demo calls `TappSdk.initialize(...)` from:

```text
ReactNative/tapp-sdk-react-native/demo-app/src/App.tsx
```

The remote JSON configuration defines:

- widget type and wheel content type;
- animation duration, spin count range, and easing;
- asset host plus relative asset paths.

The Android SDK joins `network.assets.host` with `wheel.assets.*`, downloads the images, and stores them in the app internal files directory.

## Key Files

- `Android/Library/src/main/java/com/tapp/sdk/library/TappSdk.kt`
- `Android/Library/src/main/java/com/tapp/sdk/library/widget/TappWidgetProvider.kt`
- `Android/Library/src/main/java/com/tapp/sdk/library/widget/TappWidgetRenderer.kt`
- `Android/Library/src/main/java/com/tapp/sdk/library/widget/TappWidgetAnimationController.kt`
- `ReactNative/tapp-sdk-react-native/src/library/TappSdk.native.ts`
- `ReactNative/tapp-sdk-react-native/android/src/main/java/com/tappsdkreactnative/TappSdkReactNativeModule.kt`
