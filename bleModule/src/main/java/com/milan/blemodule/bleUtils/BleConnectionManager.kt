package com.milan.blemodule.bleUtils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission



class BleConnectionManager private constructor(private val context: Context?) {
    private var bluetoothManager: BluetoothManager? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothGatt: BluetoothGatt? = null
    var bluetoothConnectionListener: BluetoothConnectionListener? = null
    var bluetoothDevice: BluetoothDevice? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var bleConnectionManagerInstance: BleConnectionManager? = null

        fun getInstance(context: Context?): BleConnectionManager? {
            if (bleConnectionManagerInstance == null) {
                bleConnectionManagerInstance = BleConnectionManager(context)
            }
            return bleConnectionManagerInstance
        }
    }

    /**
     * --------------------------------
     * This method for initializing ble references
     * --------------------------------
     * Check if bluetooth adapter and bluetooth manager is not null and if so initialize
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun initBle(context: Context?) {
        if (bluetoothManager == null) {
            bluetoothManager =
                context!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            bluetoothAdapter = bluetoothManager!!.adapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun connectToDeviceSelected(
        bluetoothDevice: BluetoothDevice,
        bluetoothConnectionListener: BluetoothConnectionListener
    ) {
        this.bluetoothConnectionListener = bluetoothConnectionListener
        this.bluetoothDevice = bluetoothDevice
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, btleGattCallback,BluetoothDevice.TRANSPORT_LE)
    }

    // Device connect call back
    private val btleGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            // this will get called anytime you perform a read or write characteristic operation
        }

        @RequiresApi(Build.VERSION_CODES.S)
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            // this will get called when a device connects or disconnects
            Log.w("BluetoothGattCallback", "New State ${newState} status ${status}")
            when (newState) {
                0 -> {
                    bluetoothConnectionListener!!.bleConnectionFail()
                    gatt.close()
                    disconnectDeviceSelected()
                }
                2 -> {
                    bluetoothConnectionListener!!.bleConnectionSuccess()
                    // discover services and characteristics for this device
                    bluetoothGatt!!.discoverServices()
                }
                else -> {
                    disconnectDeviceSelected()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
            displayGattServices(bluetoothGatt!!.services)
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate("ACTION_DATA_AVAILABLE", characteristic)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun disconnectDeviceSelected() {
        bluetoothGatt!!.disconnect()
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        println(characteristic.uuid)
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val uuid = gattService.uuid.toString()
            println("Service discovered: $uuid")
            ArrayList<HashMap<String, String>>()
            val gattCharacteristics = gattService.characteristics

            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                val charUuid = gattCharacteristic.uuid.toString()
                println("Characteristic discovered for service: $charUuid")
            }
        }
    }

}

