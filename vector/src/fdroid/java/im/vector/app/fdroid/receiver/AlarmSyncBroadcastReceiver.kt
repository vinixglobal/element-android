/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blast.vinix.fdroid.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.blast.vinix.core.di.HasVectorInjector
import com.blast.vinix.core.services.VectorSyncService
import com.blast.vinix.features.settings.VectorPreferences
import org.matrix.android.sdk.internal.session.sync.job.SyncService
import timber.log.Timber

class AlarmSyncBroadcastReceiver : BroadcastReceiver() {

    lateinit var vectorPreferences: VectorPreferences

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        if (appContext is HasVectorInjector) {
            val activeSession = appContext.injector().activeSessionHolder().getSafeActiveSession()
            if (activeSession == null) {
                Timber.v("No active session don't launch sync service.")
                return
            }
            vectorPreferences = appContext.injector().vectorPreferences()
        }

        val sessionId = intent.getStringExtra(SyncService.EXTRA_SESSION_ID) ?: return
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Timber.d("RestartBroadcastReceiver received intent")
        VectorSyncService.newPeriodicIntent(context, sessionId, vectorPreferences.backgroundSyncTimeOut(), vectorPreferences.backgroundSyncDelay()).let {
            try {
                ContextCompat.startForegroundService(context, it)
            } catch (ex: Throwable) {
                Timber.i("## Sync: Failed to start service, Alarm scheduled to restart service")
                scheduleAlarm(context, sessionId, vectorPreferences.backgroundSyncDelay())
                Timber.e(ex)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 0

        fun scheduleAlarm(context: Context, sessionId: String, delayInSeconds: Int) {
            // Reschedule
            Timber.v("## Sync: Scheduling alarm for background sync in $delayInSeconds seconds")
            val intent = Intent(context, AlarmSyncBroadcastReceiver::class.java).apply {
                putExtra(SyncService.EXTRA_SESSION_ID, sessionId)
                putExtra(SyncService.EXTRA_PERIODIC, true)
            }
            val pIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val firstMillis = System.currentTimeMillis() + delayInSeconds * 1000L
            val alarmMgr = context.getSystemService<AlarmManager>()!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, firstMillis, pIntent)
            } else {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, firstMillis, pIntent)
            }
        }

        fun cancelAlarm(context: Context) {
            Timber.v("## Sync: Cancel alarm for background sync")
            val intent = Intent(context, AlarmSyncBroadcastReceiver::class.java)
            val pIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmMgr = context.getSystemService<AlarmManager>()!!
            alarmMgr.cancel(pIntent)

            // Stop current service to restart
            VectorSyncService.stopIntent(context).let {
                try {
                    ContextCompat.startForegroundService(context, it)
                } catch (ex: Throwable) {
                    Timber.i("## Sync: Cancel sync")
                }
            }
        }
    }
}
