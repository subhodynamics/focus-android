/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.perf

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import mozilla.components.support.utils.SafeIntent
import android.provider.Settings as AndroidSettings

/**
 * A collection of objects related to app performance.
 */
object Performance {

    private const val EXTRA_IS_PERFORMANCE_TEST = "performancetest"

    fun processIntentIfPerformanceTest(intent: SafeIntent, context: Context) = isPerformanceTest(intent, context)

    /**
     * This checks for USB connections and ADB debugging in case another application tries to
     * leverage this intent to trigger a code path for Firefox that shouldn't be used unless
     * it is for testing visual metrics. These checks aren't foolproof but most of our users won't
     * have ADB on and USB connected at the same time when running Firefox.
     */
    private fun isPerformanceTest(intent: SafeIntent, context: Context): Boolean {
        if (!intent.getBooleanExtra(EXTRA_IS_PERFORMANCE_TEST, false)) {
            return false
        }

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryStatus?.let {
            val isPhonePlugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ==
                BatteryManager.BATTERY_PLUGGED_USB
            val isAdbEnabled = AndroidSettings.Global.getInt(
                context.contentResolver,
                AndroidSettings.Global.ADB_ENABLED, 0
            ) == 1

            return isPhonePlugged && isAdbEnabled
        }
        return false
    }
}
