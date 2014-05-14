package com.example.higlass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.BaseListener;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ScheduleActivity extends Activity{
	
	private final static String TAG = ScheduleActivity.class.getSimpleName();
	
	public static final String EXTRA_SCHEDULE_INDEX = "ScheduleIndex";
	
	private Schedule mSchedule;
	
	/**
	 * Displays the painting name and number in schedule
	 */
	private TextView mTextView;
	
	/**
	 * The gesture detector
	 * 		displays options menu when the user taps
	 * 		on swipe forward/back, it moves through the schedule
	 */
	private GestureDetector mGestureDetector;
	
	/**
	 * Current index into schedule
	 */
	private int mIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, APIKeys.PARSE_APP_ID, APIKeys.PARSE_APP_KEY);
		super.onCreate(savedInstanceState);
		mGestureDetector = createGestureDetector(this);
		View v = getLayoutInflater().inflate(R.layout.activity_schedule, null);
		this.mTextView = (TextView)v.findViewById(R.id.txt_schedule_item);
		
		
		if(getIntent() != null){
			this.mIndex = getIntent().getExtras().getInt(ScheduleActivity.EXTRA_SCHEDULE_INDEX, 0);
		}else{
			this.mIndex = 0;
		}
		setContentView(v);
		
		this.mSchedule = new Schedule(this);
		String paintingID = this.mSchedule.getPainting(this.mIndex);
		this.mTextView.setText("Adding painting to schedule...");
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Painting");
		query.getInBackground(paintingID, new GetCallback<ParseObject>() {
		  public void done(ParseObject object, ParseException e) {
		    if (e == null) {
		    	mTextView.setText(object.getString("PaintingName"));
		    	byte[] imageBytes;
				try {
					imageBytes = ((ParseFile)(object.get("PaintingFile"))).getData();
					Bitmap b = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
					((LinearLayout)findViewById(R.id.linear_schedule_background)).setBackgroundDrawable(new BitmapDrawable(getResources(),b));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				
		    } else {
		      e.printStackTrace();
		    }
		  }
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.schedule_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_delete_schedule){
			new Schedule(this).clearAllPaintings();
			this.finish();
		}else if(item.getItemId() == R.id.action_delete_schedule_item){
			Schedule schedule = new Schedule(this);
			String paintingName = schedule.getPainting(this.mIndex);
			schedule.removePainting(paintingName);
			Intent intent = new Intent(this, ScheduleActivity.class);
			intent.putExtra(ScheduleActivity.EXTRA_SCHEDULE_INDEX, Math.max(0,mIndex + 1));
			intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}
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
	 * On swipe forward/back, it moves through the schedule
	 */
	private GestureDetector createGestureDetector(Context context){
		final Activity activity = this;
		GestureDetector gestureDetector = new GestureDetector(context);
		gestureDetector.setBaseListener( new BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if(gesture == Gesture.TAP){
					openOptionsMenu();
					return true;
				}else if(gesture == Gesture.SWIPE_RIGHT || gesture == Gesture.SWIPE_LEFT){
					int delta = (gesture == Gesture.SWIPE_RIGHT) ? 1 : -1;
					Intent intent = new Intent(activity, ScheduleActivity.class);
					intent.putExtra(ScheduleActivity.EXTRA_SCHEDULE_INDEX, Math.min(Math.max(0,mIndex + delta), mSchedule.numPaintings()-1));
					intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
					startActivity(intent);
				}
				return false;
			}
		});
		return gestureDetector;
	}
	
	
}
