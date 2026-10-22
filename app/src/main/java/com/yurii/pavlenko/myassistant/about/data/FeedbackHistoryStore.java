package com.yurii.pavlenko.myassistant.about.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.yurii.pavlenko.myassistant.about.logic.FeedbackLimiter;
import com.yurii.pavlenko.myassistant.about.logic.FeedbackRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Remembers on this phone which feedback messages were sent, for the limits on how many and how alike they may be. */
public final class FeedbackHistoryStore {

    private static final String PREFERENCES = "feedback_history";
    private static final String KEY_RECORDS = "records";
    private static final String KEY_TIME = "t";
    private static final String KEY_TEXT = "x";
    private static final int MAX_RECORDS = 20;

    private final SharedPreferences preferences;

    public FeedbackHistoryStore(@NonNull Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    /** The messages sent in the last month, older ones no longer matter for any limit. */
    @NonNull
    public List<FeedbackRecord> load(long nowMillis) {
        List<FeedbackRecord> records = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(KEY_RECORDS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                long sentAt = item.getLong(KEY_TIME);
                if (nowMillis - sentAt < FeedbackLimiter.MONTH_MILLIS) {
                    records.add(new FeedbackRecord(sentAt, item.getString(KEY_TEXT)));
                }
            }
        } catch (JSONException e) {
            records.clear();
        }
        return records;
    }

    public void add(@NonNull FeedbackRecord record, long nowMillis) {
        List<FeedbackRecord> records = load(nowMillis);
        records.add(record);
        while (records.size() > MAX_RECORDS) {
            records.remove(0);
        }
        JSONArray array = new JSONArray();
        try {
            for (FeedbackRecord item : records) {
                array.put(new JSONObject()
                        .put(KEY_TIME, item.getSentAtMillis())
                        .put(KEY_TEXT, item.getNormalizedText()));
            }
        } catch (JSONException e) {
            return;
        }
        preferences.edit().putString(KEY_RECORDS, array.toString()).apply();
    }
}
