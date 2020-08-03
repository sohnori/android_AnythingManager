package com.daeeun.sohnori.myclass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

public class TerminalHandler {
	public enum OUTPUTFORM {
		NONE, BOTH, ASCII, HEX
	}
	private PendingIntent mPermissionIntent;
	private String mMsg;	
	private String mDriverName;
	List<String> mUsbList;
	List<UsbDevice> mDevList;	
	List<UsbSerialDriver> mAvailableDrivers;	
	private UsbDeviceConnection mConnection;
	private UsbSerialPort mPort;
	private UsbDevice mDevice;
	private SerialInputOutputManager mSerialIoManager;
	private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	private static final int RXDATA_BUFFER_SIZE = 2048;
	private static final int CLASS_BUFFER_SIZE = 512;
	private byte[] mRxDataBuffer = new byte[RXDATA_BUFFER_SIZE];
	private int mRxDataBufferPointer = 0;
	private int mRxDataBufferIndex = 0;
	private byte[] mClassBuffer = new byte[CLASS_BUFFER_SIZE];
	int mCntClassBuff = 0;	
	private Thread mThread;
	private EditText mEditLog;
	private boolean mThreadExecuteFlag;
	private TerminalHandler.OUTPUTFORM mFlagOutputForm;	
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
			Editable editLog = null;
			if(mEditLog!=null) editLog = mEditLog.getText();
			switch(msg.what) {
			case 0:									
				mCntClassBuff = 0;				
				while(mRxDataBufferIndex!=mRxDataBufferPointer) {					
					mClassBuffer[mCntClassBuff] = mRxDataBuffer[mRxDataBufferIndex];
					mRxDataBufferIndex++;
					mCntClassBuff++;
					if(mRxDataBufferIndex==RXDATA_BUFFER_SIZE) mRxDataBufferIndex=0;
				}
				mThreadExecuteFlag = false;
				if(editLog==null) break;
				// 받은 데이터를 출력
				String buff;
				if(mFlagOutputForm==TerminalHandler.OUTPUTFORM.ASCII) buff = ConvertData.bytesToAsciiString(mClassBuffer, 0, mCntClassBuff);
				else if(mFlagOutputForm==TerminalHandler.OUTPUTFORM.HEX) buff = ConvertData.bytesToHexString(mClassBuffer, 0, mCntClassBuff);
				else if(mFlagOutputForm==TerminalHandler.OUTPUTFORM.BOTH) buff = ConvertData.bytesToHexAsciiString(mClassBuffer, 0, mCntClassBuff);
				else break;
				simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
				message = simpleDate.format(new Date()) + "Received " + mCntClassBuff + " bytes: \n"
				        + buff + "\r\n";				
				editLog.append(message);
				break;				
			}			
		}
	};
	
	public TerminalHandler(Context context, EditText log, String permission){
		mUsbList = new ArrayList<String>();
		mDevList = new ArrayList<UsbDevice>();
		this.mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(permission), 0);
		this.mThreadExecuteFlag = false;
		this.mEditLog = log;		
		this.mDriverName = " ";
		this.mMsg = " ";
		this.mFlagOutputForm = TerminalHandler.OUTPUTFORM.BOTH;
		this.mThread = new ReceiveProcessThread(mHandler);
		this.mThread.setPriority(8);
		this.mThread.setDaemon(true);		
		this.mThread.start();
	}
	
	public TerminalHandler(Context context, String permission){
		this(context, null, permission);
	}
	
	public void setOutputForm(TerminalHandler.OUTPUTFORM output) {
		this.mFlagOutputForm = output;
	}
	
	public byte[] getReceivedData() {
		if(mCntClassBuff<=0) return null;
		byte[] result = new byte[mCntClassBuff];
		System.arraycopy(this.mClassBuffer, 0, result, 0, this.mCntClassBuff);
		return result;
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
	public int getDeviceCount(UsbManager manager) {
		this.mCheckUSB(manager);
		return this.mDevList.size();
	}
	public boolean openPort(UsbManager manager) {		
		this.closeConnection();
		this.closePort();
		this.stopIoManager();		
		if(this.getDeviceCount(manager)<=0) {
			this.mMsg = "장치를 찾을 수 없습니다.";
			return false;
		}
		try {											
			this.mDevice = this.mDevList.get(0);						
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
					9600, 
					UsbSerialPort.DATABITS_8, 
					UsbSerialPort.STOPBITS_1, 
					UsbSerialPort.PARITY_NONE);								
			this.startIoManager();
			//mPort.write("hello~~!".getBytes(), 300);
		}
		catch(Exception e) {
			mMsg = "에러:"+e.getMessage();
			return false;
		}		
		return true;
	}
	public boolean openPort(UsbManager manager, int devIndex,  int baudrate, int dataBit, int stop, int parity, int flow) {
		this.closeConnection();
		this.closePort();
		this.stopIoManager();
		if(devIndex+1<this.getDeviceCount(manager)) {
			this.mMsg = "디바이스 인덱스 허용범위 초과";
			return false;
		}
		try {			
			mDevice = mDevList.get(devIndex);				
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
					baudrate, 
					dataBit, 
					stop, 
					parity);								
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
			this.mMsg = "close port.";
			
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
	public PendingIntent getPendingIntent() {
		return this.mPermissionIntent;
	}
	
	public String getDevName() {
		return this.mDevice.getDeviceName();
	}
	
	public String getDriverName() {
		return this.mDriverName;
	}
	
	public String getMessage() {
		return this.mMsg;
	}
	
	public void mCheckUSB(UsbManager manager) {
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();       
        Iterator<UsbDevice> devIter = deviceList.values().iterator();
        mUsbList.clear();        
        mDevList.clear();
        while (devIter.hasNext()) {
            UsbDevice _device = devIter.next();
            mUsbList.add(_device.getDeviceName() + "\r\n---");
            mDevList.add(_device);
        }
    }
}
