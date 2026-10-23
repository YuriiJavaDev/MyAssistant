package com.yurii.pavlenko.myassistant.about.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.about.logic.InfoBlock;
import com.yurii.pavlenko.myassistant.ui.theme.ThemeColors;

/** Turns the blocks of an information page into views that are comfortable to read on a phone. */
final class InfoBlockViews {

    private static final float HEADING_SP = 18;
    private static final float TEXT_SP = 15;
    private static final float LINE_SPACING = 1.25f;
    private static final int HEADING_TOP_DP = 20;
    private static final int PARAGRAPH_TOP_DP = 8;
    private static final int BULLET_TOP_DP = 6;
    private static final int BULLET_INDENT_DP = 20;

    private final Context context;
    private final int textColor;
    private final int accentColor;

    InfoBlockViews(@NonNull Context context) {
        this.context = context;
        this.textColor = ThemeColors.get(context, R.attr.maText);
        this.accentColor = ThemeColors.get(context, R.attr.maAccent);
    }

    @NonNull
    View create(@NonNull InfoBlock block) {
        switch (block.getType()) {
            case HEADING:
                return heading(block.getText());
            case BULLET:
                return bullet(block.getText());
            default:
                return paragraph(block.getText());
        }
    }

    private View heading(String text) {
        TextView view = text(text, HEADING_SP, accentColor);
        view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        view.setLayoutParams(params(HEADING_TOP_DP));
        return view;
    }

    private View paragraph(String text) {
        TextView view = text(text, TEXT_SP, textColor);
        view.setLayoutParams(params(PARAGRAPH_TOP_DP));
        return view;
    }

    private View bullet(String text) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(params(BULLET_TOP_DP));

        TextView mark = text("•", TEXT_SP, accentColor);
        mark.setGravity(Gravity.START);
        row.addView(mark, new LinearLayout.LayoutParams(dp(BULLET_INDENT_DP), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView body = text(text, TEXT_SP, textColor);
        row.addView(body, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private TextView text(String text, float sp, int color) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTextColor(color);
        view.setLineSpacing(0, LINE_SPACING);
        view.setTextIsSelectable(true);
        return view;
    }

    private LinearLayout.LayoutParams params(int topDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(topDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }
}
