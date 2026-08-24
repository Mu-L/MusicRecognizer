package com.mrsep.musicrecognizer.core.database

import androidx.room3.ColumnTypeConverter
import java.io.File

internal class FileRoomConverter {

    @ColumnTypeConverter
    fun stringToFile(filepath: String): File {
        return File(filepath)
    }

    @ColumnTypeConverter
    fun fileToString(file: File): String {
        return file.absolutePath
    }
}
