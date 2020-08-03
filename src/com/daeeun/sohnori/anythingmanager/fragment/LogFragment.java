package com.daeeun.sohnori.anythingmanager.fragment;

import com.daeeun.sohnori.anythingmanager.R;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

public class LogFragment extends Fragment {
	EditText mEditLog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.frag_log, container, false);		
		return root;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final FrameLayout frame = (FrameLayout)getView().findViewById(R.id.fraglog_layoutframe);
		final Button minBtn = (Button)getView().findViewById(R.id.fraglog_btnminimize);
		final Button clearBtn = (Button)getView().findViewById(R.id.fraglog_btnclear);
		mEditLog = (EditText)getView().findViewById(R.id.fraglog_editlog);
		minBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				if(minBtn.getText().equals("1")==true) {
					minBtn.setText("0");
					frame.setVisibility(View.GONE);
				}
				else {
					minBtn.setText("1");
					frame.setVisibility(View.VISIBLE);
				}
			}
			
		});
		clearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Editable editable = mEditLog.getText();
				editable.clear();
			}
			
		});
	}
	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	public EditText getLogWindowID() {
		return mEditLog;
	}
}
