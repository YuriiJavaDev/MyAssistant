package com.yurii.pavlenko.myassistant.tasks.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yurii.pavlenko.myassistant.databinding.ItemTaskBinding;
import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private final OnTaskCheckedListener checkedListener;
    private final OnTaskClickListener clickListener;

    public interface OnTaskCheckedListener {
        void onTaskChecked(Task task, boolean isChecked);
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(OnTaskCheckedListener checkedListener, OnTaskClickListener clickListener) {
        this.checkedListener = checkedListener;
        this.clickListener = clickListener;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TaskViewHolder(binding);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, checkedListener, clickListener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;

        public TaskViewHolder(@NonNull ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Task task, OnTaskCheckedListener checkedListener, OnTaskClickListener clickListener) {
            binding.taskTitleTextView.setText(task.getTitle());
            binding.taskCheckBox.setOnCheckedChangeListener(null);
            binding.taskCheckBox.setChecked(task.isCompleted());

            TaskStyleHelper.applyCompletionStyle(binding.taskTitleTextView, task.isCompleted());
            TaskStyleHelper.applyImportanceColor(binding.taskTitleTextView, task.getImportance());
            TaskTimeFormatter.formatTimestamps(binding.taskTimestampTextView, task);

            binding.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (checkedListener != null) {
                    checkedListener.onTaskChecked(task, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTaskClick(task);
                }
            });
        }
    }
}