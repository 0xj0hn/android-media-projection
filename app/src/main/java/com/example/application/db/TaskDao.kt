package com.example.application.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert
    suspend fun insertAll(vararg tasks: Task)

    @Query("SELECT * FROM tasks")
    fun getAll(): LiveData<List<Task>>

    @Delete
    suspend fun delete(task: Task)
}