package com.example.todoplus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.todoplus.activities.HomeScreen
import com.example.todoplus.data.ActivityNames
import com.example.todoplus.data.Task
import com.example.todoplus.helpers.channelID
import java.io.*

const val TASK_LIST_FILE_NAME = "ToDoPlusTaskList.txt"          // Local File
const val HISTORY_LIST_FILE_NAME = "ToDoPlusHistoryList.txt"    // Local File
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var taskList = readFromFile(applicationContext, TASK_LIST_FILE_NAME)
        var history = readFromFile(applicationContext, HISTORY_LIST_FILE_NAME)

        createNotificationChannel()

        val homeScreen = Intent(this, HomeScreen::class.java)
        homeScreen.putExtra("task_list", taskList)
        homeScreen.putExtra("history", history)
        homeScreen.putExtra("activity", ActivityNames.MAIN_ACTIVITY)
        startActivity(homeScreen)
    }

    private fun createNotificationChannel() {
        val name = "ToDoPlusNotificationChannel"
        val desc = "This channel handles all notifications for the ToDoPlus app"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        channel.setSound(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.playful_notification), audioAttributes)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun readFromFile(context: Context, filename: String): ArrayList<Task> {
        try {
            val fis = context.openFileInput(filename)
            val ois = ObjectInputStream(fis)

            var list: ArrayList<Task> = ois.readObject() as ArrayList<Task>
            ois.close()
            return list
        }
        catch (e: FileNotFoundException) {
            e.printStackTrace()
            createLocalFiles()  // No local files. Either first time opened or files were deleted
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        var list = ArrayList<Task>()
        return list
    }

    private fun createLocalFiles() {
        var fileOutput = this.openFileOutput(TASK_LIST_FILE_NAME, Context.MODE_PRIVATE)
        var outputStream = ObjectOutputStream(fileOutput)
        val emptyTaskList = ArrayList<Task>()

        outputStream.writeObject(emptyTaskList)
        outputStream.close()

        fileOutput = this.openFileOutput(HISTORY_LIST_FILE_NAME, Context.MODE_PRIVATE)
        outputStream = ObjectOutputStream(fileOutput)

        outputStream.writeObject(emptyTaskList)
        outputStream.close()
    }
}