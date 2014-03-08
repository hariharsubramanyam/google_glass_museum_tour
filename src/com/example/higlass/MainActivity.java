package com.example.higlass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import pl.itraff.TestApi.ItraffApi.ItraffApi;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
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

public class MainActivity extends Activity implements TextToSpeech.OnInitListener{

	private static final String TAG = MainActivity.class.getSimpleName();
	
	private GestureDetector mGestureDetector;
	
	private static final String IMAGE_FILE_NAME = "/sdcard/ImageTest.jpg";
	
	private TextView mTextView;
	private TextView mDescriptionTextView;
	
	private TextToSpeech tts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "T5MX9poDX17FyRxpHcWY746uKEqyDRvqr7Ao6uGU", "xyZuqyacGLyGbYjvUqTEhmFmQx910xcnUXhMcqkv");
		super.onCreate(savedInstanceState);
		View v = getLayoutInflater().inflate(R.layout.activity_main, null, false);
		mTextView = (TextView)v.findViewById(R.id.txtMain);
		tts = new TextToSpeech(this,this);
		//mGestureDetector = createGestureDetector(this);
		setContentView(v);
		launchCameraIntent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if(mGestureDetector != null){
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1){
			mTextView.setText("Got an image");
			File f = new File(IMAGE_FILE_NAME);
			if(f.exists()){
				Bitmap b = BitmapFactory.decodeFile(IMAGE_FILE_NAME);
				ImageView image = (ImageView) findViewById(R.id.imgMain);
				image.setImageBitmap(b);
				if(b != null){
					if(ItraffApi.isOnline(this)){
						ItraffApi api = new ItraffApi(APIKeys.API_ID, APIKeys.API_KEY, TAG, true);
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
						byte[] pictureData = stream.toByteArray();
						api.sendPhoto(pictureData, itraffApiHandler, true);
					}
				}
			}
		}
	}
	
	private void onReceiveImageResponse(String response){
		final String id;
		try {
			id = getIDFromAPIResponse(new JSONObject(response));
			mTextView.setText(id);
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Painting");
			query.whereEqualTo("RecognizeID", id);
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> displayList, ParseException e) {
					if(e == null){
						if(displayList.size() > 0){
							String displayName = displayList.get(0).getString("PaintingName");
							String description = displayList.get(0).getString("PaintingDescription");
							byte[] imageBytes;
							try {
								imageBytes = ((ParseFile)(displayList.get(0).get("PaintingFile"))).getData();
								Bitmap b = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
								ImageView image = (ImageView) findViewById(R.id.imgMain);
								image.setImageBitmap(b);
								mTextView.setText(displayName);
								speakOut(description);
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
	
	private GestureDetector createGestureDetector(Context context){
		GestureDetector gestureDetector = new GestureDetector(context);
		gestureDetector.setBaseListener( new BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if(gesture == Gesture.TAP){
					//openOptionsMenu();
					launchCameraIntent();
					return true;
				}
				return false;
			}
		});
		return gestureDetector;
	}
	
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
		if(tts != null){
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
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

}
