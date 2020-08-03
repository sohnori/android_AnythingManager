package com.daeeun.sohnori.anythingmanager.fragment;

import com.daeeun.sohnori.anythingmanager.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class ConnectionFragment extends Fragment {
	private final int TERMINAL_NONE = 0;
	private final int TERMINAL_SERIAL = 1;
	private final int TERMINAL_TCPCLIENT = 2;
	private final int TERMINAL_TCPSERVER = 3;
	private final int TERMINAL_UDPCLIENT = 4;
	private final int TERMINAL_UDPSERVER = 5;
	private int mTerminalSpinIndex = 0;
	private final OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {
			switch(parent.getId()) {
			case R.id.fragconn_spinterminal:
				mTerminalSpinIndex = position;
				setTerminalLayout();
				break;
			case R.id.fragconn_spindev:
				
				break;
			case R.id.fragconn_spinbr:
				
				break;
			case R.id.fragconn_spindbs:
				
				break;
			case R.id.fragconn_spinparity:
				
				break;
			case R.id.fragconn_spinstop:
				
				break;
			case R.id.fragconn_spinflow:
				
				break;
			case R.id.fragconn_spintimeout:
				//mTerminal.mIndexTimeoutSpin = position;
				break;
			}				
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub				
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.frag_conn, container, false);		
		return root;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mInitSerialSet();
		final LinearLayout linearConn = (LinearLayout)getView().findViewById(R.id.fragconn_layoutconn);
		final Button minBtn = (Button)getView().findViewById(R.id.fragconn_btnminimize);
		minBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(minBtn.getText().equals("1")==true) {
					minBtn.setText("0");
					linearConn.setVisibility(View.GONE);
				}
				else {
					minBtn.setText("1");
					linearConn.setVisibility(View.VISIBLE);
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
	private void mInitSerialSet() {
		ArrayAdapter<CharSequence> adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.terminal, android.R.layout.simple_spinner_item);
		Spinner spin = (Spinner)getView().findViewById(R.id.fragconn_spinterminal);
		spin.setAdapter(adt);
		spin.setSelection(0);
		spin.setOnItemSelectedListener(mOnItemSelectedListener);
		adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.baudrate, android.R.layout.simple_spinner_item);
		spin = (Spinner)getView().findViewById(R.id.fragconn_spinbr);
		spin.setAdapter(adt);
		spin.setSelection(3);
		adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.databit, android.R.layout.simple_spinner_item);
		spin = (Spinner)getView().findViewById(R.id.fragconn_spindbs);
		spin.setAdapter(adt);
		spin.setSelection(3);
		adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.paritybit, android.R.layout.simple_spinner_item);
		spin = (Spinner)getView().findViewById(R.id.fragconn_spinparity);
		spin.setAdapter(adt);
		spin.setSelection(0);
		adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.stopbit, android.R.layout.simple_spinner_item);
		spin = (Spinner)getView().findViewById(R.id.fragconn_spinstop);
		spin.setAdapter(adt);
		spin.setSelection(0);
		adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.flow, android.R.layout.simple_spinner_item);
		spin = (Spinner)getView().findViewById(R.id.fragconn_spinflow);
		spin.setAdapter(adt);
		spin.setSelection(0);
		adt = ArrayAdapter.createFromResource(getView().getContext(), R.array.timeout, android.R.layout.simple_spinner_item);
		spin = (Spinner)getView().findViewById(R.id.fragconn_spintimeout);
		spin.setAdapter(adt);
		spin.setSelection(0);
	}
	public void setTerminalLayout() {
		View vSerial = getView().findViewById(R.id.fragconn_layoutserial);
		if(this.mTerminalSpinIndex==TERMINAL_NONE) {
			vSerial.setVisibility(View.GONE);
		}
		else if(this.mTerminalSpinIndex==TERMINAL_SERIAL) {
			vSerial.setVisibility(View.VISIBLE);
		}
		else if(this.mTerminalSpinIndex==TERMINAL_TCPCLIENT) {
			vSerial.setVisibility(View.GONE);
		}
		else if(this.mTerminalSpinIndex==TERMINAL_TCPSERVER) {
			vSerial.setVisibility(View.GONE);
		}
		else if(this.mTerminalSpinIndex==TERMINAL_UDPCLIENT) {
			vSerial.setVisibility(View.GONE);
		}
		else if(this.mTerminalSpinIndex==TERMINAL_UDPSERVER) {
			vSerial.setVisibility(View.GONE);
		}
		else {
			vSerial.setVisibility(View.GONE);
		}
		
	}
}
