// Activity for scheduling and managing fake calls

package com.silenthelp.ui.schedule

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silenthelp.R
import com.silenthelp.ui.fakecall.FakeCallReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleCallActivity : AppCompatActivity() {
    // =========================================================================
    // VIEW REFERENCES
    // =========================================================================
    private lateinit var prefs: SharedPreferences
    private lateinit var container: LinearLayout
    private lateinit var selectedDate: TextView
    private lateinit var selectedTime: TextView
    private lateinit var addButton: Button

    private val callList = mutableListOf<ScheduledCall>()
    private val rowViews = mutableListOf<View>()
    private val gson = Gson()
    private val key = "scheduled_calls"

    /* Model representing a scheduled fake call */
    data class ScheduledCall(var id: Int, var date: String, var time: String)

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_call)
        supportActionBar?.hide()

        /** Check permission for exact alarms (Android 12+) */
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        /* Initialize shared preferences and UI */
        prefs = getSharedPreferences("silenthelp_prefs", Context.MODE_PRIVATE)
        container = findViewById(R.id.container_schedule_items)
        selectedDate = findViewById(R.id.text_selected_date)
        selectedTime = findViewById(R.id.text_selected_time)
        addButton = findViewById(R.id.btn_add_call)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        /** Set up date/time pickers */
        selectedDate.setOnClickListener { showDatePicker() }
        selectedTime.setOnClickListener { showTimePicker() }

        /** Add new scheduled call */
        addButton.setOnClickListener {
            val date = selectedDate.text.toString()
            val time = selectedTime.text.toString()

            if (date == "Select Date" || time == "Select Time") {
                Toast.makeText(this, "Select both date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newId = System.currentTimeMillis().toInt()
            val newCall = ScheduledCall(newId, date, time)
            callList.add(newCall)
            saveCallList()
            scheduleAlarm(newCall)
            addRow(newCall)

            /** Reset input fields */
            selectedDate.text = "Select Date"
            selectedTime.text = "Select Time"
            hideKeyboard(addButton)
        }
    }

    // =========================================================================
    // REFRESH VIEW AND REMOVE EXPIRED SCHEDULED CALLS
    // =========================================================================
    override fun onResume() {
        super.onResume()
        container.removeAllViews()
        callList.clear()
        loadSavedCalls()
        val originalSize = callList.size
        cleanupOldScheduledCalls()
        val cleanedSize = callList.size

        callList.forEach { addRow(it) }

        if (originalSize > cleanedSize) {
            Toast.makeText(this, "Expired calls removed", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // SHOW CALENDAR POPUP
    // =========================================================================
    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            selectedDate.text = String.format("%04d-%02d-%02d", y, m + 1, d)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    // =========================================================================
    // SHOW CLOCK POPUP
    // =========================================================================
    private fun showTimePicker() {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            c.set(Calendar.HOUR_OF_DAY, h)
            c.set(Calendar.MINUTE, m)
            selectedTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(c.time)
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
    }

    // =========================================================================
    // ADD SINGLE SCHEDULED CALL TO UI
    // =========================================================================
    private fun addRow(call: ScheduledCall) {
        val inflater = LayoutInflater.from(this)
        val row = inflater.inflate(R.layout.item_scheduled_call, container, false)

        /** View elements */
        val viewMode = row.findViewById<LinearLayout>(R.id.view_mode)
        val editMode = row.findViewById<LinearLayout>(R.id.edit_mode)
        val textScheduled = row.findViewById<TextView>(R.id.text_scheduled_time)
        val editIcon = row.findViewById<ImageView>(R.id.icon_edit)
        val deleteIcon = row.findViewById<ImageView>(R.id.icon_delete)
        val dateInput = row.findViewById<TextView>(R.id.edit_date)
        val timeInput = row.findViewById<TextView>(R.id.edit_time)
        val btnSave = row.findViewById<Button>(R.id.btn_save)
        val btnCancel = row.findViewById<Button>(R.id.btn_cancel)

        /** Populate view */
        textScheduled.text = formatForDisplay(call.date, call.time)
        dateInput.text = call.date
        timeInput.text = call.time

        /** Edit mode: Date */
        dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val parts = call.date.split("-")
            if (parts.size == 3) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
            DatePickerDialog(this, { _, y, m, d ->
                dateInput.text = String.format("%04d-%02d-%02d", y, m + 1, d)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        /** Edit mode: Time */
        timeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, m)
                timeInput.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        /** Edit Icon */
        editIcon.setOnClickListener {
            viewMode.visibility = View.GONE
            editMode.visibility = View.VISIBLE
            dateInput.requestFocus()
        }

        /** Cancel Button (exit edit mode) */
        btnCancel.setOnClickListener {
            dateInput.text = call.date
            timeInput.text = call.time
            viewMode.visibility = View.VISIBLE
            editMode.visibility = View.GONE
            hideKeyboard(dateInput)
        }

        /** Save Button (saves edits) */
        btnSave.setOnClickListener {
            val newDate = dateInput.text.toString().trim()
            val newTime = timeInput.text.toString().trim()

            if (newDate.isBlank() || newTime.isBlank()) {
                Toast.makeText(this, "Date and time cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            call.date = newDate
            call.time = newTime
            textScheduled.text = formatForDisplay(newDate, newTime)
            saveCallList()

            viewMode.visibility = View.VISIBLE
            editMode.visibility = View.GONE
            hideKeyboard(dateInput)
        }

        /** Delete Icon */
        deleteIcon.setOnClickListener {
            container.removeView(row)
            callList.remove(call)
            saveCallList()
            cancelAlarm(call)
        }

        /** Add to UI */
        container.addView(row)
        rowViews.add(row)
    }

    // =========================================================================
    // DISMISS KEYBOARD
    // =========================================================================
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    // =========================================================================
    // LOAD SAVED SCHEDULED CALLS FROM SHARED PREF
    // =========================================================================
    private fun loadSavedCalls(): List<ScheduledCall> {
        val json = prefs.getString(key, "[]")
        val type = object : TypeToken<List<ScheduledCall>>() {}.type
        val list = gson.fromJson<List<ScheduledCall>>(json, type)
        callList.clear()
        callList.addAll(list)
        return list
    }
    // =========================================================================
    // SAVE CURRENT SCHEDULED CALL LIST TO SHARED PREF
    // =========================================================================
    private fun saveCallList() {
        prefs.edit().putString(key, gson.toJson(callList)).apply()
    }
    // =========================================================================
    // SCHEDULE A FAKE CALL AT DATE/TIME USING ALARM MANAGER
    // =========================================================================
    private fun scheduleAlarm(call: ScheduledCall) {
        val intent = Intent(this, FakeCallReceiver::class.java).apply {
            putExtra("call_id", call.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            call.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        val triggerTime = format.parse("${call.date} ${call.time}")?.time ?: return

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }
    // =========================================================================
    // DELETE/CANCLE AN EXISTING ALARM
    // =========================================================================
    private fun cancelAlarm(call: ScheduledCall) {
        val intent = Intent(this, FakeCallReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            call.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
    // =========================================================================
    // FORMAT DATE/TIME AS "Mon, Jul 01 @ 02:30 PM"
    // =========================================================================
    private fun formatForDisplay(date: String, time: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
            val displayFormat = SimpleDateFormat("EEE, MMM dd @ hh:mm a", Locale.getDefault())
            val parsed = inputFormat.parse("$date $time")
            displayFormat.format(parsed ?: Date())
        } catch (e: Exception) {
            "$date - $time"
        }
    }

    // =========================================================================
    // PERMANENTLY REMOVES PAST SCHEDULED CALLS OVER 2 MIN OLD
    // =========================================================================
    private fun cleanupOldScheduledCalls() {
        val now = System.currentTimeMillis()
        val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())

        val iterator = callList.iterator()
        while (iterator.hasNext()) {
            val call = iterator.next()
            val timeStr = "${call.date} ${call.time}"
            val callTime = format.parse(timeStr)?.time ?: continue
            if (now - callTime > 2 * 60 * 1000) { // 2 MIN
                cancelAlarm(call)
                iterator.remove()
            }
        }

        saveCallList()
    }
}
