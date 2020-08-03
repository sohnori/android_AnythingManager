package com.daeeun.sohnori.anythingmanager.equipment;

public class Equipment {
	public enum EquipInfo {
		MANUFACTURER, MANUFACTURERDAY, EQUIPNAME, EQUIPTYPE, MODELNAME, PARTNUMBER, SERIALNUMBER,
		WARREANTYPERIOD ,HWVERSION, SWVERSION, CERTIFICATION, ETCINFO
	}	
	protected String mManufacturer;
	protected String mManufacturerDay;
	protected String mEquipType;
	protected String mEquipName;	
	protected String mModelName;
	protected String mPartNumber;
	protected String mSerialNumber;
	protected String mWarrantyPeriod;
	protected String mHwVersion;
	protected String mSwVersion;
	protected String mCertification;
	protected String mEtcInfo;
	
	public Equipment() {
		
	}
	
	public void initEquipment() {
		this.mManufacturer = "";
		this.mManufacturerDay = "";
		this.mEquipType = "";
		this.mEquipName = "";		
		this.mModelName = "";
		this.mPartNumber = "";
		this.mSerialNumber = "";
		this.mWarrantyPeriod = "";
		this.mHwVersion = "";
		this.mSwVersion = "";
		this.mCertification = "";
		this.mEtcInfo = "";
	}
	
	public String getEquipInfo() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("manufacturer: ");
		retStr.append(this.mManufacturer+"\t");
		retStr.append("manufacturerDay: ");
		retStr.append(this.mManufacturerDay+"\r\n");
		retStr.append(this.mEquipType+"\r\n");
		retStr.append("modelName: ");
		retStr.append("equipName: ");
		retStr.append(this.mEquipName+"\t");
		retStr.append("equipType: ");		
		retStr.append(this.mModelName+"\t");
		retStr.append("partNumber: ");
		retStr.append(this.mPartNumber+"\r\n");
		retStr.append("serialNumber: ");
		retStr.append(this.mSerialNumber+"\t");
		retStr.append("warrantyPeriod: ");
		retStr.append(this.mWarrantyPeriod+"\r\n");
		retStr.append("hwVersion: ");
		retStr.append(this.mHwVersion+"\t");
		retStr.append("swVersion: ");
		retStr.append(this.mSwVersion+"\r\n");
		retStr.append("certification: ");
		retStr.append(this.mCertification+"\t");
		retStr.append("etcInfo: ");
		retStr.append(this.mEtcInfo+"\r\n");
		return retStr.toString();
	}
	
	public String getEquipInfo(Equipment.EquipInfo info) {
		switch(info) {
		case MANUFACTURER:
			return this.mManufacturer;			
		case MANUFACTURERDAY:
			return this.mManufacturerDay;
		case EQUIPTYPE:
			return this.mEquipType;	
		case EQUIPNAME:
			return this.mEquipName;						
		case MODELNAME:
			return this.mModelName;			
		case PARTNUMBER:
			return this.mPartNumber;			
		case SERIALNUMBER:
			return this.mSerialNumber;
		case WARREANTYPERIOD:
			return this.mWarrantyPeriod;			
		case HWVERSION:
			return this.mHwVersion;			
		case SWVERSION:
			return this.mSwVersion;			
		case CERTIFICATION:
			return this.mCertification;			
		case ETCINFO:
			return this.mEtcInfo;				
		}
		return "Invalid category!!";
	}
	
	public void setEquipInfo(String str, Equipment.EquipInfo info) {
		switch(info) {
		case MANUFACTURER:
			this.mManufacturer = str;
			break;
		case MANUFACTURERDAY:
			this.mManufacturerDay = str;
			break;
		case EQUIPTYPE:
			this.mEquipType = str;
			break;
		case EQUIPNAME:
			this.mEquipName = str;
			break;		
		case MODELNAME:
			this.mModelName = str;
			break;
		case PARTNUMBER:
			this.mPartNumber = str;
			break;
		case SERIALNUMBER:
			this.mSerialNumber = str;
			break;
		case WARREANTYPERIOD:
			this.mSerialNumber = str;
			break;
		case HWVERSION:
			this.mHwVersion = str;
			break;
		case SWVERSION:
			this.mSwVersion = str;
			break;
		case CERTIFICATION:
			this.mCertification = str;
			break;
		case ETCINFO:
			this.mEtcInfo = str;
			break;		
		}
	}	
}
