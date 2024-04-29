package com.kepler.phonecalmonitor.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ServiceCompat
import com.kepler.phonecalmonitor.notification.NotificationsHelper

class CallTrackingService : Service() {

    //private lateinit var phoneListener: MyPhoneStateListener

    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun stopForegroundService() {
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize TelephonyManager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        // Initialize PhoneStateListener
        phoneStateListener = object : PhoneStateListener() {

            var isStateANdRinging = false

            override fun onCallStateChanged(state: Int, phoneNumber: String) {
                Log.d("CallState", "onCallStateChanged: state=$state, phoneNumber=$phoneNumber")
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        isStateANdRinging = true
                        Log.d("CallState", "Incoming Number: $phoneNumber")
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        isStateANdRinging = true
                        Log.d("CallState", "Outgoing or StartedCall Number: $phoneNumber")
                    }

                    TelephonyManager.CALL_STATE_IDLE -> {

                        Log.d("CallState", "CALL_STATE_IDLE $phoneNumber")

                        if (isStateANdRinging){
                            val projection = arrayOf(
                                CallLog.Calls._ID,
                                CallLog.Calls.NUMBER,
                                CallLog.Calls.DATE,
                                CallLog.Calls.DURATION
                            )
                            val selection = "${CallLog.Calls.NUMBER} = ?"
                            val selectionArgs = arrayOf(phoneNumber)
                            val sortOrder = "${CallLog.Calls.DATE} DESC"

                            contentResolver.query(
                                CallLog.Calls.CONTENT_URI,
                                projection,
                                selection,
                                selectionArgs,
                                sortOrder
                            )?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val id = cursor.getLong(cursor.getColumnIndex(CallLog.Calls._ID))
                                    val callNumber =
                                        cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                                    val callDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
                                    val callDuration =
                                        cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))

                                    println("callLogDetails : id $id callNumber $callNumber callDate $callDate callDuration $callDuration")
                                    // Use the call log details as needed
                                }
                            }

                           stopForegroundService()
                        }


                        // Stop the foreground service

                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("ForKnowloadeonly : ", "flag: $flags startID : $startId")

        val phoneNumber = intent?.getStringExtra("PHONE_NUMBER")
        if (!phoneNumber.isNullOrEmpty()) {
            // Do something with the phone number
            Log.d("CallService", "Phone number: $phoneNumber")

            if (!identifyCrmNumber(phoneNumber)) {
                stopSelf()
            }

            startAsForegroundService()

            // Add the PhoneStateListener
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        }


        return START_STICKY;

    }

    /**
     * Promotes the service to a foreground service, showing a notification to the user.
     *
     * This needs to be called within 10 seconds of starting the service or the system will throw an exception.
     */
    private fun startAsForegroundService() {
        // create the notification channel
        NotificationsHelper.createNotificationChannel(this)


        try {
            // promote service to foreground service
            ServiceCompat.startForeground(
                this,
                1,
                NotificationsHelper.buildNotification(this),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                } else {
                    0
                }
            )
        }catch (e:Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                Log.d("ExceptionForgorund : "," App not in a valid state to start foreground service")
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the PhoneStateListener
        telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    fun identifyCrmNumber(crmNumber: String): Boolean {
        // handle if crm number or not
        //    println("crmNumber : $crmNumber")
        return true

    }

}