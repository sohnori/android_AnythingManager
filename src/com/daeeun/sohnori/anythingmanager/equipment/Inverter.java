package com.daeeun.sohnori.anythingmanager.equipment;

public class Inverter extends Equipment{
	public enum Phase  {
		SINGLE, THREE, SPECIAL	
	}
	
	public enum Status  {
		STOP, RUN 	
	}
	
	public enum Data {
		PVV, PVI, PVP, GRIDRV, GRIDSV, GRIDTV, GRIDRI, GRIDSI, GRIDTI, GRIDREALPOWER, TOTALPOWER, 
		FREQUENCY, POWERFACTOR
	}
	
	public enum Fault {
		NORMAL, PV_OV, PV_UV, PV_OC, INV_IGBT, INV_OVERH, GRID_OV, GRID_UV, GRID_OC, GRID_OF, GRID_UF, 
		STANDALONE, EARTH_FAULT, ETC_FAULT
	}
	public static final String ERROR_NO_RESPONSE = "인버터 응답 없음.";
	public static final String ERROR_REQUEST = "Invalid request range.";
	public static final String ERROR_ID = "Invalid Inveter ID";
	public static final String ERROR_CMD = "Invalid Inveter Command";
	public static final String ERROR_PACKET = "Invalid Inveter Packet";
	public static final String ERROR_CKSUM = "Invalid Inveter Checksum";
	public static final String ERROR_PHASE = "Invalid Inveter Phase";
	private static final String FAULT_NORMAL = "정상운전";
	private static final String FAULT_PV_OV = "태양전지 과전압";
	private static final String FAULT_PV_UV = "태양전지 저전압";
	private static final String FAULT_PV_OC = "태양전지 과전류";
	private static final String FAULT_INV_IGBT = "인버터 IGBT 에러";
	private static final String FAULT_INV_OVERH = "인버터 과온 검출";
	private static final String FAULT_GRID_OV = "계통 과전압";
	private static final String FAULT_GRID_UV = "계통 저전압";
	private static final String FAULT_GRID_OC = "계통 과전류";
	private static final String FAULT_GRID_OF = "계통 과주파수";
	private static final String FAULT_GRID_UF = "계통 저주파수";
	private static final String FAULT_STANDALONE = "단독운전(정전)";
	private static final String FAULT_EARTH = "지락(누전)";
	private static final String FAULT_ETC = "기타 에러";
	public static final int COMMUNICATION_DELAY_MS = 600;
	protected Phase mPhase;
	private Status mStatus;
	protected String mMsg = "";
	protected byte mFaultCodeHigh;
	protected byte mFaultCodeLow;
	protected int mInvTemp; // celsius_fixed 0.1
	protected int mPvV; // V
	protected int mPvI; // A_fixed 0.1
	protected int mPvP; // kWh_fixed 0.1
	protected int mGridRV; // V
	protected int mGridSV; // V
	protected int mGridTV; // V
	protected int mGridRI; // A_fixed 0.1
	protected int mGridSI; // fixed 0.1
	protected int mGridTI; // fixed 0.1
	protected int mGridRealP; // kWh_fixed 0.1
	protected int mTotalP; //kWh
	protected int mFrequency; // Hz_fixed 0.1
	protected int mPf; // fixed 0.1
	
	public Inverter(Inverter.Phase phase){
		this.mPhase = phase;
		super.setEquipInfo("Inverter", Equipment.EquipInfo.EQUIPTYPE);
	}
	
	void initInverter() {
		super.initEquipment();
		this.initInverterData();
	}
	
	void initInverterData() {
		this.mMsg = "";		
		this.mStatus = Status.STOP;
		this.mFaultCodeHigh = 0;
		this.mFaultCodeLow = 0;
		this.mInvTemp = -1;
		this.mPvV = -1;
		this.mPvI = -1;
		this.mPvP = -1;
		this.mGridRV = -1;
		this.mGridSV = -1;
		this.mGridTV = -1;
		this.mGridRI = -1;
		this.mGridSI = -1;
		this.mGridTI = -1;
		this.mGridRealP = -1;
		this.mTotalP = -1;
		this.mFrequency = -1;
		this.mPf = -1;
	}
	
