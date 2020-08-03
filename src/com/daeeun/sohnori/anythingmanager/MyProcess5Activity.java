package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.daeeun.sohnori.anythingmanager.R;

public class MyProcess5Activity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process5);
	}
	public void mOnClick(View v){
		finish();
	}
}
