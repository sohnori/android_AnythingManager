package com.daeeun.sohnori.myclass;

import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class IoTCommunicationThread extends Thread {
	private IoTeepromHandler iotHandler = new IoTeepromHandler();
	private TerminalHandler mTerminal;
	private Editable mEditableLog;
	private EditText mEditResult;
	private int mBtnIndex;
	private boolean mWriteFlag;
	Handler mThreadHandler = new Handler();
	String bufferStr;
	byte[] readBuffer;
	byte[] writeBuffer;
	String mMsg;
	Runnable runResult = new Runnable() {
		public void run() {
			mEditResult.setText(mMsg);
		}
	};
	Runnable runLog = new Runnable() {
		public void run() {			
			mEditableLog.append(bufferStr);
		}
	};
	public IoTCommunicationThread(TerminalHandler terminal ,EditText log, EditText result, int btnIndex, boolean writeFlag) {
		mTerminal = terminal;
		mEditableLog = log.getText();
		mEditResult = result;
		mBtnIndex = btnIndex;
		mWriteFlag = writeFlag;
	}
	public void setMsg(String msg) {
		this.mMsg = msg;
	}
	public void run() {		
		if(mTerminal.isConnectedPort()!=true) {
			bufferStr="터미널이 연결되지 않았습니다.";
			mThreadHandler.post(runLog);
			return ;
		}
		switch(mBtnIndex) {
		case 1: // 로라모뎀
			if(mWriteFlag==false) {
				writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENTER, 0, null);
				mTerminal.initReceivedData();
				mTerminal.writeBytes(writeBuffer, 300);
				bufferStr = ConvertData.bytesToStringLog(writeBuffer);				
				mThreadHandler.post(runLog);				
				try {					
					Thread.sleep(1000);						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				readBuffer = mTerminal.getReceivedData();
				if(readBuffer==null) {
					mMsg = "응답없음.";
					mThreadHandler.post(runResult);
					return ;
				}
				if(iotHandler.verifyResponse(readBuffer)!=true) {
					mMsg = "올바르지 않은 응답. " + readBuffer.length;
					mThreadHandler.post(runResult);
					return ;
				}
				writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.LORAMODEM, 0, null);
				mTerminal.writeBytes(writeBuffer, 300);
				bufferStr = ConvertData.bytesToStringLog(writeBuffer);
				mThreadHandler.post(runLog);
				try {					
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				readBuffer = mTerminal.getReceivedData();
				if(readBuffer==null) {
					mMsg = "응답없음.";
					mThreadHandler.post(runResult);
					return ;
				}
				mMsg = readBuffer.toString();
				mThreadHandler.post(runResult);
			}
			else {
				
			}
			break;
		case 2:
			break;
		case 3:
			break;
		}
	}
}
