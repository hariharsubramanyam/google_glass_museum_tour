package com.example.higlass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;

import android.content.Context;

public class GlobalData {
	private static GlobalData instance;
	private List<Artist> mArtists;
	private List<Painting> mPaintings;
	protected GlobalData(){}
	
	public static GlobalData getInstance(Context context){
		if(instance == null){
			instance = new GlobalData();
			ArtistContainer artistContainer = new Gson().fromJson(readAssets(context, "artist_data.json"), ArtistContainer.class);
			PaintingContainer paintingContainer = new Gson().fromJson(readAssets(context, "painting_data.json"), PaintingContainer.class);
			instance.setArtists(artistContainer.artists);
			instance.setPaintings(paintingContainer.paintings);
		}
		return instance;
	}
	
	public List<Artist> getArtists() {
		return this.mArtists;
	}

	public void setArtists(List<Artist> artists) {
		this.mArtists = artists;
	}

	public List<Painting> getPaintings() {
		return this.mPaintings;
	}

	public void setPaintings(List<Painting> paintings) {
		this.mPaintings = paintings;
	}

	public Artist getArtistForID(String artistID){
		for(Artist artist : this.getArtists()){
			if(artist.id.equals(artistID)){
				return artist;
			}
		}
		return null;
	}
	
	
	private static String readAssets(Context context, String asset_path){
		String output = "";
		try{
			BufferedReader reader = new BufferedReader(
			        new InputStreamReader(context.getAssets().open(asset_path)));
			    // do reading, usually loop until end of file reading  
			    String mLine = reader.readLine();
			    while (mLine != null) {
			    	output += mLine;
			    	mLine = reader.readLine(); 
			    }
			    reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return output;
	}
}
