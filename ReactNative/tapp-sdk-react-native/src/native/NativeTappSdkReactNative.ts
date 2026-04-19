import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface Spec extends TurboModule {
  initialize(configurationUrl: string): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('TappSdkReactNative');