	public String getEquipInfo() {
		return super.getEquipInfo();
	}
	
	@Override
	public void setEquipInfo(String str, EquipInfo InfoCode) {
		super.setEquipInfo(str, InfoCode);
	}
	
	public String getMessage() {
		return this.mMsg;
	}
			
	public String getInverterData() {
		StringBuilder returnStr = new StringBuilder();		
		returnStr.append("PV voltage: "+this.mPvV+" V\r\n"+
				"PV current: "+this.mPvI/10+"."+this.mPvI%10+" A\r\n"+
				"PV power: "+this.mPvP/10+"."+this.mPvP%10+" Kwh\r\n");
		if(this.mPhase==Inverter.Phase.SINGLE) {
			returnStr.append("Grid voltage: "+this.mGridRV+" V\r\n"+
					"Grid current: "+this.mGridRI/10+"."+this.mGridRI%10+" A\r\n");			
		}
		else {
			returnStr.append("GridR voltage: "+this.mGridRV+" V\r\n"+
					"GridS voltage: "+this.mGridSV+" V\r\n"+
					"GridT voltage: "+this.mGridTV+" V\r\n");
			returnStr.append("GridR current: "+this.mGridRI/10+"."+this.mGridRI%10+" A\r\n"+
					"GridS current: "+this.mGridSI/10+"."+this.mGridSI%10+" A\r\n"+
					"GridT current: "+this.mGridTI/10+"."+this.mGridTI%10+" A\r\n");
		}
		returnStr.append("Grid power: "+this.mGridRealP/10+"."+this.mGridRealP%10+" kWh\r\n"+
				"Total power: "+this.mTotalP+" kWh\r\n");
		returnStr.append("Frequency: "+this.mFrequency/10+"."+this.mFrequency%10+"Hz\r\n"+
				"PowerFactor: "+this.mPf/10+"."+this.mPf%10+"\r\n");
		if(this.mStatus==Inverter.Status.RUN) returnStr.append("Inverter status: RUN\r\n");
		else returnStr.append("Status: STOP\r\n");
		returnStr.append("Fault message: "+this.getInverterFaultMsg()+"\r\n");
		return returnStr.toString();
	}
	
	public boolean setInverterData(int data, Inverter.Data category) {
		switch(category) {
		case PVV:
			this.mPvV = data;
			break;
		case PVI:
			this.mPvI = data;
			break;
		case PVP:
			this.mPvP = data;
			break;
		case GRIDRV:
			this.mGridRV = data;
			break;
		case GRIDSV:
			this.mGridSV = data;
			break;
		case GRIDTV:
			this.mGridTV = data;
			break;
		case GRIDRI:
			this.mGridRI = data;
			break;
		case GRIDSI:
			this.mGridSI = data;
			break;
		case GRIDTI:
			this.mGridTI = data;
			break;
		case GRIDREALPOWER:
			this.mGridRealP = data;
			break;
		case TOTALPOWER:
			this.mTotalP = data;
			break;
		case FREQUENCY:
			this.mFrequency = data;
			break;
		case POWERFACTOR:
			this.mPf = data;
			break;
		default:
			return false;
		}
		this.mMsg = "Inverter data set.";
		return true;
	}
	
	public void setInverterData(byte faultH, byte faultL) {
		this.mFaultCodeHigh = faultH;
		this.mFaultCodeLow = faultL;
		this.mMsg = "Inverter FaultCode set.";
	}
	
