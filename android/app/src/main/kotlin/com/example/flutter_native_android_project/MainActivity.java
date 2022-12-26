package com.example.flutter_native_android_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    static final String BATTERY_CHANNEL = "samples.app.com/battery";
    static final String EVENT_CHANNEL = "samples.app.com/charging";
    private MethodChannel channel;
    private EventChannel eventChannel;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        BinaryMessenger messenger = flutterEngine.getDartExecutor().getBinaryMessenger();
        channel = new MethodChannel(messenger, BATTERY_CHANNEL);

        eventChannel = new EventChannel(messenger, EVENT_CHANNEL);
        eventChannel.setStreamHandler(new MyStreamHandler(getContext()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            int batteryLevel = getBatteryLevel();
            channel.invokeMethod("reportBatteryLevel", batteryLevel);
        }, 5000);

    }

    private int getBatteryLevel() {
        int batteryLevel = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(getApplicationContext())
                    .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            batteryLevel = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
        return batteryLevel;
    }
}

class MyStreamHandler implements EventChannel.StreamHandler {
    private BroadcastReceiver receiver;
    private final Context context;

    MyStreamHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        receiver = initReceiver(events);
        context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onCancel(Object arguments) {
        context.unregisterReceiver(receiver);
        receiver = null;
    }

    private BroadcastReceiver initReceiver(final EventChannel.EventSink events) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                switch (status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        events.success("charging");
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        events.success("full");
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        events.success("discharging");
                        break;
                    default:
                        break;
                }
            }
        };
    }
}




