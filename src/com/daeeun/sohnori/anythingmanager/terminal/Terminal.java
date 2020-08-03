package com.daeeun.sohnori.anythingmanager.terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.daeeun.sohnori.anythingmanager.R;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class Terminal {
	public static final String ACTION_USB_PERMISSION = "com.daeeun.sohnori.anythingmanager.USB_PERMISSION";	
	List<String> mUsbList;
	List<UsbDevice> mDevList;
	List<UsbSerialDriver> mAvailableDrivers;
	Iterator<UsbDevice> mDevIter;	
	public Spinner mSpinDev, mSpinBr, mSpinData, mSpinParity, mSpinStop, mSpinFlow, mSpinTimeout;
	private UsbDeviceConnection mConnection;
	private UsbSerialPort mPort;
	private UsbDevice mDevice;
	private SerialInputOutputManager mSerialIoManager;
	private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	//private SerialInputOutputManager.Listener mSerialInputOutputManagerListener;
	private final SerialInputOutputManager.Listener mSerialIOManagerListener = new SerialInputOutputManager.Listener() {	
		@Override
		public void onRunError(Exception e) {
			// TODO Auto-generated method stub				
		}		
		@Override
		public void onNewData(final byte[] data) {
			// TODO Auto-generated method stub				
			for(int cnt=0;cnt<data.length;cnt++) {
				mRxDataBuffer[mRxDataBufferPointer] = data[cnt];					
				mRxDataBufferPointer++;
				if(mRxDataBufferPointer==RXDATA_BUFFER_SIZE) mRxDataBufferPointer=0;
			}			
		}
	};
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			SimpleDateFormat simpleDate;
			String message;
			Editable editLog = mEditLog.getText();
			switch(msg.what) {
			case 0:									
				mCntClassBuff = 0;				
				while(mRxDataBufferIndex!=mRxDataBufferPointer) {					
					mClassBuffer[mCntClassBuff] = mRxDataBuffer[mRxDataBufferIndex];
					mRxDataBufferIndex++;
					mCntClassBuff++;
					if(mRxDataBufferIndex==RXDATA_BUFFER_SIZE) mRxDataBufferIndex=0;
				}
				simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
				message = simpleDate.format(new Date()) + "Received " + mCntClassBuff + " bytes: \n"
				        + HexDump.dumpHexString(mClassBuffer, mCntClassBuff) + "\r\n";				
				editLog.append(message);
				mThreadExecuteFlag = false;
				break;				
			}			
		}
	};
	private OnItemSelectedListener mItemSelectedListener;
	private	DialogInterface.OnClickListener mOnClcikListener;
	private	LinearLayout mLinearForDlg;
	private AlertDialog mDlg;
	private PendingIntent mPermissionIntent;
	public int mIndexDevSpin;
	public int mIndexBaudrateSpin;
	public int mIndexDatabitsSpin;
	public int mIndexParitySpin;
	public int mIndexStopSpin;
	public int mIndexFlowSpin;
	public int mIndexTimeoutSpin;
	private String mDevName = " ";
	private String mDriverName = " ";
	private String mMsg = " ";
	private static final int RXDATA_BUFFER_SIZE = 2048;
	private static final int CLASS_BUFFER_SIZE = 512;
	private byte[] mRxDataBuffer = new byte[RXDATA_BUFFER_SIZE];
	private int mRxDataBufferPointer = 0;
	private int mRxDataBufferIndex = 0;
	private byte[] mClassBuffer = new byte[CLASS_BUFFER_SIZE];
	int mCntClassBuff;	
	private Thread mThread;
	private EditText mEditLog;
	private boolean mThreadExecuteFlag;
	public Terminal(Context context, EditText log, OnItemSelectedListener listen1, DialogInterface.OnClickListener listen2) {		
		mUsbList = new ArrayList<String>();
		mDevList = new ArrayList<UsbDevice>();		
		mItemSelectedListener = listen1;
		mOnClcikListener = listen2;
		mLinearForDlg = (LinearLayout)View.inflate(context, R.layout.dialog_terminal, null);
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		mIndexDevSpin = 0;
		mIndexBaudrateSpin = 3;
		mIndexDatabitsSpin = 3;
		mIndexParitySpin = 0;
		mIndexStopSpin = 0;
		mIndexFlowSpin = 0;
		mIndexTimeoutSpin = 0;
		mThreadExecuteFlag = false;
		mEditLog = log;
		mDlg = new AlertDialog.Builder(context)
				.setTitle("설정하시오.")
				.setIcon(R.drawable.outline_arrow_forward_white_48dp)
				.setView(mLinearForDlg)
				.setPositiveButton("확인", mOnClcikListener)
				.setNegativeButton("취소",null)
				.create();
		this.mThread = new ReceiveProcessThread(mHandler);
		this.mThread.setDaemon(true);		
		this.mThread.start();
	}
	
	class ReceiveProcessThread extends Thread{
		Handler imHandler;
		ReceiveProcessThread(Handler handler){
			imHandler = handler;
		}
		public void run() {
			while(true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(mRxDataBufferPointer!=mRxDataBufferIndex && mThreadExecuteFlag==false) {
					mThreadExecuteFlag=true;
					imHandler.sendEmptyMessageDelayed(0, 200);					
				}
			}
		}			
	}
			
	public void initTerminal() {
		mIndexDevSpin = 0;
		mIndexBaudrateSpin = 3;
		mIndexDatabitsSpin = 3;
		mIndexParitySpin = 0;
		mIndexStopSpin = 0;
		mIndexFlowSpin = 0;
		mIndexTimeoutSpin = 0;
		mDevName = " ";
		mDriverName = " ";
		mMsg = " ";
	}	
	
	public void initReceivedData() {
		this.mCntClassBuff = 0;
	}
	
	public boolean isReceivedData() {
		if(this.mCntClassBuff>0) return true;
		else return false;
	}
	
	public int getReceivedDataCnt() {
		return this.mCntClassBuff;
	}
	
	public String getObject() {
		return this.toString();
	}
	
	public byte[] getReceicedData() {
		byte[] buff = new byte[mCntClassBuff];
		for(int cnt=0;cnt<this.mCntClassBuff;cnt++) {
			buff[cnt] = this.mClassBuffer[cnt];
		}
		return buff;
	}
	
	public PendingIntent getPendingIntent() {
		return this.mPermissionIntent;
	}
	
	public String getDevName() {
		return this.mDevName;
	}
	
	public String getDriverName() {
		return this.mDriverName;
	}
	
	public String getMessage() {
		return this.mMsg;
	}
	
	public boolean openPort(UsbManager manager) {
		try {								
			int position = mSpinDev.getSelectedItemPosition() - 1;								
			if(position<=-1) {
				mMsg = "Invalid Device.";
				return false;								
			}
			mDevice = mDevList.get(position);
			this.mDevName = mDevice.getDeviceName();			
			manager.requestPermission(mDevice, mPermissionIntent);								
			mAvailableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
			this.mDriverName = mAvailableDrivers.toString();			
			UsbSerialDriver driver = mAvailableDrivers.get(0);							
			mConnection = manager.openDevice(mDevice);								
			if(mConnection==null) {
				mMsg = "faild connection.";
				return false;														
			}
			mPort = driver.getPorts().get(0);								
			mPort.open(mConnection);
			this.mMsg = "장치연결성공: "+mConnection.getClass().toString()+"\r\n";			
			mPort.setParameters(
					Integer.parseInt(mSpinBr.getSelectedItem().toString()), 
					mIndexDatabitsSpin+5, 
					mIndexStopSpin+1, 
					mIndexParitySpin);								
			this.startIoManager();								
			//mPort.write("hello~~!".getBytes(), 300);								
		}
		catch(Exception e) {
			mMsg = "에러:"+e.getMessage();
			return false;
		}
		return true;
	}
	
	public void closePort() {
		if(mPort==null) return ;
		try {
			mPort.close();
			mPort=null;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block			
			e.printStackTrace();
		}
	}
	
	public int writeBytes(byte[] src, int timeout) {
		if(this.isConnectedPort()!=true) {
			this.mMsg = "Invalid port.";
			return -1;
		}
		try {
			mPort.write(src, timeout);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.mMsg = e.getMessage();
		}
		return src.length;
	}
	
	public boolean isConnectedPort() {
		if(mPort!=null) return true;
		return false;
	}
	
	public void closeConnection() {
		if(mConnection==null) return ;
		mConnection.close();
		mConnection=null;
	}
	
	public void stopIoManager() {			
        if (mSerialIoManager == null) return ; 	        						
		Log.i("", "Stopping io manager ..");
		mSerialIoManager.stop();
		mSerialIoManager = null;			
    }
	
	public void startIoManager() {
		if(mPort==null) return ;
		mSerialIoManager = new SerialInputOutputManager(mPort, mSerialIOManagerListener);
		mExecutor.submit(mSerialIoManager);
	}
	
	public void showDlg(Context context) {
		ArrayAdapter<CharSequence> adtChar;
		ArrayAdapter<String> adtStr;
		this.closeConnection();
		this.closePort();
		this.stopIoManager();
		mDlg.show();		
		mSpinDev = (Spinner)mLinearForDlg.findViewById(R.id.act1spindev);
		adtStr = new ArrayAdapter<String>(context, R.layout.act1_spindev, this.mUsbList);
		adtStr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinDev.setAdapter(adtStr);		
		mSpinDev.setOnItemSelectedListener(mItemSelectedListener);
		mSpinBr = (Spinner)mLinearForDlg.findViewById(R.id.act1spinbr);
		adtChar = ArrayAdapter.createFromResource(context, R.array.baudrate, android.R.layout.simple_spinner_item);
		adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinBr.setAdapter(adtChar);
		mSpinBr.setSelection(this.mIndexBaudrateSpin);
		mSpinBr.setOnItemSelectedListener(mItemSelectedListener);
		mSpinData = (Spinner)mLinearForDlg.findViewById(R.id.act1spindata);
		adtChar = ArrayAdapter.createFromResource(context, R.array.databit, android.R.layout.simple_spinner_item);
		adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinData.setAdapter(adtChar);
		mSpinData.setSelection(this.mIndexDatabitsSpin);
		mSpinData.setOnItemSelectedListener(mItemSelectedListener);
		mSpinParity = (Spinner)mLinearForDlg.findViewById(R.id.act1spinparity);
		adtChar = ArrayAdapter.createFromResource(context, R.array.paritybit, android.R.layout.simple_spinner_item);
		adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinParity.setAdapter(adtChar);
		mSpinParity.setSelection(this.mIndexParitySpin);
		mSpinParity.setOnItemSelectedListener(mItemSelectedListener);
		mSpinStop = (Spinner)mLinearForDlg.findViewById(R.id.act1spinstop);
		adtChar = ArrayAdapter.createFromResource(context, R.array.stopbit, android.R.layout.simple_spinner_item);
		adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinStop.setAdapter(adtChar);
		mSpinStop.setSelection(this.mIndexStopSpin);
		mSpinStop.setOnItemSelectedListener(mItemSelectedListener);
		mSpinFlow = (Spinner)mLinearForDlg.findViewById(R.id.act1spinflow);
		adtChar = ArrayAdapter.createFromResource(context, R.array.flow, android.R.layout.simple_spinner_item);
		adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinFlow.setAdapter(adtChar);
		mSpinFlow.setSelection(this.mIndexFlowSpin);
		mSpinFlow.setOnItemSelectedListener(mItemSelectedListener);
		mSpinTimeout = (Spinner)mLinearForDlg.findViewById(R.id.act1spintimeout);
		adtChar = ArrayAdapter.createFromResource(context, R.array.timeout, android.R.layout.simple_spinner_item);
		adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinTimeout.setAdapter(adtChar);
		mSpinTimeout.setSelection(this.mIndexTimeoutSpin);
		mSpinTimeout.setOnItemSelectedListener(mItemSelectedListener);
	}	
	
	public void checkUSB(UsbManager manager) {
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();       
        mDevIter = deviceList.values().iterator();
        mUsbList.clear();
        mUsbList.add("None\r\n---");
        mDevList.clear();
        while (mDevIter.hasNext()) {
            UsbDevice _device = mDevIter.next();
            mUsbList.add(_device.getDeviceName() + "\r\n---");
            mDevList.add(_device);
        }
    }
}
