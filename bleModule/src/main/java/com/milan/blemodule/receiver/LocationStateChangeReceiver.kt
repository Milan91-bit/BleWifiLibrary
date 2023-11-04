package com.milan.blemodule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.lifecycle.MutableLiveData
import com.milan.blemodule.runTimePermissionUtils.PermissionListener

class LocationStateChangeReceiver(private val permissionListener: PermissionListener) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (isGpsEnabled || isNetworkEnabled) {
                permissionListener.requestForLocationEnableDisable(true)
            } else {
                permissionListener.requestForLocationEnableDisable(false)
            }
        }
    }
}