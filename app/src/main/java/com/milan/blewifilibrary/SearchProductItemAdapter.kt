package com.milan.blewifilibrary

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.milan.blewifilibrary.databinding.ItemSearchDeviceListBinding
import com.milan.blewifilibrary.model.ItemClickListener
import com.milan.blewifilibrary.viewModel.SearchProductItemViewModel

class SearchProductItemAdapter (private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<SearchProductItemAdapter.ViewHolder>() {
    private var productModels: List<BluetoothDevice>? = null
    fun setBleDevices(bleDevices: ArrayList<BluetoothDevice>?) {
        productModels = bleDevices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSearchDeviceListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_search_device_list, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productModels!![position]
        val viewModel = SearchProductItemViewModel(product, itemClickListener)
        holder.itemSearchDevicesBinding.viewModel = viewModel
        holder.itemSearchDevicesBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return if (productModels == null) {
            0
        } else productModels!!.size
    }

    class ViewHolder(val itemSearchDevicesBinding: ItemSearchDeviceListBinding) : RecyclerView.ViewHolder(itemSearchDevicesBinding.root)
}