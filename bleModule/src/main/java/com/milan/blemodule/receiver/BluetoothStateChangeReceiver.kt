package com.milan.blemodule.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.milan.blemodule.runTimePermissionUtils.PermissionListener

class BluetoothStateChangeReceiver(private val permissionListener: PermissionListener) : BroadcastReceiver() {
    private val bluetoothState = MutableLiveData<Boolean?>()
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR)
            when (state) {
                BluetoothAdapter.STATE_OFF -> permissionListener.requestForBleEnableDisable(false)
                BluetoothAdapter.STATE_ON -> if (bluetoothState.value != null && !bluetoothState.value!!) {
                    permissionListener.requestForBleEnableDisable(true)
                    bluetoothState.postValue(true)
                }
            }
        } else {
            bluetoothState.postValue(false)
            permissionListener.requestForBleEnableDisable(false)
        }
    }
}