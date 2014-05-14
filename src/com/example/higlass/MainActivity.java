/**
 * Activity for automatically recognizing a painting, displaying it, and reading a description of it out loud
 * @author Harihar Subramanyam
 */

package com.example.higlass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pl.itraff.TestApi.ItraffApi.ItraffApi;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.BaseListener;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends Activity{

	/**
	 * Use the class name as a tag when logging
	 */
	private static final String TAG = MainActivity.class.getSimpleName();
	
	/**
	 * The gesture detector detects a tap so that we can display the context menu
	 */
	private GestureDetector mGestureDetector;
	
	/**
	 * The user takes a picture which must be recognized, IMAGE_FILE_NAME is where the picture is stored on the device
	 */
	private static final String IMAGE_FILE_NAME = "/sdcard/ImageTest.jpg";
	
	
	/**
	 * Text view for displaying the name of the painting
	 */
	private TextView mTextView;
	
	/**
	 * The description of the painting (read aloud by the text to speech service)
	 */
	private String mDescription;
	
	/**
	 * The ID of the artist (retrieved so that the user can click the "Artist" option from the options menu and see info about the artist)
	 */
	private String mArtistID;
	
	/**
	 * The ID of the painting (retreived so that the user can click the "Save" option from the options menu and add it to their schedule)
	 */
	private String mPaintingID;
	
	/**
	 * When the activity is created, set up the gesture detector and text to speech, then take a picture of whatever the user is looking at
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Initialize Parse (we'll need it to look up information about the painting)
		Parse.initialize(this, APIKeys.PARSE_APP_ID, APIKeys.PARSE_APP_KEY);
		
		// Inflate the view and retrieve its text field
		View v = getLayoutInflater().inflate(R.layout.activity_main, null, false);
		mTextView = (TextView)v.findViewById(R.id.txtMain);
		
		// Set up the gesture recognizer
		mGestureDetector = createGestureDetector(this);
		
		// Set the view
		setContentView(v);
		
		// Launch the camera and take a picture of whatever the user is looking at (the callback is onActivityResult)
		launchCameraIntent();
	}

	/**
	 * The menu displays the following options
	 * Repeat - read the painting description again
	 * Artist - show a picture of the artist and read aloud his/her biography
	 * Related - show paintings related to this one
	 * Save - save this painting to my itinerary
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recognize_main, menu);
		return true;
	}
	
	/**
	 * Handle menu selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_repeat){
			// Read the description again
			TextToSpeechController.getInstance(this).speak(mDescription);
		}else if(item.getItemId() == R.id.action_artist){
			// Launch an intent to get information about the artist with given ID
			Intent intent = new Intent(this, ArtistActivity.class);
			intent.putExtra(ArtistActivity.EXTRA_ARTIST_ID, mArtistID);
			startActivity(intent);
		}else if(item.getItemId() == R.id.action_save){
			new Schedule(this).addPainting(this.mPaintingID);
			Intent intent = new Intent(this, ScheduleActivity.class);
			intent.putExtra(ScheduleActivity.EXTRA_SCHEDULE_INDEX, 0);
			intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}
		return true;
	}
	
	/**
	 * Lump all motion into one event
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if(mGestureDetector != null){
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}
	
	/**
	 * When the GlassSnapshotActivity returns a picture,
	 * recognize the image using the Recognize.im API
	 * then get the info for the recognized image from Parse
	 * and display the info on the screen
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1){
			// IMAGE_FILE_NAME is where the image is stored
			File f = new File(IMAGE_FILE_NAME);
			if(f.exists()){
				// Convert the file to a bitmap
				Bitmap b = BitmapFactory.decodeFile(IMAGE_FILE_NAME);
				if(b != null){
					// Set the background of the screen to be the image
					((LinearLayout)findViewById(R.id.linear_main)).setBackgroundDrawable(new BitmapDrawable(getResources(),b));
					// If the ItraffApi (i.e. the Recognize.im API) is available,
					if(ItraffApi.isOnline(this)){
						// prepare the photo and send it to the API for recognition (the callback is onReceiveImageResponse)
						ItraffApi api = new ItraffApi(APIKeys.ITRAFF_API_ID, APIKeys.ITRAFF_API_KEY, TAG, true);
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
						byte[] pictureData = stream.toByteArray();
						api.sendPhoto(pictureData, itraffApiHandler, true);
					}
				}
			}
		}
	}
	
	/**
	 * When the Recognize.im API returns a response, look up the image on Parse and display it to the user
	 */
	private void onReceiveImageResponse(String response){
		final String id;
		final Context ctx = this;
		try {
			// Get the image ID
			id = getIDFromAPIResponse(new JSONObject(response));
			mTextView.setText("Processing...");
			
			// Query Parse and get the info for the painting with the given id
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Painting");
			query.whereEqualTo("RecognizeID", id);
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> displayList, ParseException e) {
					if(e == null){
						if(displayList.size() > 0){
							// Get the info about the painting
							String displayName = displayList.get(0).getString("PaintingName");
							mDescription = displayList.get(0).getString("PaintingDescription");
							mArtistID = displayList.get(0).getString("ArtistID");
							mPaintingID = displayList.get(0).getObjectId();
							byte[] imageBytes;
							try {
								// Set the painting as the background of the screen
								imageBytes = ((ParseFile)(displayList.get(0).get("PaintingFile"))).getData();
								Bitmap b = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
								((LinearLayout)findViewById(R.id.linear_main)).setBackgroundDrawable(new BitmapDrawable(getResources(),b));
								
								// Display the painting's name
								mTextView.setText(displayName);
								
								// Read aloud the description of the painting
								TextToSpeechController.getInstance(ctx).speak(mDescription);
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
							
						}else{
							mTextView.setText("No matches for " + id);
						}
					}else{
						mTextView.setText("Could not match image");
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Launch the GlassSnapshotActivity to get a picture from the camera
	 */
	private void launchCameraIntent(){
		Intent intent = new Intent(this, GlassSnapshotActivity.class);
		intent.putExtra("imageFileName", IMAGE_FILE_NAME);
		intent.putExtra("previewWidth", 640);
		intent.putExtra("previewHeight", 360);
		intent.putExtra("snapshotWidth", 1280);
		intent.putExtra("snapshotHeight", 720);
		intent.putExtra("maximumWaitTimeForCamera", 2000);
		startActivityForResult(intent, 1);
	}
	
	/**
	 * Create a gesture detector that opens the options menu whenever the user taps
	 */
	private GestureDetector createGestureDetector(Context context){
		GestureDetector gestureDetector = new GestureDetector(context);
		gestureDetector.setBaseListener( new BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if(gesture == Gesture.TAP){
					openOptionsMenu();
					return true;
				}
				return false;
			}
		});
		return gestureDetector;
	}
	
	/**
	 * Process the data that comes back from the Recognize.im API and call the handler 
	 */
	@SuppressLint("HandlerLeak")
	private Handler itraffApiHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			Bundle data = msg.getData();
			if(data != null){
				Integer status = data.getInt(ItraffApi.STATUS, -1);
				String response = data.getString(ItraffApi.RESPONSE);
				if(status.intValue() == 0){
					onReceiveImageResponse(response);
				}
			}
		};
	};
	
	/**
	 * The Recognize.im API returns a JSON object with the painting's ID - retreive that ID
	 */
	public String getIDFromAPIResponse(JSONObject json){
		final String JSON_ID = "id";
		final String JSON_STATUS = "status";
		try{
			int status = (int)json.getDouble(JSON_STATUS);
			if(status == 0){
				return json.getJSONArray(JSON_ID).get(0).toString();
			}else{
				return null;
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onDestroy() {
		TextToSpeechController.getInstance(this).stopTTS();
		super.onDestroy();
	}

}
