import { useEffect } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { TappSdk } from 'tapp-sdk-react-native';

const TAPP_CONFIGURATION_URL =
  'https://drive.google.com/uc?export=download&id=19VNoOAHU0Xegk9Dl99qDVFWRcqwGdx9q';

export default function App() {
  useEffect(() => {
    TappSdk.initialize(TAPP_CONFIGURATION_URL);
  }, []);

  return (
    <View style={styles.container}>
      <Text>Tapp SDK React Native wrapper is initialized.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
