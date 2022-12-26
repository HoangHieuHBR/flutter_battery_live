import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Battery Live',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MainPage(),
    );
  }
}

class MainPage extends StatefulWidget {
  const MainPage({super.key});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  late StreamSubscription _streamSubcription;

  static const batteryChannel = MethodChannel('samples.app.com/battery');
  static const chargingChannel = EventChannel('samples.app.com/charging');

  String batteryLevel = 'Listening...';
  String chargingLevel = 'Streaming...';

  @override
  void initState() {
    super.initState();

    onListenBattery();
    onStreamBattery();
  }

  @override
  void dispose() {
    super.dispose();
    _streamSubcription.cancel();
  }

  void onListenBattery() {
    batteryChannel.setMethodCallHandler((call) async {
      if (call.method == 'reportBatteryLevel') {
        final int batteryLevel = call.arguments;

        setState(() => this.batteryLevel = '$batteryLevel');
      }
    });
  }

  void onStreamBattery() {
    _streamSubcription = chargingChannel.receiveBroadcastStream().listen((event) { 
      setState(() => chargingLevel = '$event');
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Center(
        child: Container(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                batteryLevel,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 30,
                  color: Colors.blue,
                ),
              ),
              const SizedBox(height: 32),
              Text(
                chargingLevel,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 30,
                  color: Colors.blue,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
