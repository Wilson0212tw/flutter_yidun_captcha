#import "FlutterYidunCaptchaPlugin.h"


#define kScreenWidth [UIScreen mainScreen].bounds.size.width
#define kScreenHeight [UIScreen mainScreen].bounds.size.height


@implementation FlutterYidunCaptchaPlugin {
    FlutterEventSink _eventSink;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutter_yidun_captcha"
                                     binaryMessenger:[registrar messenger]];
    FlutterYidunCaptchaPlugin* instance = [[FlutterYidunCaptchaPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
    
    FlutterEventChannel* eventChannel =
    [FlutterEventChannel eventChannelWithName:@"flutter_yidun_captcha/event_channel"
                              binaryMessenger:[registrar messenger]];
    [eventChannel setStreamHandler:instance];
}

- (FlutterError*)onListenWithArguments:(id)arguments eventSink:(FlutterEventSink)eventSink {
    _eventSink = eventSink;
    
    return nil;
}

- (FlutterError*)onCancelWithArguments:(id)arguments {
    _eventSink = nil;
    
    return nil;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"getSDKVersion" isEqualToString:call.method]) {
        [self handleMethodGetSDKVersion:call result:result];
    } else if ([@"verify" isEqualToString:call.method]) {
        [self handleMethodVerify:call result:result];
    } else {
        result(FlutterMethodNotImplemented);
    }
}


- (void)handleMethodGetSDKVersion:(FlutterMethodCall*)call
                           result:(FlutterResult)result
{
    NSString *sdkVersion = [[NTESVerifyCodeManager getInstance] getSDKVersion];
    
    result(sdkVersion);
}

- (void)handleMethodVerify:(FlutterMethodCall*)call
                    result:(FlutterResult)result
{
    NSString *captchaId = call.arguments[@"captchaId"];
    NSNumber *timeout = call.arguments[@"timeout"];
    
    self.manager = [NTESVerifyCodeManager getInstance];
    self.manager.delegate = self;
    
    [self.manager configureVerifyCode:captchaId timeout:[timeout intValue]];
    self.manager.mode = NTESVerifyCodeNormal;
    self.manager.lang = NTESVerifyCodeLangCN;
    self.manager.alpha = 0.6;
    self.manager.color = [UIColor blackColor];
    self.manager.frame = CGRectNull;
    self.manager.openFallBack = YES;
    self.manager.fallBackCount = 3;
    self.manager.closeButtonHidden = NO;
    
    self.manager.shouldCloseByTouchBackground = YES;
    
    [self.manager openVerifyCodeView: nil];
    
    result([NSNumber numberWithBool: YES]);
}

- (void)sendEventData:(NSString*)method data:(NSDictionary *)data  {
    NSDictionary<NSString *, id> *eventData = @{
        @"method": method,
        @"data": data != nil ? data : @{},
    };
    self->_eventSink(eventData);
}

#pragma mark - NTESVerifyCodeManagerDelegate
- (void)verifyCodeInitFinish{
    [self sendEventData:@"onReady" data:nil];
}

- (void)verifyCodeInitFailed:(NSArray *)error {
    NSString *jsonString = [error firstObject];
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    NSError *err;
    NSDictionary<NSString *, id> *data = [NSJSONSerialization JSONObjectWithData:jsonData
                                                                         options:NSJSONReadingMutableContainers
                                                                           error:&err];

    [self sendEventData:@"onError" data:data];
}

- (void)verifyCodeValidateFinish:(BOOL)result validate:(NSString *)validate message:(NSString *)message{
    NSDictionary<NSString *, id> *data = @{
        @"result": result ? @"true" : @"false",
        @"validate": validate,
        @"message": message,
    };
    
    [self sendEventData:@"onValidate" data:data];
}

- (void)verifyCodeCloseWindow:(NTESVerifyCodeClose)closeType {
    NSString *closeTypeString = @"UNDEFINE_CLOSE";
    switch (closeType) {
        case NTESVerifyCodeCloseManual:
            closeTypeString= @"USER_CLOSE";
            break;
        case NTESVerifyCodeCloseAuto:
            closeTypeString =@"VERIFY_SUCCESS_CLOSE";
            break;
    }
    NSDictionary<NSString *, id> *data = @{
        @"closeType": closeTypeString,
    };
    [self sendEventData:@"onClose" data:data];
}

@end
