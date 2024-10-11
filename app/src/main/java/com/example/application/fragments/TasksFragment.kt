package com.example.application.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.application.databinding.FragmentTasksBinding
import androidx.viewbinding.ViewBinding
import com.example.application.adapters.TaskItemAdapter
import com.example.application.db.AppDatabase
import com.example.application.db.Task

class TasksFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = TasksFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater)
        val view = binding.root
        val application = requireNotNull(this.activity).application
        val taskDao = AppDatabase.getInstance(application).taskDao()
        val viewModel = ViewModelProvider(this, TaskViewModelFactory(taskDao = taskDao))[TasksViewModel::class.java]
        val taskItemAdapter = TaskItemAdapter {task ->
            viewModel.removeTask(task)
        }
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tasks.adapter = taskItemAdapter
        viewModel.tasks.observe(viewLifecycleOwner, Observer {
            taskItemAdapter.submitList(it)
        })
        return view
    }
}