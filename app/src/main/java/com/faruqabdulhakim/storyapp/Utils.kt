package com.faruqabdulhakim.storyapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

fun String.withDateFormat(): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = format.parse(this) as Date
        DateFormat.getDateInstance(DateFormat.FULL).format(date)
    } catch (e: Exception) {
        ""
    }
}

fun getTimestamp(): String {
    return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        .format(System.currentTimeMillis())
}

fun createPhotoTempFile(context: Context): File {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val timestamp = getTimestamp()
    return File.createTempFile(timestamp, ".jpg", storageDir)
}

fun uriToPhotoFile(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val tempFile = createPhotoTempFile(context)

    val inputStream = contentResolver.openInputStream(uri) as InputStream
    val outputStream = FileOutputStream(tempFile) as OutputStream
    val buffer = ByteArray(1024)
    var len: Int
    while (inputStream.read(buffer).also { len = it } > 0) outputStream.write(buffer, 0, len)
    outputStream.close()
    inputStream.close()

    return tempFile
}

fun compressPhoto(file: File, maxSize: Int): File {
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    do {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, stream)
        val streamByteArray = stream.toByteArray()
        streamLength = streamByteArray.size
        compressQuality -= 5
    } while (streamLength > maxSize)
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}