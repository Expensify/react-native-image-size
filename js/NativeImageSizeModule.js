// @flow
import type { TurboModule } from 'react-native/Libraries/TurboModule/RCTExport';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
    +getSize: (uri: string) => Promise<{ width: number, height: number }>;
}

export default (TurboModuleRegistry.getEnforcing<Spec>('RNImageSize'): ?Spec);
