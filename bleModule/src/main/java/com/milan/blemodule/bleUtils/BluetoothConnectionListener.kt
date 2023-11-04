package com.milan.blemodule.bleUtils

import android.bluetooth.le.ScanResult

interface BluetoothConnectionListener {
    fun getConnectedBLEDevice(fullName: String?, result: ScanResult?)
    fun onConnectionFailed(rrorCode: Int)
    fun bleConnectionSuccess()
    fun bleConnectionFail()
    fun onStopScan()
}