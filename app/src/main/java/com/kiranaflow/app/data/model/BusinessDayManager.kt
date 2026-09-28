package com.kiranaflow.app.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the business day calculation.
 * Kirana shops often operate past midnight (e.g. till 1:00 AM or 2:00 AM).
 * If the shopkeeper makes a sale at 00:30 AM, it belongs to the previous business day.
 */
@Singleton
class BusinessDayManager @Inject constructor() {

    // Cutoff hour in 24h format (e.g. 2 means transactions before 02:00 AM count as the previous day)
    var dayCutoffHour: Int = 2

    fun getBusinessDate(timestampMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampMs
        }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour < dayCutoffHour) {
            // Roll back 1 day
            cal.add(Calendar.DATE, -1)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun getBusinessDateStartTimestamp(timestampMs: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampMs
        }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour < dayCutoffHour) {
            cal.add(Calendar.DATE, -1)
        }
        cal.set(Calendar.HOUR_OF_DAY, dayCutoffHour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
