package com.example.higlass;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ShowMapActivity extends Activity{
	ImageView mImg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = getLayoutInflater().inflate(R.layout.activity_map, null);
		mImg = (ImageView)v.findViewById(R.id.img_map);
		mImg.setImageResource(R.drawable.map);
		setContentView(v);
	}
}
