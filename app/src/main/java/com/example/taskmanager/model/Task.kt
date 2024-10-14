package com.example.taskmanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var priority: String = "Low",
    var dueDate: Long = 0L,
    var isCompleted: Boolean = false,
    var reminderTime: Long = 0L
) {
    fun dueDateFormatted(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(this.dueDate))
    }
}
