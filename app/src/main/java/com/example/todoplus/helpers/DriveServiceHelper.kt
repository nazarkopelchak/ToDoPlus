package com.example.todoplus.helpers

import com.example.todoplus.data.GoogleDriveFileHolder
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections
import java.util.concurrent.Executors

class DriveServiceHelper(driveService: Drive) {
    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mDriveService: Drive

    init {
        mDriveService = driveService
    }

    fun createFolder(folderId: String?): Task<GoogleDriveFileHolder> {
        return Tasks.call(mExecutor) {
            val googleDriveFileHolder = GoogleDriveFileHolder()
            var root: List<String>

            if (folderId == null) {
                root = Collections.singletonList("root")
            } else {
                root = Collections.singletonList(folderId)
            }

            val metadata = com.google.api.services.drive.model.File()
                .setParents(root)
                .setMimeType("application/vnd.google-apps.folder")
                .setName("ToDoPlus Backups")

            val googleFile = mDriveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation")

            googleDriveFileHolder.id = googleFile.id
            return@call googleDriveFileHolder
        }
    }

    fun searchForFolder(): Task<GoogleDriveFileHolder> {
        return Tasks.call(mExecutor) {
            val googleDriveFileHolder = GoogleDriveFileHolder()
            val query = mDriveService.files().list()
                .setQ("name = 'ToDoPlus Backups' and trashed = false")
                .setFields("files(id, name, mimeType)")
                .execute()
            for (file in query.files) {
                if (file.mimeType == "application/vnd.google-apps.folder" && file.name == "ToDoPlus Backups") {
                    googleDriveFileHolder.id = file.id
                    return@call googleDriveFileHolder
                }
            }
            return@call googleDriveFileHolder
        }

    }

    fun searchForFile(filename: String): Task<GoogleDriveFileHolder> {
        return Tasks.call(mExecutor) {
            val googleDriveFileHolder = GoogleDriveFileHolder()
            val query = mDriveService.files().list()
                .setQ("trashed = false")
                .setFields("files(id, name, mimeType)")
                .execute()
            for (file in query.files) {
                if (file.mimeType == "text/plain" && file.name == filename) {
                    googleDriveFileHolder.id = file.id
                    return@call googleDriveFileHolder
                }
            }
            return@call googleDriveFileHolder
        }

    }

    fun createBackupTaskListFile(folderId: String?): Task<GoogleDriveFileHolder> {
        return Tasks.call(mExecutor) {
            val googleDriveFileHolder = GoogleDriveFileHolder()
            val root = Collections.singletonList(folderId)

            val metadata = com.google.api.services.drive.model.File()
                .setParents(root)
                .setMimeType("text/plain")
                .setName("ToDoPlusTaskListBackup.txt")

            val googleFile = mDriveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation")

            googleDriveFileHolder.id = googleFile.id

            return@call googleDriveFileHolder
        }
    }

    fun createBackupHistoryFile(folderId: String?): Task<GoogleDriveFileHolder> {
        return Tasks.call(mExecutor) {
            val googleDriveFileHolder = GoogleDriveFileHolder()
            val root = Collections.singletonList(folderId)

            val metadata = com.google.api.services.drive.model.File()
                .setParents(root)
                .setMimeType("text/plain")
                .setName("ToDoPlusHistoryBackup.txt")

            val googleFile = mDriveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation")

            googleDriveFileHolder.id = googleFile.id

            return@call googleDriveFileHolder
        }
    }

    fun updateBackupFile(localFile: File, fileId: String): Task<Void> {
        return Tasks.call(mExecutor) {
            val fileContent = FileContent("text/plain", localFile)
            mDriveService.files().update(fileId, null, fileContent).execute()
            return@call null
        }
    }

    fun downloadBackupFile(targetFile: File, fileId: String): Task<Void> {
        return Tasks.call(mExecutor) {
            var outputStream = FileOutputStream(targetFile)
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            return@call null
        }
    }
}