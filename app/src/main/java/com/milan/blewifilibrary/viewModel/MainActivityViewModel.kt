package com.milan.blewifilibrary.viewModel

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.milan.blewifilibrary.SearchProductItemAdapter
import com.milan.blewifilibrary.model.ItemClickListener


class MainActivityViewModel() : ViewModel() ,ItemClickListener{
    val currentState = ObservableInt(STATE_SEARCHING)
    private val productModels: ArrayList<BluetoothDevice>?
    val adapter: SearchProductItemAdapter
    val onRescanClick = MutableLiveData<Boolean>()
    val selectedProduct = MutableLiveData<BluetoothDevice?>()
    var devicesDiscovered = ArrayList<BluetoothDevice>()

    val isPairingView = ObservableBoolean(false)

    init {
        productModels = ArrayList()
        adapter = SearchProductItemAdapter(this)
        currentState.set(STATE_NO_DEVICE_FOUND)
    }

    fun rescanButtonClick() {
        devicesDiscovered.clear()
        adapter.setBleDevices(devicesDiscovered)
        currentState.set(STATE_SEARCHING)
        onRescanClick.postValue(true)
    }

    fun onStopScan() {
        onRescanClick.postValue(false)
        if (productModels != null && productModels.size > 0) {
            currentState.set(STATE_SEARCHED_AND_DEVICE_FOUND)
        } else {
            currentState.set(STATE_NO_DEVICE_FOUND)
        }
    }

    fun addProduct(device: BluetoothDevice) {
        if (!productModels!!.contains(device)) {
            productModels.add(device)
            adapter.setBleDevices(productModels)
            currentState.set(STATE_SEARCHING_AND_DEVICE_FOUND)
        }
    }


    companion object {
        const val STATE_SEARCHING = 0
        const val STATE_SEARCHING_AND_DEVICE_FOUND = 1
        const val STATE_SEARCHED_AND_DEVICE_FOUND = 2
        const val STATE_NO_DEVICE_FOUND = 3
    }


   /* override fun itemClicked(product: Product?) {
        selectedProduct.postValue(product)
    }*/

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    override fun itemClicked(product: BluetoothDevice?) {
        selectedProduct.postValue(product)
    }
}