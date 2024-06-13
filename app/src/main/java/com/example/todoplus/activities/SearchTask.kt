package com.example.todoplus.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoplus.R
import com.example.todoplus.adapters.SearchTaskRecyclerAdapter
import com.example.todoplus.data.ActivityNames
import com.example.todoplus.data.Priorities
import com.example.todoplus.data.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import kotlin.collections.ArrayList

class SearchTask : AppCompatActivity() {

    lateinit var searchButton: Button
    lateinit var clearButton: Button
    lateinit var searchDate: EditText
    lateinit var searchRecyclerView: RecyclerView
    lateinit var navigationView: BottomNavigationView
    lateinit var calendar: Calendar

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<SearchTaskRecyclerAdapter.ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_task)

        searchButton = findViewById(R.id.search_date_button)
        clearButton = findViewById(R.id.clear_date_button_search)
        searchDate = findViewById(R.id.search_date_edit_text)
        navigationView = findViewById(R.id.bottom_navigation)
        calendar = Calendar.getInstance()

        navigationView.selectedItemId = R.id.search

        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(android.icu.util.Calendar.YEAR, year)
            calendar.set(android.icu.util.Calendar.MONTH, month)
            calendar.set(android.icu.util.Calendar.DAY_OF_MONTH, day)
            updateDate()
        }

        searchDate.setOnClickListener {
            val datePicker = DatePickerDialog(this, R.style.DialogTheme, date, calendar.get(android.icu.util.Calendar.YEAR), calendar.get(android.icu.util.Calendar.MONTH), calendar.get(android.icu.util.Calendar.DAY_OF_MONTH))
            datePicker.show()
            datePicker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            datePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }

        var taskList: ArrayList<Task> = intent.getSerializableExtra("task_list") as ArrayList<Task>
        var history: ArrayList<Task> = intent.getSerializableExtra("history") as ArrayList<Task>

        navigationView.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.add -> {
                    if (adapter == null) {
                        val addTaskIntent = Intent(this, AddTask::class.java)
                        addTaskIntent.putExtra("task_list", taskList)
                        addTaskIntent.putExtra("history", history)
                        startActivity(addTaskIntent)
                        overridePendingTransition(0, 0)
                        true
                    }
                    else {
                        val addTaskIntent = Intent(this, AddTask::class.java)
                        val adapter = searchRecyclerView.adapter as SearchTaskRecyclerAdapter
                        addTaskIntent.putExtra("task_list", adapter.getTaskList())
                        addTaskIntent.putExtra("history", adapter.getHistory())
                        startActivity(addTaskIntent)
                        overridePendingTransition(0, 0)
                        true
                    }
                }
                R.id.history -> {
                    if (adapter == null) {
                        val historyTaskIntent = Intent(this, History::class.java)
                        historyTaskIntent.putExtra("task_list", taskList)
                        historyTaskIntent.putExtra("history", history)
                        startActivity(historyTaskIntent)
                        overridePendingTransition(0, 0)
                        true
                    }
                    else {
                        val historyTaskIntent = Intent(this, History::class.java)
                        val adapter = searchRecyclerView.adapter as SearchTaskRecyclerAdapter
                        historyTaskIntent.putExtra("task_list", adapter.getTaskList())
                        historyTaskIntent.putExtra("history", adapter.getHistory())
                        startActivity(historyTaskIntent)
                        overridePendingTransition(0, 0)
                        true
                    }
                }
                R.id.home -> {
                    if (adapter == null) {
                        val homeTaskIntent = Intent(this, HomeScreen::class.java)
                        homeTaskIntent.putExtra("task_list", taskList)
                        homeTaskIntent.putExtra("history", history)
                        homeTaskIntent.putExtra("activity", ActivityNames.SEARCH_ACTIVITY)
                        startActivity(homeTaskIntent)
                        overridePendingTransition(0, 0)
                        true
                    }
                    else {
                        val homeTaskIntent = Intent(this, HomeScreen::class.java)
                        val adapter = searchRecyclerView.adapter as SearchTaskRecyclerAdapter
                        homeTaskIntent.putExtra("task_list", adapter.getTaskList())
                        homeTaskIntent.putExtra("history", adapter.getHistory())
                        homeTaskIntent.putExtra("activity", ActivityNames.SEARCH_ACTIVITY)
                        startActivity(homeTaskIntent)
                        overridePendingTransition(0, 0)
                        true
                    }
                }
                R.id.search -> true
            }
            false
        }

        searchButton.setOnClickListener {
            var searchedTaskList: ArrayList<Task> = ArrayList()

            if (searchDate.text.isEmpty()) {
                searchedTaskList = taskList
            }
            else {
                for (i in 0 until taskList.size) {
                    if (taskList.get(i).date.contentEquals(searchDate.text)) {
                        searchedTaskList = orderByPriority(searchedTaskList, taskList.get(i))
                    }
                }
            }

            if (searchedTaskList.size == 0) {
                Toast.makeText(applicationContext, "No tasks found for this date", Toast.LENGTH_SHORT).show()
            }

            layoutManager = LinearLayoutManager(this)
            adapter = SearchTaskRecyclerAdapter(this, taskList, history, searchedTaskList)
            searchRecyclerView = findViewById(R.id.search_task_recycler_view)
            searchRecyclerView.layoutManager = layoutManager
            searchRecyclerView.adapter = adapter
        }

        clearButton.setOnClickListener {
            searchDate.setText("")
        }
    }

    private fun updateDate() {
        val format = "MM/dd/yyyy"
        val dateFormat = SimpleDateFormat(format, Locale.US)
        searchDate.setText(dateFormat.format(calendar.time))
    }

    private fun orderByPriority(taskList: ArrayList<Task>, newTask: Task) : ArrayList<Task> {
        var isTaskAdded = false

        if (taskList.size == 0) {
            taskList.add(newTask)
            isTaskAdded = true
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

        return taskList
    }
}