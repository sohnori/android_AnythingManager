package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.daeeun.sohnori.anythingmanager.R;
import com.daeeun.sohnori.anythingmanager.equipment.ThingPlugSKT;

public class MyProcess2Activity extends Activity {
	private static final String version = "V_1_0_1";
	private static EditText mEditLog, mEditResult;
	private static final int lOG_MAX_LENGTH = 4096;
	private final String mAppEUI = "0190691000000074";
	private final String mFixedDevEUI = "702c1ffffe";
	private final String mUkey = "YWxtdzNSdGVUV2U5UlBpWElreGNoajFQbXpJcWNuZGYzY1Fva2ZZbnU2WFdlZEo0NjUvTzNBZGJ3bUtJMUhSWA==";
	double mLatitude = 0;
	double mLongitude = 0;
	TextView mText;
	ThingPlugSKT mLoraModem;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_process2);
		mEditLog = (EditText)findViewById(R.id.act2editlog);
		mEditLog.setFilters(new InputFilter[] {
			new InputFilter.LengthFilter(lOG_MAX_LENGTH)
		});
		mText = (TextView)findViewById(R.id.act2editdeui);
		mText.setOnKeyListener(new View.OnKeyListener() {			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==KeyEvent.ACTION_DOWN) {
					switch(keyCode) {
					case KeyEvent.KEYCODE_ENTER:
						mOnClick((TextView)findViewById(R.id.act2btntest));
						return true;
					}
				}				
				return false;
			}
		});
		mEditResult = (EditText)findViewById(R.id.act2editresult);
	}
	public void mPrintLog(String str) {
		if(mEditLog.length()>=lOG_MAX_LENGTH) {
			new AlertDialog.Builder(this)
			.setTitle("로그창 넘침")
			.setMessage("로그창이 가득 찼습니다.\r\n비우거나 저장해야 합니다.")
			.setPositiveButton("저장", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mClearLog();
				}
			})
			.setNegativeButton("지우기", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mClearLog();
				}
			}).show();					
		}
		Editable edit = mEditLog.getText();
		edit.append(str);
		ScrollView scroll = (ScrollView)findViewById(R.id.act2scrv);	
		scroll.fullScroll(View.FOCUS_DOWN);
	}	
	public void mClearLog() {
		Editable edit = mEditLog.getText();
		edit.clear();
		
	}
	public void mPrintResult(String str) {		
		mEditResult.setText(str);
	}
	
	public void mClearResult() {
		Editable edit = mEditResult.getText();
		edit.clear();
	}
	public void mOnClick(View v){
		String str;		
		ScrollView scroll = (ScrollView)findViewById(R.id.act2scrv);	
		switch(v.getId()) {		
		case R.id.act2btntest:
			this.mClearResult();			
			str = mText.getText().toString();
			if(str.length()<6) {
				Toast.makeText(this, "유효하지 않은 모뎀 번호입니다.", Toast.LENGTH_SHORT).show();
				break;
			}
			mLoraModem = new ThingPlugSKT(this.mAppEUI, this.mFixedDevEUI+str, this.mUkey);
			mLoraModem.runThread(mEditLog, mEditResult);						
			scroll.fullScroll(View.FOCUS_DOWN);
			break;
		case R.id.act2btnposition:
			if(this.mLoraModem==null || this.mLoraModem.getLatitude()==0 || this.mLoraModem.getLongitude()==0) {
				Toast.makeText(this, "우선 로라모뎀 정보를 받아야 합니다.", Toast.LENGTH_SHORT).show();
				break;
			}
			this.mLatitude = this.mLoraModem.getLatitude();
			this.mLongitude = this.mLoraModem.getLongitude();
			try {
				String pos = String.format("geo:%f,%f?z=16", mLatitude, mLongitude);
				Uri uri = Uri.parse(pos);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			catch(Exception e) {
				this.mPrintLog(e.getMessage());
			}			
			break;
		case R.id.act2btnclear:
			this.mClearLog();
			this.mClearResult();			
			break;
		case R.id.act2editdeui:
			EditText edittxt = (EditText)findViewById(R.id.act2editdeui);
			Editable edit = edittxt.getText();
			edit.clear();
			break;
		}
		
	}
	@Override
	public void onResume() {
		super.onResume();
	}
	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onStop() {
		super.onStop();
	}
	@Override
	// 메뉴 생성 콜백 메서드
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	// 메뉴 수정 및 편집 메서드 - 메뉴가 열릴 때마다 호출된다.
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}
	@Override
	// 사용자가 선택한 메뉴항목(item)의 ID를 조사하고 적당한 명령을 수행 동작을 정의 하는 메서드
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id) {
		case R.id.action_version:
			Toast.makeText(this, version, Toast.LENGTH_LONG).show();
			return true;		
		case R.id.action_settings:
			return true;
		case R.id.action_exit:
			finish();
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onDestroy() {		
		super.onDestroy();
	}
}
