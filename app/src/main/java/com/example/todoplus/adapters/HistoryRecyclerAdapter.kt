package com.example.todoplus.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoplus.HISTORY_LIST_FILE_NAME
import com.example.todoplus.R
import com.example.todoplus.activities.AddTask
import com.example.todoplus.data.Priorities
import com.example.todoplus.data.Task
import java.io.ObjectOutputStream

class HistoryRecyclerAdapter(_context: Context, _taskList: ArrayList<Task>, _history: ArrayList<Task>) : RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder>() {

    private var context: Context
    private var taskList: ArrayList<Task>
    private var history: ArrayList<Task>

    init {
        context = _context
        taskList = _taskList
        history = _history
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val taskName: TextView
        val priority: TextView
        val removeButton: ImageButton

        init {
            taskName = itemView.findViewById(R.id.history_card_view_task_name)
            priority = itemView.findViewById(R.id.history_card_view_priority)
            removeButton = itemView.findViewById(R.id.remove_task_from_history_image_button)

            itemView.setOnClickListener {
                val addTaskIntent = Intent(context, AddTask::class.java)
                addTaskIntent.putExtra("task_list", taskList)
                addTaskIntent.putExtra("history", history)
                addTaskIntent.putExtra("history_task_position", adapterPosition)
                context.startActivity(addTaskIntent)
            }

            removeButton.setOnClickListener {
                history.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                writeToFile(context, history, HISTORY_LIST_FILE_NAME)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.history_card_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.taskName.text = history.get(position).taskName
        if (history.get(position).priority == Priorities.LOW) {
            holder.priority.text = "Low"
            holder.priority.setTextColor(Color.GREEN)
        }
        else if (history.get(position).priority == Priorities.MEDIUM) {
            holder.priority.text = "Medium"
            holder.priority.setTextColor(Color.parseColor("#FEE12B"))
        }
        else if (history.get(position).priority == Priorities.HIGH) {
            holder.priority.text = "High"
            holder.priority.setTextColor(Color.parseColor("#FFA500"))
        }
        else {
            holder.priority.text = "Very High"
            holder.priority.setTextColor(Color.RED)
        }
    }

    fun getTaskList() : ArrayList<Task> {
        return taskList
    }

    fun getHistory() : ArrayList<Task> {
        return history
    }

    private fun writeToFile(context: Context, list: ArrayList<Task>, filename: String) {
        val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)

        oos.writeObject(list)
        oos.close()
    }
}