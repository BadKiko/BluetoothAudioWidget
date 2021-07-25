package com.kikoproject.bluetoothaudiowidget

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.polidea.rxandroidble2.RxBleClient


class RecieveBluetooth : Service() {

    var firstConnected = false

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(this, "Сервис RecieveBluetooth запустился", Toast.LENGTH_SHORT).show()
        registerReceiver(btConnected, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
        registerReceiver(levelChanged, IntentFilter("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"))

    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Сервис RecieveBluetooth уничтожен", Toast.LENGTH_SHORT).show()
        unregisterReceiver(btConnected)
        unregisterReceiver(levelChanged)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private val btConnected: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            firstConnected=true
        }
    }

    var batteryLevel = -1
    private val levelChanged: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val sharedPref = getSharedPreferences("IMain", 0)
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

            if (firstConnected && sharedPref.getString("adress", "None") == device?.address) {
                batteryLevel = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                Log.d("BATTERY_LEVEL", batteryLevel.toString())
                firstConnected=false
                val simpleFloatingWindow = Overlay(applicationContext)
                simpleFloatingWindow.setBatteryLevel(batteryLevel)
                simpleFloatingWindow.show()
            }
        }
    }
}