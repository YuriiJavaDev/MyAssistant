package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogAddTaskBinding;
import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskDeadlinePickerHelper;

import java.time.LocalDate;

public class TaskDialogFragment extends DialogFragment {

    private static final String ARG_TASK = "arg_task";
    private static final String ARG_INITIAL_TEXT = "arg_initial_text";

    // Updated interfaces to include custom reminder date
    public interface OnTaskSavedListener {
        void onTaskSaved(String title, String importance, LocalDate deadline, boolean remindSound, boolean showTimestamps, LocalDate customReminderDate);
    }

    public interface OnTaskUpdatedListener {
        void onTaskUpdated(Task task, String title, String importance, LocalDate deadline, boolean remindSound, boolean showTimestamps, LocalDate customReminderDate);
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted(Task task);
    }

    private Task task;
    private String initialText;
    private OnTaskSavedListener createListener;
    private OnTaskUpdatedListener updateListener;
    private OnTaskDeletedListener deleteListener;

    private TaskDeadlinePickerHelper deadlineHelper;

    public static TaskDialogFragment newInstance(@Nullable String initialText, OnTaskSavedListener listener) {
        TaskDialogFragment fragment = new TaskDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_TEXT, initialText);
        fragment.setArguments(args);
        fragment.createListener = listener;
        return fragment;
    }

    public static TaskDialogFragment newInstance(Task task, OnTaskUpdatedListener updateListener, OnTaskDeletedListener deleteListener) {
        TaskDialogFragment fragment = new TaskDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK, task);
        fragment.setArguments(args);
        fragment.updateListener = updateListener;
        fragment.deleteListener = deleteListener;
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment parent = getParentFragment();
        if (parent instanceof OnTaskSavedListener) {
            createListener = (OnTaskSavedListener) parent;
        }
        if (parent instanceof OnTaskUpdatedListener) {
            updateListener = (OnTaskUpdatedListener) parent;
        }
        if (parent instanceof OnTaskDeletedListener) {
            deleteListener = (OnTaskDeletedListener) parent;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = (Task) getArguments().getSerializable(ARG_TASK);
            initialText = getArguments().getString(ARG_INITIAL_TEXT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogAddTaskBinding binding = DialogAddTaskBinding.inflate(LayoutInflater.from(requireContext()));

        String[] importanceOptions = {"Normal", "Important", "Urgent"};
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, importanceOptions);
        importanceAdapter.setDropDownViewResource(R.layout.item_spinner);
        binding.dialogImportanceSpinner.setAdapter(importanceAdapter);
        binding.dialogImportanceSpinner.setPopupBackgroundResource(R.drawable.bg_spinner_dropdown);

        deadlineHelper = new TaskDeadlinePickerHelper(requireContext());
        boolean isEditMode = task != null;

        LocalDate initialDeadline = null;
        LocalDate initialCustomReminder = null;

        binding.dialogTitleTextView.setText(isEditMode ? "Edit Task" : "Add New Task");
        binding.btnSave.setText(isEditMode ? "Save" : "Add");

        if (isEditMode) {
            binding.dialogTaskEditText.setText(task.getTitle());
            binding.dialogShowTimestampsCheckBox.setChecked(task.isShowTimestamps());
            initialDeadline = task.getDeadline();
            // If your Task model has custom reminder field, fetch it here:
            initialCustomReminder = task.getCustomReminderDate();

            boolean hasDeadline = initialDeadline != null;
            binding.dialogRemindCheckBox.setEnabled(hasDeadline);
            binding.dialogRemindCheckBox.setChecked(hasDeadline && task.isRemindSoundOneDayBefore());

            if (task.getImportance() != null) {
                for (int i = 0; i < importanceOptions.length; i++) {
                    if (importanceOptions[i].equalsIgnoreCase(task.getImportance())) {
                        binding.dialogImportanceSpinner.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            if (initialText != null && !initialText.isEmpty()) {
                binding.dialogTaskEditText.setText(initialText);
            }
            binding.dialogShowTimestampsCheckBox.setChecked(true);

            binding.dialogRemindCheckBox.setEnabled(false);
            binding.dialogRemindCheckBox.setChecked(false);
            binding.dialogImportanceSpinner.setSelection(0);
        }

        deadlineHelper.setupDeadlinePicker(
                requireContext(),
                binding.dialogDeadlineTextView,
                binding.dialogRemindCheckBox,
                binding.dialogCustomReminderTextView,
                initialDeadline,
                initialCustomReminder
        );

        binding.dialogTaskEditText.setSelection(binding.dialogTaskEditText.getText().length());

        if (isEditMode && deleteListener != null) {
            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.btnDelete.setOnClickListener(v -> {
                DeleteConfirmationDialog.show(requireContext(), true, () -> {
                    deleteListener.onTaskDeleted(task);
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            });
        } else {
            binding.btnDelete.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot());

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        binding.btnSave.setOnClickListener(v -> {
            String title = binding.dialogTaskEditText.getText().toString().trim();
            String importance = binding.dialogImportanceSpinner.getSelectedItem().toString();
            LocalDate deadline = deadlineHelper.getSelectedDeadline();
            LocalDate customReminderDate = deadlineHelper.getCustomReminderDate(); // Retaining custom reminder date

            boolean remindSound = deadline != null && binding.dialogRemindCheckBox.isChecked();
            boolean showTimestamps = binding.dialogShowTimestampsCheckBox.isChecked();

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Task description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode && updateListener != null) {
                updateListener.onTaskUpdated(task, title, importance, deadline, remindSound, showTimestamps, customReminderDate);
                Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show();
            } else if (!isEditMode && createListener != null) {
                createListener.onTaskSaved(title, importance, deadline, remindSound, showTimestamps, customReminderDate);
                Toast.makeText(requireContext(), "Task created", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (deadlineHelper != null) {
            deadlineHelper.release();
        }
    }
}