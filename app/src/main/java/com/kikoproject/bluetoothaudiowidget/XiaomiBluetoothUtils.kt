package com.kikoproject.bluetoothaudiowidget

import android.bluetooth.BluetoothDevice
import kotlin.math.roundToInt

class XiaomiBluetoothUtils constructor() : BluetoothUtils{

    override fun isDeviceConnectedByBatteryLevel(batteryLevel: Int): Boolean {
        return batteryLevel in 0..100
    }

    override fun getDeviceByAddress(
        devices: Set<BluetoothDevice?>,
        address: String
    ): BluetoothDevice? {
        for (bluetoothDevice in devices) {
            bluetoothDevice?.let {
                if (it.address == address)
                    return bluetoothDevice
            }
        }
        return null
    }

    override fun getBatteryLeftBasedOnModel(
        batteryLevel: Int,
        xiaomiSpeakerModel: XiaomiModels
    ): Int {
        val factor: Double = batteryLevel / 100.0
        val minutes = when (xiaomiSpeakerModel) {
            XiaomiModels.AIR_DOTS -> factor * AIR_DOTS_BATTERY_MINUTES
            XiaomiModels.AIR_DOTS_PRO_1 -> factor * AIR_DOTS_PRO_BATTERY_MINUTES
            XiaomiModels.AIR_DOTS_PRO_2 -> factor * AIR_DOTS_PRO_2_BATTERY_MINUTES
            XiaomiModels.MI_SPEAKER -> factor * MI_SPEAKER_BATTERY_MINUTES
            XiaomiModels.MI_POCKET_SPEAKER_2 -> factor * MI_POCKET_SPEAKER_2_BATTERY_MINUTES
            XiaomiModels.XIAOMI_WIRELESS_BLUETOOTH_SPEAKER -> factor * XIAOMI_WIRELESS_BLUETOOTH_SPEAKER
            XiaomiModels.TWS_HONOR_CHOICE -> factor * TWS_HONOR_CHOICE_BATTERY_MINUTES
            XiaomiModels.AIR_DOTS_2_SE -> factor * AIR_DOTS_2_SE_BATTERY_MINUTES
            XiaomiModels.NICEBOY_HIVE -> factor * NICEBOY_HIVE_BATTERY_MINUTES
            XiaomiModels.NICEBOY_HIVE_PODSIE -> factor * NICEBOY_HIVE_PODSIE_BATTERY_MINUTES
            XiaomiModels.AIR_DOTS_S -> factor * AIR_DOTS_S_BATTERY_MINUTES
            XiaomiModels.AIR_DOTS_3 -> factor * AIR_DOTS_3_BATTERY_MINUTES
            XiaomiModels.AIR_DOTS_PRO_3 -> factor * AIR_DOTS_3_PRO_BATTERY_MINUTES
            else -> {
                -1.0
            }
        }
        return if (minutes > 0)
            minutes.roundToInt()
        else
            0
    }

    override fun getBatteryLevelReflection(pairedDevice: BluetoothDevice?): Int {
        return pairedDevice?.let { bluetoothDevice ->
            (bluetoothDevice.javaClass.getMethod("getBatteryLevel")).invoke(pairedDevice) as Int
        } ?: BluetoothUtils.DEFAULT_BATTERY_NOT_CONNECT
    }

    companion object {
        private const val AIR_DOTS_BATTERY_MINUTES = 240
        private const val AIR_DOTS_S_BATTERY_MINUTES = 240
        private const val AIR_DOTS_3_BATTERY_MINUTES = 360
        private const val AIR_DOTS_3_PRO_BATTERY_MINUTES = 360
        private const val AIR_DOTS_PRO_BATTERY_MINUTES = 180
        private const val AIR_DOTS_PRO_2_BATTERY_MINUTES = 240
        private const val MI_SPEAKER_BATTERY_MINUTES = 480
        private const val MI_POCKET_SPEAKER_2_BATTERY_MINUTES = 420
        private const val XIAOMI_WIRELESS_BLUETOOTH_SPEAKER = 420
        private const val TWS_HONOR_CHOICE_BATTERY_MINUTES = 360
        private const val AIR_DOTS_2_SE_BATTERY_MINUTES = 300
        private const val NICEBOY_HIVE_BATTERY_MINUTES = 780
        private const val NICEBOY_HIVE_PODSIE_BATTERY_MINUTES = 210
    }

}

interface BluetoothUtils {

    fun isDeviceConnectedByBatteryLevel(batteryLevel: Int): Boolean

    fun getDeviceByAddress(devices: Set<BluetoothDevice?>, address: String): BluetoothDevice?

    fun getBatteryLeftBasedOnModel(batteryLevel: Int, xiaomiSpeakerModel: XiaomiModels): Int

    fun getBatteryLevelReflection(pairedDevice: BluetoothDevice?): Int

    companion object {
        const val DEFAULT_BATTERY_NOT_CONNECT = -1
    }
}