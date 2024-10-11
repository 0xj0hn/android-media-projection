package com.example.application.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    val taskId: Long = 0L,
    @ColumnInfo(name = "task_name")
    val taskName: String = "",
    @ColumnInfo(name = "task_done")
    val taskDone: Boolean = false,
)
