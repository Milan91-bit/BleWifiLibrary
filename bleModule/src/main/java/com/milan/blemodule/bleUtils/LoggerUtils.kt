package com.milan.blemodule.bleUtils

import android.util.Log

import java.text.SimpleDateFormat
import java.util.*

object LoggerUtils {
    private var applicationId: String? = null
    private var writeLogsToFile = false
    const val LOG_LEVEL_VERBOSE = 4
    const val LOG_LEVEL_DEBUG = 3
    const val LOG_LEVEL_INFO = 2
    const val LOG_LEVEL_ERROR = 1
    private var currentLogLevel = LOG_LEVEL_VERBOSE

    /**
     * Init logger related parameters
     * Call this method in Application Class
     *
     * @param appId          Application ID for log
     * @param logDirectory   directory name for log file
     * @param logLevel       maximum permissible log level
     * @param isOkToWriteLog validation for write file operation
     */
    fun init(appId: String?, logDirectory: String,
             logLevel: Int, isOkToWriteLog: Boolean) {
        setAppId(appId)
        setLogDirectory(logDirectory)
        setLogLevel(logLevel)
        enableWriteLogToFile(isOkToWriteLog)
    }

    /**
     * Set application id
     *
     * @param appId Application Id
     */
    fun setAppId(appId: String?) {
        applicationId = appId
    }

    /**
     * Set application log directory
     *
     * @param logDirectory Application Log Directory name
     */
    fun setLogDirectory(logDirectory: String) {
        val logDir = "/$logDirectory"
    }

    /**
     * Set application log level
     *
     * @param logLevel Application Log level
     */
    fun setLogLevel(logLevel: Int) {
        currentLogLevel = logLevel
    }

    /**
     * Set application log file creation flag
     *
     * @param isOkToWriteLog check for Application Log file creation
     */
    fun enableWriteLogToFile(isOkToWriteLog: Boolean) {
        writeLogsToFile = isOkToWriteLog
    }

    /**
     * prints log
     *
     * @param message  log message
     * @param logLevel log level
     */
    private fun log(message: String?, logLevel: Int) {
        if (logLevel <= currentLogLevel) {
            Log.v(applicationId, message.toString())
            if (writeLogsToFile) {
                //  writeToFile(message)
            }
        }
    }

    /**
     * log as verbose
     *
     * @param message log message
     */
    fun verbose(message: String?) {
        log(message, LOG_LEVEL_VERBOSE)
    }

    /**
     * log as debug
     *
     * @param message log message
     */
    fun debug(message: String?) {
        log(message, LOG_LEVEL_DEBUG)
    }

    /**
     * log as error
     *
     * @param message log message
     */
    fun error(message: String?) {
        log(message, LOG_LEVEL_ERROR)
    }

    /**
     * log as information
     *
     * @param message log message
     */
    fun info(message: String?) {
        log(message, LOG_LEVEL_INFO)
    }


    private fun displayDate(): String {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd HH:mm:SSS") //dd/MM/yyyy
        val now = Date()
        return sdfDate.format(now)
    }
}