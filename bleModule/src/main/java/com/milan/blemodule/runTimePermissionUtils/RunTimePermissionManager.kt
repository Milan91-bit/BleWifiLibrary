package com.milan.blemodule.runTimePermissionUtils

import android.Manifest.permission.*
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.milan.blemodule.R
import com.milan.blemodule.bleUtils.BluetoothConnectionListener
import com.milan.blemodule.receiver.BluetoothStateChangeReceiver
import com.milan.blemodule.receiver.LocationStateChangeReceiver


class RunTimePermissionManager {
    companion object {

        // permissionListener
        private var permissionListener: PermissionListener? = null
        private var bluetoothStateChangeReceiver: BluetoothStateChangeReceiver? = null
        private var locationStateChangeReceiver: LocationStateChangeReceiver? = null
        private var bluetoothAdapter: BluetoothAdapter? = null

       fun simpleToast(activity: Activity, message:String){
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }

        fun showAlertDialog(activity: Activity, message:String){

        }

        /**
         * Check if permission granted
         *
         * @param activity   [Activity]
         * @param permission [String] permission
         * @return true if permission granted else false
         */
        private fun hasPermission(activity: Context?, permission: String): Boolean {
            return activity!!.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }

        /**
         *
         * Method to check if permission denied from user side if so explain user requirement
         * @param activity
         * @param permission
         */

        fun isPermissionRationaleRequired(activity: Activity, permission: String): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }

        /**
         *
         * Method to request bluetooth permission from user side for ble scan and connect
         * @param activity
         * @param runtimeMultiplePermissionLauncher
         */

        @RequiresApi(Build.VERSION_CODES.S)
        fun requestPermission(
            activity: Activity,
            requestBlePermission: List<String>,
            runtimeMultiplePermissionLauncher: ActivityResultLauncher<Array<String>>
        ) {
            val missingPermissions = requestBlePermission.filterNot { permission -> hasPermission(activity, permission = permission) }
            val isPermissionRationaleRequired = isPermissionRationaleRequired(activity, BLUETOOTH_SCAN)
             && isPermissionRationaleRequired(activity, BLUETOOTH_CONNECT)
             && isPermissionRationaleRequired(activity, ACCESS_COARSE_LOCATION)
             && isPermissionRationaleRequired(activity, ACCESS_FINE_LOCATION)

            if (missingPermissions.isNotEmpty()) {
                if (!isPermissionRationaleRequired) {
                    runtimeMultiplePermissionLauncher.launch(missingPermissions.toTypedArray())
                } else {
                    sendRequestDenied(activity)
                }
            } else {
                sendRequestDenied(activity)
            }
        }

        private fun sendRequestDenied(activity: Activity) {
            if(!checkBLEPermissionsEnable(activity)){
                permissionListener!!.requestDeniedBle()
            }else if(!checkLocationPermissionsEnable(activity)){
                permissionListener!!.requestDeniedLocation()
            }
        }

        fun checkLocationPermissionsEnable(activity: Activity): Boolean {
            var permissionsStatus = false
            if (ContextCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_COARSE_LOCATION)) {
                    permissionsStatus = false
                }
            } else {
                permissionsStatus = true
            }
            return permissionsStatus
        }

        fun checkBLEPermissionsEnable(activity: Activity): Boolean {
            var permissionsStatus = false
            if (ContextCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, BLUETOOTH_CONNECT)) {
                    permissionsStatus = false
                }
            }else if (ContextCompat.checkSelfPermission(activity, BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, BLUETOOTH_SCAN)) {
                    permissionsStatus = false
                }
            } else {
                permissionsStatus = true
            }
            return permissionsStatus
        }

        /**
         *
         * Method to request all permission for pairing device
         * @param activity
         * @param runtimeMultiplePermissionLauncher
         * @param permissionListener
         */
        @RequiresApi(Build.VERSION_CODES.S)
        fun askForPermission(
            activity: Activity,
            requestBlePermission: List<String>,
            permissionListener: PermissionListener,
            runtimeMultiplePermissionLauncher: ActivityResultLauncher<Array<String>>
        ){
            this.permissionListener = permissionListener
            requestPermission(activity,requestBlePermission,runtimeMultiplePermissionLauncher)
            registerListener(activity,permissionListener)
            checkLocationServicesEnabled(activity)
        }

        fun setBluetoothAdapter(bluetoothAdapter : BluetoothAdapter){
            this.bluetoothAdapter = bluetoothAdapter
        }

        /**
         *
         * Register broadcast message for bluetooth and location on change state
         * @param activity
         */
        private fun registerListener(activity: Activity, permissionListener: PermissionListener) {
            bluetoothStateChangeReceiver = BluetoothStateChangeReceiver(permissionListener)
            locationStateChangeReceiver = LocationStateChangeReceiver(permissionListener)
            activity.registerReceiver(bluetoothStateChangeReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
            activity.registerReceiver(locationStateChangeReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        }


        private fun checkLocationServicesEnabled(activity: Activity) {
            val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var gpsEnabled = false
            var networkEnabled = false
            try {
                gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
            }
            try {
                networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
            }
            val locationServiceEnabled = gpsEnabled || networkEnabled
            if (!locationServiceEnabled) {
                val title = activity.getString(R.string.this_app_needs_location_services)
                val message = activity.getString(R.string.please_enable_location_services)
                val positiveButton = activity.getString(android.R.string.ok)
                 showAlert(activity, title, message, positiveButton, { paramDialogInterface: DialogInterface?, paramInt: Int ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivity(intent)
                }, null)
            }
        }


        // BLUETOOTH AND PERMISSIONS
        val isBluetoothServiceEnabled: Boolean
            get() = bluetoothAdapter != null && bluetoothAdapter!!.isEnabled

        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_SCAN])
        fun enableBluetoothService(activity: Activity) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                activity.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }else {
                bluetoothAdapter!!.enable()
            }
        }

        private fun showAlert(activity: Activity?, title: String?, message: String?,
                              positiveButton: String?, positiveButtonListener: DialogInterface.OnClickListener?,
                              onDismissListener: DialogInterface.OnDismissListener?): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            if (!TextUtils.isEmpty(title)) {
                builder.setTitle(title)
            }
            if (!TextUtils.isEmpty(message)) {
                builder.setMessage(message)
            }
            if (!TextUtils.isEmpty(positiveButton)) {
                builder.setPositiveButton(positiveButton, positiveButtonListener)
            }
            builder.setOnDismissListener(onDismissListener)
            return builder.show()
        }
    }
}