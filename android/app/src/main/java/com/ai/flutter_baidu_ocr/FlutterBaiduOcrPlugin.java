package com.ai.flutter_baidu_ocr;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

import com.ai.flutter_baidu_ocr_example.MainActivity;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.google.gson.Gson;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FluttertoastPlugin
 */
public class FlutterBaiduOcrPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin, ActivityAware {

    private static final int REQUEST_CODE_GENERAL = 105;// 通用文字识别（含位置信息版）
    private static final int REQUEST_CODE_GENERAL_BASIC = 106;// 通用文字识别
    private static final int REQUEST_CODE_ACCURATE_BASIC = 107; // 通用文字识别(高精度版)
    private static final int REQUEST_CODE_ACCURATE = 108; // 通用文字识别（含位置信息高精度版）
    private static final int REQUEST_CODE_GENERAL_ENHANCED = 109;// 通用文字识别（含生僻字版）
    private static final int REQUEST_CODE_GENERAL_WEBIMAGE = 110; // 网络图片识别
    private static final int REQUEST_CODE_BANKCARD = 111;  // 银行卡识别
    private static final int REQUEST_CODE_VEHICLE_LICENSE = 120; // 行驶证识别
    private static final int REQUEST_CODE_DRIVING_LICENSE = 121;// 驾驶证识别
    private static final int REQUEST_CODE_LICENSE_PLATE = 122;// 车牌识别
    private static final int REQUEST_CODE_BUSINESS_LICENSE = 123;// 营业执照识别
    private static final int REQUEST_CODE_RECEIPT = 124;// 通用票据识别

    private static final int REQUEST_CODE_PASSPORT = 125; // 护照识别
    private static final int REQUEST_CODE_NUMBERS = 126; // 数字识别
    private static final int REQUEST_CODE_QRCODE = 127;// 二维码识别
    private static final int REQUEST_CODE_BUSINESSCARD = 128; // 名片识别
    private static final int REQUEST_CODE_HANDWRITING = 129;// 增值税发票识别
    private static final int REQUEST_CODE_LOTTERY = 130; // 彩票识别
    private static final int REQUEST_CODE_VATINVOICE = 131; // 手写识别
    private static final int REQUEST_CODE_CUSTOM = 132; // 自定义模板
    private static final int REQUEST_CODE_PICK_IMAGE_FRONT = 201;//相册选择 正面
    private static final int REQUEST_CODE_PICK_IMAGE_BACK = 202;//相册选择 反面
    private static final int REQUEST_CODE_CAMERA = 102;// 身份证拍照


    private String ak = "oWhYHquxoOWI1V4k0BgASNP5";
    private String sk = "AiEINj1Iww46TzyTjOeo9qW50z7kz9YY";

    private boolean hasGotToken = false;

    private static final String CHANNEL_NAME = "95Flutter/flutter_baidu_ocr";
    private MethodChannel channel;
    private MethodChannel.Result mResult;
    private OctResult mOctResult;
    private FlutterPluginBinding pluginBinding;
    private ActivityPluginBinding activityBinding;
    private Activity mActivity;
    private Application mApplication;
    private PluginRegistry.ActivityResultListener mActivityResultListener;

    public static void registerWith(PluginRegistry.Registrar registrar) {
        Activity activity = registrar.activity();
        Application application = null;
        if (registrar.context() != null) {
            application = (Application) (registrar.context().getApplicationContext());
        }
        FlutterBaiduOcrPlugin plugin = new FlutterBaiduOcrPlugin();
        plugin.setup(registrar.messenger(), application, activity, registrar, null);
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        pluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        pluginBinding = null;
    }

    private void teardownChannel() {
        activityBinding.removeActivityResultListener(mActivityResultListener);
        activityBinding = null;
        mActivityResultListener = null;
        channel.setMethodCallHandler(null);
        channel = null;
        mApplication = null;
        mActivity = null;
    }

