package com.milan.blewifilibrary

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.milan.blemodule.bleUtils.BleConnectionManager
import com.milan.blemodule.bleUtils.BluetoothConnectionListener
import com.milan.blemodule.bleUtils.OneTimeBleScan
import com.milan.blemodule.runTimePermissionUtils.PermissionListener
import com.milan.blemodule.runTimePermissionUtils.RunTimePermissionManager
import com.milan.blewifilibrary.databinding.ActivityMainBinding
import com.milan.blewifilibrary.viewModel.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var viewModel: MainActivityViewModel? = null

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        setObserver()
    }

    private fun setBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = MainActivityViewModel()
        binding!!.viewModel = viewModel
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)

        // pass it to rvLists layoutManager
        binding!!.rvList.layoutManager = layoutManager
        binding!!.rvList.adapter = viewModel!!.adapter

        binding!!.executePendingBindings()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private fun setObserver(){
        viewModel!!.onRescanClick.observe(this){
            if(it == true){
                requestForBleScan()
            }
        }

        viewModel!!.selectedProduct.observe(this){
            if(it?.address != null){
                OneTimeBleScan.getInstance(this)!!.stopScan()
                Handler(Looper.getMainLooper()).postDelayed({
                    requestForConnectProduct(it)
                },1000)
              viewModel!!.selectedProduct.value = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private fun requestForConnectProduct(it: BluetoothDevice) {
        viewModel!!.isPairingView.set(true)
        BleConnectionManager.getInstance(this)!!.connectToDeviceSelected(it ,bluetoothConnectionListener)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private fun requestForBleScan() {
            val gcs = "267f0001-eb15-43f5-94c3-67d2221188f7"
            val legacy = "bccb0001-ca66-11e5-88a4-0002a5d5c51b"
            val serviceUUID = ArrayList<String>()
            serviceUUID.add(gcs)
            serviceUUID.add(legacy)
            OneTimeBleScan(this,
                OneTimeBleScan.SCAN_PERIOD_SHORT,
                bluetoothConnectionListener,
                runtimePermissionListener,
                runtimeMultiplePermissionLauncher,
                serviceUUID)
    }


    private val bluetoothConnectionListener: BluetoothConnectionListener = object : BluetoothConnectionListener {
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
        override fun getConnectedBLEDevice(fullName: String?, result: ScanResult?) {
            if (fullName != null && fullName.isNotEmpty()) {
                val device = result!!.device
                viewModel!!.addProduct(device)
            }
        }

        override fun onConnectionFailed(errorCode: Int) {
            viewModel!!.onStopScan()
        }

        override fun bleConnectionSuccess() {
            onBleConnectionSuccess()
        }

        override fun bleConnectionFail() {
            onBleConnectionFail()
        }

        override fun onStopScan() {
            viewModel!!.onStopScan()
        }
    }

    fun onBleConnectionSuccess() {
        Handler(Looper.getMainLooper()).post {
            RunTimePermissionManager.simpleToast(this, "Product Paired Successfully")
        }
    }

    fun onBleConnectionFail() {
        Handler(Looper.getMainLooper()).post {
            RunTimePermissionManager.simpleToast(this, "Product Pairing Failed")
        }
    }


    private val runtimePermissionListener : PermissionListener = object : PermissionListener{
        override fun requestDeniedBle() {
            permissionDenied()
        }

        override fun requestDeniedLocation() {
            permissionDeniedLocation()
        }

        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
        override fun requestForBleEnableDisable(value: Boolean) {
            bleStateEnableDisable(value)
        }

        @RequiresApi(Build.VERSION_CODES.S)
        override fun requestForLocationEnableDisable(value: Boolean) {
            locationStateEnableDisable(value)
        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private fun checkBleEnable() {
        RunTimePermissionManager.setBluetoothAdapter(BleConnectionManager.getInstance(this)!!.bluetoothAdapter!!)
        if(!RunTimePermissionManager.isBluetoothServiceEnabled){
            RunTimePermissionManager.enableBluetoothService(this)
        }
    }

    private fun permissionDenied(){
        RunTimePermissionManager.simpleToast(this,"show dialog permission denied Ble")
    }

    private fun permissionDeniedLocation(){
        RunTimePermissionManager.simpleToast(this,"show dialog permission denied Location")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private fun bleStateEnableDisable(value : Boolean){
        if(value) {
            Handler(Looper.getMainLooper()).post {
                RunTimePermissionManager.simpleToast(this, "Ble enable")
            }
        }else{
            checkBleEnable()
            Handler(Looper.getMainLooper()).post {
                RunTimePermissionManager.simpleToast(this, "Ble disable")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun locationStateEnableDisable(value : Boolean){
        if(value) {
            RunTimePermissionManager.simpleToast(this, "Location enable")
        }else{
            checkForPermission(this, runtimePermissionListener, runtimeMultiplePermissionLauncher)
            RunTimePermissionManager.simpleToast(this, "Location disable")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private val runtimeMultiplePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        if (permissions[BLUETOOTH_SCAN] == false
            || permissions[BLUETOOTH_CONNECT] == false
            || permissions[ACCESS_COARSE_LOCATION] == false
            || permissions[ACCESS_FINE_LOCATION] == false) {
            val isPermissionRationaleRequired = RunTimePermissionManager.isPermissionRationaleRequired(this, BLUETOOTH_SCAN)
                    && RunTimePermissionManager.isPermissionRationaleRequired(this, BLUETOOTH_CONNECT)
                    && RunTimePermissionManager.isPermissionRationaleRequired(this, ACCESS_COARSE_LOCATION)
                    && RunTimePermissionManager.isPermissionRationaleRequired(this, ACCESS_FINE_LOCATION)
            if (isPermissionRationaleRequired) {
                permissionDenied()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkForPermission(
        context: Activity,
        runtimePermissionListener: PermissionListener,
        runtimeMultiplePermissionLauncher: ActivityResultLauncher<Array<String>>
    ) {
        val requestBlePermission = listOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT,
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION
        )

        RunTimePermissionManager.askForPermission(context,requestBlePermission,runtimePermissionListener,runtimeMultiplePermissionLauncher)
    }

}