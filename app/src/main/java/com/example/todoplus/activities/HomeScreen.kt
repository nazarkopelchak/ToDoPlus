package com.example.todoplus.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoplus.HISTORY_LIST_FILE_NAME
import com.example.todoplus.R
import com.example.todoplus.TASK_LIST_FILE_NAME
import com.example.todoplus.adapters.HomeScreenRecyclerAdapter
import com.example.todoplus.data.ActivityNames
import com.example.todoplus.data.Priorities
import com.example.todoplus.data.Task
import com.example.todoplus.helpers.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import java.io.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Collections

const val TASK_LIST_BACKUP_FILE = "ToDoPlusTaskListBackup.txt"
const val HISTORY_LIST_BACKUP_FILE = "ToDoPlusHistoryBackup.txt"
class HomeScreen : AppCompatActivity() {

    lateinit var userNameTextView: TextView
    lateinit var toolbar: Toolbar
    lateinit var navigationView: BottomNavigationView
    lateinit var recyclerView: RecyclerView
    lateinit var signInButton: Button
    lateinit var signOutButton: Button
    lateinit var gso: GoogleSignInOptions
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var googleDriveService: Drive
    lateinit var mDriveServiceHelper: DriveServiceHelper
    lateinit var sharPreferences: SharedPreferences

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<HomeScreenRecyclerAdapter.ViewHolder>? = null
    private var taskList: ArrayList<Task> = ArrayList<Task>()
    private var history: ArrayList<Task> = ArrayList<Task>()
    private val RC_SIGN_IN = 0
    private val RC_AUTHORIZE_DRIVE = 200
    private var account: GoogleSignInAccount? = null
    private var activityName: ActivityNames = ActivityNames.MAIN_ACTIVITY
    private var notificationPermissionGranted = false
    private var signInButtonClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        sharPreferences = getSharedPreferences("GoogleDriveInfo", Context.MODE_PRIVATE)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        userNameTextView = findViewById(R.id.home_screen_toolbar_user_name)
        toolbar = findViewById(R.id.home_screen_toolbar)
        signInButton = findViewById(R.id.log_in_button)
        signOutButton = findViewById(R.id.log_out_button)

        signInButton.setOnClickListener {
            activityName = ActivityNames.MAIN_ACTIVITY
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        signOutButton.setOnClickListener {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                Log.d("GOOGLE SIGN IN","SIGNED OUT SUCCESSFULLY")
                userNameTextView.setText("Guest")
                signInButton.visibility = View.VISIBLE
                signOutButton.visibility = View.GONE
            }
        }

        navigationView = findViewById(R.id.bottom_navigation)
        navigationView.selectedItemId = R.id.home

