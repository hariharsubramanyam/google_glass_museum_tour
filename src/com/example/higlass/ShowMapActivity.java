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
	ImageView mImg;
	private GestureDetector mGestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = getLayoutInflater().inflate(R.layout.activity_map, null);
		mImg = (ImageView)v.findViewById(R.id.img_map);
		mImg.setImageResource(R.drawable.map);
		mGestureDetector = createGestureDetector(this);
		setContentView(v);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.recognize_main, menu);
		return true;
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
