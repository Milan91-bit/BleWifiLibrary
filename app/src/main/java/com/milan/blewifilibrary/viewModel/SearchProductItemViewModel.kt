package com.milan.blewifilibrary.viewModel

import android.bluetooth.BluetoothDevice
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.milan.blewifilibrary.model.ItemClickListener

class SearchProductItemViewModel (private val product: BluetoothDevice, private val itemClickListener: ItemClickListener) : ViewModel() {
    val productName = ObservableField("")
    fun itemClicked() {
        itemClickListener.itemClicked(product)
    }

    init {
        productName.set(product.name.toString())
    }
}