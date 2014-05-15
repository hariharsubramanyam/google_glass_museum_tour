/**
 * Activity for displaying information about an artist and reading his/her biography out loud
 * @author Harihar Subramanyam
 */

package com.example.higlass;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.Parse;

public class ArtistActivity extends Activity implements OnInitListener{
	
	/**
	 * Tag for logging
	 */
	private static final String TAG = ArtistActivity.class.getSimpleName();
	
	
	/**
	 * When this Activity is created, it receives an extra called "ARTIST_ID"
	 */
	public static final String EXTRA_ARTIST_ID = "ARTIST_ID";
	
	/**
	 * Title text view which displays the artist name
	 */
	private TextView mTextView;
	
	/**
	 * String which contains the artist's biography (spoken aloud by the text-to-speech service)
	 */
	private String mBio;
	
	/**
	 * The text to speech for reading the user's location
	 */
	private TextToSpeech tts;

	
	/**
	 * When the activity is created,
	 * display the artist's picture, display his/her name,
	 * and read aloud his/her biography
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up Parse (we use it to retrieve the artist's name and biography)
		Parse.initialize(this, APIKeys.PARSE_APP_ID, APIKeys.PARSE_APP_KEY);
		
		// Inflate the view and get the text field
		View v = getLayoutInflater().inflate(R.layout.activity_main, null, false);
		mTextView = (TextView)v.findViewById(R.id.txtMain);
		
		// From the intent that created this activity, retrieve the ARTIST_ID
		String artistID = getIntent().getExtras().getString(EXTRA_ARTIST_ID);
		
		// Display the view
		setContentView(v);
		
		// Retrieve the artist from the global data store
		GlobalData globalData = GlobalData.getInstance(this);
		Artist artist = globalData.getArtistForID(artistID);
		
		if(tts == null){
			tts = new TextToSpeech(this, this);
		}
		
		// Set the fields
		mTextView.setText(artist.name);
		mBio = artist.bio;
		int drawableId = getResources().getIdentifier(artist.picture.replace(".jpeg", ""), "drawable", getPackageName());
		Log.d(TAG, artist.picture.replace(".jpeg", ""));
		findViewById(R.id.linear_main).setBackgroundResource(drawableId);	
		// Read aloud the biography
	}
	
	@Override
	protected void onDestroy() {
		if (tts != null) {
	        tts.stop();
	        tts.shutdown();
	    }
		super.onDestroy();
	}
	
	private void speakIt(String someThing) {
		tts.speak(someThing, TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	public void onInit(int status) {
	    if (status == TextToSpeech.SUCCESS) {

	        if (tts == null) {
	            tts = new TextToSpeech(this, this);
	            tts.setSpeechRate(0.8f);
	        }
	        int result = tts.setLanguage(Locale.ENGLISH);

	        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	            Log.e("TTS", "This Language is not supported");
	            Intent installIntent = new Intent();
	            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installIntent);
	        }
	        
	        speakIt(mBio);
	    }
	}
	
}
