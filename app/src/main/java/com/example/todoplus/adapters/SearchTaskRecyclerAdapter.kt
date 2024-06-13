package com.example.todoplus.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoplus.HISTORY_LIST_FILE_NAME
import com.example.todoplus.R
import com.example.todoplus.TASK_LIST_FILE_NAME
import com.example.todoplus.activities.AddTask
import com.example.todoplus.data.Priorities
import com.example.todoplus.data.Task
import java.io.ObjectOutputStream

class SearchTaskRecyclerAdapter(_context: Context, _taskList: ArrayList<Task>, _history: ArrayList<Task>, _searchTaskList: ArrayList<Task>) : RecyclerView.Adapter<SearchTaskRecyclerAdapter.ViewHolder>() {

    private var context: Context
    private var taskList: ArrayList<Task>
    private var history: ArrayList<Task>
    private var searchTaskList: ArrayList<Task>

    init {
        context = _context
        taskList = _taskList
        history = _history
        searchTaskList = _searchTaskList
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var taskName: TextView
        var priority: TextView
        var radioButton: RadioButton

        init {
            taskName = itemView.findViewById(R.id.home_screen_card_view_task_name)
            priority = itemView.findViewById(R.id.home_screen_card_view_priority)
            radioButton = itemView.findViewById(R.id.home_screen_card_view_radio_button)

            itemView.setOnClickListener {
                val addTaskIntent = Intent(context, AddTask::class.java)
                addTaskIntent.putExtra("task_list", taskList)
                addTaskIntent.putExtra("history", history)
                for (i in 0 until taskList.size) {
                    if (taskList.get(i).equals(searchTaskList.get(adapterPosition))) {
                        addTaskIntent.putExtra("task_position", i)
                        break
                    }
                }
                context.startActivity(addTaskIntent)
            }

            radioButton.setOnClickListener {
                if (history.size == 0) {
                    history.add(searchTaskList.get(adapterPosition))
                    taskList.remove(searchTaskList.get(adapterPosition))
                    searchTaskList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    return@setOnClickListener
                }

                if (searchTaskList.get(adapterPosition).priority == Priorities.VERY_HIGH) {
                    history.add(0, searchTaskList.get(adapterPosition))
                    taskList.remove(searchTaskList.get(adapterPosition))
                    searchTaskList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    return@setOnClickListener
                }

                var isTaskAdded = false

                if (searchTaskList.get(adapterPosition).priority == Priorities.HIGH) {
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.HIGH && !isTaskAdded) {
                            history.add(i, searchTaskList.get(adapterPosition))
                            taskList.remove(searchTaskList.get(adapterPosition))
                            searchTaskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.MEDIUM && !isTaskAdded) {
                            history.add(i, searchTaskList.get(adapterPosition))
                            taskList.remove(searchTaskList.get(adapterPosition))
                            searchTaskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.LOW && !isTaskAdded) {
                            history.add(i, searchTaskList.get(adapterPosition))
                            taskList.remove(searchTaskList.get(adapterPosition))
                            searchTaskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    if (!isTaskAdded) {
                        history.add(searchTaskList.get(adapterPosition))
                        taskList.remove(searchTaskList.get(adapterPosition))
                        searchTaskList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    return@setOnClickListener
                }

                if (searchTaskList.get(adapterPosition).priority == Priorities.MEDIUM) {
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.MEDIUM && !isTaskAdded) {
                            history.add(i, searchTaskList.get(adapterPosition))
                            taskList.remove(searchTaskList.get(adapterPosition))
                            searchTaskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.LOW && !isTaskAdded) {
                            history.add(i, searchTaskList.get(adapterPosition))
                            taskList.remove(searchTaskList.get(adapterPosition))
                            searchTaskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    if (!isTaskAdded) {
                        history.add(searchTaskList.get(adapterPosition))
                        taskList.remove(searchTaskList.get(adapterPosition))
                        searchTaskList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    return@setOnClickListener
                }

                if (searchTaskList.get(adapterPosition).priority == Priorities.LOW) {
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.LOW && !isTaskAdded) {
                            history.add(i, searchTaskList.get(adapterPosition))
                            taskList.remove(searchTaskList.get(adapterPosition))
                            searchTaskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    if (!isTaskAdded) {
                        history.add(taskList.get(adapterPosition))
                        taskList.remove(searchTaskList.get(adapterPosition))
                        searchTaskList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    return@setOnClickListener
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_screen_card_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return searchTaskList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.taskName.text = searchTaskList.get(position).taskName
        if (searchTaskList.get(position).priority == Priorities.LOW) {
            holder.priority.text = "Low"
            holder.priority.setTextColor(Color.GREEN)
        }
        else if (searchTaskList.get(position).priority == Priorities.MEDIUM) {
            holder.priority.text = "Medium"
            holder.priority.setTextColor(Color.parseColor("#FEE12B"))
        }
        else if (searchTaskList.get(position).priority == Priorities.HIGH) {
            holder.priority.text = "High"
            holder.priority.setTextColor(Color.parseColor("#FFA500"))
        }
        else {
            holder.priority.text = "Very High"
            holder.priority.setTextColor(Color.RED)
        }
    }

    fun getTaskList(): ArrayList<Task> {
        return taskList
    }

    fun getHistory(): ArrayList<Task> {
        return history
    }
    private fun writeToFile(context: Context, list: ArrayList<Task>, filename: String) {
        val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)

        oos.writeObject(list)
        oos.close()
    }
}