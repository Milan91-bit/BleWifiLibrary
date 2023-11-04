package com.milan.blewifilibrary.model

import android.bluetooth.BluetoothDevice

interface ItemClickListener {
    fun itemClicked(product: BluetoothDevice?)
}