package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class SolarLampOptimizer extends Equipment {
	public enum Command {
		GROUP0_MODE, GROUP1_STATUS, GROUP2_DATA, GROUP3_SETDATA, GROUP4_BMS
	}	
	private static final String SEQUENCE_STATE = "알 수 없는 상태";
	private static final String SEQUENCE_STATE0 = "사용자모드_배터리와 통신 불가";
	private static final String SEQUENCE_STATE1 = "사용자모드_배터리 초기 충전 모드";
	private static final String SEQUENCE_STATE2 = "사용자모드_배터리 충전모드(PV발전)";
	private static final String SEQUENCE_STATE3 = "사용자모드_LED 방전모드(Set power3)";
	private static final String SEQUENCE_STATE4 = "사용자모드_LED 방전모드(Set power2)";
	private static final String SEQUENCE_STATE5 = "사용자모드_LED 방전모드(Set power1)";
	private static final String SEQUENCE_STATE6 = "사용자모드_Unkwown State";
	private static final String SEQUENCE_STATE7 = "사용자모드_master run off";
	private static final String SEQUENCE_STATE10 = "엔지니어 모드";
	private static final String SEQUENCE_STATE20 = "관리자 모드";
	public static final String ERROR_NO_RESPONSE = "장치 응답 없음.";
	public static final String ERROR_REQUEST = "Invalid request range.";
	public static final String ERROR_ID = "Invalid equipment ID";
	public static final String ERROR_CMD = "Invalid equipment Command";
	public static final String ERROR_LENGTH = "Invalid packet length";
	public static final String ERROR_PACKET = "Invalid equipment Packet";
	public static final String ERROR_CKSUM = "Invalid equipment Checksum";
	private static final String ERROR_NORMAL = "시스템 정상운전";
	private static final String ERROR_STATUS0 = "태양전지 과전압";
	private static final String ERROR_STATUS1 = "LED 과전압";
	private static final String ERROR_STATUS2 = "배터리 과전압";
	private static final String ERROR_STATUS3 = "인덕터 과전류";
	private static final String ERROR_STATUS4 = "태양전지 과전류";
	private static final String ERROR_STATUS5 = "배터리 과전류";
	private static final String ERROR_STATUS6 = "RTC 기능 불량";
	private static final String FAULT_BATTERYMODULE = "배터리 정상 운전";
	private static final String FAULT_BATTERYMODULE2 = "셀 과전압 Fault";
	private static final String FAULT_BATTERYMODULE3 = "셀 저전압 Fault";
	private static final String FAULT_BATTERYMODULE4 = "과충전전류 Fault";
	private static final String FAULT_BATTERYMODULE5 = "과방전전류 Fault";
	private static final String FAULT_BATTERYMODULE6 = "과온도 Fault";
	private static final String FAULT_BATTERYMODULE7 = "저온도 Fault";
	private static final String FAULT_BATTERYMODULE8 = "셀전압 불균형 Fault";
	private static final String FAULT_BATTERYMODULE9 = "셀온도 불균형 Fault";
	private static final String FAULT_BATTERYMODULE10 = "과중전 Fault";
	private static final String FAULT_BATTERYMODULE11 = "과방전 Fault";
	private static final String FAULT_BATTERYMODULE12 = "내부 통신 Fault";
	public static final int COMMUNICATION_DELAY_MS = 600;
	private Thread mThread;
	String mMsg = "";
	private String mSequnceState = "";
	private String mErrorStatus = "";	
	private String mFaultBatt = "";
	private int mEngineerMode;	
	private int mOpMode;
	private int mSetLampPower;
	private int mFoceRun;
	private int mFaultReset;	
	private int mFaultAutoResetCount;
	private int mPowerOptimizerStatus;	
	private int pvV = 0; // V fix 2
	private int pvI = 0; // A fix 2
	private int pvP = 0; // W fix 1
	private int pvTotal = 0; // W fix 1
	private int ledV = 0; // V fix 2
	private int ledI = 0; // A fix 2 
	private int battV = 0; // V fix 2
	private short battI = 0; // A fix 2
	private short battP = 0; // W fix 1
	private int battChargingPowerTotal = 0; // 
	private int battDischargingPowerTotal = 0;
	private int rtcSec = 0;
	private int rtcMin = 0;
	private int rtcHour = 0;
	private int rtcDayOfWeek = 0; // 1(Sunday) ~  7
	private int rtcDate = 0; // 1 ~ 31
	private int rtcMonth = 0;
	private int rtcYear = 0; // 0 ~ 99
	private int lampSetHour1 = 0;
	private int lampSetHour2 = 0;
	private int lampSetHour3 = 0;
	private int lampSetHour4 = 0;
	private int lampSetPower1 = 0;
	private int lampSetPower2 = 0;
	private int lampSetPower3 = 0;
	private int setLowSOC = 0;
	private int setHighSOC = 0;
	private int rtcSetSec = 0;
	private int rtcSetMin = 0;
	private int rtcSetHour = 0;
	private int rtcSetDayOfWeek = 0;
	private int rtcSetDate = 0;
	private int rtcSetMonth = 0;
	private int rtcSetYear = 0;
	private int battStatus = 0;
	private int battSOC = 0;
	private int battSOH = 0;
	private int battTemp1 = 0;
	private int battTemp2 = 0;
	private int bmsID = 0;
	private int battModuleV = 0; 
	private int battModuleI = 0; // A fix 1
	private int cellVoltMin = 0; // V fix 3
	private int cellVoltMax = 0; // V fix 3
	private int cellVoltAvg = 0; // V fix 3
	public SolarLampOptimizer() {
		super.setEquipInfo("Solar Lamp", Equipment.EquipInfo.EQUIPTYPE);
		super.setEquipInfo("(주)데스틴파워", Equipment.EquipInfo.MANUFACTURER);
		super.setEquipInfo("태양광가로등 옵티마이저", Equipment.EquipInfo.ETCINFO);
	}
	void initData() {
		
	}
	public String getEquipInfo() {
		return super.getEquipInfo();
	}
	public String getMessage() {
		return this.mMsg;
	}
	@Override
	public void setEquipInfo(String str, Equipment.EquipInfo info) {
		super.setEquipInfo(str, info);
	}
	public void runCommunicationThread(Terminal terminal, int id, EditText log, EditText result) {
		mThread = new RequestDataThread(terminal, id, log, result);
		mThread.setDaemon(true);
		mThread.start();
	}
	public byte[] getRequestPacket(int id, byte functionCode, int address, int length) {
		// MODBUS-RTU protocol
		byte[] packet = new byte[8];		
		if(id>99) {
			mMsg = "ID_ERROR";
			return null;
		}
		packet[0] = (byte)id; // ID
		packet[1] = functionCode;		
		packet[2] = (byte)(address>>8);
		packet[3] = (byte)address; 
		packet[4] = (byte)(length>>8);
		packet[5] = (byte)length;
		CyclicalRedundancyCheck crc16 = new CyclicalRedundancyCheck();
		byte[] crc = crc16.getCRC16Bytes(packet, 6, CyclicalRedundancyCheck.Crc16.CRC16_MODBUS);
		packet[6] = (byte)crc[0];
		packet[7] = (byte)crc[1];					
		return packet;
	}
	public byte[] getRequestPacket(int id, SolarLampOptimizer.Command cmd) {		
		byte[] packet = null;		
		switch(cmd) {
		case GROUP0_MODE:
			packet = getRequestPacket(id, (byte)0x03, 40001, 7);
			break;
		case GROUP1_STATUS:
			packet = getRequestPacket(id, (byte)0x03, 41001, 2);
			break;
		case GROUP2_DATA:
			packet = getRequestPacket(id, (byte)0x03, 42001, 18);
			break;
		case GROUP3_SETDATA:
			packet = getRequestPacket(id, (byte)0x03, 43001, 16);
			break;
		case GROUP4_BMS:
			packet = getRequestPacket(id, (byte)0x03, 44001, 12);
			break;		
		default:
			this.mMsg = SolarLampOptimizer.ERROR_REQUEST;
			return null;
		}
		return packet;
	}
	public boolean verifyResponse(byte[] src, int id, byte functionCode) {
		// verify header1, 2		
		if (src.length<2 || src[1]!=functionCode) {
			mMsg = SolarLampOptimizer.ERROR_PACKET;
			return false;
		}
		if (src[0]!=id) {
			mMsg = SolarLampOptimizer.ERROR_ID;
			return false;
		}
		CyclicalRedundancyCheck crcModbus = new CyclicalRedundancyCheck();
		byte[] crc = crcModbus.getCRC16Bytes(src, src.length-2, CyclicalRedundancyCheck.Crc16.CRC16_MODBUS);

		if (crc[0]!=(byte)src[src.length-2] || crc[1]!=(byte)src[src.length-1]) {
			mMsg =  SolarLampOptimizer.ERROR_CKSUM;
			return false;
		}
		return true;
	}
	public String getData() {
		String engMode;
		String opMode;
		String battStatus;
		if(this.mEngineerMode==0) engMode = "엔지니어 모드";
		else if(this.mEngineerMode==1) engMode = "유저 모드";
		else engMode="알수없는 값";
		if(this.mOpMode==0) opMode = "배터리 충전 모드";
		else if(this.mOpMode==1) opMode = "LED dimming 모드";
		else if(this.mOpMode==2) opMode = "Precharge 모드";
		else if(this.mOpMode==3) opMode = "Open loop 모드";
		else opMode="알수없는 값";
		if(this.battStatus==0) battStatus = "OFF";
		else if(this.battStatus==1) battStatus = "Charging";
		else if(this.battStatus==2) battStatus = "DisCharging";
		else if(this.battStatus==3) battStatus = "Idle";
		else battStatus="알수없는 값";
		StringBuilder returnStr = new StringBuilder();		
		returnStr.append("Enginner mode activation: "+engMode+"\r\n"+
				"옵티마이저 모드: "+opMode+"\r\n"+
				"Set lamp power: "+this.mSetLampPower+"\r\n"+
				"Foce run: "+this.mFoceRun+"\r\n"+
				"Fault reset: "+this.mFaultReset+"\r\n"+
				"Sequence state: "+this.mSequnceState+"\r\n"+
				"Fault auto reset count: "+this.mFaultAutoResetCount+"\r\n"+
				"Power optimizer status: "+this.mPowerOptimizerStatus+"\r\n"+
				"Error status: "+this.mErrorStatus+"\r\n"+
				"PV V: "+this.pvV/100+"."+this.pvV%100+" V\r\n"+
				"PV I: "+this.pvI/100+"."+this.pvI%100+" A\r\n"+
				"PV P: "+this.pvP/10+"."+this.pvP%10+" W\r\n"+
				"PV cumulative power: "+this.pvTotal+" W\r\n"+
				"LED V: "+this.ledV/100+"."+this.ledV%100+" V\r\n"+
				"LED I: "+this.ledI/100+"."+this.ledI%100+" A\r\n"+
				"Batt V: "+this.battV/100+"."+this.battV%100+" V\r\n");
		if(this.battI<0) returnStr.append("Batt I: "+this.battI/100+"."+this.battI%100*(-1)+" A\r\n");
		else returnStr.append("Batt I: "+this.battI/100+"."+this.battI%100+" A\r\n");
		if(this.battP<0)returnStr.append("Batt P: "+this.battP/10+"."+this.battP%10*(-1)+" W\r\n");
		else returnStr.append("Batt P: "+this.battP/10+"."+this.battP%10+" W\r\n");
		returnStr.append("Batt cumulative charging power: "+this.battChargingPowerTotal+" W\r\n"+
				"Batt cumulative discharging power: "+this.battDischargingPowerTotal+" W\r\n"+
				"RTC 값: "+this.rtcYear+"년 "+this.rtcMonth+"월 "+this.rtcDate+"일 "+
				this.rtcHour+"시 "+this.rtcMin+"분 "+this.rtcSec+"초\r\n"+
				"PV lamp set hour1: "+this.lampSetHour1+" 시\r\n"+
				"PV lamp set hour2: "+this.lampSetHour2+" 시\r\n"+
				"PV lamp set hour3: "+this.lampSetHour3+" 시\r\n"+
				"PV lamp set hour4: "+this.lampSetHour4+" 시\r\n"+
				"PV lamp set power1: "+this.lampSetPower1+" %\r\n"+
				"PV lamp set power2: "+this.lampSetPower2+" %\r\n"+
				"PV lamp set power3: "+this.lampSetPower3+" %\r\n"+
				"Set low SOC: "+this.setLowSOC+" %\r\n"+
				"Set high SOC: "+this.setHighSOC+" %\r\n"+
				"Batt Status: "+battStatus+"\r\n"+
				"Batt module fault1: "+this.mFaultBatt+"\r\n"+
				"Batt SOC: "+this.battSOC+" %\r\n"+
				"Batt SOH: "+this.battSOH+" %\r\n"+
				"Batt temp1: "+this.battTemp1+" 도\r\n"+
				"Batt temp2: "+this.battTemp2+" 도\r\n"+
				"BMS ID: "+this.bmsID+" 번\r\n"+
				"Batt Module V: "+this.battModuleV/10+"."+this.battModuleV%10+" V\r\n"+
				"Batt Module I: "+this.battModuleI/10+"."+this.battModuleI%10+" A\r\n"+
				"Min. cell V: "+this.cellVoltMin/1000+"."+this.cellVoltMin%1000+" V\r\n"+
				"Max. cell V: "+this.cellVoltMax/1000+"."+this.cellVoltMax%1000+" V\r\n"+
				"Avg. cell V: "+this.cellVoltAvg/1000+"."+this.cellVoltAvg%1000+" V\r\n");				
		return returnStr.toString();
	}
	public boolean setData(byte[] src, SolarLampOptimizer.Command cmd) {
		int data;		
		switch(cmd) {
		case GROUP0_MODE:
			if(src.length<17) {
				this.mMsg = SolarLampOptimizer.ERROR_LENGTH + src.length;
				return false;
			}
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			this.mEngineerMode = data;
			data = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			this.mOpMode = data;
			data = (src[7]&0xFF)<<8 | (src[8]&0xFF);
			this.mSetLampPower = data;
			data = (src[9]&0xFF)<<8 | (src[10]&0xFF);
			this.mFoceRun = data;
			data = (src[11]&0xFF)<<8 | (src[12]&0xFF);
			this.mFaultReset = data;
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);
			if(data==0) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE0;
			else if(data==1) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE1;
			else if(data==2) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE2;
			else if(data==3) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE3;
			else if(data==4) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE4;
			else if(data==5) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE5;
			else if(data==6) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE6;
			else if(data==7) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE7;
			else if(data==10) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE10;
			else if(data==20) this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE20;
			else this.mSequnceState=SolarLampOptimizer.SEQUENCE_STATE;
			data = (src[15]&0xFF)<<8 | (src[16]&0xFF);
			this.mFaultAutoResetCount = data;
			break;
		case GROUP1_STATUS:
			if(src.length<9) {
				this.mMsg = SolarLampOptimizer.ERROR_LENGTH + src.length;
				return false;
			}
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			this.mPowerOptimizerStatus = data;
			data = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			if((byte)(data&0b00000001)!=0) this.mErrorStatus=SolarLampOptimizer.ERROR_STATUS0;
			else if((byte)(data&0b00000010)!=0) this.mErrorStatus=SolarLampOptimizer.ERROR_STATUS1;
			else if((byte)(data&0b00000100)!=0) this.mErrorStatus=SolarLampOptimizer.ERROR_STATUS2;
			else if((byte)(data&0b00001000)!=0) this.mErrorStatus=SolarLampOptimizer.ERROR_STATUS3;
			else if((byte)(data&0b00010000)!=0) this.mErrorStatus=SolarLampOptimizer.ERROR_STATUS4;
			else if((byte)(data&0b00100000)!=0) this.mErrorStatus=SolarLampOptimizer.ERROR_STATUS5;
			else if((byte)(data&0b01000000)!=0) this.mErrorStatus=SolarLampOptimizer.ERROR_STATUS6;
			else this.mErrorStatus=SolarLampOptimizer.ERROR_NORMAL;
			break;
		case GROUP2_DATA:
			if(src.length<47) {
				this.mMsg = SolarLampOptimizer.ERROR_LENGTH + src.length;
				return false;
			}
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			this.pvV = data;
			data = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			this.pvI = data;
			data = (src[7]&0xFF)<<8 | (src[8]&0xFF);
			this.pvP = data;
			data = (src[9]&0xFF)<<24 | (src[10]&0xFF)<<16 | (src[11]&0xFF)<<8 | (src[12]&0xFF);
			this.pvTotal = data;
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);
			this.ledV = data;
			data = (src[15]&0xFF)<<8 | (src[16]&0xFF);
			this.ledI = data;
			data = (src[17]&0xFF)<<8 | (src[18]&0xFF);
			this.battV = data;			
			this.battI = (short) ((src[19]&0xFF)<<8 | (src[20]&0xFF));			
			this.battP = (short) ((src[21]&0xFF)<<8 | (src[22]&0xFF));
			data = (src[23]&0xFF)<<24 | (src[24]&0xFF)<<16 | (src[25]&0xFF)<<8 | (src[26]&0xFF);
			this.battChargingPowerTotal = data;
			data = (src[27]&0xFF)<<24 | (src[28]&0xFF)<<16 | (src[29]&0xFF)<<8 | (src[30]&0xFF);
			this.battDischargingPowerTotal = data;
			data = (src[31]&0xFF)<<8 | (src[32]&0xFF);
			this.rtcSec = data;
			data = (src[33]&0xFF)<<8 | (src[34]&0xFF);
			this.rtcMin = data;
			data = (src[35]&0xFF)<<8 | (src[36]&0xFF);
			this.rtcHour = data;
			data = (src[37]&0xFF)<<8 | (src[38]&0xFF);
			this.rtcDayOfWeek = data;
			data = (src[39]&0xFF)<<8 | (src[40]&0xFF);
			this.rtcDate = data;
			data = (src[41]&0xFF)<<8 | (src[42]&0xFF);
			this.rtcMonth = data;
			data = (src[43]&0xFF)<<8 | (src[44]&0xFF);
			this.rtcYear = data;
			break;
		case GROUP3_SETDATA:
			if(src.length<19) {
				this.mMsg = SolarLampOptimizer.ERROR_LENGTH + src.length;
				return false;
			}
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			this.lampSetHour1 = data;
			data = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			this.lampSetHour2 = data;
			data = (src[7]&0xFF)<<8 | (src[8]&0xFF);
			this.lampSetHour3 = data;
			data = (src[9]&0xFF)<<8 | (src[10]&0xFF);
			this.lampSetHour4 = data;
			data = (src[11]&0xFF)<<8 | (src[12]&0xFF);
			this.lampSetPower1 = data;
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);
			this.lampSetPower2 = data;
			data = (src[15]&0xFF)<<8 | (src[16]&0xFF);
			this.lampSetPower3 = data;
			break;
		case GROUP4_BMS:
			if(src.length<29) {
				this.mMsg = SolarLampOptimizer.ERROR_LENGTH + src.length;
				return false;
			}
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			this.battStatus = data;
			data = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			if((byte)(data&0b00000100)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE2;
			else if((byte)(data&0b00001000)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE3;
			else if((byte)(data&0b00010000)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE4;
			else if((byte)(data&0b00100000)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE5;
			else if((byte)(data&0b01000000)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE6;
			else if((byte)(data&0b10000000)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE7;
			else if((byte)((data>>8)&0b00000001)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE8;
			else if((byte)((data>>8)&0b00000010)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE9;
			else if((byte)((data>>8)&0b00000100)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE10;
			else if((byte)((data>>8)&0b00001000)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE11;
			else if((byte)((data>>8)&0b00010000)!=0) this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE12;
			else this.mFaultBatt=SolarLampOptimizer.FAULT_BATTERYMODULE;
			data = (src[7]&0xFF)<<8 | (src[8]&0xFF);
			this.battSOC = data;
			data = (src[9]&0xFF)<<8 | (src[10]&0xFF);
			this.battSOH = data;
			data = (src[11]&0xFF)<<8 | (src[12]&0xFF);
			this.battTemp1 = data;
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);
			this.battTemp2 = data;
			data = (src[15]&0xFF)<<8 | (src[16]&0xFF);
			this.bmsID = data;
			data = (src[17]&0xFF)<<8 | (src[18]&0xFF);
			this.battModuleV = data;
			data = (src[19]&0xFF)<<8 | (src[20]&0xFF);
			this.battModuleI = data;
			data = (src[21]&0xFF)<<8 | (src[22]&0xFF);
			this.cellVoltMin = data;
			data = (src[23]&0xFF)<<8 | (src[24]&0xFF);
			this.cellVoltMax = data;
			data = (src[25]&0xFF)<<8 | (src[26]&0xFF);
			this.cellVoltAvg = data;
			break;
		default:
			this.mMsg = SolarLampOptimizer.ERROR_REQUEST;
			return false;
		}
		return true;
	}
	private class RequestDataThread extends Thread {
		int id;		
		SimpleDateFormat simpleDate;
		String strPacket;		
		Terminal terminal;
		EditText log, result;
		Editable edit;
		byte[] txPacketbuff;
		byte[] rxPacketbuff;
		Handler threadHandler = new Handler();
		Runnable runReault =  new Runnable() {
			public void run() {
				result.setText(getMessage());				
			}
		};
		RequestDataThread(Terminal terminal, int id, EditText log, EditText result){
			this.id = id;
			this.terminal = terminal;
			this.log = log;			
			this.result = result;
		}
		public void run() {
			initData();
			txPacketbuff = getRequestPacket(id, SolarLampOptimizer.Command.GROUP0_MODE);
			if(txPacketbuff==null) return ;
			terminal.initReceivedData();
			try {
				terminal.writeBytes(txPacketbuff, 300);
			}		
			catch(Exception e) {
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}			
			simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
			strPacket = simpleDate.format(new Date()) + "Transmit " + txPacketbuff.length + " bytes: \n"
			        + HexDump.dumpHexString(txPacketbuff, txPacketbuff.length) + "\r\n";
			threadHandler.post(new Runnable() {
				public void run() {
					edit = log.getText();
					edit.append(strPacket);					
				}						
			});
			try {
				Thread.sleep(COMMUNICATION_DELAY_MS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}
			if(terminal.isReceivedData()!=true) {
				mMsg = ERROR_NO_RESPONSE;
				threadHandler.post(runReault);		
				return;
			}			
			rxPacketbuff = terminal.getReceicedData();				
			if(verifyResponse(rxPacketbuff, id, (byte)0x03)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setData(rxPacketbuff, SolarLampOptimizer.Command.GROUP0_MODE)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, SolarLampOptimizer.Command.GROUP1_STATUS);
			if(txPacketbuff==null) return ;
			terminal.initReceivedData();
			try {
				terminal.writeBytes(txPacketbuff, 300);
			}		
			catch(Exception e) {
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}			
			simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
			strPacket = simpleDate.format(new Date()) + "Transmit " + txPacketbuff.length + " bytes: \n"
			        + HexDump.dumpHexString(txPacketbuff, txPacketbuff.length) + "\r\n";
			threadHandler.post(new Runnable() {
				public void run() {
					edit = log.getText();
					edit.append(strPacket);					
				}						
			});
			try {
				Thread.sleep(COMMUNICATION_DELAY_MS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}
			if(terminal.isReceivedData()!=true) {
				mMsg = ERROR_NO_RESPONSE;
				threadHandler.post(runReault);		
				return;
			}			
			rxPacketbuff = terminal.getReceicedData();				
			if(verifyResponse(rxPacketbuff, id, (byte)0x03)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setData(rxPacketbuff, SolarLampOptimizer.Command.GROUP1_STATUS)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, SolarLampOptimizer.Command.GROUP2_DATA);
			if(txPacketbuff==null) return ;
			terminal.initReceivedData();
			try {
				terminal.writeBytes(txPacketbuff, 300);
			}		
			catch(Exception e) {
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}			
			simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
			strPacket = simpleDate.format(new Date()) + "Transmit " + txPacketbuff.length + " bytes: \n"
			        + HexDump.dumpHexString(txPacketbuff, txPacketbuff.length) + "\r\n";
			threadHandler.post(new Runnable() {
				public void run() {
					edit = log.getText();
					edit.append(strPacket);					
				}						
			});
			try {
				Thread.sleep(COMMUNICATION_DELAY_MS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}
			if(terminal.isReceivedData()!=true) {
				mMsg = ERROR_NO_RESPONSE;
				threadHandler.post(runReault);		
				return;
			}			
			rxPacketbuff = terminal.getReceicedData();				
			if(verifyResponse(rxPacketbuff, id, (byte)0x03)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setData(rxPacketbuff, SolarLampOptimizer.Command.GROUP2_DATA)!=true) {
				threadHandler.post(runReault);
				return;
			}
			txPacketbuff = getRequestPacket(id, SolarLampOptimizer.Command.GROUP3_SETDATA);
			if(txPacketbuff==null) return ;
			terminal.initReceivedData();
			try {
				terminal.writeBytes(txPacketbuff, 300);
			}		
			catch(Exception e) {
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}			
			simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
			strPacket = simpleDate.format(new Date()) + "Transmit " + txPacketbuff.length + " bytes: \n"
			        + HexDump.dumpHexString(txPacketbuff, txPacketbuff.length) + "\r\n";
			threadHandler.post(new Runnable() {
				public void run() {
					edit = log.getText();
					edit.append(strPacket);					
				}						
			});
			try {
				Thread.sleep(COMMUNICATION_DELAY_MS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}
			if(terminal.isReceivedData()!=true) {
				mMsg = ERROR_NO_RESPONSE;
				threadHandler.post(runReault);		
				return;
			}			
			rxPacketbuff = terminal.getReceicedData();				
			if(verifyResponse(rxPacketbuff, id, (byte)0x03)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setData(rxPacketbuff, SolarLampOptimizer.Command.GROUP3_SETDATA)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, SolarLampOptimizer.Command.GROUP4_BMS);
			if(txPacketbuff==null) return ;
			terminal.initReceivedData();
			try {
				terminal.writeBytes(txPacketbuff, 300);
			}		
			catch(Exception e) {
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}			
			simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
			strPacket = simpleDate.format(new Date()) + "Transmit " + txPacketbuff.length + " bytes: \n"
			        + HexDump.dumpHexString(txPacketbuff, txPacketbuff.length) + "\r\n";
			threadHandler.post(new Runnable() {
				public void run() {
					edit = log.getText();
					edit.append(strPacket);					
				}						
			});
			try {
				Thread.sleep(COMMUNICATION_DELAY_MS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}
			if(terminal.isReceivedData()!=true) {
				mMsg = ERROR_NO_RESPONSE;
				threadHandler.post(runReault);		
				return;
			}			
			rxPacketbuff = terminal.getReceicedData();				
			if(verifyResponse(rxPacketbuff, id, (byte)0x03)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setData(rxPacketbuff, SolarLampOptimizer.Command.GROUP4_BMS)!=true) {
				threadHandler.post(runReault);
				return;
			}
			threadHandler.post(new Runnable() {
				public void run() {
					result.setText(getData());				
				}						
			});
		}
	}
}
