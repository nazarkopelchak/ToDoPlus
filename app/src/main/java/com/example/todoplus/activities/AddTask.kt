package com.example.todoplus.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.todoplus.HISTORY_LIST_FILE_NAME
import com.example.todoplus.R
import com.example.todoplus.TASK_LIST_FILE_NAME
import com.example.todoplus.data.ActivityNames
import com.example.todoplus.data.Priorities
import com.example.todoplus.data.Task
import com.example.todoplus.helpers.DriveServiceHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.ObjectOutputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class AddTask : AppCompatActivity() {

    lateinit var calendar: Calendar

    lateinit var navigationView: BottomNavigationView
    lateinit var taskName: EditText
    lateinit var taskDetails: EditText
    lateinit var taskDate: EditText
    lateinit var taskLocation: EditText
    lateinit var taskPriority: Spinner
    lateinit var taskTime: EditText
    lateinit var clearDateButton: Button
    lateinit var clearTimeButton: Button
    lateinit var saveTaskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        var priorityDropdownPosition = 0
        calendar = Calendar.getInstance()

        taskName = findViewById(R.id.task_name)
        taskDetails = findViewById(R.id.task_details)
        taskDate = findViewById(R.id.task_date)
        taskLocation = findViewById(R.id.task_location)
        taskPriority = findViewById(R.id.task_priority)
        taskTime = findViewById(R.id.task_time)
        clearDateButton = findViewById(R.id.clear_date_button)
        clearTimeButton = findViewById(R.id.clear_time_button)
        saveTaskButton = findViewById(R.id.save_task_button)
        navigationView = findViewById(R.id.bottom_navigation)

        navigationView.selectedItemId = R.id.add

        val date = DatePickerDialog.OnDateSetListener {view, year, month, day ->
            calendar.set(android.icu.util.Calendar.YEAR, year)
            calendar.set(android.icu.util.Calendar.MONTH, month)
            calendar.set(android.icu.util.Calendar.DAY_OF_MONTH, day)
            updateDate()
        }

        taskDate.setOnClickListener {
            taskDate.error = null
            val datePicker = DatePickerDialog(this, R.style.DialogTheme, date, calendar.get(android.icu.util.Calendar.YEAR), calendar.get(android.icu.util.Calendar.MONTH), calendar.get(android.icu.util.Calendar.DAY_OF_MONTH))
            datePicker.show()
            datePicker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            datePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }

        taskTime.setOnClickListener {
            taskTime.error = null
            val mHour = calendar.get(android.icu.util.Calendar.HOUR_OF_DAY)
            val mMinute = calendar.get(android.icu.util.Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, R.style.DialogTheme, object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    taskTime.setText(String.format("%02d : %02d", hourOfDay, minute))
                }

            }, mHour, mMinute, false)
            timePicker.show()
            timePicker.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            timePicker.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }

        var priorities = ArrayList<String>()
        priorities.add("Low")
        priorities.add("Medium")
        priorities.add("High")
        priorities.add("Very High")

        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, priorities)
        taskPriority.adapter = arrayAdapter

        var taskList = intent.getSerializableExtra("task_list") as ArrayList<Task>
        var history = intent.getSerializableExtra("history") as ArrayList<Task>
        val taskPosition = intent.getIntExtra("task_position", -1)
        val historyTaskPosition = intent.getIntExtra("history_task_position", -1)

        if (taskPosition != -1) {
            val toolbarTextView = findViewById<TextView>(R.id.add_task_toolbar_title)
            toolbarTextView.setText("Edit a Task")
        }
        else {
            val toolbarTextView = findViewById<TextView>(R.id.add_task_toolbar_title)
            toolbarTextView.setText("Add a New Task")
        }

        // This if-else statement handles intents sent from History and Search activities
        if (taskPosition >= 0) {
            taskName.setText(taskList.get(taskPosition).taskName)
            taskDetails.setText(taskList.get(taskPosition).description)
            taskDate.setText(taskList.get(taskPosition).date)
            taskLocation.setText(taskList.get(taskPosition).location)
            when (taskList.get(taskPosition).priority) {
                Priorities.VERY_HIGH -> { taskPriority.setSelection(3) }
                Priorities.HIGH -> { taskPriority.setSelection(2) }
                Priorities.MEDIUM -> { taskPriority.setSelection(1) }
                Priorities.LOW -> { taskPriority.setSelection(0) }
            }
            taskTime.setText(taskList.get(taskPosition).time)
        }
        else if (historyTaskPosition >= 0) {
            taskName.setText(history.get(historyTaskPosition).taskName)
            taskDetails.setText(history.get(historyTaskPosition).description)
            taskDate.setText(history.get(historyTaskPosition).date)
            taskLocation.setText(history.get(historyTaskPosition).location)
            when (history.get(historyTaskPosition).priority) {
                Priorities.VERY_HIGH -> { taskPriority.setSelection(3) }
                Priorities.HIGH -> { taskPriority.setSelection(2) }
                Priorities.MEDIUM -> { taskPriority.setSelection(1) }
                Priorities.LOW -> { taskPriority.setSelection(0) }
            }
            taskTime.setText(history.get(historyTaskPosition).time)
        }

        navigationView.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.home -> {
                    val homeTaskIntent = Intent(this, HomeScreen::class.java)
                    homeTaskIntent.putExtra("task_list", taskList)
                    homeTaskIntent.putExtra("history", history)
                    homeTaskIntent.putExtra("activity", ActivityNames.ADD_ACTIVITY)
                    startActivity(homeTaskIntent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.history -> {
                    val historyTaskIntent = Intent(this, History::class.java)
                    historyTaskIntent.putExtra("task_list", taskList)
                    historyTaskIntent.putExtra("history", history)
                    startActivity(historyTaskIntent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.search -> {
                    val searchTaskIntent = Intent(this, SearchTask::class.java)
                    searchTaskIntent.putExtra("task_list", taskList)
                    searchTaskIntent.putExtra("history", history)
                    startActivity(searchTaskIntent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.add -> true
            }
            false
        }

        taskPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                priorityDropdownPosition = p2
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        clearDateButton.setOnClickListener {
            taskDate.error = null
            taskDate.setText("")
        }
        clearTimeButton.setOnClickListener {
            taskTime.error = null
            taskTime.setText("")
        }

        saveTaskButton.setOnClickListener {
            if (taskName.text.toString().isEmpty()) {
                taskName.error = "Task name is required"
                taskName.requestFocus()
                return@setOnClickListener
            }

            if (taskDate.text.isNotEmpty() && taskTime.text.isEmpty()) {
                Toast.makeText(applicationContext, "Please fill out the time field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (taskTime.text.isNotEmpty() && taskDate.text.isEmpty()) {
                Toast.makeText(applicationContext, "Please fill out the date field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!taskDate.text.isEmpty()) {
                val dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                val currentDateString = LocalDate.now().format(dateFormat)
                val currentDate: LocalDate = LocalDate.parse(currentDateString, dateFormat)
                val userDate = LocalDate.parse(taskDate.text.toString(), dateFormat)

                when {
                    userDate < currentDate -> {
                        taskDate.setError("The date has already passed")
                        Toast.makeText(this, "The date has already passed", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    userDate == currentDate -> {
                        if (!taskTime.text.isEmpty()) {
                            val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
                            val currentTimeString = LocalTime.now().format(timeFormat)
                            val currentTime = LocalTime.parse(currentTimeString, timeFormat)
                            val userTime = LocalTime.parse(taskTime.text.toString().replace(" ", ""), timeFormat)

                            when {
                                userTime < currentTime -> {
                                    taskTime.error = "The time has already passed"
                                    Toast.makeText(this, "The time has already passed", Toast.LENGTH_SHORT).show()
                                    return@setOnClickListener
                                }
                            }
                        }
                    }
                }
            }

            if (taskPosition != -1) {
                taskList.removeAt(taskPosition)
            }
            // Assigns a unique Id for each task
            var notificationId = 0
            for (i in taskList) {
                if (i.taskId == notificationId) {
                    notificationId++
                }
                else {
                    break
                }
            }

            val newTask = Task(taskName.text.toString(), taskDetails.text.toString(), taskDate.text.toString(), taskTime.text.toString(), taskLocation.text.toString(), getPriorityFromPosition(priorityDropdownPosition), notificationId)

            val homeScreenIntent = Intent(this, HomeScreen::class.java)
            var isTaskAdded = false

            // Next if-else statements handle the priority insertion
            if (taskList.size == 0) {
                taskList.add(newTask)
                isTaskAdded = true
                writeToFile(applicationContext, taskList, TASK_LIST_FILE_NAME)
                writeToFile(applicationContext, history, HISTORY_LIST_FILE_NAME)

                homeScreenIntent.putExtra("task_list", taskList)
                homeScreenIntent.putExtra("history", history)
                homeScreenIntent.putExtra("activity", ActivityNames.ADD_ACTIVITY)
                startActivity(homeScreenIntent)
            }

            if (newTask.priority == Priorities.VERY_HIGH && !isTaskAdded) {
                taskList.add(0, newTask)
            }

            if (newTask.priority == Priorities.HIGH) {
                for (i in 0 until taskList.size) {
                    if (taskList.get(i).priority == Priorities.HIGH && !isTaskAdded) {
                        taskList.add(i, newTask)
                        isTaskAdded = true
                        break
                    }
                }
                for (i in 0 until taskList.size) {
                    if (taskList.get(i).priority == Priorities.MEDIUM && !isTaskAdded) {
                        taskList.add(i, newTask)
                        isTaskAdded = true
                        break
                    }
                }
                for (i in 0 until taskList.size) {
                    if (taskList.get(i).priority == Priorities.LOW && !isTaskAdded) {
                        taskList.add(i, newTask)
                        isTaskAdded = true
                        break
                    }
                }
                if (!isTaskAdded) { taskList.add(newTask) }
            }

            if (newTask.priority == Priorities.MEDIUM) {
                for (i in 0 until taskList.size) {
                    if (taskList.get(i).priority == Priorities.MEDIUM && !isTaskAdded) {
                        taskList.add(i, newTask)
                        isTaskAdded = true
                        break
                    }
                }
                for (i in 0 until taskList.size) {
                    if (taskList.get(i).priority == Priorities.LOW && !isTaskAdded) {
                        taskList.add(i, newTask)
                        isTaskAdded = true
                        break
                    }
                }
                if (!isTaskAdded) { taskList.add(newTask) }
            }

            if (newTask.priority == Priorities.LOW) {
                for (i in 0 until taskList.size) {
                    if (taskList.get(i).priority == Priorities.LOW && !isTaskAdded) {
                        taskList.add(i, newTask)
                        isTaskAdded = true
                        break
                    }
                }
                if (!isTaskAdded) { taskList.add(newTask) }
            }

            writeToFile(applicationContext, taskList, TASK_LIST_FILE_NAME)
            writeToFile(applicationContext, history, HISTORY_LIST_FILE_NAME)

            homeScreenIntent.putExtra("task_list", taskList)
            homeScreenIntent.putExtra("history", history)
            homeScreenIntent.putExtra("activity", ActivityNames.ADD_ACTIVITY)

            startActivity(homeScreenIntent)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val view = currentFocus
        if (view != null && (ev?.action == MotionEvent.ACTION_UP || ev?.action == MotionEvent.ACTION_MOVE) && view is EditText && !view.javaClass.name.startsWith("android.webkit.")) {
            var scrcoords = IntArray(2)
            view.getLocationOnScreen(scrcoords)
            var x = ev.rawX + view.left - scrcoords[0]
            var y = ev.rawY + view.top - scrcoords[1]
            if (x < view.left || x > view.right || y < view.top || y > view.bottom) {
                (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(this.window.decorView.applicationWindowToken, 0)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun updateDate() {
        val format = "MM/dd/yyyy"
        val dateFormat = SimpleDateFormat(format, Locale.US)
        taskDate.setText(dateFormat.format(calendar.time))
    }

    private fun getPriorityFromPosition(position: Int) : Priorities {
        var priority: Priorities = Priorities.LOW
        when (position) {
            0 -> { priority = Priorities.LOW }
            1 -> { priority = Priorities.MEDIUM }
            2 -> { priority = Priorities.HIGH }
            3 -> { priority = Priorities.VERY_HIGH }
        }
        return priority
    }

    private fun writeToFile(context: Context, list: ArrayList<Task>, filename: String) {
        val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)

        oos.writeObject(list)
        oos.close()
    }
}