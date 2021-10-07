import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_yidun_captcha/flutter_yidun_captcha.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_yidun_captcha');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await YidunCaptcha.sdkVersion, '42');
  });
}
