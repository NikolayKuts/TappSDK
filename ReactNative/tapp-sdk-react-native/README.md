# tapp-sdk-react-native

React Native wrapper for the Tapp Android SDK.

## Installation


```sh
npm install tapp-sdk-react-native
```

For Android local development, publish the Android SDK first:

```sh
./gradlew :Library:publishTappSdkToMavenLocal
```

The Android app that consumes this package must resolve dependencies from `mavenLocal()`.


## Usage


```ts
import { TappSdk } from 'tapp-sdk-react-native';

// ...

TappSdk.initialize('https://example.com/tapp-config.json');
```


## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
