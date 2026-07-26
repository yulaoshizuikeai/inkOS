package com.github.gezimos.inkos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.gezimos.inkos.MainActivity
import com.github.gezimos.inkos.data.Prefs

class UnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("UnlockReceiver", "Received action: $action")
        if (Intent.ACTION_USER_PRESENT == action || Intent.ACTION_BOOT_COMPLETED == action) {
            val prefs = Prefs(context)
            if (prefs.autoFrontOnUnlock) {
                try {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    context.startActivity(launchIntent)
                    Log.d("UnlockReceiver", "Successfully launched MainActivity on unlock/boot")
                } catch (e: Exception) {
                    Log.e("UnlockReceiver", "Failed to launch MainActivity on unlock/boot", e)
                }
            }
        }
    }
}
