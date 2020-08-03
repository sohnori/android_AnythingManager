package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.daeeun.sohnori.anythingmanager.R;
import com.daeeun.sohnori.anythingmanager.equipment.Inverter;
import com.daeeun.sohnori.anythingmanager.equipment.InverterDASS;
import com.daeeun.sohnori.anythingmanager.equipment.InverterECOS;
import com.daeeun.sohnori.anythingmanager.equipment.InverterEKOS;
import com.daeeun.sohnori.anythingmanager.equipment.InverterE_P3;
import com.daeeun.sohnori.anythingmanager.equipment.InverterE_P5;
import com.daeeun.sohnori.anythingmanager.equipment.InverterG2PW;
import com.daeeun.sohnori.anythingmanager.equipment.InverterHANS;
import com.daeeun.sohnori.anythingmanager.equipment.InverterHEXP;
import com.daeeun.sohnori.anythingmanager.equipment.InverterREFU;
import com.daeeun.sohnori.anythingmanager.equipment.InverterREMS;
import com.daeeun.sohnori.anythingmanager.equipment.InverterSMAI;
import com.daeeun.sohnori.anythingmanager.equipment.InverterSUNG;
import com.daeeun.sohnori.anythingmanager.equipment.InverterVELT;
import com.daeeun.sohnori.anythingmanager.equipment.InverterWILL;
import com.daeeun.sohnori.anythingmanager.equipment.LoraModemSKT;
import com.daeeun.sohnori.anythingmanager.equipment.SolarLampOptimizer;
import com.daeeun.sohnori.anythingmanager.equipment.InverterABBI;
import com.daeeun.sohnori.anythingmanager.terminal.Terminal;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MyProcess1Activity extends Activity {
	private static final String version = "V_1_1_2";
	private final OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {
			switch(parent.getId()) {
			case R.id.act1spindev:
				mTerminal.mIndexDevSpin = position;
				break;
			case R.id.act1spinbr:
				mTerminal.mIndexBaudrateSpin = position;
				break;
			case R.id.act1spindata:
				mTerminal.mIndexDatabitsSpin = position;
				break;
			case R.id.act1spinparity:
				mTerminal.mIndexParitySpin = position;
				break;
			case R.id.act1spinstop:
				mTerminal.mIndexStopSpin = position;
				break;
			case R.id.act1spinflow:
				mTerminal.mIndexFlowSpin = position;
				break;
			case R.id.act1spintimeout:
				mTerminal.mIndexTimeoutSpin = position;
				break;
			}				
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub				
		}
	};
	private final DialogInterface.OnClickListener mDlgInterfaceListener =  new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
			if(mTerminal.openPort(manager)==false) {
				mPrintLog(mTerminal.getMessage());
				return ;
			}
			mPrintLog(mTerminal.getDevName());
			mPrintLog(mTerminal.getDriverName());
			mPrintLog(mTerminal.getMessage());
			Resources res = getResources();							
			TextView text = (Button)findViewById(R.id.act1btnterminal);
			text.setText(mTerminal.mSpinDev.getSelectedItem().toString());
			text = (TextView)findViewById(R.id.act1txtbr);							
			text.setText(res.getString(R.string.str_terminalbr) +"  "+ mTerminal.mSpinBr.getSelectedItem().toString());
			text = (TextView)findViewById(R.id.act1txtdata);
			text.setText(res.getString(R.string.str_terminaldata) +"  "+ mTerminal.mSpinData.getSelectedItem().toString());
			text = (TextView)findViewById(R.id.act1txtparity);
			text.setText(res.getString(R.string.str_terminalparity) +"  "+ mTerminal.mSpinParity.getSelectedItem().toString());
			text = (TextView)findViewById(R.id.act1txtstop);
			text.setText(res.getString(R.string.str_terminalstop) +"  "+ mTerminal.mSpinStop.getSelectedItem().toString());
			text = (TextView)findViewById(R.id.act1txtflow);
			text.setText(res.getString(R.string.str_terminalflow) +"  "+ mTerminal.mSpinFlow.getSelectedItem().toString());
			text = (TextView)findViewById(R.id.act1txttimeout);
			text.setText(res.getString(R.string.str_terminaltimeout) +"  "+ mTerminal.mSpinTimeout.getSelectedItem().toString());							
		}
	};
	private static final String BROADCAST_MESSAGE = "com.daeeun.sohnori.anythingmanager";	
	private static final int lOG_MAX_LENGTH = 4096;
	private BroadcastReceiver mReceiver = null;		
	private static final String TAG = "LogTest";
	int mCnt;	
	Button mBtninv;
	TextView mText;		
	private static boolean mInitUsbPermition = false;	
	private static EditText mEditLog, mEditResult;
	ScrollView mScroll;
	Terminal mTerminal;
	EquipmentControl mEquipmentControl;	
	public enum InverterType {
		DASS, E_P3, E_P5, HANS, HEXP, EKOS, WILL, ABBI, REFU, REMS, SUNG, ECOS, SMAI, VELT
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process1);
		registerReceiver();		
		mEquipmentControl = new EquipmentControl();
		mEditLog = (EditText)findViewById(R.id.act1editlog);
		mEditLog.setFilters(new InputFilter[] {
			new InputFilter.LengthFilter(lOG_MAX_LENGTH)
		});
		mEditResult = (EditText)findViewById(R.id.act1editresult);		
		mScroll = (ScrollView)findViewById(R.id.act1scrv);		
		mTerminal = new Terminal(this, mEditLog, mOnItemSelectedListener, mDlgInterfaceListener);		
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
			Toast.makeText(this, version, Toast.LENGTH_LONG).show();
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
		mUnregisterReceiver();
		super.onDestroy();		
	}
	
	public void mPrintLog(String str) {
		if(mEditLog.length()>=lOG_MAX_LENGTH) {
			new AlertDialog.Builder(this)
			.setTitle("로그창 넘침")
			.setMessage("로그창이 가득 찼습니다.\r\n비우거나 저장해야 합니다.")
			.setPositiveButton("저장", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mClearLog();
				}
			})
			.setNegativeButton("지우기", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mClearLog();
				}
			}).show();					
		}
		Editable edit = mEditLog.getText();
		edit.append(str);
		mScroll.fullScroll(View.FOCUS_DOWN);
	}
	
	public void mClearLog() {
		Editable edit = mEditLog.getText();
		edit.clear();
		
	}
	
	public void mPrintResult(String str) {		
		mEditResult.setText(str);
	}
	
	public void mClearResult() {
		Editable edit = mEditResult.getText();
		edit.clear();
	}		
	
	public void mOnClick(View v){		
		String str;			
		switch(v.getId()) {		
		case R.id.act1btnterminal:
			Resources res = getResources();
			TextView text = (TextView)findViewById(R.id.act1txtbr);
			text.setText(res.getString(R.string.str_terminalbr));
			text = (TextView)findViewById(R.id.act1txtdata);
			text.setText(res.getString(R.string.str_terminaldata));
			text = (TextView)findViewById(R.id.act1txtparity);
			text.setText(res.getString(R.string.str_terminalparity));
			text = (TextView)findViewById(R.id.act1txtstop);
			text.setText(res.getString(R.string.str_terminalstop));
			text = (TextView)findViewById(R.id.act1txtflow);
			text.setText(res.getString(R.string.str_terminalflow));
			text = (TextView)findViewById(R.id.act1txttimeout);
			text.setText(res.getString(R.string.str_terminaltimeout));				
			TextView btn = (Button)findViewById(R.id.act1btnterminal);
			btn.setText("연결하시오.");
			mTerminal.checkUSB((UsbManager) getSystemService(Context.USB_SERVICE));
			try {
				mTerminal.showDlg(this);	
			}
			catch(Exception e) {
				mPrintLog(e.getMessage());
			}
			
			break;
		case R.id.act1btninfo:			
			mEquipmentControl.showBtnInfoDlg(this);						
			break;
		case R.id.act1btnequipset:
			mEquipmentControl.initEquipmentControl();
			mEquipmentControl.showEquipSetDlg(this);
			break;
		case R.id.act1btn1:
			if(mTerminal.isConnectedPort()==false) {
				mPrintLog("터미널을 먼저 연결하시오.");
				break;
			}
			if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_INVERTER) {				
				mClearResult();
				switch(mEquipmentControl.imEquip) {
				case EquipmentControl.EQUIP_INV_DASS:					
					InverterDASS invDASS;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invDASS = new InverterDASS(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invDASS = new InverterDASS(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");										
						break;
					}
					invDASS.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_E_P3:
					InverterE_P3 invE_P3 = new InverterE_P3(Inverter.Phase.SINGLE);											
					invE_P3.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_E_P5:
					InverterE_P5 invE_P5 = new InverterE_P5(Inverter.Phase.SINGLE);											
					invE_P5.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_HANS:
					InverterHANS invHANS;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invHANS = new InverterHANS(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invHANS = new InverterHANS(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}					
					invHANS.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_HEXP:
					InverterHEXP invHEXP;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_HEXPONE) invHEXP = new InverterHEXP(Inverter.Phase.SINGLE, InverterHEXP.Model.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_HEXPTHREE) invHEXP = new InverterHEXP(Inverter.Phase.THREE, InverterHEXP.Model.THREE);
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_HEXPSPECIAL) invHEXP = new InverterHEXP(Inverter.Phase.THREE, InverterHEXP.Model.H30xxS_ML);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}	
					invHEXP.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_EKOS:
					InverterEKOS invEKOS;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invEKOS = new InverterEKOS(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invEKOS = new InverterEKOS(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}					
					invEKOS.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_WILL:
					InverterWILL invWILL;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_WILLUVHT) invWILL = new InverterWILL(Inverter.Phase.THREE, InverterWILL.Model.UVHT_TYPE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_WILLM) invWILL = new InverterWILL(Inverter.Phase.THREE, InverterWILL.Model.M_TYPE);
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_WILLSL) invWILL = new InverterWILL(Inverter.Phase.THREE, InverterWILL.Model.SL_TYPE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}					
					invWILL.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_ABBI:
					InverterABBI invABBI;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invABBI = new InverterABBI(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invABBI = new InverterABBI(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}					
					invABBI.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);					
					break;
				case EquipmentControl.EQUIP_INV_REFU:
					InverterREFU invREFU;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invREFU = new InverterREFU(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invREFU = new InverterREFU(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}						
					invREFU.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_REMS:
					InverterREMS invREMS;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invREMS = new InverterREMS(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invREMS = new InverterREMS(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}						
					invREMS.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);	
					break;
				case EquipmentControl.EQUIP_INV_SUNG:
					InverterSUNG invSUNG;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invSUNG = new InverterSUNG(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invSUNG = new InverterSUNG(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}						
					invSUNG.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);	
					break;
				case EquipmentControl.EQUIP_INV_ECOS:
					InverterECOS invECOS;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invECOS = new InverterECOS(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invECOS = new InverterECOS(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}						
					invECOS.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);					
					break;
				case EquipmentControl.EQUIP_INV_SMAI:
					InverterSMAI invSMAI;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invSMAI = new InverterSMAI(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invSMAI = new InverterSMAI(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}						
					invSMAI.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_VELT:
					InverterVELT invVELT;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invVELT = new InverterVELT(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invVELT = new InverterVELT(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}						
					invVELT.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_INV_G2PW:
					InverterG2PW invG2PW;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTSINGLE) invG2PW = new InverterG2PW(Inverter.Phase.SINGLE);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_INVMODEL_DEFAULTTHREE) invG2PW = new InverterG2PW(Inverter.Phase.THREE);
					else {
						mPrintLog("인버터 상 설정이 올바르지 않습니다.\r\n");
						break;
					}						
					invG2PW.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				}				
			}
			if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_PVLM) {
				switch(mEquipmentControl.imEquip) {
				case EquipmentControl.EQUIP_PVLM_OPTIMIZER:
					SolarLampOptimizer optimizer = new SolarLampOptimizer();
					optimizer.runCommunicationThread(mTerminal, mEquipmentControl.imEquipID, mEditLog, mEditResult);
					break;
				}			
			}
			if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_LORA) {
				switch(mEquipmentControl.imEquip) {
				case EquipmentControl.EQUIP_LORA_SKT:
					LoraModemSKT loraModem;
					if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_LORASKT_IPL) loraModem = new LoraModemSKT(LoraModemSKT.Model.IPL);				
					else if(mEquipmentControl.imEquipModel==EquipmentControl.EQUIP_LORASKT_F1M) loraModem = new LoraModemSKT(LoraModemSKT.Model.F1M);
					else {
						mPrintLog("모델 설정이 올바르지 않습니다.\r\n");
						break;
					}
					loraModem.runCommunicationThread(mTerminal, mEditLog, mEditResult);
					break;
				case EquipmentControl.EQUIP_LORA_NODELINK:					
					break;
				}			
			}
			break;
		case R.id.act1btn2:
			break;
		case R.id.act1btn3:
			break;
		case R.id.act1btn4:
			break;
		case R.id.act1btn5:
			break;
		case R.id.act1btn6:		
			short test = -1234;
			mPrintLog("test: "+test/100+"."+test%100+"\r\n");			
			break;
		case R.id.act1btn7:
