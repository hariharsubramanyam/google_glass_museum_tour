package com.example.higlass;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.glass.app.Card;

public class TestActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GlobalData globalData = GlobalData.getInstance(this);
		Card card = new Card(this);
		card.setText(globalData.getArtists().get(0).name + " and " + globalData.getPaintings().get(0).name);
		setContentView(card.toView());
	}

}
