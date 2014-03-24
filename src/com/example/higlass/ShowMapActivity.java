package com.example.higlass;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.BaseListener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ShowMapActivity extends Activity{
	
	/**
	 * The image that displays the map of the museum
	 */
	ImageView mImg;
	
	/**
	 * The gesture detector (displays the options menu when the user taps)
	 */
	private GestureDetector mGestureDetector;
	
	/**
	 * When the activity is created, display the map
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = getLayoutInflater().inflate(R.layout.activity_map, null);
		mGestureDetector = createGestureDetector(this);
		setContentView(v);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if(mGestureDetector != null){
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}
	
	/**
	 * On a tap, display the options menu
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
}
