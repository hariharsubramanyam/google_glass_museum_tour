package com.example.higlass;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Schedule {
	private final static String TAG = Schedule.class.getSimpleName();
	
	private List<String> mPaintingIDs;
	private Context context;
	
	private final static String SETTINGS = "settings";
	private final static String SCHEDULE = "schedule";
	
	public Schedule(Context context) {
		this.mPaintingIDs = new ArrayList<String>();
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
	
	public void clearAllPaintings(){
		this.mPaintingIDs.clear();
		this.save();
	}
	
	public void addPainting(String paintingID){
		if(this.mPaintingIDs.contains(paintingID)){
			return;
		}
		this.mPaintingIDs.add(paintingID);
		this.save();
	}
	
	public void removePainting(String paintingID){
		this.mPaintingIDs.remove(paintingID);
		this.save();
	}
	
	public String getPainting(int index){
		index = Math.min(Math.max(0, index), this.mPaintingIDs.size()-1);
		return this.mPaintingIDs.get(index);
	}
	
	public int numPaintings(){
		return this.mPaintingIDs.size();
	}
	
	private void save(){
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		String value = gson.toJson(this.mPaintingIDs.toArray(new String[]{}));
		SharedPreferences prefs = this.context.getSharedPreferences(Schedule.SETTINGS, Context.MODE_PRIVATE);
		Editor e = prefs.edit();
		e.clear();
		e.putString(Schedule.SCHEDULE, value);
		e.commit();
	}
}
