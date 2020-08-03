package com.daeeun.sohnori.anythingmanager.fragment;

import com.daeeun.sohnori.anythingmanager.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class CommandFragment extends Fragment {
	public enum CMD_SENDTYPE {
		HEX, ASCII
	}
	RadioGroup.OnCheckedChangeListener mRgListener = new RadioGroup.OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if(group.getId()==R.id.fragcmd_rgcmdtype) {
				switch(checkedId) {
				case R.id.fragcmd_radiohex:
					mCmdSendType = CommandFragment.CMD_SENDTYPE.HEX;
					break;
				case R.id.fragcmd_radioascii:
					mCmdSendType = CommandFragment.CMD_SENDTYPE.ASCII;
					break;
				}
			}
			if(group.getId()==R.id.fragcmd_rgcrc) {
				ArrayAdapter<CharSequence> adt;
				switch(checkedId) {
				case R.id.fragcmd_radiocrc8:
					adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.crc8, android.R.layout.simple_spinner_item);
					mSpinCksum.setAdapter(adt);
					mSpinCksum.setSelection(0);					
					break;
				case R.id.fragcmd_radiocrc16:
					adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.crc16, android.R.layout.simple_spinner_item);
					mSpinCksum.setAdapter(adt);
					mSpinCksum.setSelection(0);	
					break;
				case R.id.fragcmd_radiocrc32:
					adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.crc32, android.R.layout.simple_spinner_item);
					mSpinCksum.setAdapter(adt);
					mSpinCksum.setSelection(0);	
					break;
				case R.id.fragcmd_radiouserdefined:
					adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.userdefined, android.R.layout.simple_spinner_item);
					mSpinCksum.setAdapter(adt);
					mSpinCksum.setSelection(0);	
					break;
				}
			}
		}
	};
	CommandFragment.CMD_SENDTYPE mCmdSendType = CommandFragment.CMD_SENDTYPE.HEX;
	Spinner mSpinCksum;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.frag_cmd, container, false);		
		return root;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);		
		this.mSpinCksum = (Spinner)getView().findViewById(R.id.fragcmd_spincksum);
		final LinearLayout linearCksum = (LinearLayout)getView().findViewById(R.id.fragcmd_layoutcksum);
		linearCksum.setVisibility(View.GONE);
		final LinearLayout linearCmdOption = (LinearLayout)getView().findViewById(R.id.fragcmd_layoutcmdoption);		
		RadioGroup crcGroup = (RadioGroup)getView().findViewById(R.id.fragcmd_rgcrc);
		crcGroup.setOnCheckedChangeListener(mRgListener);
		RadioGroup cmdTypeGroup = (RadioGroup)getView().findViewById(R.id.fragcmd_rgcmdtype);
		cmdTypeGroup.setOnCheckedChangeListener(mRgListener);
		final Button sendBtn = (Button)getView().findViewById(R.id.fragcmd_btnsend);
		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
			
		});
		EditText cmdEdit = (EditText)getView().findViewById(R.id.fragcmd_editcmd);
		cmdEdit.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==KeyEvent.ACTION_DOWN) {
					switch(keyCode){
					case KeyEvent.KEYCODE_ENTER:
						sendBtn.callOnClick();
						return true;
					}
				}
				return false;
			}
		});
		final Button minBtn = (Button)getView().findViewById(R.id.fragcmd_btnminimize);
		minBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				if(minBtn.getText().equals("1")==true) {
					minBtn.setText("0");
					linearCmdOption.setVisibility(View.GONE);
				}
				else {
					minBtn.setText("1");
					linearCmdOption.setVisibility(View.VISIBLE);
				}
			}
			
		});
		final CheckBox cksumCkBox = (CheckBox)getView().findViewById(R.id.fragcmd_ckcksum);
		cksumCkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked==true) {
					linearCksum.setVisibility(View.VISIBLE);
				}
				else {
					linearCksum.setVisibility(View.GONE);
				}
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
	public CommandFragment.CMD_SENDTYPE getCommandSendType(){
		return mCmdSendType;
	}
}
