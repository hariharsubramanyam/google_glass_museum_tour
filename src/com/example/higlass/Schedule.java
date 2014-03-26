package com.example.higlass;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Schedule {
	private Set<String> mPaintingIDs;
	private Context context;
	
	private final static String SETTINGS = "settings";
	private final static String SCHEDULE = "schedule";
	
	public Schedule(Context context) {
		this.mPaintingIDs = new HashSet<String>();
		this.context = context;
		SharedPreferences prefs = context.getSharedPreferences(Schedule.SETTINGS, Context.MODE_PRIVATE);
		String value = prefs.getString(Schedule.SCHEDULE, null);
		if(value != null){
			GsonBuilder gsonb = new GsonBuilder();
			Gson gson = gsonb.create();
			String[] items = gson.fromJson(value, String[].class);
			for(String item : items){
				this.mPaintingIDs.add(item);
			}
		}
	}
	
	public void addPainting(String paintingID){
		this.mPaintingIDs.add(paintingID);
		this.save();
	}
	
	public void removePainting(String paintingID){
		this.mPaintingIDs.remove(paintingID);
		this.save();
	}
	
	private void save(){
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		String value = gson.toJson(this.mPaintingIDs.toArray(new String[]{}));
		SharedPreferences prefs = this.context.getSharedPreferences(Schedule.SETTINGS, Context.MODE_PRIVATE);
		Editor e = prefs.edit();
		e.putString(Schedule.SCHEDULE, value);
		e.commit();
	}
}
