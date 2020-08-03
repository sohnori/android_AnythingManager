package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daeeun.sohnori.anythingmanager.R;

public class MyProcess12Activity extends Activity {
	private static final String VERSION = "V_1_0_0";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process12);			
	}
	public static class CounterFragment extends Fragment{		
		public static CounterFragment newInstance(int initValue) {
			CounterFragment cf = new CounterFragment();
			
			Bundle args = new Bundle();
			args.putInt("start", initValue);
			cf.setArguments(args);
			return cf;
		} // 초기값 설정
		
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.act12_fragmenttest, container, false);
			return root;
		}
		public void onActivityCreated(Bundle savedInstance) {			
			super.onActivityCreated(savedInstance);
			Button btnIncrease = (Button)getView().findViewById(R.id.act12btntest);
			final TextView textCounter=(TextView)getView().findViewById(R.id.act12txtcounter);			
			final EditText edit = (EditText)getView().findViewById(R.id.act12editresult); 
			int start = 0;
			Bundle args = getArguments();
			if(savedInstance!=null) {
				textCounter.setText(Integer.toString(savedInstance.getInt("counter")));
			}
			else if(args!=null) {
				start = args.getInt("start");
				textCounter.setText(Integer.toString(start));
			}
			
			btnIncrease.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					int count = Integer.parseInt(textCounter.getText().toString());
					textCounter.setText(Integer.toString(count+1));
					Editable ed = edit.getText();
					ed.append(Integer.toString(count+1)+"\r\n");
				}
			});
		}
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			
			TextView textCounter = (TextView)getView().findViewById(R.id.act12txtcounter);
			int value = Integer.parseInt(textCounter.getText().toString());
			outState.putInt("counter", value);
		}
	}	
	public void mOnClick(View v){
		FragmentManager fm = getFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.act12frame);
		switch(v.getId()) {
		case R.id.act12btnhome:
			finish();
			break;
		case R.id.act12btnfragadd:
			if(fragment==null) {
				FragmentTransaction tr = fm.beginTransaction();
				CounterFragment cf = CounterFragment.newInstance(5);
				tr.add(R.id.act12frame, cf, "counter");
				tr.commit();
				//fm.beginTransaction().add(R.id.act12frame, cf, "counter").commit();
			}
			else {
				Toast.makeText(this, "이미 존재합니다.", 0).show();
			}
			break;
		case R.id.act12btnfragremove:
			if(fragment==null) Toast.makeText(this, "대상이 존재하지 않습니다.", 0).show();
			else {
				FragmentTransaction tr = fm.beginTransaction();
				tr.remove(fragment);
				tr.commit();
			}
			break;
		case R.id.act12btnfragreplace:			
			break;
		case R.id.act12btnfraghide:
			if(fragment==null) Toast.makeText(this, "대상이 존재하지 않습니다.", 0).show();
			else {
				FragmentTransaction tr = fm.beginTransaction();
				if(fragment.isHidden()) tr.show(fragment);				
				else tr.hide(fragment);
				tr.commit();
			}			
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
			Toast.makeText(this, VERSION, Toast.LENGTH_LONG).show();
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
