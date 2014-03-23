package com.example.higlass;

import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ArtistActivity extends Activity implements TextToSpeech.OnInitListener{
	
	private static final String TAG = ArtistActivity.class.getSimpleName();
	public static final String EXTRA_ARTIST_ID = "ARTIST_ID";
	private TextView mTextView;
	private String mBio;
	private TextToSpeech tts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize(this, APIKeys.PARSE_APP_ID, APIKeys.PARSE_APP_KEY);
		tts = new TextToSpeech(this,this);
		View v = getLayoutInflater().inflate(R.layout.activity_main, null, false);
		mTextView = (TextView)v.findViewById(R.id.txtMain);
		String artistID = getIntent().getExtras().getString(EXTRA_ARTIST_ID);
		Log.d(TAG, artistID);
		setContentView(v);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Artist");
		query.getInBackground(artistID, new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject object, ParseException ex) {
				if(ex == null){
					try{
						mTextView.setText(object.getString("Name"));
						byte[] imageBytes = ((ParseFile)(object.get("Picture"))).getData();
						Bitmap b = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
						((LinearLayout)findViewById(R.id.linear_main)).setBackgroundDrawable(new BitmapDrawable(getResources(),b));
						mBio = object.getString("Bio");
						speakOut(mBio);
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					ex.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
	}

	private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
	
	private void stopTTS(){
		if(tts != null){
			tts.stop();
			tts.shutdown();
		}
	}
	
	@Override
	protected void onDestroy() {
		stopTTS();
		super.onDestroy();
	}
	
}
