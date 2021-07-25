package com.kikoproject.bluetoothaudiowidget

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import android.widget.Toast

class BatteryLevel : Service() {

    var batteryLevel = -1

    override fun onCreate() {
        super.onCreate()

        val intentFilter = IntentFilter()
        intentFilter.addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
        registerReceiver(levelChanged, intentFilter)

    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Сервис BatteryLevel уничтожен", Toast.LENGTH_SHORT).show()
        unregisterReceiver(levelChanged)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private val levelChanged: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            batteryLevel = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
            Log.d("BATTERY_LEVEL", batteryLevel.toString())
            Overlay(context).setBatteryLevel(50)
        }
    }
}