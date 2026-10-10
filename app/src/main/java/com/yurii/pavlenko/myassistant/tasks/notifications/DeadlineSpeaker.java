package com.yurii.pavlenko.myassistant.tasks.notifications;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

/** Speaks the overdue-deadline announcement with Text-to-Speech. */
public final class DeadlineSpeaker {

    private static final String UTTERANCE_ID = "deadline_alert";

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
        textToSpeech = new TextToSpeech(context.getApplicationContext(), this::onInit);
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

    /** Speaks right away, or as soon as the engine is initialized. */
    public synchronized void speak(int dueTasksCount) {
        String phrase = "Attention! You have " + dueTasksCount + " task deadlines requiring immediate attention.";
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
        if (status != TextToSpeech.SUCCESS || !isEnglishAvailable()) {
            notifyFinished();
            return;
        }
        ready = true;
        if (pendingPhrase != null) {
            say(pendingPhrase);
            pendingPhrase = null;
        }
    }

    private boolean isEnglishAvailable() {
        int result = textToSpeech.setLanguage(Locale.US);
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