package com.yurii.pavlenko.myassistant.about.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yurii.pavlenko.myassistant.about.logic.InfoBlock;
import com.yurii.pavlenko.myassistant.about.logic.InfoMarkup;
import com.yurii.pavlenko.myassistant.databinding.ActivityInfoBinding;

/** One page of text to read: the guide, the privacy notes or the license. It works without internet. */
public class InfoActivity extends AppCompatActivity {

    private static final String EXTRA_SECTION = "extra_section";

    public static Intent createIntent(@NonNull Context context, @NonNull InfoSection section) {
        return new Intent(context, InfoActivity.class).putExtra(EXTRA_SECTION, section.name());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInfoBinding binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InfoSection section = InfoSection.valueOf(getIntent().getStringExtra(EXTRA_SECTION));
        binding.tvInfoTitle.setText(section.title);
        binding.btnInfoClose.setOnClickListener(v -> finish());

        InfoBlockViews views = new InfoBlockViews(this);
        addBlocks(binding.infoContent, views, section.texts);
        if (section.moreTexts != 0) {
            addBlocks(binding.infoContent, views, section.moreTexts);
        }
    }

    private void addBlocks(ViewGroup parent, InfoBlockViews views, @ArrayRes int texts) {
        for (InfoBlock block : InfoMarkup.parse(getResources().getStringArray(texts))) {
            parent.addView(views.create(block));
        }
    }
}
