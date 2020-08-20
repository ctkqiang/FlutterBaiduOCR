import 'package:flutter/services.dart';

class BaiduOcrPlugin {
  static const MethodChannel _channel = const MethodChannel('95Flutter/flutter_baidu_ocr');

  static Future<String> init(String appKey, String secretKey) async {
    String result = await _channel.invokeMethod('init', <String, dynamic>{'appKey': appKey, 'secretKey': secretKey});
    return result;
  }

  static Future<String> getIdCardInfo(bool isFront) async {
    String result = await _channel.invokeMethod(isFront ? 'idCardFront' : 'idCardBack');
    return result;
  }

  static Future<String> getBankCard() async {
    String result = await _channel.invokeMethod('bankCard');
    return result;
  }

  static Future<String> getVehicle() async {
    String result = await _channel.invokeMethod('vehicle');
    return result;
  }

  static Future<String> getDriving() async {
    String result = await _channel.invokeMethod('driving');
    return result;
  }
}
