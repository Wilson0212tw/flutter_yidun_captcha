package org.leanflutter.plugins.flutter_yidun_captcha;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.netease.nis.captcha.Captcha;
import com.netease.nis.captcha.CaptchaConfiguration;
import com.netease.nis.captcha.CaptchaListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterYidunCaptchaPlugin
 */
public class FlutterYidunCaptchaPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware {
    private static final String CHANNEL_NAME = "flutter_yidun_captcha";
    private static final String EVENT_CHANNEL_NAME = "flutter_yidun_captcha/event_channel";

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private EventChannel eventChannel;
    private EventChannel.EventSink eventSink;

    private Context context;
    private Activity activity;
    private Handler handler = new Handler(Looper.getMainLooper());

    private void setupChannel(BinaryMessenger messenger, Context context) {
        this.context = context;

        this.channel = new MethodChannel(messenger, CHANNEL_NAME);
        this.channel.setMethodCallHandler(this);

        this.eventChannel = new EventChannel(messenger, EVENT_CHANNEL_NAME);
        this.eventChannel.setStreamHandler(this);
    }

    private void teardownChannel() {
        this.context = null;

        this.channel.setMethodCallHandler(null);
        this.channel = null;

        this.eventChannel.setStreamHandler(null);
        this.eventChannel = null;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.setupChannel(flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        this.teardownChannel();
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        this.activity = activityPluginBinding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
        this.activity = activityPluginBinding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        this.activity = null;
    }

    @Override
    public void onListen(Object args, EventChannel.EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @Override
    public void onCancel(Object args) {
        this.eventSink = null;
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        FlutterYidunCaptchaPlugin plugin = new FlutterYidunCaptchaPlugin();
        plugin.setupChannel(registrar.messenger(), registrar.activeContext());
    }

    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {
        if (call.method.equals("getSDKVersion")) {
            handleMethodGetSDKVersion(call, result);
        } else if (call.method.equals("verify")) {
            handleMethodVerify(call, result);
        } else {
            result.notImplemented();
        }
    }

    private void handleMethodGetSDKVersion(@NonNull MethodCall call, @NonNull Result result) {
        result.success("3.2.0");
    }

    private void handleMethodVerify(@NonNull MethodCall call, @NonNull final Result result) {
        final CaptchaConfiguration.Builder configurationBuilder = new CaptchaConfiguration.Builder();
        configurationBuilder.listener(new CaptchaListener() {
            @Override
            public void onReady() {
                sendEventData("onReady", null);
            }

            @Override
            public void onValidate(String result, String validate, String message) {
                final Map<String, Object> data = new HashMap<>();
                data.put("result", result);
                data.put("validate", validate);
                data.put("message", message);

                sendEventData("onValidate", data);
            }

            @Override
            public void onError(int code, String message) {
                final Map<String, Object> data = new HashMap<>();
                data.put("code", code);
                data.put("message", message);

                sendEventData("onError", data);
            }

            @Override
            public void onClose(Captcha.CloseType closeType) {
                String[] closeTypes = new String[]{
                        "UNDEFINE_CLOSE",
                        "USER_CLOSE",
                        "VERIFY_SUCCESS_CLOSE"
                };
                final Map<String, Object> data = new HashMap<>();
                data.put("closeType", closeTypes[closeType.ordinal()]);
                sendEventData("onClose", data);
            }
        });

        if (call.hasArgument("captchaId"))
            configurationBuilder.captchaId((String) call.argument("captchaId"));
        if (call.hasArgument("mode"))
            configurationBuilder.mode(CaptchaConfiguration.ModeType.valueOf((String) call.argument("mode")));
        if (call.hasArgument("timeout"))
            configurationBuilder.timeout((int) call.argument("timeout"));
        if (call.hasArgument("languageType"))
            configurationBuilder.languageType(CaptchaConfiguration.LangType.valueOf((String) call.argument("languageType")));

        final CaptchaConfiguration captchaConfiguration = configurationBuilder.build(activity);
        final Captcha captcha = Captcha.getInstance().init(captchaConfiguration);

        captcha.validate();

        result.success(true);
    }

    private void sendEventData(String method, Map<String, Object> data) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put("method", method);
        if (data != null) {
            eventData.put("data", data);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                eventSink.success(eventData);
            }
        };
        handler.post(runnable);
    }
}
