#import <Flutter/Flutter.h>
#import <VerifyCode/NTESVerifyCodeManager.h>

@interface FlutterYidunCaptchaPlugin : NSObject<FlutterPlugin, FlutterStreamHandler, NTESVerifyCodeManagerDelegate>

@property(nonatomic,strong)NTESVerifyCodeManager *manager;

@end
