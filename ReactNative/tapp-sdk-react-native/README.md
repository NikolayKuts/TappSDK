# Tapp SDK React Native

React Native wrapper for the Tapp Android SDK. It initializes the native SDK so the host app can fetch widget configuration, cache assets, and expose the Tapp home screen widget.

## Requirements

- Node.js 22+
- Android Studio / Android SDK
- Yarn 4 via Corepack or the bundled Yarn release

## Install in a React Native App

Install the packaged React Native library in a host app:

```sh
yarn add ./tapp-sdk-react-native-0.1.0.tgz
```

The package includes the Tapp Android SDK AAR, so a host app does not need to publish the Android SDK separately.

## API

```ts
import { TappSdk } from 'tapp-sdk-react-native';

TappSdk.initialize('https://example.com/tapp-config.json');
```

`initialize` should be called once when the app starts. On Android it fetches the remote JSON configuration, stores the last fetch time, caches image assets locally, and prepares the home screen widget configuration.

## Demo App

The demo app already depends on the local `.tgz` package. No manual `yarn add` is needed for the demo.

From `ReactNative/tapp-sdk-react-native`:

```sh
yarn install
yarn demo android
```

The demo app calls `TappSdk.initialize(...)` on startup. After installing it, add `TappWidget` from the launcher widget picker and tap the center spin button.

## Notes

- Android is the supported platform for this assignment.
- The current wrapper exposes a minimal API: `TappSdk.initialize(configurationUrl)`.
- JSON configuration and image assets are cached by the native Android SDK.
- The `.tgz` package is the intended installable React Native artifact for review.