        navigationView.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.add -> {
                    val addTaskIntent = Intent(this, AddTask::class.java)
                    val adapter = recyclerView.adapter as HomeScreenRecyclerAdapter
                    addTaskIntent.putExtra("task_list", adapter.getTaskList())
                    addTaskIntent.putExtra("history", adapter.getHistory())
                    addTaskIntent.putExtra("task_position", -1)
                    startActivity(addTaskIntent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.history -> {
                    val historyTaskIntent = Intent(this, History::class.java)
                    val adapter = recyclerView.adapter as HomeScreenRecyclerAdapter
                    historyTaskIntent.putExtra("task_list", adapter.getTaskList())
                    historyTaskIntent.putExtra("history", adapter.getHistory())
                    startActivity(historyTaskIntent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.search -> {
                    val searchTaskIntent = Intent(this, SearchTask::class.java)
                    val adapter = recyclerView.adapter as HomeScreenRecyclerAdapter
                    searchTaskIntent.putExtra("task_list", adapter.getTaskList())
                    searchTaskIntent.putExtra("history", adapter.getHistory())
                    startActivity(searchTaskIntent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.home -> true
            }
            false
        }

        taskList = intent.getSerializableExtra("task_list") as ArrayList<Task>
        history = intent.getSerializableExtra("history") as ArrayList<Task>

        layoutManager = LinearLayoutManager(this)
        adapter = HomeScreenRecyclerAdapter(this , taskList, history)

        recyclerView = findViewById(R.id.home_screen_recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                notificationPermissionGranted = true
                notificationScheduler()
            }
        }
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                notificationPermissionGranted = true
                notificationScheduler()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun notificationScheduler() {
        if (notificationPermissionGranted) {
            for (i in taskList) {
                scheduleNotification(i.taskId, i.taskName, i.description, i.date, i.time)
            }
        }
    }

    private fun scheduleNotification(taskId: Int, taskTitle: String, taskDescription: String, taskDate: String, taskTime: String) {
        if (taskDate.isEmpty()) { return }
        if (datePassed(taskDate, taskTime)) { return }

        val nIntent = Intent(this, ToDoNotifications::class.java)
        nIntent.putExtra(titleExtra, taskTitle)
        nIntent.putExtra(detailsExtra, taskDescription)
        nIntent.putExtra(notificationIDExtra, taskId)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskId,
            nIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime(taskDate, taskTime)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }

    private fun getTime(taskDate: String, taskTime: String): Long {
        val calendar = Calendar.getInstance()
        val dateList = taskDate.split("/")
        val timeList = taskTime.replace(" ", "").split(":")

        if (taskTime.isEmpty()) {
            calendar.set(dateList[2].toInt(), dateList[0].toInt()-1, dateList[1].toInt(), 8, 0, 0)
        }
        else {
            calendar.set(dateList[2].toInt(), dateList[0].toInt()-1, dateList[1].toInt(), timeList[0].toInt(), timeList[1].toInt(), 0)
        }

        return calendar.timeInMillis
    }

    private fun datePassed(taskDate: String, taskTime: String): Boolean {
        val dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val currentDateString = LocalDate.now().format(dateFormat)
        val currentDate: LocalDate = LocalDate.parse(currentDateString, dateFormat)
        val userDate = LocalDate.parse(taskDate, dateFormat)

        when {
            userDate < currentDate -> {
                return true
            }
            userDate == currentDate -> {
                if (taskTime.isEmpty()) { return true }

                val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
                val currentTimeString = LocalTime.now().format(timeFormat)
                val currentTime = LocalTime.parse(currentTimeString, timeFormat)
                val userTime = LocalTime.parse(taskTime.replace(" ", ""), timeFormat)

                when {
                    userTime <= currentTime -> {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResults(task)
        }
    }

    private fun handleSignInResults(completedTask: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            account = completedTask.getResult(ApiException::class.java)
            userNameTextView.setText(account?.displayName)
            signInButtonClicked = true
            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE

            checkForGooglePermissions()
        } catch (e: ApiException) {
            Log.d("GOOGLE LOG IN","FAILED TO LOG IN")
        }

    }

    private fun checkForGooglePermissions() {
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(applicationContext),
                Scope(Scopes.DRIVE_FILE),
                Scope(Scopes.EMAIL)
            )) {
            GoogleSignIn.requestPermissions(
                this,
                RC_AUTHORIZE_DRIVE,
                GoogleSignIn.getLastSignedInAccount(this),
                Scope(Scopes.DRIVE_FILE),
                Scope(Scopes.EMAIL)
            )
        } else {
            driveSetUp()
        }
    }

    override fun onStart() {
        super.onStart()

        account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            userNameTextView.setText(account?.displayName)
            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
            activityName = intent.getSerializableExtra("activity") as ActivityNames

            checkForGooglePermissions()
        }
    }

    private fun driveSetUp() {
        val credential = GoogleAccountCredential.usingOAuth2(applicationContext, Collections.singleton(Scopes.DRIVE_FILE))
        credential.setSelectedAccount(account?.account)

        googleDriveService = Drive.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            credential).setApplicationName("GoogleDriveIntegration").build()

        mDriveServiceHelper = DriveServiceHelper(googleDriveService)

        backupFolderExists()
    }

    private fun createBackupFolder() {
        mDriveServiceHelper.createFolder(null)
            .addOnSuccessListener {
                Log.d("GOOGLE DRIVE","Folder has been successfully created")
                sharPreferences.edit().remove("folder_id").apply()
                sharPreferences.edit().putString("folder_id", it.id).apply()

                createBackupTaskListFile()
                createBackupHistoryTaskFile()
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR", "Failed to create a folder")
                println(it.message)
            }
    }

    private fun createBackupTaskListFile() {
        val folderId = sharPreferences.getString("folder_id", "")
        mDriveServiceHelper.createBackupTaskListFile(folderId)
            .addOnSuccessListener {
                Log.d("GOOGLE DRIVE", "File has been successfully created")
                sharPreferences.edit().remove("task_list_id").apply()
                sharPreferences.edit().putString("task_list_id", it.id).apply()

                updateBackupFile("task_list_id", TASK_LIST_FILE_NAME)
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR", "Failed to create a file")
                println(it.message)
            }
    }
    private fun createBackupHistoryTaskFile() {
        val folderId = sharPreferences.getString("folder_id", "")
        mDriveServiceHelper.createBackupHistoryFile(folderId)
            .addOnSuccessListener {
                Log.d("GOOGLE DRIVE", "File has been successfully created")
                sharPreferences.edit().remove("history_list_id").apply()
                sharPreferences.edit().putString("history_list_id", it.id).apply()

                updateBackupFile("history_list_id", HISTORY_LIST_FILE_NAME)
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR", "Failed to create a file")
                println(it.message)
            }
    }
    private fun backupFolderExists() {
        mDriveServiceHelper.searchForFolder()
            .addOnSuccessListener {
                if (it.id.isEmpty()) {
                    Log.d("GOOGLE DRIVE", "No folder")
                    sharPreferences.edit().remove("folder_id").apply()

                    createBackupFolder()
                }
                else {
                    Log.d("GOOGLE DRIVE", "Folder is found")
                    sharPreferences.edit().remove("folder_id").apply()
                    sharPreferences.edit().putString("folder_id", it.id).apply()

                    backupFileExists()
                }
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR" ,"Failed to search for a folder")
                println(it.message)
            }
    }

    private fun backupFileExists() {
        mDriveServiceHelper.searchForFile("ToDoPlusTaskListBackup.txt")
            .addOnSuccessListener {
                if (it.id.isEmpty()) {
                    Log.d("GOOGLE DRIVE", "No task list file was found")
                    sharPreferences.edit().remove("task_list_id").apply()

                    createBackupTaskListFile()
                }
                else {
                    Log.d("GOOGLE DRIVE", "Task list file is found")
                    sharPreferences.edit().remove("task_list_id").apply()
                    sharPreferences.edit().putString("task_list_id", it.id).apply()

                    if (activityName == ActivityNames.MAIN_ACTIVITY) {
                        downloadFileFromDrive("task_list_id", TASK_LIST_BACKUP_FILE)
                    }
                    else {
                        if (signInButtonClicked) {
                            downloadFileFromDrive("task_list_id", TASK_LIST_BACKUP_FILE)
                        }
                        else {
                            updateBackupFile("task_list_id", TASK_LIST_FILE_NAME)
                        }
                    }

                }
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR", "Failed to search for a task list file")
                println(it.message)
            }

        mDriveServiceHelper.searchForFile("ToDoPlusHistoryBackup.txt")
            .addOnSuccessListener {
                if (it.id.isEmpty()) {
                    Log.d("GOOGLE DRIVE", "No history list file  was found")
                    sharPreferences.edit().remove("history_list_id").apply()

                    createBackupHistoryTaskFile()
                }
                else {
                    Log.d("GOOGLE DRIVE", "History list file is found")
                    sharPreferences.edit().remove("history_list_id").apply()
                    sharPreferences.edit().putString("history_list_id", it.id).apply()

                    if (activityName == ActivityNames.MAIN_ACTIVITY) {
                        downloadFileFromDrive("history_list_id", HISTORY_LIST_BACKUP_FILE)
                    }
                    else {
                        if (signInButtonClicked) {
                            downloadFileFromDrive("history_list_id", HISTORY_LIST_BACKUP_FILE)
                        }
                        else {
                            updateBackupFile("history_list_id", HISTORY_LIST_FILE_NAME)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR", "Failed to search for a history list file")
                println(it.message)
            }
    }

    private fun downloadFileFromDrive(fileId: String, targetFile: String) {
        val fileIdToDownload = sharPreferences.getString(fileId, "")
        var localfile = File(filesDir, targetFile)

        mDriveServiceHelper.downloadBackupFile(localfile, fileIdToDownload!!)
            .addOnSuccessListener {
                Log.d("GOOGLE DRIVE", "$targetFile downloaded")
                if (targetFile.equals(TASK_LIST_BACKUP_FILE)) {
                    var cTasks = readFromFile(applicationContext, TASK_LIST_BACKUP_FILE)

                    removeDuplicates(cTasks, taskList)
                    writeToFile(this, taskList, TASK_LIST_FILE_NAME)

                    val homeScreenAdapter = recyclerView.adapter as HomeScreenRecyclerAdapter
                    homeScreenAdapter.setTaskList(taskList)
                    recyclerView.adapter?.notifyDataSetChanged()
                    notificationScheduler()

                    if (signInButtonClicked) {
                        updateBackupFile(fileId, TASK_LIST_FILE_NAME)
                    }
                }
                else if (targetFile.equals(HISTORY_LIST_BACKUP_FILE)) {
                    var hTasks = readFromFile(applicationContext, HISTORY_LIST_BACKUP_FILE)

                    removeDuplicates(hTasks, history)
                    writeToFile(this, history, HISTORY_LIST_FILE_NAME)

                    val homeScreenAdapter = recyclerView.adapter as HomeScreenRecyclerAdapter
                    homeScreenAdapter.setHistoryList(history)
                    recyclerView.adapter?.notifyDataSetChanged()

                    if (signInButtonClicked) {
                        updateBackupFile(fileId, HISTORY_LIST_FILE_NAME)
                    }
                }
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE ERROR", "Failed to download a file: $fileId")
                println(it.message)
            }

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

    private fun removeDuplicates(list: ArrayList<Task>, finalList: ArrayList<Task>) {
        for (i in list) {
            if (!finalList.contains(i)) {
                addTaskAtCorrectOrder(i, finalList)
            }
        }
    }

    // Adds a task to the list according to priority
    private fun addTaskAtCorrectOrder(newTask: Task, _taskList: ArrayList<Task>) {
        var isTaskAdded = false

        if (_taskList.size == 0) {
            _taskList.add(newTask)
            return
        }

        if (newTask.priority == Priorities.VERY_HIGH && !isTaskAdded) {
            _taskList.add(0, newTask)
        }

        if (newTask.priority == Priorities.HIGH) {
            for (i in 0 until _taskList.size) {
                if (_taskList.get(i).priority == Priorities.HIGH && !isTaskAdded) {
                    _taskList.add(i, newTask)
                    isTaskAdded = true
                    break
                }
            }
            for (i in 0 until _taskList.size) {
                if (_taskList.get(i).priority == Priorities.MEDIUM && !isTaskAdded) {
                    _taskList.add(i, newTask)
                    isTaskAdded = true
                    break
                }
            }
            for (i in 0 until _taskList.size) {
                if (_taskList.get(i).priority == Priorities.LOW && !isTaskAdded) {
                    _taskList.add(i, newTask)
                    isTaskAdded = true
                    break
                }
            }
            if (!isTaskAdded) { _taskList.add(newTask) }
        }

        if (newTask.priority == Priorities.MEDIUM) {
            for (i in 0 until _taskList.size) {
                if (_taskList.get(i).priority == Priorities.MEDIUM && !isTaskAdded) {
                    _taskList.add(i, newTask)
                    isTaskAdded = true
                    break
                }
            }
            for (i in 0 until _taskList.size) {
                if (_taskList.get(i).priority == Priorities.LOW && !isTaskAdded) {
                    _taskList.add(i, newTask)
                    isTaskAdded = true
                    break
                }
            }
            if (!isTaskAdded) { _taskList.add(newTask) }
        }

        if (newTask.priority == Priorities.LOW) {
            for (i in 0 until _taskList.size) {
                if (_taskList.get(i).priority == Priorities.LOW && !isTaskAdded) {
                    _taskList.add(i, newTask)
                    isTaskAdded = true
                    break
                }
            }
            if (!isTaskAdded) { _taskList.add(newTask) }
        }
    }
    private fun writeToFile(context: Context, list: ArrayList<Task>, filename: String) {
        val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)

        oos.writeObject(list)
        oos.close()
    }

    private fun updateBackupFile(driveFileId: String, localFileName: String) {
        var fileId = sharPreferences.getString(driveFileId, "")
        var fileToUpdate = File(filesDir, localFileName)

        mDriveServiceHelper.updateBackupFile(fileToUpdate, fileId!!)
            .addOnSuccessListener {
                Log.d("GOOGLE DRIVE","SuccessFully updated $driveFileId list")
            }
            .addOnFailureListener {
                Log.d("GOOGLE DRIVE", "Failed to upload file to Drive: $driveFileId")
                println(it.message)
            }
    }
}