    private void setup(
            final BinaryMessenger messenger,
            final Application application,
            final Activity activity,
            final PluginRegistry.Registrar registrar,
            final ActivityPluginBinding activityBinding) {
        this.mActivity = activity;
        this.mApplication = application;
        channel = new MethodChannel(messenger, CHANNEL_NAME);
        channel.setMethodCallHandler(this);
        mActivityResultListener = new PluginRegistry.ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                        if (!TextUtils.isEmpty(contentType)) {
                            if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                                String filePath = FileUtil.getIdCardFrontFile(mApplication).getAbsolutePath();
                                recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
                            } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                                String filePath = FileUtil.getIdCardBackFile(mApplication).getAbsolutePath();
                                recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
                            }
                        }
                    }
                }
                // 识别成功回调，银行卡识别
                if (requestCode == REQUEST_CODE_BANKCARD && resultCode == Activity.RESULT_OK) {
                    String path = FileUtil.getBankCardFile(mApplication).getAbsolutePath();
                    RecognizeService.recBankCard(mActivity, path,
                            new RecognizeService.ServiceListener() {
                                @Override
                                public void onResult(String result) {
                                    Log.v("", "BANKCARD===>" + result);
                                    if (mResult != null) {
                                        mOctResult = new OctResult();
                                        mOctResult.setFilePath(path);
                                        mOctResult.setBody(new Gson().fromJson(result, Map.class));
                                        mResult.success(new Gson().toJson(mOctResult));
                                        mResult = null;
                                    }
                                }
                            });
                }
                // 识别成功回调，行驶证识别
                if (requestCode == REQUEST_CODE_VEHICLE_LICENSE && resultCode == Activity.RESULT_OK) {
                    String path = FileUtil.getVehicleFile(mApplication).getAbsolutePath();
                    RecognizeService.recVehicleLicense(mActivity, path,
                            new RecognizeService.ServiceListener() {
                                @Override
                                public void onResult(String result) {
                                    Log.v("", "VEHICLE===>" + result);
                                    if (mResult != null) {
                                        mOctResult = new OctResult();
                                        mOctResult.setFilePath(path);
                                        mOctResult.setBody(new Gson().fromJson(result, Map.class));
                                        mResult.success(new Gson().toJson(mOctResult));
                                        mResult = null;
                                    }
                                }
                            });
                }
                // 识别成功回调，驾驶证识别
                if (requestCode == REQUEST_CODE_DRIVING_LICENSE && resultCode == Activity.RESULT_OK) {
                    String path = FileUtil.getDrivingFile(mApplication).getAbsolutePath();
                    RecognizeService.recDrivingLicense(mActivity, path,
                            new RecognizeService.ServiceListener() {
                                @Override
                                public void onResult(String result) {
                                    Log.v("", "DRIVING===>" + result);
                                    if (mResult != null) {
                                        mOctResult = new OctResult();
                                        mOctResult.setFilePath(path);
                                        mOctResult.setBody(new Gson().fromJson(result, Map.class));
                                        mResult.success(new Gson().toJson(mOctResult));
                                        mResult = null;
                                    }
                                }
                            });
                }

                return false;
            }
        };
        if (registrar != null) {
            // V1 embedding setup for activity listeners.
            registrar.addActivityResultListener(mActivityResultListener);
        } else {
            // V2 embedding setup for activity listeners.
            activityBinding.addActivityResultListener(mActivityResultListener);
        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activityBinding = binding;
        setup(
                pluginBinding.getBinaryMessenger(),
                (Application) pluginBinding.getApplicationContext(),
                activityBinding.getActivity(),
                null,
                activityBinding
        );
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        teardownChannel();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "init": {
                mResult = null;
                String appKey = (String) call.argument("appKey");
                String secretKey = (String) call.argument("secretKey");
                initAccessTokenWithAkSk(appKey, secretKey, result);
                break;
            }
            case "idCardFront": {
                mResult = result;
                Intent intent = new Intent(mActivity, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getIdCardFrontFile(mApplication).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
                mActivity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
                break;
            }
            case "idCardBack": {
                mResult = result;
                Intent intent = new Intent(mActivity, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getIdCardBackFile(mApplication).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
                mActivity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
                break;
            }
            case "bankCard": {
                mResult = result;
                Intent intent = new Intent(mActivity, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getBankCardFile(mApplication).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                        CameraActivity.CONTENT_TYPE_BANK_CARD);
                mActivity.startActivityForResult(intent, REQUEST_CODE_BANKCARD);
                break;
            }
            case "vehicle": {
                mResult = result;
                Intent intent = new Intent(mActivity, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getVehicleFile(mApplication).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                        CameraActivity.CONTENT_TYPE_GENERAL);
                mActivity.startActivityForResult(intent, REQUEST_CODE_VEHICLE_LICENSE);
                break;
            }
            case "driving": {
                mResult = result;
                Intent intent = new Intent(mActivity, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getDrivingFile(mApplication).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                        CameraActivity.CONTENT_TYPE_GENERAL);
                mActivity.startActivityForResult(intent, REQUEST_CODE_DRIVING_LICENSE);
                break;
            }
            default:
                mResult = null;
                result.notImplemented();
                break;
        }
    }

    private void initAccessTokenWithAkSk(String appKey, String secretKey, MethodChannel.Result result) {
        OCR.getInstance(mActivity).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String token = accessToken.getAccessToken();
                        hasGotToken = true;
                        result.success(token);
                    }
                });
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
//                alertText("AK，SK方式获取token失败", error.getMessage());
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result.error("", error.getMessage(), "AK，SK方式获取token失败");
                    }
                });
            }
        }, mApplication, appKey, secretKey);

    }

    private void recIDCard(String idCardSide, String filePath) {
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        // 设置身份证正反面
        param.setIdCardSide(idCardSide);
        // 设置方向检测
        param.setDetectDirection(true);
        // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
        param.setImageQuality(20);

        OCR.getInstance(mActivity).recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult idCardResult) {
                if (idCardResult != null) {
                    Log.v("", "idCardResult======>" + idCardResult.toString());
                    if (mResult != null) {
                        mOctResult = new OctResult();
                        mOctResult.setFilePath(filePath);
                        mOctResult.setBody(new Gson().fromJson(idCardResult.toString(), Map.class));
                        mResult.success(new Gson().toJson(mOctResult));
                        mResult = null;
                    }
                }
            }

            @Override
            public void onError(OCRError error) {
//                alertText("", error.getMessage());
                if (mResult != null) {
                    mResult.error("", error.getMessage(), "身份证识别失败");
                    mResult = null;
                }
            }
        });
    }
}
