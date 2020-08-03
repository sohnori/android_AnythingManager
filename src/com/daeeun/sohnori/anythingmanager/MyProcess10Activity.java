package com.daeeun.sohnori.anythingmanager;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.daeeun.sohnori.anythingmanager.R;

public class MyProcess10Activity extends Activity {	
	int value = 0;
	TextView mText;
	TextView mText2;
	Button mBtninvtype;
	Button mBtn;
	private static final String TAG = "LogTest";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process10);
		
		mText = (TextView)findViewById(R.id.act10txt);
		mText2 = (TextView)findViewById(R.id.act10txt2);
		mBtn = (Button)findViewById(R.id.act10btnck);
		mHandler.sendEmptyMessage(0);
		new CountDownTimer(5*1000,1000) {
			public void onTick(long millisUntilFinished) {
				mText2.setText("Value = " + value);
				if(value==100) {
					cancel();
				}
			}
			public void onFinish() {
				Log.v(TAG, "onFinish");
			}
		}.start();
		Log.v(TAG, "onCreate");		
	}
	
	public void mOnClick(View v){
		Log.v(TAG, "mOnClick");
		PopupMenu popup = new PopupMenu(this, v);
		switch(v.getId()) {
		case R.id.act10btnhome:
			finish();
			break;
		case R.id.act10btntype:
			Toast.makeText(this, "인버터를 선택하세요.", Toast.LENGTH_SHORT).show();
			MenuInflater inflater = popup.getMenuInflater();
			Menu menu = popup.getMenu();
			inflater.inflate(R.menu.act10menuinv, menu);
			//popup.inflate(R.menu.act1menu); // 위의 세 줄 코드를 하나로 압축하여 표현
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					// TODO Auto-generated method stub
					switch(item.getItemId()) {
					case R.id.inv_dass:
						mBtninvtype = (Button)findViewById(R.id.act10btntype);
						mBtninvtype.setText("다쓰테크");						
						break;
					case R.id.inv_e_p3:
						mBtninvtype = (Button)findViewById(R.id.act10btntype);
						mBtninvtype.setText("동양 E&P3");
						break;
					case R.id.inv_hans:
						mBtninvtype = (Button)findViewById(R.id.act10btntype);
						mBtninvtype.setText("한솔");
						break;
					case R.id.inv_hexp:
						mBtninvtype = (Button)findViewById(R.id.act10btntype);
						mBtninvtype.setText("헥스파워");
						break;
					case R.id.inv_ecos:
						mBtninvtype = (Button)findViewById(R.id.act10btntype);
						mBtninvtype.setText("동이에코스");
						break;
					default:
						
						break;
					}
					return false;
				}
			});
			popup.show();
			break;
		case R.id.act10btnsptest:
			int i;
			int a, b=123, c=456;
			long start, end;
			start = System.currentTimeMillis();
			for(i=0;i<500000000;i++) {
				a=b+c;				
			}
			end = System.currentTimeMillis();
			
			TextView result = (TextView)findViewById(R.id.act10txtresult);
			String sres = "덧셈 5억번에 총 "+(end-start)/1000.0 + " 초가 걸렸습니다.";
			result.setText(sres);
			break;
//		case R.id.act10btnck:
//			popup.inflate(R.menu.act10menu2);
//			popup.show();
//			break;
		}
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			value++;
			mText.setText("Value = " + value);
			mHandler.sendEmptyMessageDelayed(0, 1000);
		}
	};	
	@Override
	// 메뉴 생성 콜백 메서드
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.act10menu2, menu);
		Log.v(TAG, "onCreateOptionMenu");
		return true;
	}
	// 메뉴 수정 및 편집 메서드 - 메뉴가 열릴 때마다 호출된다.
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v(TAG, "onPrepareOptionMenu");
		if(mBtn.getTextSize()==40) {
			menu.findItem(R.id.act10bigfont).setChecked(true);
		} else {
			menu.findItem(R.id.act10bigfont).setChecked(false);
		}
		
		int color = mBtn.getTextColors().getDefaultColor();
		 if(color==Color.RED) menu.findItem(R.id.act10red).setChecked(true);
		 if(color==Color.GREEN) menu.findItem(R.id.act10green).setChecked(true);
		 if(color==Color.BLUE) menu.findItem(R.id.act10blue).setChecked(true);
		 if(color==Color.YELLOW) menu.findItem(R.id.act10yellow).setChecked(true);
		 // 메뉴의 교체가 필요할때 아래 메서드를 이용하여 메뉴를 무효화하고 재설정 할 수 있다.
		 // 아래 메서드를 실행하면 메뉴생성콜백 메서드가 재실행된다.
		 // void invalidateOptionMenu();
		return true;
	}
	@Override
	// 사용자가 선택한 메뉴항목(item)의 ID를 조사하고 적당한 명령을 수행 동작을 정의 하는 메서드
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Log.v(TAG, "onOptionsItemSelected");
		int id = item.getItemId();			
		switch(id) {
		case R.id.act10bigfont:
			if(item.isChecked()) {
				mBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
			} else {
				mBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40);
			}
			return true;
		case R.id.act10red:
			mBtn.setTextColor(Color.RED);
			return true;
		case R.id.act10green:
			mBtn.setTextColor(Color.GREEN);
			return true;
		case R.id.act10blue:
			mBtn.setTextColor(Color.BLUE);
			return true;
		case R.id.act10yellow:
			mBtn.setTextColor(Color.YELLOW);
			return true;
		}
//		if (id == R.id.action_settings) {
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}
}
