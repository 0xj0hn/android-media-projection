package com.example.application.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.application.db.Task
import com.example.application.db.TaskDao
import kotlinx.coroutines.launch

class TasksViewModel(val taskDao: TaskDao) : ViewModel() {
    var taskName = MutableLiveData<String>("")
    val tasks = taskDao.getAll()
    val tasksString = tasks.map {
        tasks -> formatTasks(tasks)
    }
    private fun formatTasks(tasks: List<Task>): String {
        var result = ""
        tasks.forEach {
            result += "ID: ${it.taskId} | "
            result += "Name: ${it.taskName} | "
            result += "Is task done? ${it.taskDone}\n\n"
        }
        return result
    }

    fun saveTask() {
        println("HELLO FROM VIEWMODEL SCOPE")
        viewModelScope.launch {
            val task = Task(
                taskName = taskName.value!!,
                taskDone = false
            )
            taskDao.insertAll(task)

        }
    }

    fun removeTask(task: Task) {
        viewModelScope.launch {
            taskDao.delete(task)
        }
    }
}