	public void setInverterData(Inverter.Fault fault) {
		switch(fault) {
		case ETC_FAULT:
			this.mFaultCodeHigh = 0b00100000;
			this.mFaultCodeLow = 0;
			break;
		case EARTH_FAULT:
			this.mFaultCodeHigh = 0b00010000;
			this.mFaultCodeLow = 0;
			break;
		case STANDALONE:
			this.mFaultCodeHigh = 0b00001000;
			this.mFaultCodeLow = 0;
			break;
		case GRID_UF:
			this.mFaultCodeHigh = 0b00000100;
			this.mFaultCodeLow = 0;
			break;
		case GRID_OF:
			this.mFaultCodeHigh = 0b00000010;
			this.mFaultCodeLow = 0;
			break;
		case GRID_OC:
			this.mFaultCodeHigh = 0b00000001;
			this.mFaultCodeLow = 0;
			break;
		case GRID_UV:
			this.mFaultCodeHigh = 0;
			this.mFaultCodeLow = -128;
			break;
		case GRID_OV:
			this.mFaultCodeHigh = 0;
			this.mFaultCodeLow = 0b01000000;
			break;
		case INV_OVERH:	
			this.mFaultCodeHigh = 0;
			this.mFaultCodeLow = 0b00100000;
			break;
		case INV_IGBT:
			this.mFaultCodeHigh = 0;
			this.mFaultCodeLow = 0b00010000;
			break;
		case PV_OC:
			this.mFaultCodeHigh = 0;
			this.mFaultCodeLow = 0b00001000;
			break;
		case PV_UV:
			this.mFaultCodeHigh = 0;
			this.mFaultCodeLow = 0b00000100;
			break;
		case PV_OV:
			this.mFaultCodeHigh = 0;
			this.mFaultCodeLow = 0b00000010;
			break;
		case NORMAL:
			this.mFaultCodeHigh = 0;
			this.mFaultCodeLow = 0;
			break;
		default:
			this.mMsg = "Invalid FaultCode category.";
			return ;
		}		
		this.mMsg = "Inverter FaultCode set.";
	}
	
	public void setInverterStatus(Inverter.Status status) {
		this.mStatus = status;
		this.mMsg = "Inverter Status set.";
	}
	
	public String getInverterFaultMsg() {
		byte codeH = this.mFaultCodeHigh;
		byte codeL = this.mFaultCodeLow;
		if((codeH&0b00100000)==1) return Inverter.FAULT_ETC;
		else if((codeH&0b00010000)==1) return Inverter.FAULT_EARTH;
		else if((codeH&0b00001000)==1) return Inverter.FAULT_STANDALONE;
		else if((codeH&0b00000100)==1) return Inverter.FAULT_GRID_UF;		
		else if((codeH&0b00000010)==1) return Inverter.FAULT_GRID_OF;
		else if((codeH&0b00000001)==1) return Inverter.FAULT_GRID_OC;
		else if((codeL&0b10000000)==1) return Inverter.FAULT_GRID_UV;
		else if((codeL&0b01000000)==1) return Inverter.FAULT_GRID_OV;
		else if((codeL&0b00100000)==1) return Inverter.FAULT_INV_OVERH;
		else if((codeL&0b00010000)==1) return Inverter.FAULT_INV_IGBT;
		else if((codeL&0b00001000)==1) return Inverter.FAULT_PV_OC;
		else if((codeL&0b00000100)==1) return Inverter.FAULT_PV_UV;
		else if((codeL&0b00000010)==1) return Inverter.FAULT_PV_OV;		
		return Inverter.FAULT_NORMAL;
	}
	
	public void setInverterFault(byte codeHigh, byte codeLow) {
		this.mFaultCodeHigh = codeHigh;
		this.mFaultCodeLow = codeLow;
	}
	
	public void delayTime(int timeMillis) {
		long compareTime = System.currentTimeMillis();
		long currentTime = 0;
		while(currentTime-compareTime<timeMillis) {
			currentTime = System.currentTimeMillis();
		}

	}
	
}
