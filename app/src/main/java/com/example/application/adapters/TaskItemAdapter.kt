package com.example.application.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.application.R
import com.example.application.db.Task

class TaskItemAdapter(val onClickListener: (task: Task) -> Unit) : ListAdapter<Task, TaskItemAdapter.TaskItemViewHolder>(object :
    DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.taskId == newItem.taskId
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }

}) {
    class TaskItemViewHolder(val rootView: TextView, val onClickListener: (taskId: Task) -> Unit) : RecyclerView.ViewHolder(rootView) {
        companion object {
            fun inflateFrom(parent: ViewGroup, onClickListener: (task: Task) -> Unit): TaskItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.task_item, parent, false) as TextView
                return TaskItemViewHolder(view, onClickListener)
            }
        }

        fun bind(item: Task) {
            rootView.text = item.taskName
            rootView.setOnClickListener { this.onClickListener(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        val view = TaskItemViewHolder.inflateFrom(parent, onClickListener)
        return view
    }

    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}