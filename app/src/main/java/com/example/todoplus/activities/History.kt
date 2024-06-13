package com.example.todoplus.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoplus.R
import com.example.todoplus.adapters.HistoryRecyclerAdapter
import com.example.todoplus.data.ActivityNames
import com.example.todoplus.data.Task
import com.google.android.material.bottomnavigation.BottomNavigationView

class History : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var navigationView: BottomNavigationView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var adapter: RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder>

    private var taskList: ArrayList<Task> = ArrayList<Task>()
    private var history: ArrayList<Task> = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        navigationView = findViewById(R.id.bottom_navigation)
        navigationView.selectedItemId = R.id.history

        navigationView.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.add -> {
                    val addTaskIntent = Intent(this, AddTask::class.java)
                    val adapter = recyclerView.adapter as HistoryRecyclerAdapter
                    addTaskIntent.putExtra("task_list", adapter.getTaskList())
                    addTaskIntent.putExtra("history", adapter.getHistory())
                    addTaskIntent.putExtra("task_position", -1)
                    startActivity(addTaskIntent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.home -> {
                    val homeTaskIntent = Intent(this, HomeScreen::class.java)
                    val adapter = recyclerView.adapter as HistoryRecyclerAdapter
                    homeTaskIntent.putExtra("task_list", adapter.getTaskList())
                    homeTaskIntent.putExtra("history", adapter.getHistory())
                    homeTaskIntent.putExtra("activity", ActivityNames.HISTORY_ACTIVITY)
                    startActivity(homeTaskIntent)
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
                R.id.history -> true
            }
            false
        }

        taskList = intent.getSerializableExtra("task_list") as ArrayList<Task>
        history = intent.getSerializableExtra("history") as ArrayList<Task>

        layoutManager = LinearLayoutManager(this)
        adapter = HistoryRecyclerAdapter(this , taskList, history)

        recyclerView = findViewById(R.id.history_recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }
}