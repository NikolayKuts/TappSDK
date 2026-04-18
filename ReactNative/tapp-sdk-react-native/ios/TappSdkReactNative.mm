#import "TappSdkReactNative.h"

@implementation TappSdkReactNative
- (void)initialize:(NSString *)configurationUrl {
    (void)configurationUrl;
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeTappSdkReactNativeSpecJSI>(params);
}

+ (NSString *)moduleName
{
  return @"TappSdkReactNative";
}

@end
