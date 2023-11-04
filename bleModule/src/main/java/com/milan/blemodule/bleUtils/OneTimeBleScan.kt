package com.milan.blemodule.bleUtils

import android.Manifest
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.milan.blemodule.runTimePermissionUtils.PermissionListener
import com.milan.blemodule.runTimePermissionUtils.RunTimePermissionManager
import java.util.*
import kotlin.collections.ArrayList

class OneTimeBleScan(context: Context) {
    private var bluetoothConnectionListener: BluetoothConnectionListener? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanPeriod = 0
    private var scanner: Scanner? = null
    private val handler = Handler(Looper.getMainLooper())
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private val runnable = Runnable { stopScan() }
    private val devices: MutableSet<BluetoothDevice> = HashSet()
    private var serviceId = ArrayList<String>()


    /**
     * Constructor required scan period,connectionListener and service UUID to filter the product search result
     *
     * @param context   [Activity]
     * @param bluetoothConnectionListener [BluetoothConnectionListener] call back
     * @param runtimePermissionListener [PermissionListener]
     * @param runtimeMultiplePermissionLauncher [ArrayList]
     * @param serviceUuid [ArrayList]
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    constructor(
        context: Activity, scanPeriod: Int,
        bluetoothConnectionListener: BluetoothConnectionListener?,
        runtimePermissionListener: PermissionListener,
        runtimeMultiplePermissionLauncher: ActivityResultLauncher<Array<String>>,
        serviceUuid: ArrayList<String>
    ) : this(context) {
        this.serviceId = serviceUuid
        this.scanPeriod = scanPeriod
        this.bluetoothConnectionListener = bluetoothConnectionListener
        BleConnectionManager.getInstance(context)!!.initBle(context)
        checkForPermission(context,runtimePermissionListener,runtimeMultiplePermissionLauncher)

        Handler(Looper.getMainLooper()).postDelayed({
            checkBleEnable(context)
            if(RunTimePermissionManager.checkLocationPermissionsEnable(context) && RunTimePermissionManager.checkBLEPermissionsEnable(context)){
                init(BleConnectionManager.getInstance(context)!!.bluetoothAdapter!!)
            }
        },200)

    }


    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    fun checkBleEnable(context: Activity) {
        RunTimePermissionManager.setBluetoothAdapter(BleConnectionManager.getInstance(context)!!.bluetoothAdapter!!)
        if(!RunTimePermissionManager.isBluetoothServiceEnabled){
            RunTimePermissionManager.enableBluetoothService(context)
        }
    }




    /**
     * checking Permission and requesting if not granted
     *
     * @param context   [Activity]
     * @param runtimePermissionListener [PermissionListener]
     * @param runtimeMultiplePermissionLauncher [ArrayList]
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private fun checkForPermission(
        context: Activity,
        runtimePermissionListener: PermissionListener,
        runtimeMultiplePermissionLauncher: ActivityResultLauncher<Array<String>>) {
        val requestBlePermission = listOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        RunTimePermissionManager.askForPermission(context,requestBlePermission,runtimePermissionListener,runtimeMultiplePermissionLauncher)

        Handler(Looper.getMainLooper()).postDelayed({
            if(RunTimePermissionManager.checkLocationPermissionsEnable(context) && RunTimePermissionManager.checkBLEPermissionsEnable(context)){
                init(BleConnectionManager.getInstance(context)!!.bluetoothAdapter!!)
            }
        },1000)
    }

    /**
     * Initiating scanning process
     *
     * @param bluetoothAdapter   [BluetoothAdapter]
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private fun init(bluetoothAdapter: BluetoothAdapter) {
        scanPeriod = SCAN_PERIOD_SHORT
        this.bluetoothAdapter = bluetoothAdapter
        scanner = Scanner()
        scan()
    }

    /**
     * Stop Handler use to stop scan process
     *
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun stopHandler() {
        handler.removeCallbacks(runnable)
    }

    /**
     * Scan method use for start scan and stop handler
     *
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    fun scan() {
        if (!isAdapterOn) return
        stopHandler()
        startScan()
        handler.postDelayed(runnable, scanPeriod.toLong())
    }

    /**
     * Start Scan method initialize bluetoothLeScanner
     *
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    private fun startScan() {
        devices.clear()
        bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
        bluetoothLeScanner?.startScan(scanner)
    }

    /**
     * Stop Scan method
     *
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
    fun stopScan() {
        stopHandler()
        if (!isAdapterOn) return
        bluetoothAdapter!!.bluetoothLeScanner.flushPendingScanResults(scanner)
        bluetoothLeScanner!!.stopScan(scanner)
        bluetoothConnectionListener!!.onStopScan()
    }

    private val isAdapterOn: Boolean
        get() = bluetoothAdapter != null && bluetoothAdapter!!.state == BluetoothAdapter.STATE_ON


    /**
     * Scanner class use for scanning all near by product gives appropriate result.
     *
     */
    private inner class Scanner : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            fetchDeviceDataAndSend(result)
        }

        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                fetchDeviceDataAndSend(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            /*val message: String = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "Fails to start scan as BLE scan with the same settings is already started by the app."
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Fails to start scan as app cannot be registered."
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "Fails to start power optimized scan as this feature is not supported."
                else -> "Fails to start scan due an internal error"
            }*/
            bluetoothConnectionListener!!.onConnectionFailed(errorCode)
        }

        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
        private fun fetchDeviceDataAndSend(result: ScanResult) {
            if (result.scanRecord != null && result.scanRecord!!.serviceUuids != null
                && result.scanRecord!!.serviceUuids.size > 0
                && serviceId.contains(result.scanRecord!!.serviceUuids[0].toString())) {
                val device = result.device
                if (devices.add(device)) {
                    val deviceName = if (result.scanRecord != null) {
                        Objects.requireNonNull(result.scanRecord)?.deviceName
                    } else {
                        device.name
                    }
                    bluetoothConnectionListener!!.getConnectedBLEDevice(deviceName, result)
                }
            }
        }
    }

    companion object {
        const val SCAN_PERIOD_SHORT = 5000
        private var oneTimeBleScan: OneTimeBleScan? = null

        fun getInstance(context: Context?): OneTimeBleScan? {
            if (oneTimeBleScan == null) {
                oneTimeBleScan = OneTimeBleScan(context!!)
            }
            return oneTimeBleScan
        }

    }
}