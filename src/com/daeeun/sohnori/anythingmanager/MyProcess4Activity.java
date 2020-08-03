package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.daeeun.sohnori.anythingmanager.R;
import com.daeeun.sohnori.anythingmanager.fragment.CommandFragment;
import com.daeeun.sohnori.anythingmanager.fragment.ConnectionFragment;
import com.daeeun.sohnori.anythingmanager.fragment.LogFragment;
import com.daeeun.sohnori.anythingmanager.fragment.SettingFragment;

public class MyProcess4Activity extends Activity {
	private static final String version = "V_1_0_0";
	boolean mMenuConnFlag = true;
	boolean mMenuLogFlag = true;
	boolean mMenuCmdFlag = true;
	boolean mMenuSettingFlag = true;
	LogFragment mLogFrag;
	EditText mEditLog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process4);
		FragmentManager fm = getFragmentManager();		
		FragmentTransaction ftr = fm.beginTransaction();
		SettingFragment setFrag = new SettingFragment();
		CommandFragment cmdFrag = new CommandFragment();
		ConnectionFragment connFrag = new ConnectionFragment();
		mLogFrag = new LogFragment();
		ftr.add(R.id.act4frame1, connFrag, "connection");		
		ftr.add(R.id.act4frame2, cmdFrag, "command");
		ftr.add(R.id.act4frame3, mLogFrag, "log");
		ftr.add(R.id.act4frame4, setFrag, "setting");
		ftr.commit();		
	}
	public void mOnClick(View v){
		finish();
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
		getMenuInflater().inflate(R.menu.act4menu, menu);
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
		FragmentManager fm = getFragmentManager();
		Fragment fragment;
		FragmentTransaction ftr;
		int id = item.getItemId();
		switch(id) {
		case R.id.act4menu_version:
			Toast.makeText(this, version, Toast.LENGTH_LONG).show();
			return true;
		case R.id.act4menu_conn:
			fragment = fm.findFragmentById(R.id.act4frame1);
			if(fragment==null) return true;
			ftr = fm.beginTransaction();
			if(item.isChecked()==true) {
				item.setChecked(false);
				this.mMenuConnFlag = false;
				ftr.hide(fragment).commit();
			}
			else {
				item.setChecked(true);
				this.mMenuConnFlag = true;
				ftr.show(fragment).commit();
			}
			return true;
		case R.id.act4menu_cmd:
			fragment = fm.findFragmentById(R.id.act4frame2);
			if(fragment==null) return true;
			ftr = fm.beginTransaction();
			if(item.isChecked()==true) {
				item.setChecked(false);
				this.mMenuCmdFlag = false;
				ftr.hide(fragment).commit();
			}
			else {
				item.setChecked(true);
				this.mMenuCmdFlag = true;
				ftr.show(fragment).commit();
			}
			return true;
		case R.id.act4menu_log:
			fragment = fm.findFragmentById(R.id.act4frame3);
			if(fragment==null) return true;
			ftr = fm.beginTransaction();
			if(item.isChecked()==true) {
				item.setChecked(false);
				this.mMenuLogFlag = false;
				ftr.hide(fragment).commit();
			}
			else {
				item.setChecked(true);
				this.mMenuLogFlag = true;
				ftr.show(fragment).commit();
			}
			return true;
		case R.id.act4menu_setting:
			fragment = fm.findFragmentById(R.id.act4frame4);
			if(fragment==null) return true;
			ftr = fm.beginTransaction();
			if(item.isChecked()==true) {
				item.setChecked(false);
				this.mMenuSettingFlag = false;
				ftr.hide(fragment).commit();
			}
			else {
				item.setChecked(true);
				this.mMenuSettingFlag = true;
				ftr.show(fragment).commit();
			}
			return true;
		case R.id.act4menu_exit:
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
