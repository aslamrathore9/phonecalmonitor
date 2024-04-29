package com.kepler.phonecalmonitor.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.kepler.phonecalmonitor.service.CallTrackingService


class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            val outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            if (!outgoingNumber.isNullOrEmpty()) {
                Log.d("CRMCall", "Outgoing call to: $outgoingNumber")
                startService(context, outgoingNumber)
            }
        } else if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (state == TelephonyManager.EXTRA_STATE_RINGING && !incomingNumber.isNullOrEmpty()) {
                Log.d("CRMCall", "Incoming call from: $incomingNumber")
                startService(context, incomingNumber)
            }
        }

    }

    private fun startService(context: Context, phoneNumber: String?) {
        if (!phoneNumber.isNullOrEmpty()) {
            val serviceIntent = Intent(context, CallTrackingService::class.java)
            serviceIntent.putExtra("PHONE_NUMBER", phoneNumber)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
               // context.startService(serviceIntent)
              ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

}
