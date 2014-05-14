package com.example.higlass;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;


public class TextToSpeechController implements OnInitListener {
	private static final String TAG = "TextToSpeechController";
    private TextToSpeech myTTS;
    private String textToSpeak;
    private Context context;

    private static TextToSpeechController singleton;

    public static TextToSpeechController getInstance(Context ctx) {
        if (singleton == null)
            singleton = new TextToSpeechController(ctx);
        return singleton;
    }

    private TextToSpeechController(Context ctx) {
        context = ctx;
    }

    public void speak(String text) {
        textToSpeak = text;

        if (myTTS == null) {
            // currently can't change Locale until speech ends
            try {
                // Initialize text-to-speech. This is an asynchronous operation.
                // The OnInitListener (second argument) is called after
                // initialization completes.
                myTTS = new TextToSpeech(context, this);

            } catch (Exception e) {             
                e.printStackTrace();
            }
        }

        sayText();

    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            if (myTTS.isLanguageAvailable(Locale.UK) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.UK);
        }

        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (initStatus == TextToSpeech.SUCCESS) {
            int result = myTTS.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS missing or not supported (" + result + ")");
                // Language data is missing or the language is not supported.
                // showError(R.string.tts_lang_not_available);

            } else {
                // Initialization failed.
                Log.e(TAG, "Error occured");
            }

        }
    }

    private void sayText() {
        // ask TTs to say the text
        myTTS.speak(this.textToSpeak, TextToSpeech.QUEUE_FLUSH,     null);
    }

    public void stopTTS() {
        if (myTTS != null) {
            myTTS.shutdown();
            myTTS.stop();
            myTTS = null;
        }
    }
}