//			String path = getFilesDir().getAbsolutePath();
//			//String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//			File file = new File(path);
//			// 일치하는 폴더가 없으면 생성
//			if(!file.exists()) {
//				file.mkdirs();
//				Toast.makeText(this, "폴더생성", Toast.LENGTH_SHORT).show();
//			}
//			str = new SimpleDateFormat("yyMMdd_hh:mm:ss_").format(new Date());
//			File saveFile = new File(path+"/"+str+"_log.txt");
//			try (FileOutputStream fos = new FileOutputStream(saveFile)){				
//				String buff = mEditLog.getText().toString();				
//				try {
//					fos.write(buff.getBytes());
//					Toast.makeText(this, path+buff.length()+" 바이트 저장완료!", Toast.LENGTH_SHORT).show();	
//					mPrintLog(path);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					mPrintLog(e.getMessage());
//					Toast.makeText(this, "저장실패!", Toast.LENGTH_SHORT).show();
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				mPrintLog(e.getMessage());
//				Toast.makeText(this, "파일스트림열기실패!", Toast.LENGTH_SHORT).show();
//			} finally {
//				
//			}
			int id=111;
			char[] idChar = new char[5];
			str = Integer.toString(id);
			if(id>=100) {				
				idChar = str.toCharArray();
			}
			try {
				mPrintResult("문자:"+idChar[0]+idChar[1]+idChar[2]+idChar.length+"  문자열:"+str+"\r\n"
						+"문자1\r\n"+"문자2\r\n"+"문자3\r\n"+"문자3\r\n"+"문자3\r\n"+"문자3\r\n"+"문자3\r\n"+"문자3\r\n"+
						"문자3\r\n"+"문자끝\r\n");
			} catch(Exception e) {
				mPrintLog(e.getMessage());
			}
			
			break;
		case R.id.act1btn8:
