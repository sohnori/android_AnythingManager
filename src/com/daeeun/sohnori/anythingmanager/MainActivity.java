package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.daeeun.sohnori.anythingmanager.R;

public class MainActivity extends Activity{
	private static final String version = "프로그램 버전 V_1_0_2"; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
	}

	public void mOnClick(View v) {
		Intent intent;
		switch(v.getId()) {
		case R.id.btnProcess1:
			intent = new Intent(this, MyProcess1Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess2:
			intent = new Intent(this, MyProcess2Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess3:
			intent = new Intent(this, MyProcess3Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess4:
			intent = new Intent(this, MyProcess4Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess5:
			intent = new Intent(this, MyProcess5Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess6:
			intent = new Intent(this, MyProcess6Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess7:
			intent = new Intent(this, MyProcess7Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess8:
			intent = new Intent(this, MyProcess8Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess9:
			intent = new Intent(this, MyProcess9Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess10:
			intent = new Intent(this, MyProcess10Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess11:
			intent = new Intent(this, MyProcess11Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess12:
			intent = new Intent(this, MyProcess12Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess13:
			intent = new Intent(this, MyProcess13Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess14:
			intent = new Intent(this, MyProcess14Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess15:
			intent = new Intent(this, MyProcess15Activity.class);
			startActivity(intent);
			break;
		case R.id.btnProcess16:
			intent = new Intent(this, MyProcess16Activity.class);
			startActivity(intent);
			break;
			
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id==R.id.action_version) {
			Toast.makeText(this, version, Toast.LENGTH_LONG).show();
			return true;
		}
		if(id == R.id.action_settings) {
			return true;
		}
		if(id == R.id.action_exit) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

