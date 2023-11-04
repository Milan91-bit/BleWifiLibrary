package com.milan.blemodule.runTimePermissionUtils

interface PermissionListener {
    fun requestDeniedBle()
    fun requestDeniedLocation()
    fun requestForBleEnableDisable(value : Boolean)
    fun requestForLocationEnableDisable(value : Boolean)
}
