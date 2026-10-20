package com.yurii.pavlenko.myassistant.tasks.notifications;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;


import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.ui.locale.AppLocale;

import java.util.Locale;

/** Speaks the overdue-deadline announcement with Text-to-Speech. */
public final class DeadlineSpeaker {

    private static final String UTTERANCE_ID = "deadline_alert";

    private final Context context;
    private final TextToSpeech textToSpeech;
    private final Runnable onFinished;
    private boolean ready;
    private boolean finished;
    private boolean shutDown;
    private String pendingPhrase;

    /**
     * @param onFinished called once when the speech is over or cannot be played; may be null
     */
    public DeadlineSpeaker(Context context, Runnable onFinished) {
        this.onFinished = onFinished;
        this.context = context.getApplicationContext();
        textToSpeech = new TextToSpeech(this.context, this::onInit);
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                notifyFinished();
            }

            @Override
            public void onError(String utteranceId) {
                notifyFinished();
            }
        });
    }

    /** Announces the number of overdue tasks. */
    public void speak(int dueTasksCount) {
        speak(context.getResources().getQuantityString(R.plurals.tasks_alert_speech_count, dueTasksCount, dueTasksCount));
    }

    /** Speaks right away, or as soon as the engine is initialized. */
    public synchronized void speak(String phrase) {
        if (ready) {
            say(phrase);
        } else {
            pendingPhrase = phrase;
        }
    }

    public synchronized void shutdown() {
        if (!shutDown) {
            shutDown = true;
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private synchronized void onInit(int status) {
        if (status != TextToSpeech.SUCCESS || !isLanguageAvailable()) {
            notifyFinished();
            return;
        }
        ready = true;
        if (pendingPhrase != null) {
            say(pendingPhrase);
            pendingPhrase = null;
        }
    }

    private boolean isLanguageAvailable() {
        return isAvailable(textToSpeech.setLanguage(AppLocale.current()))
                || isAvailable(textToSpeech.setLanguage(Locale.getDefault()));
    }

    private static boolean isAvailable(int result) {
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
    }

    private void say(String phrase) {
        textToSpeech.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
    }

    private synchronized void notifyFinished() {
        if (!finished) {
            finished = true;
            if (onFinished != null) {
                onFinished.run();
            }
        }
    }
}