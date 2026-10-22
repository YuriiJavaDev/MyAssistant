package com.yurii.pavlenko.myassistant.about.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.about.data.DeveloperContact;
import com.yurii.pavlenko.myassistant.about.data.FeedbackHistoryStore;
import com.yurii.pavlenko.myassistant.about.logic.FeedbackCheck;
import com.yurii.pavlenko.myassistant.about.logic.FeedbackLimiter;
import com.yurii.pavlenko.myassistant.about.logic.FeedbackRecord;
import com.yurii.pavlenko.myassistant.about.logic.FeedbackText;
import com.yurii.pavlenko.myassistant.databinding.DialogFeedbackBinding;

/** Lets the user write a message to the developer; the limits keep the mailbox of the developer free of floods. */
public class FeedbackDialog extends DialogFragment {

    private DialogFeedbackBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogFeedbackBinding.inflate(LayoutInflater.from(requireContext()));
        binding.etFeedback.setFilters(new InputFilter[]{new InputFilter.LengthFilter(FeedbackLimiter.MAX_LENGTH)});
        binding.cbIncludeDetails.setChecked(savedInstanceState == null
                || savedInstanceState.getBoolean("include_details", true));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        binding.btnFeedbackCancel.setOnClickListener(v -> dismiss());
        binding.btnFeedbackSend.setOnClickListener(v -> send());
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("include_details", binding.cbIncludeDetails.isChecked());
    }

    private void send() {
        String text = String.valueOf(binding.etFeedback.getText());
        long now = System.currentTimeMillis();
        FeedbackHistoryStore store = new FeedbackHistoryStore(requireContext());
        FeedbackCheck check = FeedbackLimiter.check(text, now, store.load(now));
        if (!check.isAllowed()) {
            binding.tilFeedback.setError(FeedbackErrorText.of(requireContext(), check));
            return;
        }
        if (!FeedbackMailer.open(requireContext(), text, binding.cbIncludeDetails.isChecked())) {
            binding.tilFeedback.setError(getString(R.string.feedback_no_mail_app, DeveloperContact.EMAIL));
            return;
        }
        store.add(new FeedbackRecord(now, FeedbackText.normalize(text)), now);
        Toast.makeText(requireContext(), R.string.feedback_opened_mail, Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
