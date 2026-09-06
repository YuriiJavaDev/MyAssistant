package com.yurii.pavlenko.myassistant.adapter;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.model.tasks.Task;

import java.time.format.DateTimeFormatter;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, checkedListener, clickListener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox taskCheckBox;
        private final TextView taskTitleTextView;
        private final TextView taskTimestampTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskTitleTextView = itemView.findViewById(R.id.taskTitleTextView);
            taskTimestampTextView = itemView.findViewById(R.id.taskTimestampTextView);
        }

        public void bind(Task task, OnTaskCheckedListener checkedListener, OnTaskClickListener clickListener) {
            taskTitleTextView.setText(task.getTitle());
            taskCheckBox.setOnCheckedChangeListener(null);
            taskCheckBox.setChecked(task.isCompleted());

            applyCompletionStyle(task.isCompleted());
            applyImportanceColor(task.getImportance());
            formatTimestamps(task);

            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                applyCompletionStyle(isChecked);
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

        private void applyCompletionStyle(boolean isCompleted) {
            if (isCompleted) {
                taskTitleTextView.setPaintFlags(taskTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                taskTitleTextView.setPaintFlags(taskTitleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }

        private void applyImportanceColor(String importance) {
            if (importance == null) {
                importance = "Normal";
            }
            switch (importance.toLowerCase()) {
                case "urgent":
                    taskTitleTextView.setTextColor(Color.parseColor("#D32F2F")); // Red
                    break;
                case "important":
                    taskTitleTextView.setTextColor(Color.parseColor("#FA8C16")); // Brownish-orange
                    break;
                case "normal":
                default:
                    taskTitleTextView.setTextColor(Color.parseColor("#3E2773")); // Standard app text color
                    break;
            }
        }

        private void formatTimestamps(Task task) {
            StringBuilder sb = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            if (task.getCreatedAt() != null) {
                sb.append("Created: ").append(task.getCreatedAt().format(formatter));
            }
            if (task.getUpdatedAt() != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("Edited: ").append(task.getUpdatedAt().format(formatter));
            }
            if (task.isCompleted() && task.getCompletedAt() != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("Completed: ").append(task.getCompletedAt().format(formatter));
            }

            taskTimestampTextView.setText(sb.toString());
        }
    }
}