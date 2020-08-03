package com.daeeun.sohnori.anythingmanager.equipment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.daeeun.sohnori.myclass.DaeeunProtocol;
import com.daeeun.sohnori.myclass.RemsProtocol;

import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class ThingPlugSKT {
	private String mAppEui;
	private String mDevEui;
	private String mLtid;
	private String mUkey;
	private Thread mThread;
	private String msg;
	private double mLatitude;
	private double mLongitude;
	public ThingPlugSKT(String appEui, String devEui, String uKey) {
		mAppEui = appEui;
		mDevEui = devEui;
		mLtid = mAppEui.substring(8) + mDevEui;
		mUkey = uKey;
		this.mLatitude = 0;
		this.mLongitude = 0;
	}
	public void runThread(EditText log, EditText result) {
		mThread = new RequestDataThread(log, result);		
		mThread.setDaemon(true);
		mThread.start();
	}
	public String getMessage() {
		return this.msg;
	}
	public double getLatitude() {
		return this.mLatitude;
	}
	public double getLongitude() {
		return this.mLongitude;
	}
	private class RequestDataThread extends Thread {						
		EditText editLog;
		EditText editResult;
		Editable edit;		
		String resultTime;
		String resultData;
		String resultTest;
		StringBuilder str2 = new StringBuilder();
		Handler threadHandler = new Handler();		
		RequestDataThread(EditText log, EditText result){			
			this.editLog = log;
			this.editResult = result;
		}		
		@Override
		public void run() {
			int indexStart;
			int indexEnd;
			HttpURLConnection conn = null;
			String requestStr = mAppEui+"/v1_0/remoteCSE-"+mLtid+"/container-LoRa/latest";
			//String requestStr = mAppEui+"/v1_0/node-"+mLtid;
			URL url = null;
			try {
				url = new URL("http://thingplugpf.sktiot.com:9000/" + requestStr);					
				//url = new URL("https://www.naver.com");
				conn = (HttpURLConnection)url.openConnection();				
				conn.setRequestProperty("accept", "application/xml");
				conn.setRequestProperty("x-m2m-origin", "ThingPlug");
				conn.setRequestProperty("x-m2m-ri", "12345");
				conn.setRequestProperty("ukey", mUkey);
				conn.setConnectTimeout(3000);
				conn.setReadTimeout(3000);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);				
				//conn.setRequestMethod("POST");
				//conn.setDoOutput(true);				
				
				InputStream ins = conn.getInputStream();
				
				StringBuilder str = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
				String line;
				str2.append("URL:"+url.toExternalForm()+"\r\n");
				str2.append("getRequstMethod():"+conn.getRequestMethod()+"\r\n");
				str2.append("getContentType():"+conn.getContentType()+"\r\n");
				str2.append("getResponseCode():"+conn.getResponseCode()+"\r\n");
				str2.append("getResponseMessage():"+conn.getResponseMessage()+"\r\n");
				for(Map.Entry<String, List<String>> header:conn.getHeaderFields().entrySet()) {
					for(String value:header.getValue()) {
						str2.append(header.getKey()+":"+value+"\r\n");
					}
				}
				threadHandler.post(new Runnable() {
					public void run() {
						edit = editLog.getText();
						edit.append(str2);					
					}						
				});
				while((line = reader.readLine())!=null) {
					str.append(line+"\r\n");
				}
				
				msg = str.toString();				
			}
			catch(Exception e) {
				msg = e.getMessage();
				threadHandler.post(new Runnable() {
					public void run() {
						edit = editLog.getText();
						edit.append(msg);					
					}						
				});	
			}
			threadHandler.post(new Runnable() {
				public void run() {
					edit = editLog.getText();
					edit.append(msg);					
				}						
			});			
			indexStart = msg.indexOf("<ct>");
			indexEnd = msg.indexOf("</ct>");
			try {
				resultTime = msg.substring(indexStart, indexEnd);
				threadHandler.post(new Runnable() {
					public void run() {
						edit = editResult.getText();
						edit.append("수신시각: "+resultTime.substring(4)+"\r\n");					
					}						
				});
			}
			catch(Exception e) {				
				threadHandler.post(new Runnable() {
					public void run() {
						edit = editResult.getText();
						edit.append("수신시각: 없음\r\n");					
					}						
				});
			}			
			indexStart = msg.indexOf("<con>");
			indexEnd = msg.indexOf("</con>");
			try {
				resultData = msg.substring(indexStart, indexEnd);
				threadHandler.post(new Runnable() {
					public void run() {
						edit = editResult.getText();
						edit.append("수신데이터: "+resultData.substring(5)+"\r\n");
						String result = DaeeunProtocol.getResult(resultData.substring(5));
						if(result==null) result = RemsProtocol.getResult(resultData.substring(5));
						if(result==null) {
							edit.append("패킷: 알수없는 패킷형태");
						}						
						if(result!=null) edit.append(result);						
					}						
				});
			}
			catch(Exception e) {				
				threadHandler.post(new Runnable() {
					public void run() {
						edit = editResult.getText();
						edit.append("수신데이터: 없음\r\n");					
					}						
				});
			}
			indexStart = msg.indexOf("<devl>");
			indexEnd = msg.indexOf("</devl>");			
			try {
				mLatitude = 0;
				mLongitude = 0;
				resultTest = msg.substring(indexStart, indexEnd);
				if(resultTest!=null) {
					resultTest = resultTest.substring(6);				
					mLatitude = Double.valueOf(resultTest.substring(0, resultTest.indexOf(',')));
					mLongitude = Double.valueOf(resultTest.substring(resultTest.indexOf(',')+1, resultTest.indexOf(",0")));					
				}
			}
			catch(Exception e) {
				threadHandler.post(new Runnable() {
					public void run() {
						edit = editResult.getText();
						edit.append("좌표: 없음\r\n");					
					}						
				});
			}
			conn.disconnect();
		}		
	}	
}
