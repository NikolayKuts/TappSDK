export const TappSdk = {
  initialize(_configurationUrl: string): void {
    throw new Error(
      'Tapp SDK initialization is currently supported only on native platforms.'
    );
  },
};
