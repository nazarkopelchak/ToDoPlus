package com.example.todoplus.adapters

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
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
import com.example.todoplus.helpers.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import java.io.File
import java.io.ObjectOutputStream
import java.util.Collections

class HomeScreenRecyclerAdapter(_context: Context, _taskList: ArrayList<Task>, _history: ArrayList<Task>) : RecyclerView.Adapter<HomeScreenRecyclerAdapter.ViewHolder>() {

    lateinit var driveServiceHelper: DriveServiceHelper

    private var context: Context
    private var taskList: ArrayList<Task>
    private var history: ArrayList<Task>

    init {
        context = _context
        taskList = _taskList
        history = _history

        initializeDrive()
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
                addTaskIntent.putExtra("task_position", adapterPosition)
                context.startActivity(addTaskIntent)
            }

            radioButton.setOnClickListener {
                if (history.size == 0) {
                    cancelNotification(adapterPosition)
                    history.add(taskList.get(adapterPosition))
                    taskList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    updateBackupFile()
                    return@setOnClickListener
                }

                if (taskList.get(adapterPosition).priority == Priorities.VERY_HIGH) {
                    cancelNotification(adapterPosition)
                    history.add(0, taskList.get(adapterPosition))
                    taskList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    updateBackupFile()
                    return@setOnClickListener
                }

                var isTaskAdded = false

                if (taskList.get(adapterPosition).priority == Priorities.HIGH) {
                    cancelNotification(adapterPosition)
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.HIGH && !isTaskAdded) {
                            history.add(i, taskList.get(adapterPosition))
                            taskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.MEDIUM && !isTaskAdded) {
                            history.add(i, taskList.get(adapterPosition))
                            taskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.LOW && !isTaskAdded) {
                            history.add(i, taskList.get(adapterPosition))
                            taskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    if (!isTaskAdded) {
                        history.add(taskList.get(adapterPosition))
                        taskList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    updateBackupFile()
                    return@setOnClickListener
                }

                if (taskList.get(adapterPosition).priority == Priorities.MEDIUM) {
                    cancelNotification(adapterPosition)
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.MEDIUM && !isTaskAdded) {
                            history.add(i, taskList.get(adapterPosition))
                            taskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.LOW && !isTaskAdded) {
                            history.add(i, taskList.get(adapterPosition))
                            taskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    if (!isTaskAdded) {
                        history.add(taskList.get(adapterPosition))
                        taskList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    updateBackupFile()
                    return@setOnClickListener
                }

                if (taskList.get(adapterPosition).priority == Priorities.LOW) {
                    cancelNotification(adapterPosition)
                    for (i in 0 until history.size) {
                        if (history.get(i).priority == Priorities.LOW && !isTaskAdded) {
                            history.add(i, taskList.get(adapterPosition))
                            taskList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            isTaskAdded = true
                            break
                        }
                    }
                    if (!isTaskAdded) {
                        history.add(taskList.get(adapterPosition))
                        taskList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }
                    writeToFile(context, taskList, TASK_LIST_FILE_NAME)
                    writeToFile(context, history, HISTORY_LIST_FILE_NAME)
                    updateBackupFile()
                    return@setOnClickListener
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_screen_card_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.taskName.text = taskList.get(position).taskName
        if (taskList.get(position).priority == Priorities.LOW) {
            holder.priority.text = "Low"
            holder.priority.setTextColor(Color.GREEN)
        }
        else if (taskList.get(position).priority == Priorities.MEDIUM) {
            holder.priority.text = "Medium"
            holder.priority.setTextColor(Color.parseColor("#FEE12B"))
        }
        else if (taskList.get(position).priority == Priorities.HIGH) {
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

    fun setTaskList(_taskList: ArrayList<Task>) {
        taskList = _taskList
    }

    fun setHistoryList(_history: ArrayList<Task>) {
        history = _history
    }
    private fun writeToFile(context: Context, list: ArrayList<Task>, filename: String) {
        val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)

        oos.writeObject(list)
        oos.close()
    }

    private fun cancelNotification(taskPosition: Int) {
        try {
            val notificationIntent = Intent(context, ToDoNotifications::class.java)
            notificationIntent.putExtra(titleExtra, taskList[taskPosition].taskName)
            notificationIntent.putExtra(detailsExtra, taskList[taskPosition].description)
            notificationIntent.putExtra(notificationIDExtra, taskList[taskPosition].taskId)
            val pendingIntent = PendingIntent.getBroadcast(context, taskList[taskPosition].taskId, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        } catch (e: java.lang.Exception) {
            Log.d("LOCAL NOTIFICATION", "Failed to cancel the notification:  ${taskList[taskPosition].taskName}")
            e.printStackTrace()
        }

    }
    private fun initializeDrive() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val credentials = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(Scopes.DRIVE_FILE))
        credentials.setSelectedAccount(account?.account)
        val googleDriveService = Drive.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            credentials).setApplicationName("GoogleDriveIntegration").build()
        driveServiceHelper = DriveServiceHelper(googleDriveService)
    }

    private fun updateBackupFile() {
        val sharedPreferences = context.getSharedPreferences("GoogleDriveInfo", Context.MODE_PRIVATE)
        var fileId = sharedPreferences.getString("task_list_id", "")
        var fileToUpdate = File(context.filesDir, "ToDoPlusTaskList.txt")

        driveServiceHelper.updateBackupFile(fileToUpdate, fileId!!)
            .addOnSuccessListener {
                Log.d("GOOGLE DRIVE", "SuccessFully updated task list")
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR", "Failed to update the task backup file")
                println(it.message)
            }
        fileId = sharedPreferences.getString("history_list_id", "")
        fileToUpdate = File(context.filesDir, "ToDoPlusHistoryList.txt")

        driveServiceHelper.updateBackupFile(fileToUpdate, fileId!!)
            .addOnSuccessListener {
                Log.d("GOOGLE DRIVE", "SuccessFully updated history list")
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR", "Failed to update the history backup file")
                println(it.message)
            }
    }
}