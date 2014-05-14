/**
 * Activity for displaying information about an artist and reading his/her biography out loud
 * @author Harihar Subramanyam
 */

package com.example.higlass;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.Parse;

public class ArtistActivity extends Activity{
	
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
		
		// Set the fields
		mTextView.setText(artist.name);
		mBio = artist.bio;
		int drawableId = getResources().getIdentifier(artist.picture.replace(".jpeg", ""), "drawable", getPackageName());
		Log.d(TAG, artist.picture.replace(".jpeg", ""));
		findViewById(R.id.linear_main).setBackgroundResource(drawableId);	
		// Read aloud the biography
		TextToSpeechController.getInstance(this).speak(mBio);
	}
	
	@Override
	protected void onDestroy() {
		TextToSpeechController.getInstance(this).stopTTS();
		super.onDestroy();
	}
	
}
