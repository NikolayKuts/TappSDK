import { Platform } from 'react-native';
import TappSdkReactNative from '../native/NativeTappSdkReactNative';

export const TappSdk = {
  initialize(configurationUrl: string): void {
    if (Platform.OS !== 'android') {
      throw new Error(
        'Tapp SDK initialization is currently supported only on Android.'
      );
    }

    if (typeof configurationUrl !== 'string') {
      throw new Error('configurationUrl must be a string.');
    }

    const normalizedConfigurationUrl = configurationUrl.trim();

    if (normalizedConfigurationUrl.length === 0) {
      throw new Error('configurationUrl must not be empty.');
    }

    TappSdkReactNative.initialize(normalizedConfigurationUrl);
  },
};
