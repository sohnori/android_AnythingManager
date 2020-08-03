package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.daeeun.sohnori.anythingmanager.R;

public class MyProcess8Activity extends Activity {
	View mPage1, mPage2, mPage3;
	SoundPool mPool;
	int mDdok;
	AudioManager mAm;
	Vibrator mVib;
	Button.OnClickListener mClickListener = new Button.OnClickListener() {
		public void onClick(View v) {
			mPage1.setVisibility(View.INVISIBLE);
			mPage2.setVisibility(View.INVISIBLE);
			mPage3.setVisibility(View.INVISIBLE);
			switch (v.getId()) {
			case R.id.act8btntest:
				mPage1.setVisibility(View.VISIBLE);
				Toast.makeText(MyProcess8Activity.this, "잠시 나타나는 메시지", Toast.LENGTH_SHORT).show();
				mPool.play(mDdok, 1, 1, 0, 0, 1);
				mVib.vibrate(500);
				break;
			case R.id.act8btntest2:
				mPage2.setVisibility(View.VISIBLE);
				Toast.makeText(MyProcess8Activity.this, "조금 길게 나타나는 메시지", Toast.LENGTH_LONG).show();
				mAm.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
				mVib.vibrate(new long[] {100, 50, 200, 50}, 0);
				break;
			case R.id.act8btntest3:
				mPage3.setVisibility(View.VISIBLE);
				mVib.cancel();
				break;
			case R.id.chulsu:
				Toast.makeText(MyProcess8Activity.this, "철수 텍스트 클릭", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process8);
		
		mPage1 = findViewById(R.id.actp8test1);
		mPage2 = findViewById(R.id.actp8test2);
		mPage3 = findViewById(R.id.actp8test3);
		
		findViewById(R.id.act8btntest).setOnClickListener(mClickListener);
		findViewById(R.id.act8btntest2).setOnClickListener(mClickListener);
		findViewById(R.id.act8btntest3).setOnClickListener(mClickListener);
		findViewById(R.id.chulsu).setOnClickListener(mClickListener);
		mPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		mDdok = mPool.load(this, R.raw.ddok, 1);
		mAm = (AudioManager)getSystemService(AUDIO_SERVICE);
		
		mVib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	public void mOnClick(View v){
		finish();
	}
}