//			// 사용자가 설정한 인텐드 메시지를 전달한다.
//			Intent intent = new Intent(BROADCAST_MESSAGE);
//			intent.putExtra("value", mCnt);
//			sendBroadcast(intent);			
//			mCnt++;
//			str = "edit" + mCnt + "\r\n";
//			
//			mPrintLog(str);
//			if(mCnt==5) {
//				mClearLog();
//				mCnt=0;
//			}			
			mClearLog();
			break;			
		}		
	}
		
	
	private void registerReceiver() {
		if(mReceiver != null) return;
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_MESSAGE);
		filter.addAction(Terminal.ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);		
		this.mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent==null) return ;
				int receivedData = intent.getIntExtra("value", 0);
				if(intent.getAction().equals(BROADCAST_MESSAGE)) {
					Toast.makeText(context,  "received Data : "+receivedData, Toast.LENGTH_SHORT).show();
				}
				else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
					Toast.makeText(context,  "장치 연결 감지", Toast.LENGTH_SHORT).show();
				}
				else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
					mTerminal.initTerminal();
					mTerminal.closeConnection();
					mTerminal.closePort();
					mTerminal.stopIoManager();
					TextView btn = (Button)findViewById(R.id.act1btnterminal);
					btn.setText("연결하시오.");
					Resources res = getResources();
					TextView text = (TextView)findViewById(R.id.act1txtbr);
					text.setText(res.getString(R.string.str_terminalbr));
					text = (TextView)findViewById(R.id.act1txtdata);
					text.setText(res.getString(R.string.str_terminaldata));
					text = (TextView)findViewById(R.id.act1txtparity);
					text.setText(res.getString(R.string.str_terminalparity));
					text = (TextView)findViewById(R.id.act1txtstop);
					text.setText(res.getString(R.string.str_terminalstop));
					text = (TextView)findViewById(R.id.act1txtflow);
					text.setText(res.getString(R.string.str_terminalflow));
					text = (TextView)findViewById(R.id.act1txttimeout);
					text.setText(res.getString(R.string.str_terminaltimeout));
					Toast.makeText(context,  "장치 해제 감지", Toast.LENGTH_SHORT).show();
				}
				else if (intent.getAction().equals(Terminal.ACTION_USB_PERMISSION)) {
					synchronized(this) {
						if(mInitUsbPermition==true) return ;
						UsbDevice device;	
						device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);						
						// 임시 코드
						Toast.makeText(context,  "장치 권한 허가\r\n", Toast.LENGTH_SHORT).show();										
						if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
							if(device!=null) {							
								//임시 코드								
							}
							else {
								Log.d(TAG, "permission denied for device " + device);
								mPrintLog(TAG + "permission denied for device " + device);
							}
						}						
						mInitUsbPermition = true;
					}
				}
			}
		};
		this.registerReceiver(this.mReceiver, filter);
	}
	
	private void mUnregisterReceiver() {
		if(mReceiver==null) return ;
		this.unregisterReceiver(mReceiver);
		mReceiver = null;
	}
	
	public class EquipmentControl{
		public static final int EQUIPTYPE_NONE = 0;
		public static final int EQUIPTYPE_INVERTER = 1;
		public static final int EQUIPTYPE_LORA = 2;
		public static final int EQUIPTYPE_PVLM = 3;
		public static final int EQUIPTYPE_ETC = 4;
		
		public static final int EQUIP_INV_NONE = 0;
		public static final int EQUIP_INV_DASS = 1;
		public static final int EQUIP_INV_E_P3 = 2;
		public static final int EQUIP_INV_E_P5 = 3;
		public static final int EQUIP_INV_HANS = 4;
		public static final int EQUIP_INV_HEXP = 5;
		public static final int EQUIP_INV_EKOS = 6;
		public static final int EQUIP_INV_WILL = 7;
		public static final int EQUIP_INV_ABBI = 8;
		public static final int EQUIP_INV_REFU = 9;
		public static final int EQUIP_INV_SUNG = 10;
		public static final int EQUIP_INV_REMS = 11;		
		public static final int EQUIP_INV_ECOS = 12;
		public static final int EQUIP_INV_SMAI = 13;
		public static final int EQUIP_INV_VELT = 14;
		public static final int EQUIP_INV_G2PW = 15;
		
		public static final int EQUIP_LORA_NONE = 0;
		public static final int EQUIP_LORA_SKT = 1;
		public static final int EQUIP_LORA_NODELINK = 2;
		
		public static final int EQUIP_LORASKT_NONE = 0;
		public static final int EQUIP_LORASKT_IPL = 1;
		public static final int EQUIP_LORASKT_F1M = 2;
		
		public static final int EQUIP_PVLM_NONE = 0;
		public static final int EQUIP_PVLM_OPTIMIZER = 1;
		
		public static final int EQUIP_INVMODEL_NONE = 0;
		public static final int EQUIP_INVMODEL_DEFAULTSINGLE = 1;
		public static final int EQUIP_INVMODEL_DEFAULTTHREE = 2;
		public static final int EQUIP_INVMODEL_HEXPONE = 1;
		public static final int EQUIP_INVMODEL_HEXPTHREE = 2;
		public static final int EQUIP_INVMODEL_HEXPSPECIAL = 3;
		public static final int EQUIP_INVMODEL_WILLUVHT = 1;
		public static final int EQUIP_INVMODEL_WILLM = 2;
		public static final int EQUIP_INVMODEL_WILLSL = 3;
		AlertDialog imDlg;		
		public Spinner imSpinEquipType, imSpinEquip, imSpinEquipModel;
		public int imEquipType, imEquip, imEquipModel, imEquipID;	
		
		public EquipmentControl(){
			this.imEquipType = EQUIPTYPE_NONE;
			this.imEquip = 0;
			this.imEquipModel = 0;
			this.imEquipID = 0;	
		}
		
		public void initEquipmentControl() {
			this.imEquipType = EQUIPTYPE_NONE;
			this.imEquip = 0;
			this.imEquipModel = 0;
			this.imEquipID = 0;
			Resources res = getResources();
			TextView text = (TextView)findViewById(R.id.act1txtequiptype);
			text.setText(res.getString(R.string.str_equiptype));
			text = (TextView)findViewById(R.id.act1txtequip);
			text.setText(res.getString(R.string.str_equip));
			text = (TextView)findViewById(R.id.act1txtequipmodel);
			text.setText(res.getString(R.string.str_equipmodel));
			text = (TextView)findViewById(R.id.act1txtequipid);
			text.setText(res.getString(R.string.str_equipid));
		}				
		
		public void showBtnInfoDlg(Context context) {
			TextView txtView;
			LinearLayout linear = (LinearLayout)View.inflate(context, R.layout.act1_dialogbtninfo, null);
			if(this.imEquipType == EquipmentControl.EQUIPTYPE_NONE) {
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn1);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn2);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn3);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn4);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn5);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn6);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn7);
				txtView.setText("로그를 파일로 저장.(추후구현)");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn8);
				txtView.setText("로그창을 지웁니다.");
				txtView = (TextView)linear.findViewById(R.id.act1txtinvnote);
				txtView.setText("");
			}
			else if(this.imEquipType == EquipmentControl.EQUIPTYPE_INVERTER) {
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn1);
				txtView.setText("해당 인버터 아이디로 데이터 요청");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn2);
				txtView.setText("인버터 아이디 스캔.(추후구현)");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn3);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn4);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn5);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn6);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn7);
				txtView.setText("로그를 파일로 저장.(추후구현)");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn8);
				txtView.setText("로그창을 지웁니다.");
				txtView = (TextView)linear.findViewById(R.id.act1txtinvnote);
				txtView.setText("한솔,동양E_P5,ABB_19200\r\nREFUsol_115200");
			}else if(this.imEquipType == EquipmentControl.EQUIPTYPE_PVLM) {
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn1);
				txtView.setText("해당 장치 아이디로 모든 데이터 요청");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn2);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn3);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn4);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn5);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn6);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn7);
				txtView.setText("로그를 파일로 저장.(추후구현)");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn8);
				txtView.setText("로그창을 지웁니다.");
				txtView = (TextView)linear.findViewById(R.id.act1txtinvnote);
				txtView.setText("옵티마이저:9600, EVEN parity");
			}
			else if(this.imEquipType == EquipmentControl.EQUIPTYPE_LORA) {
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn1);
				txtView.setText("장치 버전 요청");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn2);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn3);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn4);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn5);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn6);
				txtView.setText("");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn7);
				txtView.setText("로그를 파일로 저장.(추후구현)");
				txtView = (TextView)linear.findViewById(R.id.act1txtbtn8);
				txtView.setText("로그창을 지웁니다.");
				txtView = (TextView)linear.findViewById(R.id.act1txtinvnote);
				txtView.setText("통신속도:115200");
			}	
			new AlertDialog.Builder(context)
					.setTitle("버튼 정보")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setView(linear)
					.setPositiveButton("닫기", null)
					.show();											
		}
		
		public void showEquipSetDlg(Context context) {	
			ArrayAdapter<CharSequence> adtChar;
			LinearLayout linear = (LinearLayout)View.inflate(context, R.layout.act1_dialogequipset, null);
			imDlg = new AlertDialog.Builder(context)
					.setTitle("설비 설정창")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setView(linear)					
					.setNegativeButton("취소", null)
					.setPositiveButton("설정", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							TextView view = (TextView)mEquipmentControl.imDlg.findViewById(R.id.act1editequipid);
							mEquipmentControl.imEquipID = Integer.parseInt(view.getText().toString());
							mEquipmentControl.imEquipType = mEquipmentControl.imSpinEquipType.getSelectedItemPosition();
							mEquipmentControl.imEquip = mEquipmentControl.imSpinEquip.getSelectedItemPosition();
							mEquipmentControl.imEquipModel = mEquipmentControl.imSpinEquipModel.getSelectedItemPosition();							
							Resources res = getResources();							
							TextView text = (TextView)findViewById(R.id.act1txtequiptype);
							text.setText(res.getString(R.string.str_equiptype) +"  "+ mEquipmentControl.imSpinEquipType.getSelectedItem().toString());													
							text = (TextView)findViewById(R.id.act1txtequip);
							text.setText(res.getString(R.string.str_equip) +"  "+ mEquipmentControl.imSpinEquip.getSelectedItem().toString());
							text = (TextView)findViewById(R.id.act1txtequipmodel);
							text.setText(res.getString(R.string.str_equipmodel) +"  "+ mEquipmentControl.imSpinEquipModel.getSelectedItem().toString());
							text = (TextView)findViewById(R.id.act1txtequipid);
							text.setText(res.getString(R.string.str_equipid) +"  "+ mEquipmentControl.imEquipID);							
						}
					})
					.show();
			this.imSpinEquipType = (Spinner)imDlg.findViewById(R.id.act1spinequiptype);
			adtChar = ArrayAdapter.createFromResource(context, R.array.equiptype, android.R.layout.simple_spinner_item);
			adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.imSpinEquipType.setAdapter(adtChar);
			this.imSpinEquipType.setSelection(mEquipmentControl.imEquipType);
			this.imSpinEquipType.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {					
					ArrayAdapter<CharSequence> adtChar;
					mEquipmentControl.imEquipType = position;
					mEquipmentControl.imSpinEquip = (Spinner)imDlg.findViewById(R.id.act1spinequip);
					if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_INVERTER) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.equipmentinv, android.R.layout.simple_spinner_item);
					else if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_LORA) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.equipmentlora, android.R.layout.simple_spinner_item);
					else if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_PVLM) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.equipmentpvlm, android.R.layout.simple_spinner_item);
					else if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_ETC) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.equipmentetc, android.R.layout.simple_spinner_item);
					else adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.nullvalue, android.R.layout.simple_spinner_item);
					adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					mEquipmentControl.imSpinEquip.setAdapter(adtChar);
					mEquipmentControl.imSpinEquip.setSelection(0);
					mEquipmentControl.imSpinEquip.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {					
							ArrayAdapter<CharSequence> adtChar;
							mEquipmentControl.imEquip = position;
							mEquipmentControl.imSpinEquipModel = (Spinner)imDlg.findViewById(R.id.act1spinmodel);
							if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_INVERTER&&mEquipmentControl.imEquip==EquipmentControl.EQUIP_INV_HEXP) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.invhexp_model, android.R.layout.simple_spinner_item);
							else if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_INVERTER&&mEquipmentControl.imEquip==EquipmentControl.EQUIP_INV_WILL) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.invwill_model, android.R.layout.simple_spinner_item);
							else if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_INVERTER) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.invdef_model, android.R.layout.simple_spinner_item);
							else if(mEquipmentControl.imEquipType==EquipmentControl.EQUIPTYPE_LORA&&mEquipmentControl.imEquip==EquipmentControl.EQUIP_LORA_SKT) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.loraskt_model, android.R.layout.simple_spinner_item);
							else adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.nullvalue, android.R.layout.simple_spinner_item);
							adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							mEquipmentControl.imSpinEquipModel.setAdapter(adtChar);
							mEquipmentControl.imSpinEquipModel.setSelection(0);
							mEquipmentControl.imSpinEquipModel.setOnItemSelectedListener(new OnItemSelectedListener() {
								@Override
								public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {
									mEquipmentControl.imEquipModel = position;
								}
								@Override
								public void onNothingSelected(AdapterView<?> parent) {
									// TODO Auto-generated method stub				
								}
							});
						}
						@Override
						public void onNothingSelected(AdapterView<?> parent) {
							// TODO Auto-generated method stub				
						}
					});
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub				
				}
			});					
		}
	}
}
