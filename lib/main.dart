import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_baidu_ocr/plugins/baidu_ocr_plugin.dart';
import 'dart:convert' as convert;

import 'package:flutter_baidu_ocr/utils/file_image_ex.dart';

const String ak = "oWhYHquxoOWI1V4k0BgASNP5";
const String sk = "AiEINj1Iww46TzyTjOeo9qW50z7kz9YY";

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  String result = await BaiduOcrPlugin.init(ak, sk);
  print('百度OCR 初始化=》$result');
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
        // This makes the visual density adapt to the platform that you run
        // the app on. For desktop platforms, the controls will be smaller and
        // closer together (more dense) than on mobile platforms.
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String _result;
  String _filePath;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _result ?? "",
            ),
            _filePath != null && _filePath.isNotEmpty
                ? Image(
                    image: FileImageEx(File(_filePath ?? "")),
                    height: 200.0,
                  )
                : new SizedBox(),
            new OutlineButton(
                onPressed: () async {
                  String result = await BaiduOcrPlugin.getIdCardInfo(true);
                  print(result);
                  setState(() {
                    Map data = convert.jsonDecode(result);
                    _filePath = data['filePath'];
                    _result = result;
                  });
                },
                child: new Text("身份证正面")),
            new OutlineButton(
                onPressed: () async {
                  String result = await BaiduOcrPlugin.getIdCardInfo(false);
                  print(result);
                  setState(() {
                    Map data = convert.jsonDecode(result);
                    _filePath = data['filePath'];
                    _result = result;
                  });
                },
                child: new Text("身份证反面")),
            new OutlineButton(
                onPressed: () async {
                  String result = await BaiduOcrPlugin.getBankCard();
                  print(result);
                  setState(() {
                    Map data = convert.jsonDecode(result);
                    _filePath = data['filePath'];
                    _result = result;
                  });
                },
                child: new Text("银行卡")),
            new OutlineButton(
                onPressed: () async {
                  String result = await BaiduOcrPlugin.getVehicle();
                  print(result);
                  setState(() {
                    Map data = convert.jsonDecode(result);
                    _filePath = data['filePath'];
                    _result = result;
                  });
                },
                child: new Text("行驶证")),
            new OutlineButton(
                onPressed: () async {
                  String result = await BaiduOcrPlugin.getDriving();
                  print(result);
                  setState(() {
                    Map data = convert.jsonDecode(result);
                    _filePath = data['filePath'];
                    _result = result;
                  });
                },
                child: new Text("驾驶证")),
          ],
        ),
      ),
    );
  }
}
