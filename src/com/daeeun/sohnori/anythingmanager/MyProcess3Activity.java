package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daeeun.sohnori.anythingmanager.R;
import com.daeeun.sohnori.anythingmanager.MyProcess1Activity.EquipmentControl;
import com.daeeun.sohnori.myclass.ConvertData;
import com.daeeun.sohnori.myclass.IoTeepromHandler;
import com.daeeun.sohnori.myclass.TerminalHandler;

public class MyProcess3Activity extends Activity {
	private final static char[] HEX_DIGITS = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
	public static final int DECMD_TXPK = 0;
	public static final int DECMD_INFO_FVER = 1;
	public static final int DECMD_RCNT = 2;
	public static final int DECMD_LRST = 3;
	public static final int DECMD_LSRS = 4;
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
	public static final int EQUIP_NONE = 0;
	public static final int EQUIP_PV1P = 1;
	public static final int EQUIP_PV3P = 2;
	public static final int EQUIP_PVHF = 3;
	public static final int EQUIP_PVHN = 4;
	public static final int EQUIP_GEOT = 5;
	public static final int EQUIP_WIND = 6;
	public static final int EQUIP_FUEL = 7;
	public static final int EQUIP_ESSS = 8;
	public static final int EQUIP_INVMODEL_NONE = 0;
	public static final int EQUIP_INVMODEL_DEFAULTSINGLE = 1;
	public static final int EQUIP_INVMODEL_DEFAULTTHREE = 2;
	public static final int EQUIP_INVMODEL_HEXPONE = 1;
	public static final int EQUIP_INVMODEL_HEXPTHREE = 2;
	public static final int EQUIP_INVMODEL_HEXPSPECIAL = 3;
	public static final int EQUIP_INVMODEL_WILLUVHT = 1;
	public static final int EQUIP_INVMODEL_WILLM = 2;
	public static final int EQUIP_INVMODEL_WILLSL = 3;
	public final static int DISABLE = 0;
	public final static int LORA_IPL = 1;
	public final static int LORA_F1M = 2;
	public final static int LORA_NODELINK = 3;
	public final static int INMD_NORMAL = 1;
	public final static int INMD_MASTER_LOCAL = 2;
	public final static int INMD_MASTER_LOCAL_LORA = 3;
	public final static int ZBMD_SLAVE_1CH = 1;
	public final static int ZBMD_SLAVE_2CH = 2;
	public final static int ZBMD_SLAVE_4CH = 3;
	public final static int ZBMD_MASTER_LOCAL_1CH = 4;
	public final static int ZBMD_MASTER_LOCAL_2CH = 5;
	public final static int ZBMD_MASTER_LOCAL_4CH = 6;
	public final static int ZBMD_MASTER_LOCAL_LORA_1CH = 7;
	public final static int ZBMD_MASTER_LOCAL_LORA_2CH = 8;
	public final static int ZBMD_MASTER_LOCAL_LORA_4CH = 9;
	public final static int PVEM_SLAVE_INNER = 1;
	public final static int PVEM_SLAVE_OUTER = 2;
	public final static int PVEM_MASTER_LOCAL_INNER = 3;
	public final static int PVEM_MASTER_LOCAL_OUTER = 4;
	public final static int PVEM_MASTER_LOCAL_LORA_INNER = 5;
	public final static int PVEM_MASTER_LOCAL_LORA_OUTER = 6;
	public final static int IOT_NORMAL = 1;
	public final static int IOT_REMS = 2;
	public final static String STR_READ = "읽기: ";
	public final static String STR_WRITE = "쓰기: ";
	private static final String ACTION_USB_PERMISSION = "com.daeeun.sohnori.anythingmanager.USB_PERMISSION";
	private static final String BTN_INIT_TEXT = "연결하고 누르시오.";
	private static final String TAG = "LogTest";
	private static boolean mInitUsbPermition = false;
	private static final String VERSION = "V_1_0_1";
	private static final int lOG_MAX_LENGTH = 4096;
	private static EditText mEditLog, mEditResult;	
	private BroadcastReceiver mBroadcastReceiver = null;
	ScrollView mScroll;
	private TerminalHandler mTerminal;
	AlertDialog dlg1, dlg2, dlg3, dlg4, dlg5, dlg6, dlg7, dlg8, dlg9, dlg10,
				dlg11, dlg12, dlg13, dlg14, dlg15;
	int mBtn = 0;
	int mDevice = 0;
	int mLoraIndex = 0;	
	int mDlgIndex = 0;
	int mEquipNumPosition = 0;
	int mEquipTypePosition = 0;
	int mEquipModelPosition = 0;	
	int mEquipID = 0;
	int mCoordiCnt = 0;
	int mMsensorCnt = 0;
	int mCoordiID = 0;
	boolean[] mEnableIndex = new boolean[16];
	boolean mWriteFlag = false;
	String mMsg;
	IoTCommunicationThread mThread;	
	DialogInterface.OnClickListener mReadListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			mWriteFlag = false;
			mThread = new IoTCommunicationThread();
			mThread.setDaemon(true);
			mThread.start();
		}
	};
	DialogInterface.OnClickListener mWriteListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			mWriteFlag = true;			
			mThread = new IoTCommunicationThread();
			mThread.setDaemon(true);
			mThread.start();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process3);
		mRegisterReceiver();
		mEditLog = (EditText)findViewById(R.id.act3editlog);
		mEditLog.setFilters(new InputFilter[] {
			new InputFilter.LengthFilter(lOG_MAX_LENGTH)
		});
		mEditResult = (EditText)findViewById(R.id.act3editresult);
		mScroll = (ScrollView)findViewById(R.id.act3scrv);
		mTerminal = new TerminalHandler(this, mEditLog, ACTION_USB_PERMISSION);				
		this.mInitDlg();
	}
	
	private void mRegisterReceiver() {
		if(mBroadcastReceiver != null) return;
		final IntentFilter filter = new IntentFilter();		
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);		
		this.mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent==null) return ;						
				if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
					Toast.makeText(context,  "장치 연결 감지", Toast.LENGTH_SHORT).show();
				}
				else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){					
					mTerminal.closeConnection();
					mTerminal.closePort();
					mTerminal.stopIoManager();
					TextView btn = (Button)findViewById(R.id.act3btnterminal);
					btn.setText(BTN_INIT_TEXT);					
					Toast.makeText(context,  "장치 해제 감지", Toast.LENGTH_SHORT).show();
				}
				else if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
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
		this.registerReceiver(this.mBroadcastReceiver, filter);
	}	
	private void mUnregisterReceiver() {
		if(mBroadcastReceiver==null) return ;
		this.unregisterReceiver(mBroadcastReceiver);
		mBroadcastReceiver = null;
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
		ScrollView scroll = (ScrollView)findViewById(R.id.act3scrv);	
		scroll.fullScroll(View.FOCUS_DOWN);
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
		switch(v.getId()) {
		case R.id.act3btnterminal:
			this.mBtn = 0;
			UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
			TextView btn = (Button)findViewById(R.id.act3btnterminal);
			if(mTerminal.isConnectedPort()==true) {
				mTerminal.closePort();
				btn.setText(BTN_INIT_TEXT);
				break;
			}			
			btn.setText(BTN_INIT_TEXT);			
			mTerminal.openPort(manager);			
			if(mTerminal.openPort(manager)==false) {
				mPrintLog(mTerminal.getMessage()+"\r\n");
				break;
			}
			mPrintLog(mTerminal.getDevName());
			mPrintLog(mTerminal.getDriverName());
			mPrintLog(mTerminal.getMessage());
			btn.setText(mTerminal.getDevName());
			break;
		case R.id.act3btn1:
			this.mBtn = 1;
			if(mDevice==0) return ;
			mThread = new IoTCommunicationThread();
			mThread.setDaemon(true);
			mThread.start();			
			break;
		case R.id.act3btn2:
			this.mBtn = 2;
			if(dlg2==null) break;
			dlg2.show();
			break;
		case R.id.act3btn3:
			this.mBtn = 3;
			if(dlg3==null) break;			
			dlg3.show();
			break;
		case R.id.act3btn4:
			ArrayAdapter<CharSequence> adspin;
			Spinner spin;
			this.mBtn = 4;
			if(dlg4==null) break;
			dlg4.show();
			// 대화상자가 먼저 보여진 후 스피너를 수정할 수 있다.			
			if(this.mDevice==MyProcess3Activity.IOT_NORMAL) {
				spin = (Spinner)dlg4.findViewById(R.id.act3spininvnum);				
				adspin = ArrayAdapter.createFromResource
						(this, R.array.enable, android.R.layout.simple_spinner_item);
				adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spin.setAdapter(adspin);
				spin.setSelection(mEquipNumPosition);
				spin.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {
						mEquipNumPosition = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub					
					}
				});
				spin = (Spinner)dlg4.findViewById(R.id.act3spininvtype);
				spin.setPrompt("인버터를 선택하시오.");
				adspin = ArrayAdapter.createFromResource
						(getApplicationContext(), R.array.equipmentinv, android.R.layout.simple_spinner_item);
				adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spin.setAdapter(adspin);
				spin.setSelection(mEquipTypePosition);
				spin.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {
						mEquipTypePosition = position;
						ArrayAdapter<CharSequence> adtChar;						
						Spinner spin = (Spinner)dlg4.findViewById(R.id.act3spininvphase);
						if(mEquipTypePosition==EQUIP_INV_HEXP) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.invhexp_model, android.R.layout.simple_spinner_item);
						else if(mEquipTypePosition==EquipmentControl.EQUIP_INV_WILL) adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.invwill_model, android.R.layout.simple_spinner_item);
						else adtChar = ArrayAdapter.createFromResource(view.getContext(), R.array.invdef_model, android.R.layout.simple_spinner_item);												
						adtChar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spin.setAdapter(adtChar);
						spin.setSelection(0);
						spin.setOnItemSelectedListener(new OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {
								mEquipModelPosition = position;
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
			else if(this.mDevice==MyProcess3Activity.IOT_REMS) {
				spin = (Spinner)dlg4.findViewById(R.id.act3spinequipnum);				
				adspin = ArrayAdapter.createFromResource
						(this, R.array.enable, android.R.layout.simple_spinner_item);
				adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spin.setAdapter(adspin);
				spin.setSelection(mEquipNumPosition);
				spin.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {
						mEquipNumPosition = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub					
					}
				});
				spin = (Spinner)dlg4.findViewById(R.id.act3spinequiptype);
				spin.setPrompt("설비를 선택하시오.");
				adspin = ArrayAdapter.createFromResource
						(getApplicationContext(), R.array.equipmentrems, android.R.layout.simple_spinner_item);
				adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spin.setAdapter(adspin);
				spin.setSelection(mEquipTypePosition);
				spin.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?>parent, View view, int position, long id) {
						mEquipTypePosition = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub					
					}
				});
			}
			break;
		case R.id.act3btn5:
			this.mBtn = 5;
			if(dlg5==null) break;
			dlg5.show();			
			break;
		case R.id.act3btn6:
			this.mBtn = 6;
			if(dlg6==null) break;
			dlg6.show();
			break;
		case R.id.act3btn7:
			this.mBtn = 7;
			if(dlg7==null) break;
			dlg7.show();
			break;
		case R.id.act3btn8:
			this.mBtn = 8;
			if(dlg8==null) break;
			dlg8.show();
			break;
		case R.id.act3btn9:
			this.mBtn = 9;
			if(dlg9==null) break;
			dlg9.show();
			break;
		case R.id.act3btn10:
			this.mBtn = 10;
			if(dlg10==null) break;
			dlg10.show();
			break;
		case R.id.act3btn11:			
			this.mBtn = 11;
			if(dlg11==null) break;
			dlg11.show();		
			break;
		case R.id.act3btn12:
			this.mBtn = 12;
			if(mDevice==0) return ;
			mThread = new IoTCommunicationThread();
			mThread.setDaemon(true);
			mThread.start();
			break;
		case R.id.act3btn13:
			this.mBtn = 13;
			this.mClearLog();
			this.mClearResult();
			break;
		case R.id.act3btn14:
			this.mBtn = 14;
			if(dlg14==null) break;			
			dlg14.show();
			break;
		case R.id.act3btn15:
			this.mBtn = 15;
			if(dlg15==null) break;
			dlg15.show();
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
		this.mUnregisterReceiver();
		super.onDestroy();
	}
	private void mInitDlg() {
		TextView txtView;
		LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.act3_dialogbtninfo, null);
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
		txtView.setText("");
		txtView = (TextView)linear.findViewById(R.id.act1txtbtn8);
		txtView.setText("");
		txtView = (TextView)linear.findViewById(R.id.act1txtbtn9);
		txtView.setText("");
		txtView = (TextView)linear.findViewById(R.id.act1txtbtn10);
		txtView.setText("");
		txtView = (TextView)linear.findViewById(R.id.act1txtbtn11);
		txtView.setText("");
		txtView = (TextView)linear.findViewById(R.id.act1txtbtn12);
		txtView.setText("");			
		txtView = (TextView)linear.findViewById(R.id.act1txtbtn13);
		txtView.setText("로그창을 지웁니다.");
		txtView = (TextView)linear.findViewById(R.id.act1txtinvnote);
		txtView.setText("통신속도:9600");
		dlg14 = new AlertDialog.Builder(this)
				.setTitle("참조")
				.setIcon(R.drawable.outline_arrow_forward_white_48dp)
				.setView(linear)
				.setPositiveButton("확인", null)
				.create();
		
		dlg15 = new AlertDialog.Builder(this)
				.setTitle("장치선택")
				.setIcon(R.drawable.outline_arrow_forward_white_48dp)
				.setSingleChoiceItems(R.array.iotdevice, mDevice, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mDevice = which;
					}
				})				
				.setPositiveButton("설정", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						resetDlg();
					}
				})
				.create();
	}
	public void resetDlg() {
		TextView txtView;
		LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.act3_dialogbtninfo, null);
		if(mDevice == MyProcess3Activity.IOT_NORMAL) {
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn1);
			txtView.setText("커멘드모드 진입");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn2);
			txtView.setText("로라모드 설정");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn3);
			txtView.setText("인버터 활성화 설정");			
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn4);
			txtView.setText("인버터 설정");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn5);
			txtView.setText("인버터 모드설정");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn6);
			txtView.setText("ZB 모드설정");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn7);
			txtView.setText("환경센서 모드설정");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn8);
			txtView.setText("발전소,코디네이터 정보 설정");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn9);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn10);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn11);
			txtView.setText("기타명령");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn12);
			txtView.setText("커멘드모드 탈출");			
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn13);
			txtView.setText("로그창을 지웁니다.");
			txtView = (TextView)linear.findViewById(R.id.act1txtinvnote);
			txtView.setText("IoT 기본\r\n통신속도:9600");
		}
		else if(mDevice == MyProcess3Activity.IOT_REMS) {
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn1);
			txtView.setText("커멘드모드 진입");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn2);
			txtView.setText("로라모뎀 모드");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn3);
			txtView.setText("설비 활성화 설정");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn4);
			txtView.setText("설비 설정");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn5);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn6);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn7);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn8);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn9);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn10);
			txtView.setText("로라서브모뎀 모드");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn11);
			txtView.setText("기타명령");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn12);
			txtView.setText("커멘드모드 탈출");			
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn13);
			txtView.setText("로그창을 지웁니다.");
			txtView = (TextView)linear.findViewById(R.id.act1txtinvnote);
			txtView.setText("IoT REMS\r\n통신속도:9600");
		}
		else {
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
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn8);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn9);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn10);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn11);
			txtView.setText("");
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn12);
			txtView.setText("");			
			txtView = (TextView)linear.findViewById(R.id.act1txtbtn13);
			txtView.setText("로그창을 지웁니다.");
			txtView = (TextView)linear.findViewById(R.id.act1txtinvnote);
			txtView.setText("통신속도:9600");
		}
		dlg14 = new AlertDialog.Builder(this)
				.setTitle("참조")
				.setIcon(R.drawable.outline_arrow_forward_white_48dp)
				.setView(linear)
				.setPositiveButton("확인", null)
				.create();
		if(this.mDevice==0) {
			dlg1 = null;
			dlg2 = null;
			dlg3 = null;
			dlg4 = null;
			dlg5 = null;
			dlg6 = null;
			dlg7 = null;
			dlg8 = null;
			dlg9 = null;
			dlg10 = null;
			dlg11 = null;
			dlg12 = null;
			dlg13 = null;						
			return ;
		}
		dlg2 = new AlertDialog.Builder(this)
				.setTitle("장치선택")
				.setIcon(R.drawable.outline_arrow_forward_white_48dp)
				.setSingleChoiceItems(R.array.lomm, mLoraIndex, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mLoraIndex = which;
					}
				})
				.setNegativeButton("읽기", mReadListener)
				.setPositiveButton("쓰기", mWriteListener)
				.create();
		dlg3 = new AlertDialog.Builder(this)
				.setTitle("선택")
				.setIcon(R.drawable.outline_arrow_forward_white_48dp)
				.setMultiChoiceItems(R.array.enable, mEnableIndex, new DialogInterface.OnMultiChoiceClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						// TODO Auto-generated method stub
						mEnableIndex[which] = isChecked;
					}
				})
				.setNegativeButton("읽기", mReadListener)
				.setPositiveButton("쓰기", mWriteListener)
				.create();		
		if(this.mDevice==MyProcess3Activity.IOT_NORMAL) {
			dlg4 = new AlertDialog.Builder(this)
					.setTitle("선택")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setView(View.inflate(this, R.layout.act3_dialoginvset, null))
					.setNegativeButton("읽기", mReadListener)
					.setPositiveButton("쓰기", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mWriteFlag = true;
							EditText edit = (EditText)dlg4.findViewById(R.id.act3editinvid);							
							try{
								mEquipID = Integer.parseInt(edit.getText().toString());
							}
							catch(Exception e) {
								mEquipID = 255;
							}							
							mThread = new IoTCommunicationThread();
							mThread.setDaemon(true);
							mThread.start();
						}
					})					
					.create();				
		}
		else if(this.mDevice==MyProcess3Activity.IOT_REMS) {
			dlg4 = new AlertDialog.Builder(this)
					.setTitle("참조")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setView(View.inflate(this, R.layout.act3_dialogequipset, null))
					.setNegativeButton("읽기", mReadListener)
					.setPositiveButton("쓰기", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mWriteFlag = true;
							EditText edit = (EditText)dlg4.findViewById(R.id.act3editequipid);
							try{
								mEquipID = Integer.parseInt(edit.getText().toString());
							}
							catch(Exception e) {
								mEquipID = 255;
							}
							mThread = new IoTCommunicationThread();
							mThread.setDaemon(true);
							mThread.start();
						}
					})
					.create();			
		}
		if(this.mDevice==MyProcess3Activity.IOT_NORMAL) {
			dlg5 = new AlertDialog.Builder(this)
					.setTitle("선택")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setSingleChoiceItems(R.array.inmd, mDlgIndex, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mDlgIndex = which;
						}
					})
					.setNegativeButton("읽기", mReadListener)
					.setPositiveButton("쓰기", mWriteListener)
					.create();
		}
		if(this.mDevice==MyProcess3Activity.IOT_NORMAL) {
			dlg6 = new AlertDialog.Builder(this)
					.setTitle("선택")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setSingleChoiceItems(R.array.zbmd, mDlgIndex, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mDlgIndex = which;
						}
					})
					.setNegativeButton("읽기", mReadListener)
					.setPositiveButton("쓰기", mWriteListener)
					.create();
		}
		if(this.mDevice==MyProcess3Activity.IOT_NORMAL) {
			dlg7 = new AlertDialog.Builder(this)
					.setTitle("선택")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setSingleChoiceItems(R.array.pvem, mDlgIndex, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mDlgIndex = which;
						}
					})
					.setNegativeButton("읽기", mReadListener)
					.setPositiveButton("쓰기", mWriteListener)
					.create();
		}
		if(this.mDevice==MyProcess3Activity.IOT_NORMAL) {
			linear = (LinearLayout)View.inflate(this, R.layout.act3_dialogstationinfo, null);
			dlg8 = new AlertDialog.Builder(this)
					.setTitle("선택")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setView(linear)
					.setNegativeButton("읽기", mReadListener)
					.setPositiveButton("쓰기", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mWriteFlag = true;
							EditText edit = (EditText)dlg8.findViewById(R.id.act3editcoordicnt);							
							try{
								mCoordiCnt = Integer.parseInt(edit.getText().toString());
							}
							catch(Exception e) {
								mPrintLog(e.getMessage());
							}
							edit = (EditText)dlg8.findViewById(R.id.act3editsensorcnt);							
							try{
								mMsensorCnt = Integer.parseInt(edit.getText().toString());
							}
							catch(Exception e) {
								mPrintLog(e.getMessage());
							}
							edit = (EditText)dlg8.findViewById(R.id.act3editcoordiid);							
							try{
								mCoordiID = Integer.parseInt(edit.getText().toString());
							}
							catch(Exception e) {
								mPrintLog(e.getMessage());
							}
							mThread = new IoTCommunicationThread();
							mThread.setDaemon(true);
							mThread.start();
						}
					})
					.create();
		}
		if(this.mDevice==MyProcess3Activity.IOT_REMS) {
			dlg10 = new AlertDialog.Builder(this)
					.setTitle("장치선택")
					.setIcon(R.drawable.outline_arrow_forward_white_48dp)
					.setSingleChoiceItems(R.array.lomm, mLoraIndex, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mLoraIndex = which;
						}
					})
					.setNegativeButton("읽기", mReadListener)
					.setPositiveButton("쓰기", mWriteListener)
					.create();		
		}
		dlg11 = new AlertDialog.Builder(this)
				.setTitle("선택")
				.setIcon(R.drawable.outline_arrow_forward_white_48dp)
				.setSingleChoiceItems(R.array.etccmd, mDlgIndex, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mDlgIndex = which;
					}
				})
				.setNegativeButton("취소", mReadListener)
				.setPositiveButton("쓰기", mWriteListener)
				.create();		
	}
	public class IoTCommunicationThread extends Thread {
		private IoTeepromHandler iotHandler = new IoTeepromHandler();		
		Handler threadHandler = new Handler();
		String bufferStr;
		byte[] readBuffer;
		byte[] writeBuffer;
		byte[] addByte;
		Editable editable = mEditLog.getText();
		Runnable runResult = new Runnable() {
			public void run() {
				mEditResult.setText(mMsg);
			}
		};
		Runnable runLog = new Runnable() {
			public void run() {			
				editable.append(bufferStr);
			}
		};		
		public void run() {				
			if(mTerminal.isConnectedPort()!=true) {
				bufferStr="터미널이 연결되지 않았습니다.\r\n";
				threadHandler.post(runLog);
				return ;
			}
			mTerminal.initReceivedData();
			switch(mBtn) {
			case 1:
				writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENTER, 0, null);				
				mTerminal.writeBytes(writeBuffer, 300);
				bufferStr = ConvertData.bytesToStringLog(writeBuffer);				
				threadHandler.post(runLog);				
				try {					
					Thread.sleep(1000);						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				readBuffer = mTerminal.getReceivedData();
				if(readBuffer==null) {
					mMsg = "응답없음.";
					threadHandler.post(runResult);
					return ;
				}
				if(iotHandler.verifyResponse(readBuffer)!=true) {
					mMsg = "올바르지 않은 응답. ";
					threadHandler.post(runResult);
					return ;
				}
				mMsg = "장치 커멘드 모드 진입";
				threadHandler.post(runResult);
				break;
			case 2: // 로라모뎀				
				if(mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.LORAMODEM, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg =  String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer));
						if(mMsg.equals("01")==true) mMsg = STR_READ+"로라모뎀_IPL 모뎀(SKT) 설정.";
						else if(mMsg.equals("02")==true) mMsg = STR_READ+"로라모뎀_FIM 모뎀(SKT) 설정.";
						else if(mMsg.equals("03")==true) mMsg = STR_READ+"로라모뎀_노드링크 모뎀(자가망) 설정.";
						else mMsg = "로라모뎀_미사용 설정.";
					}
					catch(Exception e) {
						mMsg = STR_READ+"로라모뎀_알 수 없는 값.";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}					
					threadHandler.post(runResult);
				}
				else {					
					if(mLoraIndex==DISABLE) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.LORAMODEM, 0, IoTeepromHandler.DISABLE);
					else if(mLoraIndex==LORA_IPL) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.LORAMODEM, 0, IoTeepromHandler.LORA_IPL);
					else if(mLoraIndex==LORA_F1M) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.LORAMODEM, 0, IoTeepromHandler.LORA_F1M);
					else if(mLoraIndex==LORA_NODELINK) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.LORAMODEM, 0, IoTeepromHandler.LORA_NODELINK);
					else return ;
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					if(mLoraIndex==DISABLE) mMsg = STR_WRITE+"로라모뎀_미사용 설정.";					
					else if(mLoraIndex==LORA_IPL) mMsg = STR_WRITE+"로라모뎀_IPL 모뎀(SKT) 설정.";
					else if(mLoraIndex==LORA_F1M) mMsg = STR_WRITE+"로라모뎀_FIM 모뎀(SKT) 설정.";
					else if(mLoraIndex==LORA_NODELINK) mMsg = STR_WRITE+"로라모뎀_노드링크 모뎀(자가망) 설정.";					
					threadHandler.post(runResult);
				}
				break;
			case 3: // 인버터 활성화 설정
				if(mDevice==MyProcess3Activity.IOT_NORMAL && mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_ENABLE_H, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg = STR_READ+"인버터활성화 15번~8번_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer, 2))+"\r\n";
					}
					catch(Exception e) {
						mMsg = STR_READ+"인버터활성화 15번~8번_알 수 없는 값.\r\n";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}									
										
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_ENABLE_L, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg += STR_READ+"인버터활성화 7번~0번_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer, 2))+"\r\n";
					}
					catch(Exception e) {
						mMsg += STR_READ+"인버터활성화 7번~0번_알 수 없는 값.\r\n";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}
					threadHandler.post(runResult);
				}
				else if(mDevice==MyProcess3Activity.IOT_NORMAL && mWriteFlag==true) {					
					addByte = new byte[2];
					if(mEnableIndex[15]==true) addByte[0] = (byte)0b00001000;
					if(mEnableIndex[14]==true) addByte[0] |= (byte)0b00000100;
					if(mEnableIndex[13]==true) addByte[0] |= (byte)0b00000010;
					if(mEnableIndex[12]==true) addByte[0] |= (byte)0b00000001;
					addByte[0] = (byte) HEX_DIGITS[addByte[0]];
					if(mEnableIndex[11]==true) addByte[1] = (byte)0b00001000;
					if(mEnableIndex[10]==true) addByte[1] |= (byte)0b00000100;
					if(mEnableIndex[9]==true) addByte[1] |= (byte)0b00000010;
					if(mEnableIndex[8]==true) addByte[1] |= (byte)0b00000001;
					addByte[1] = (byte) HEX_DIGITS[addByte[1]];															
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_ENABLE_H, 0, addByte);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					mMsg = STR_WRITE+"인버터활성화 15번~8번_"+String.valueOf(ConvertData.bytesAsciiToCharArray(addByte, 2))+"\r\n";
					addByte[0]=0;
					addByte[1]=0;
					if(mEnableIndex[7]==true) addByte[0] = (byte)0b00001000;
					if(mEnableIndex[6]==true) addByte[0] |= (byte)0b00000100;
					if(mEnableIndex[5]==true) addByte[0] |= (byte)0b00000010;
					if(mEnableIndex[4]==true) addByte[0] |= (byte)0b00000001;
					addByte[0] = (byte) HEX_DIGITS[addByte[0]];
					if(mEnableIndex[3]==true) addByte[1] = (byte)0b00001000;
					if(mEnableIndex[2]==true) addByte[1] |= (byte)0b00000100;
					if(mEnableIndex[1]==true) addByte[1] |= (byte)0b00000010;
					if(mEnableIndex[0]==true) addByte[1] |= (byte)0b00000001;
					addByte[1] = (byte) HEX_DIGITS[addByte[1]];
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_ENABLE_L, 0, addByte);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					mMsg += STR_WRITE+"인버터활성화 7번~0번_"+String.valueOf(ConvertData.bytesAsciiToCharArray(addByte, 2))+"\r\n";
					threadHandler.post(runResult);
				}
				if(mDevice==MyProcess3Activity.IOT_REMS && mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.EQUIP_ENABLE_H, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg = STR_READ+"설비활성화 15번~8번_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer, 2))+"\r\n";
					}
					catch(Exception e) {
						mMsg = STR_READ+"설비활성화 15번~8번_알 수 없는 값.\r\n";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}									
										
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.EQUIP_ENABLE_L, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg += STR_READ+"설비활성화 7번~0번_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer, 2))+"\r\n";
					}
					catch(Exception e) {
						mMsg += STR_READ+"설비활성화 7번~0번_알 수 없는 값.\r\n";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}
					threadHandler.post(runResult);
				}
				else if(mDevice==MyProcess3Activity.IOT_REMS && mWriteFlag==true) {					
					addByte = new byte[2];
					if(mEnableIndex[15]==true) addByte[0] = (byte)0b00001000;
					if(mEnableIndex[14]==true) addByte[0] |= (byte)0b00000100;
					if(mEnableIndex[13]==true) addByte[0] |= (byte)0b00000010;
					if(mEnableIndex[12]==true) addByte[0] |= (byte)0b00000001;
					addByte[0] = (byte) HEX_DIGITS[addByte[0]];
					if(mEnableIndex[11]==true) addByte[1] = (byte)0b00001000;
					if(mEnableIndex[10]==true) addByte[1] |= (byte)0b00000100;
					if(mEnableIndex[9]==true) addByte[1] |= (byte)0b00000010;
					if(mEnableIndex[8]==true) addByte[1] |= (byte)0b00000001;
					addByte[1] = (byte) HEX_DIGITS[addByte[1]];															
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.EQUIP_ENABLE_H, 0, addByte);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					mMsg = STR_WRITE+"설비활성화 15번~8번_"+String.valueOf(ConvertData.bytesAsciiToCharArray(addByte, 2))+"\r\n";
					addByte[0]=0;
					addByte[1]=0;
					if(mEnableIndex[7]==true) addByte[0] = (byte)0b00001000;
					if(mEnableIndex[6]==true) addByte[0] |= (byte)0b00000100;
					if(mEnableIndex[5]==true) addByte[0] |= (byte)0b00000010;
					if(mEnableIndex[4]==true) addByte[0] |= (byte)0b00000001;
					addByte[0] = (byte) HEX_DIGITS[addByte[0]];
					if(mEnableIndex[3]==true) addByte[1] = (byte)0b00001000;
					if(mEnableIndex[2]==true) addByte[1] |= (byte)0b00000100;
					if(mEnableIndex[1]==true) addByte[1] |= (byte)0b00000010;
					if(mEnableIndex[0]==true) addByte[1] |= (byte)0b00000001;
					addByte[1] = (byte) HEX_DIGITS[addByte[1]];
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.EQUIP_ENABLE_L, 0, addByte);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					mMsg += STR_WRITE+"설비활성화 7번~0번_"+String.valueOf(ConvertData.bytesAsciiToCharArray(addByte, 2))+"\r\n";
					threadHandler.post(runResult);
				}
				break;
			case 4: // 인버터 설정
				if(mDevice==MyProcess3Activity.IOT_NORMAL && mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTERTYPE, mEquipNumPosition, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg = STR_READ+"인버터 "+mEquipNumPosition+"번\r\n"+
								"타입_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,0, 4))+"\r\n"+
								"아이디_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,4, 3))+"\r\n"+
								"상_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,7, 1))+"\r\n";
					}
					catch(Exception e) {
						mMsg = STR_READ+"인버터 "+mEquipNumPosition+"번 알 수 없는 값\r\n";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}															
					threadHandler.post(runResult);
				}
				else if(mDevice==MyProcess3Activity.IOT_NORMAL && mWriteFlag==true) {
					byte[] idByte = new byte[3];
					byte phase = '0';	
					if(mEquipID==255 || mEquipID>100) {
						idByte[0] = '0';					
						idByte[1] = 'F';					
						idByte[2] = 'F';
					}
					else {
						idByte[0] = '0';					
						idByte[1] = (byte)(mEquipID/10+0x30);					
						idByte[2] = (byte)(mEquipID%10+0x30);
					}
					if(mEquipTypePosition==0) {
						bufferStr="인버터 타입을 설정하시오.";
						threadHandler.post(runLog);
						return ;
					}
					if(mEquipModelPosition==0) {
						bufferStr="인버터 상을 설정하시오.";
						threadHandler.post(runLog);
						return ;
					}
					if(mEquipTypePosition==EQUIP_INV_HEXP&&mEquipModelPosition==3) phase = (byte)'S';
					else if(mEquipTypePosition==EQUIP_INV_WILL&&mEquipModelPosition==1) phase = (byte)'T';
					else if(mEquipTypePosition==EQUIP_INV_WILL&&mEquipModelPosition==2) phase = (byte)'M';
					else if(mEquipTypePosition==EQUIP_INV_WILL&&mEquipModelPosition==3) phase = (byte)'S';
					else if(mEquipModelPosition==1) phase = (byte)'1';
					else if(mEquipModelPosition==2) phase = (byte)'3';					
					if(mEquipTypePosition==EQUIP_INV_DASS) addByte = IoTeepromHandler.INV_DASS;
					else if(mEquipTypePosition==EQUIP_INV_E_P3) addByte = IoTeepromHandler.INV_E_P3;
					else if(mEquipTypePosition==EQUIP_INV_E_P5) addByte = IoTeepromHandler.INV_E_P5;
					else if(mEquipTypePosition==EQUIP_INV_HANS) addByte = IoTeepromHandler.INV_HANS;
					else if(mEquipTypePosition==EQUIP_INV_HEXP) addByte = IoTeepromHandler.INV_HEXP;
					else if(mEquipTypePosition==EQUIP_INV_EKOS) addByte = IoTeepromHandler.INV_EKOS;
					else if(mEquipTypePosition==EQUIP_INV_WILL) addByte = IoTeepromHandler.INV_WILL;
					else if(mEquipTypePosition==EQUIP_INV_ABBI) addByte = IoTeepromHandler.INV_ABBI;
					else if(mEquipTypePosition==EQUIP_INV_REFU) addByte = IoTeepromHandler.INV_REFU;
					else if(mEquipTypePosition==EQUIP_INV_SUNG) addByte = IoTeepromHandler.INV_SUNG;
					else if(mEquipTypePosition==EQUIP_INV_REMS) addByte = IoTeepromHandler.INV_REMS;
					else if(mEquipTypePosition==EQUIP_INV_ECOS) addByte = IoTeepromHandler.INV_ECOS;
					else if(mEquipTypePosition==EQUIP_INV_SMAI) addByte = IoTeepromHandler.INV_SMAI;
					else if(mEquipTypePosition==EQUIP_INV_VELT) addByte = IoTeepromHandler.INV_VELT;
					else if(mEquipTypePosition==EQUIP_INV_G2PW) addByte = IoTeepromHandler.INV_G2PW;
					addByte = ConvertData.byteArraysAdd(addByte, idByte);
					addByte = ConvertData.bytesAdd(addByte, phase);
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTERTYPE, mEquipNumPosition, addByte);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg = STR_WRITE+"인버터 "+mEquipNumPosition+"번\r\n"+
								"타입_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,0, 4))+"\r\n"+
								"아이디_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,4, 3))+"\r\n"+
								"상_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,7, 1))+"\r\n";
					}
					catch(Exception e) {
						mMsg = STR_WRITE+"인버터 "+mEquipNumPosition+"번 알 수 없는 값\r\n";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}					
					threadHandler.post(runResult);
				}
				if(mDevice==MyProcess3Activity.IOT_REMS && mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.EQUIP_TYPE, mEquipNumPosition, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg = STR_READ+"REMS설비"+mEquipNumPosition+"번\r\n"+
								"타입_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,0, 4))+"\r\n"+
								"아이디_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,4, 3))+"\r\n";								
					}
					catch(Exception e) {
						mMsg = STR_READ+"설비 "+mEquipNumPosition+"번 알 수 없는 값\r\n";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}															
					threadHandler.post(runResult);
				}
				else if(mDevice==MyProcess3Activity.IOT_REMS && mWriteFlag==true) {
					byte[] idByte = new byte[3];					
					if(mEquipID==255 || mEquipID>100) {
						idByte[0] = '0';					
						idByte[1] = 'F';					
						idByte[2] = 'F';
					}
					else {
						idByte[0] = '0';					
						idByte[1] = (byte)(mEquipID/10+0x30);					
						idByte[2] = (byte)(mEquipID%10+0x30);
					}
					if(mEquipTypePosition==0) {
						bufferStr="REMS설비 타입을 설정하시오.";
						threadHandler.post(runLog);
						return ;
					}														
					if(mEquipTypePosition==EQUIP_PV1P) addByte = IoTeepromHandler.EQP_PV1P;
					else if(mEquipTypePosition==EQUIP_PV3P) addByte = IoTeepromHandler.EQP_PV3P;
					else if(mEquipTypePosition==EQUIP_PVHF) addByte = IoTeepromHandler.EQP_PVHF;
					else if(mEquipTypePosition==EQUIP_PVHN) addByte = IoTeepromHandler.EQP_PVHN;
					else if(mEquipTypePosition==EQUIP_GEOT) addByte = IoTeepromHandler.EQP_GEOT;
					else if(mEquipTypePosition==EQUIP_WIND) addByte = IoTeepromHandler.EQP_WIND;
					else if(mEquipTypePosition==EQUIP_FUEL) addByte = IoTeepromHandler.EQP_FUEL;
					else if(mEquipTypePosition==EQUIP_ESSS) addByte = IoTeepromHandler.EQP_ESSS;
					addByte = ConvertData.byteArraysAdd(addByte, idByte);					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.EQUIP_TYPE, mEquipNumPosition, addByte);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg = STR_WRITE+"REMS설비 "+mEquipNumPosition+"번\r\n"+
								"타입_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,0, 4))+"\r\n"+
								"아이디_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,4, 3))+"\r\n";								
					}
					catch(Exception e) {
						mMsg = STR_WRITE+"REMS설비 "+mEquipNumPosition+"번 알 수 없는 값\r\n";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}					
					threadHandler.post(runResult);
				}
				break;
			case 5: // 인버터모드				
				if(mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_MODE, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg =  String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer));
						if(mMsg.equals("01\r")==true) mMsg = STR_READ+"인버터 모드_사용(노멀) 설정.";
						else if(mMsg.equals("11\r")==true) mMsg = STR_READ+"인버터 모드_마스터 로컬 설정.";
						else if(mMsg.equals("21\r")==true) mMsg = STR_READ+"인버터 모드_마스터 로컬_로라 설정.";
						else mMsg = "인버터 모드_미사용 설정.";
					}
					catch(Exception e) {
						mMsg = STR_READ+"인버터 모드_알 수 없는 값.";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}					
					threadHandler.post(runResult);
				}
				else {					
					if(mDlgIndex==DISABLE) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_MODE, 0, IoTeepromHandler.DISABLE);
					else if(mDlgIndex==INMD_NORMAL) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_MODE, 0, IoTeepromHandler.INMD_NORMAL);
					else if(mDlgIndex==INMD_MASTER_LOCAL) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_MODE, 0, IoTeepromHandler.INMD_MASTER_LOCAL);
					else if(mDlgIndex==INMD_MASTER_LOCAL_LORA) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INVERTER_MODE, 0, IoTeepromHandler.INMD_MASTER_LOCAL_LORA);
					else return ;
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					if(mDlgIndex==DISABLE) mMsg = STR_WRITE+"인버터 모드_미사용 설정.";					
					else if(mDlgIndex==INMD_NORMAL) mMsg = STR_WRITE+"인버터 모드_사용(노멀) 설정.";
					else if(mDlgIndex==INMD_MASTER_LOCAL) mMsg = STR_WRITE+"인버터 모드_마스터 로컬 설정.";
					else if(mDlgIndex==INMD_MASTER_LOCAL_LORA) mMsg = STR_WRITE+"인버터 모드_마스터 로컬_로라 설정.";					
					threadHandler.post(runResult);
				}
				break;
			case 6: // 지그비 모드				
				if(mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg =  String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer));
						if(mMsg.equals("01\r")==true) mMsg = STR_READ+"ZB 모드_슬레이브 1채널 설정.";
						else if(mMsg.equals("02\r")==true) mMsg = STR_READ+"ZB 모드_슬레이브 2채널 설정.";
						else if(mMsg.equals("04\r")==true) mMsg = STR_READ+"ZB 모드_슬레이브 4채널 설정.";
						else if(mMsg.equals("11\r")==true) mMsg = STR_READ+"ZB 모드_마스터_로컬 1채널 설정.";
						else if(mMsg.equals("12\r")==true) mMsg = STR_READ+"ZB 모드_마스터_로컬 2채널 설정.";
						else if(mMsg.equals("14\r")==true) mMsg = STR_READ+"ZB 모드_마스터_로컬 4채널 설정.";
						else if(mMsg.equals("21\r")==true) mMsg = STR_READ+"ZB 모드_마스터_로컬_로라 1채널 설정.";
						else if(mMsg.equals("22\r")==true) mMsg = STR_READ+"ZB 모드_마스터_로컬_로라 2채널 설정.";
						else if(mMsg.equals("24\r")==true) mMsg = STR_READ+"ZB 모드_마스터_로컬_로라 4채널 설정.";
						else mMsg = "ZB 모드_미사용 설정.";
					}
					catch(Exception e) {
						mMsg = STR_READ+"ZB 모드_알 수 없는 값.";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}					
					threadHandler.post(runResult);
				}
				else {					
					if(mDlgIndex==DISABLE) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.DISABLE);
					else if(mDlgIndex==ZBMD_SLAVE_1CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_SLAVE_1CH);
					else if(mDlgIndex==ZBMD_SLAVE_2CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_SLAVE_2CH);
					else if(mDlgIndex==ZBMD_SLAVE_4CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_SLAVE_4CH);
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_1CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_MASTER_LOCAL_1CH);
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_2CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_MASTER_LOCAL_2CH);
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_4CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_MASTER_LOCAL_4CH);
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_LORA_1CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_MASTER_LOCAL_LORA_1CH);
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_LORA_2CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_MASTER_LOCAL_LORA_2CH);
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_LORA_4CH) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ZB_MODE, 0, IoTeepromHandler.ZBMD_MASTER_LOCAL_LORA_4CH);
					else return ;
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					if(mDlgIndex==DISABLE) mMsg = STR_WRITE+"ZB 모드_미사용 설정.";					
					else if(mDlgIndex==ZBMD_SLAVE_1CH) mMsg = STR_WRITE+"ZB 모드_슬레이브 1채널  설정.";
					else if(mDlgIndex==ZBMD_SLAVE_2CH) mMsg = STR_WRITE+"ZB 모드_슬레이브 2채널  설정.";
					else if(mDlgIndex==ZBMD_SLAVE_4CH) mMsg = STR_WRITE+"ZB 모드_슬레이브 4채널  설정.";
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_1CH) mMsg = STR_WRITE+"ZB 모드_마스터_로컬 1채널  설정.";
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_2CH) mMsg = STR_WRITE+"ZB 모드_마스터_로컬 2채널  설정.";
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_4CH) mMsg = STR_WRITE+"ZB 모드_마스터_로컬 4채널  설정.";
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_LORA_1CH) mMsg = STR_WRITE+"ZB 모드_마스터_로컬_로라 1채널  설정.";
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_LORA_2CH) mMsg = STR_WRITE+"ZB 모드_마스터_로컬_로라 2채널  설정.";
					else if(mDlgIndex==ZBMD_MASTER_LOCAL_LORA_4CH) mMsg = STR_WRITE+"ZB 모드_마스터_로컬_로라 4채널  설정.";
					threadHandler.post(runResult);
				}
				break;
			case 7: // 환경센서 모드				
				if(mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENVIRONMENT_MODE, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg =  String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer));
						if(mMsg.equals("01\r")==true) mMsg = STR_READ+"환경센서 모드_온보드 센싱 설정.";
						else if(mMsg.equals("02\r")==true) mMsg = STR_READ+"환경센서 모드_외부장치 센싱 설정.";
						else if(mMsg.equals("11\r")==true) mMsg = STR_READ+"환경센서 모드_온보드 센싱 마스터_로컬 설정.";
						else if(mMsg.equals("12\r")==true) mMsg = STR_READ+"환경센서 모드_외부장치 센싱 마스터_로컬 설정.";
						else if(mMsg.equals("21\r")==true) mMsg = STR_READ+"환경센서 모드_온보드 센싱 마스터_로컬_로라 설정.";
						else if(mMsg.equals("22\r")==true) mMsg = STR_READ+"환경센서 모드_외부장치 센싱 마스터 로컬_로라 설정.";
						else mMsg = "환경센서 모드_미사용 설정.";
					}
					catch(Exception e) {
						mMsg = STR_READ+"환경센서 모드_알 수 없는 값.";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}					
					threadHandler.post(runResult);
				}
				else {					
					if(mDlgIndex==DISABLE) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENVIRONMENT_MODE, 0, IoTeepromHandler.DISABLE);
					else if(mDlgIndex==PVEM_SLAVE_INNER) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENVIRONMENT_MODE, 0, IoTeepromHandler.PVEM_SLAVE_INNER);
					else if(mDlgIndex==PVEM_SLAVE_OUTER) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENVIRONMENT_MODE, 0, IoTeepromHandler.PVEM_SLAVE_OUTER);
					else if(mDlgIndex==PVEM_MASTER_LOCAL_INNER) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENVIRONMENT_MODE, 0, IoTeepromHandler.PVEM_MASTER_LOCAL_INNER);
					else if(mDlgIndex==PVEM_MASTER_LOCAL_OUTER) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENVIRONMENT_MODE, 0, IoTeepromHandler.PVEM_MASTER_LOCAL_OUTER);
					else if(mDlgIndex==PVEM_MASTER_LOCAL_LORA_INNER) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENVIRONMENT_MODE, 0, IoTeepromHandler.PVEM_MASTER_LOCAL_LORA_INNER);
					else if(mDlgIndex==PVEM_MASTER_LOCAL_LORA_OUTER) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.ENVIRONMENT_MODE, 0, IoTeepromHandler.PVEM_MASTER_LOCAL_LORA_OUTER);
					else return ;
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					if(mDlgIndex==DISABLE) mMsg = STR_WRITE+"환경센서 모드_미사용 설정.";					
					else if(mDlgIndex==PVEM_SLAVE_INNER) mMsg = STR_WRITE+"환경센서 모드_온보드 센싱 설정.";
					else if(mDlgIndex==PVEM_SLAVE_OUTER) mMsg = STR_WRITE+"환경센서 모드_외부장치 센싱 설정.";
					else if(mDlgIndex==PVEM_MASTER_LOCAL_INNER) mMsg = STR_WRITE+"환경센서 모드_온보드 센싱 마스터_로컬 설정.";
					else if(mDlgIndex==PVEM_MASTER_LOCAL_OUTER) mMsg = STR_WRITE+"환경센서 모드_외부장치 센싱 마스터_로컬  설정.";
					else if(mDlgIndex==PVEM_MASTER_LOCAL_LORA_INNER) mMsg = STR_WRITE+"환경센서 모드_온보드 센싱 마스터_로컬_로라 설정.";
					else if(mDlgIndex==PVEM_MASTER_LOCAL_LORA_OUTER) mMsg = STR_WRITE+"환경센서 모드_외부장치 센싱 마스터_로컬_로라 설정.";
					threadHandler.post(runResult);
				}
				break;
			case 8: // 발전소 정보, 코디네이터 정보				
				if(mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.C1ES, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						int valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,0,2)), 16);
						mMsg = STR_READ+"국가번호_"+valueInt+"\r\n";
						valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,2,2)), 16);
						mMsg += "지역번호_"+valueInt+"\r\n";
						valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,4,4)), 16);
						mMsg += "국번_"+valueInt+"\r\n";
						valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,8,4)), 16);
						mMsg += "전화번호_"+valueInt+"\r\n";
						valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,12,2)), 16);
						mMsg += "인버터갯수_"+valueInt+"\r\n";
						valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,14,2)), 16);
						mMsg += "중계기갯수_"+valueInt+"\r\n";
						valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,16,2)), 16);
						mMsg += "어레이갯수_"+valueInt+"\r\n";
						valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,18,4)), 16);
						mMsg += "모듈센서갯수_"+valueInt+"\r\n";
						valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,22,2)), 16);
						mMsg += "DC PATH_"+valueInt+"\r\n";
					}
					catch(Exception e) {
						mMsg = STR_READ+"발전소 정보_알 수 없는 값.";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.C1CD, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						int valueInt = Integer.parseInt(String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,0,2)), 16);
						mMsg += "코디ID_"+valueInt+"\r\n";						
						mMsg += "코디 시리얼번호_"+String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer,2,16))+"\r\n";						
					}
					catch(Exception e) {
						mMsg = STR_READ+"코디 정보_알 수 없는 값.";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}
					threadHandler.post(runResult);
				}
				else {
					addByte = new byte[32];
					addByte[0] = '5';
					addByte[1] = '2'; // 국가번호 82
					addByte[2] = '4';
					addByte[3] = '0'; // 지역번호 64					
					addByte[4] = '0';
					addByte[5] = '2';
					addByte[6] = 'D'; //
					addByte[7] = 'B'; // 국번 731
					addByte[8] = '2';
					addByte[9] = '0';
					addByte[10] = 'F';
					addByte[11] = 'E'; // 전화번호 8446
					addByte[12] = '0';
					addByte[13] = '1'; // 인버터갯수
					addByte[16] = '0';
					addByte[17] = '1'; // 어레이갯수
					addByte[22] = '0';
					addByte[23] = '1'; // DC PATH
					addByte[24] = '0';
					addByte[25] = '0';
					addByte[26] = '0';
					addByte[27] = '0';
					addByte[28] = '0';
					addByte[29] = '0';
					addByte[30] = '0';
					addByte[31] = '0'; // null
					if(mCoordiCnt>255) return ;
					addByte[14] = (byte)HEX_DIGITS[(mCoordiCnt>>4)&0x0F];
					addByte[15] = (byte)HEX_DIGITS[mCoordiCnt&0x0F]; // 중계기갯수
					if(mMsensorCnt>65535) return ;
					addByte[18] = (byte)HEX_DIGITS[(mCoordiCnt>>12)&0x0F];
					addByte[19] = (byte)HEX_DIGITS[(mCoordiCnt>>8)&0x0F];
					addByte[20] = (byte)HEX_DIGITS[(mCoordiCnt>>4)&0x0F];
					addByte[21] = (byte)HEX_DIGITS[mCoordiCnt&0x0F]; // 모듈센서갯수
					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.C1ES, 0, addByte);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					if(mCoordiID>255) return ;
					addByte = new byte[18];
					addByte[0] = (byte)HEX_DIGITS[(mCoordiID>>4)&0x0F];
					addByte[1] = (byte)HEX_DIGITS[mCoordiID&0x0F]; // 코디ID
					addByte[2] = '0';
					addByte[3] = '0';
					addByte[4] = '1';
					addByte[5] = '3';
					addByte[6] = 'A';
					addByte[7] = '2';
					addByte[8] = '0';
					addByte[9] = '0';
					addByte[10] = '4';
					addByte[11] = '0';
					addByte[12] = '0';
					addByte[13] = '0';
					addByte[14] = '0';
					addByte[15] = '0';
					addByte[16] = '0';
					addByte[17] = '0'; // null 값				
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.C1CD, 0, addByte);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					mMsg = STR_WRITE+"_중계기갯수_"+mCoordiCnt+"\r\n"+
							"모듈센서갯수_"+mMsensorCnt+"\r\n"+
							"코디ID_"+mMsensorCnt+"\r\n";
					threadHandler.post(runResult);
				}
				break;
			case 10: // REMS 로라서브모뎀
				if(mWriteFlag==false) {					
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.LORAMODEM_SUB, 0, null);
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					try {
						mMsg =  String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer));
						if(mMsg.equals("01")==true) mMsg = STR_READ+"로라모뎀_IPL 모뎀(SKT) 설정.";
						else if(mMsg.equals("02")==true) mMsg = STR_READ+"로라모뎀_FIM 모뎀(SKT) 설정.";
						else if(mMsg.equals("03")==true) mMsg = STR_READ+"로라모뎀_노드링크 모뎀(자가망) 설정.";
						else mMsg = "로라모뎀_미사용 설정.";
					}
					catch(Exception e) {
						mMsg = STR_READ+"로라모뎀_알 수 없는 값.";						
						bufferStr = e.getMessage();
						threadHandler.post(runLog);
					}					
					threadHandler.post(runResult);
				}
				else {					
					if(mLoraIndex==DISABLE) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.LORAMODEM_SUB, 0, IoTeepromHandler.DISABLE);
					else if(mLoraIndex==LORA_IPL) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.LORAMODEM_SUB, 0, IoTeepromHandler.LORA_IPL);
					else if(mLoraIndex==LORA_F1M) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.LORAMODEM_SUB, 0, IoTeepromHandler.LORA_F1M);
					else if(mLoraIndex==LORA_NODELINK) 
						writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.LORAMODEM_SUB, 0, IoTeepromHandler.LORA_NODELINK);
					else return ;
					mTerminal.writeBytes(writeBuffer, 300);
					bufferStr = ConvertData.bytesToStringLog(writeBuffer);
					threadHandler.post(runLog);
					try {					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					readBuffer = mTerminal.getReceivedData();
					if(readBuffer==null) {
						mMsg = "응답없음.";
						threadHandler.post(runResult);
						return ;
					}
					if(iotHandler.verifyResponse(readBuffer)!=true) {
						mMsg = "올바르지 않은 응답.";
						threadHandler.post(runResult);
						return ;
					}
					if(mLoraIndex==DISABLE) mMsg = STR_WRITE+"로라서브모뎀_미사용 설정.";					
					else if(mLoraIndex==LORA_IPL) mMsg = STR_WRITE+"로라서브모뎀_IPL 모뎀(SKT) 설정.";
					else if(mLoraIndex==LORA_F1M) mMsg = STR_WRITE+"로라서브모뎀_FIM 모뎀(SKT) 설정.";
					else if(mLoraIndex==LORA_NODELINK) mMsg = STR_WRITE+"로라서브모뎀_노드링크 모뎀(자가망) 설정.";					
					threadHandler.post(runResult);
				}
				break;
			case 11:
				if(mWriteFlag==false) return ;
				if(mDevice!=MyProcess3Activity.IOT_NORMAL && mDevice!=MyProcess3Activity.IOT_REMS) return ;				
				switch(mDlgIndex) {
				case DECMD_TXPK:
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.TXPK, 0, null);					
					break;
				case DECMD_INFO_FVER:
					if(mDevice==MyProcess3Activity.IOT_NORMAL) writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.INFO, 0, null);
					else if(mDevice==MyProcess3Activity.IOT_REMS) writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.FVER, 0, null);
					break;
				case DECMD_RCNT:
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.RCNT, 0, null);
					break;
				case DECMD_LRST:
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.LRST, 0, null);
					break;
				case DECMD_LSRS:
					writeBuffer = iotHandler.getCommand(IoTeepromHandler.Commandrems.LSRS, 0, null);
					break;
				}
				mTerminal.writeBytes(writeBuffer, 300);
				bufferStr = ConvertData.bytesToStringLog(writeBuffer);				
				threadHandler.post(runLog);							
				mMsg = "로그창에서 결과 확인하시오.";
				threadHandler.post(runResult);
				break;
			case 12:
				writeBuffer = iotHandler.getCommand(IoTeepromHandler.Command.EXIT, 0, null);				
				mTerminal.writeBytes(writeBuffer, 300);
				bufferStr = ConvertData.bytesToStringLog(writeBuffer);
				threadHandler.post(runLog);
				try {					
					Thread.sleep(1000);						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				readBuffer = mTerminal.getReceivedData();
				if(readBuffer==null) {
					mMsg = "응답없음.";
					threadHandler.post(runResult);
					return ;
				}
				mMsg =  String.valueOf(ConvertData.bytesAsciiToCharArray(readBuffer));
				if(mDevice==MyProcess3Activity.IOT_NORMAL && mMsg.equals("CMD time out\n")!=true) {
					mMsg = "올바르지 않은 응답. ";
					threadHandler.post(runResult);
					return ;
				}
				else if (mDevice==MyProcess3Activity.IOT_REMS && mMsg.equals("Command mode out!")!=true) {
					mMsg = "올바르지 않은 응답. ";
					threadHandler.post(runResult);
					return ;
				}
				mMsg = "장치 커멘드 모드 탈출";
				threadHandler.post(runResult);
				break;
			}
			mScroll.fullScroll(View.FOCUS_DOWN);
		}
	}
}
