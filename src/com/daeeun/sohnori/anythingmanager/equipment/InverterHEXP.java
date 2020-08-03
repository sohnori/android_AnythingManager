package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterHEXP extends Inverter {
	public enum Model {
		SINGLE, THREE, H30xxS_ML
	}
	public enum Command {
		FAULT, PV, GRID, POWER, SYSTEM, EVIRONMENT
	}
	private Thread mThread;
	private InverterHEXP.Model mModel;	
	public InverterHEXP(Inverter.Phase phase, InverterHEXP.Model model) {
		super(phase);
		this.mModel = model;
		super.setEquipInfo("헥스파워(주)", Equipment.EquipInfo.MANUFACTURER);
		if(phase==Inverter.Phase.SINGLE) super.setEquipInfo("Single phase", Equipment.EquipInfo.ETCINFO);
		else if(phase==Inverter.Phase.THREE) super.setEquipInfo("Three phase", Equipment.EquipInfo.ETCINFO);
	}
	@Override 
	public void initInverterData(){
		super.initInverterData();
	}
	
	@Override
	public void setEquipInfo(String str, Equipment.EquipInfo info) {
		super.setEquipInfo(str, info);
	}
	@Override
	public String getMessage() {
		return super.mMsg;
	}
	public void runCommunicationThread(Terminal terminal, int id, EditText log, EditText result) {
		mThread = new RequestDataThread(terminal, id, log, result);
		mThread.setDaemon(true);
		mThread.start();
	}
	public byte[] getRequestPacket(int id, InverterHEXP.Command cmd) {
		byte[] packet = new byte[15];
		int cksum = 0;
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		packet[0] = 0x05; //ENQ
		packet[1] = (byte)(id/10+0x30); // id high_ASCII
		packet[2] = (byte)(id%10+0x30); // id low_ASCII
		packet[3] = 'R'; // command
		switch(cmd) {
		case FAULT:
			packet[4] = 0x30;
			packet[5] = 0x30;
			packet[6] = 0x30;
			packet[7] = 0x34;
			packet[8] = 0x30; // length high
			packet[9] = 0x34; // length low
			break;
		case PV:
			packet[4] = 0x30;
			packet[5] = 0x30;
			packet[6] = 0x32;
			packet[7] = 0x30;
			packet[8] = 0x30; // length high
			packet[9] = 0x32; // length low
			break;
		case GRID:
			packet[4] = 0x30;
			packet[5] = 0x30;
			packet[6] = 0x35;
			packet[7] = 0x30;
			packet[8] = 0x30; // length high
			packet[9] = 0x37; // length low
			break;
		case POWER:
			packet[4] = 0x30;
			packet[5] = 0x30;
			packet[6] = 0x36;
			packet[7] = 0x30;
			packet[8] = 0x30; // length high
			packet[9] = 0x38; // length low
			break;
		case SYSTEM:
			packet[4] = 0x30;
			packet[5] = 0x31;
			packet[6] = 0x65;
			packet[7] = 0x30;
			packet[8] = 0x30; // length high
			packet[9] = 0x33; // length low
			break;
		case EVIRONMENT:
			packet[4] = 0x30;
			packet[5] = 0x30;
			packet[6] = 0x37;
			packet[7] = 0x30;
			packet[8] = 0x30; // length high
			packet[9] = 0x34; // length low
			break;
		default:
			break;
		}
		for(int cnt=1;cnt<10;cnt++) {
			cksum += (packet[cnt]&0xFF);
		}
		String str = Integer.toHexString(cksum);		
		char[] cksumChar = str.toCharArray();
		if(cksumChar.length==4) {
			packet[10] = (byte)cksumChar[0];
			packet[11] = (byte)cksumChar[1];
			packet[12] = (byte)cksumChar[2];
			packet[13] = (byte)cksumChar[3];
		}
		else if(cksumChar.length==3) {
			packet[10] = 0x30;
			packet[11] = (byte)cksumChar[0];
			packet[12] = (byte)cksumChar[1];
			packet[13] = (byte)cksumChar[2];
		}
		else if(cksumChar.length==2) {
			packet[10] = 0x30;
			packet[11] = 0x30;
			packet[12] = (byte)cksumChar[0];
			packet[13] = (byte)cksumChar[1];
		}
		else if(cksumChar.length==1) {
			packet[10] = 0x30;
			packet[11] = 0x30;
			packet[12] = 0x30;
			packet[13] = (byte)cksumChar[0];
		}
		else {
			packet[10] = 0x30;
			packet[11] = 0x30;
			packet[12] = 0x30;
			packet[13] = 0x30;
		}
		packet[14] = 0x04;
		return packet;
	}
	public boolean verifyResponse(byte[] src, int id) {	
		// verify header1, 2
		if (!((src[0]==(byte)0x06) && (src[3]==(byte)0x52))) {
			mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if (src[1]!=(byte)(id/10+0x30) || src[2]!=(byte)(id%10+0x30)) {
			mMsg = Inverter.ERROR_ID;
			return false;
		}		
		return true;
	}
	@Override
	public boolean setInverterData(int data, Inverter.Data category) {
		return super.setInverterData(data, category);		
	}
	@Override
	public void setInverterData(byte faultH, byte faultL) {
		super.setInverterData(faultH, faultL);
	}
	@Override
	public void setInverterStatus(Inverter.Status status) {
		super.setInverterStatus(status);
	}	
	public boolean setInverterData(byte[] src, InverterHEXP.Command cmd) {		
		int data=0;	
		char[] value;		
		switch(cmd) {
		case FAULT:
			setInverterData(this.mGetFaultCode(src));
			break;
		case PV:
			value = new char[4]; 
			value[0] = (char)src[8];
			value[1] = (char)src[9];
			value[2] = (char)src[10];
			value[3] = (char)src[11];
			data = Integer.parseInt(String.valueOf(value), 16);
			super.setInverterData(data, Inverter.Data.PVV);
			value[0] = (char)src[12];
			value[1] = (char)src[13];
			value[2] = (char)src[14];
			value[3] = (char)src[15];
			data = Integer.parseInt(String.valueOf(value), 16);
			if(this.mModel==InverterHEXP.Model.THREE) data*=10; // to fixed 1
			super.setInverterData(data, Inverter.Data.PVI);
			break;
		case GRID:
			value = new char[4];
			value[0] = (char)src[8];
			value[1] = (char)src[9];
			value[2] = (char)src[10];
			value[3] = (char)src[11];
			data = Integer.parseInt(String.valueOf(value), 16);
			super.setInverterData(data, Inverter.Data.GRIDRV);
			value[0] = (char)src[12];
			value[1] = (char)src[13];
			value[2] = (char)src[14];
			value[3] = (char)src[15];
			data = Integer.parseInt(String.valueOf(value), 16);
			super.setInverterData(data, Inverter.Data.GRIDSV);
			value[0] = (char)src[16];
			value[1] = (char)src[17];
			value[2] = (char)src[18];
			value[3] = (char)src[19];
			data = Integer.parseInt(String.valueOf(value), 16);
			super.setInverterData(data, Inverter.Data.GRIDTV);
			value[0] = (char)src[20];
			value[1] = (char)src[21];
			value[2] = (char)src[22];
			value[3] = (char)src[23];
			data = Integer.parseInt(String.valueOf(value), 16);
			if(this.mModel==InverterHEXP.Model.THREE) data*=10; // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDRI);
			value[0] = (char)src[24];
			value[1] = (char)src[25];
			value[2] = (char)src[26];
			value[3] = (char)src[27];
			data = Integer.parseInt(String.valueOf(value), 16);
			if(this.mModel==InverterHEXP.Model.THREE) data*=10; // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDSI);
			value[0] = (char)src[28];
			value[1] = (char)src[29];
			value[2] = (char)src[30];
			value[3] = (char)src[31];
			data = Integer.parseInt(String.valueOf(value), 16);
			if(this.mModel==InverterHEXP.Model.THREE) data*=10; // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDTI);
			value[0] = (char)src[32];
			value[1] = (char)src[33];
			value[2] = (char)src[34];
			value[3] = (char)src[35];
			data = Integer.parseInt(String.valueOf(value), 16);			
			super.setInverterData(data, Inverter.Data.FREQUENCY);
			break;
		case POWER:
			value = new char[4];			
			if(this.mModel==InverterHEXP.Model.SINGLE) {
				char[] value2 = new char[4];
				value[0] = (char)src[8];
				value[1] = (char)src[9];
				value[2] = (char)src[10];
				value[3] = (char)src[11];
				data = Integer.parseInt(String.valueOf(value), 16);
				data/=100; // W to fixed 1
				super.setInverterData(data, Inverter.Data.PVP);
				value[0] = (char)src[12];
				value[1] = (char)src[13];
				value[2] = (char)src[14];
				value[3] = (char)src[15];
				value2[0] = (char)src[16];
				value2[1] = (char)src[17];
				value2[2] = (char)src[18];
				value2[3] = (char)src[19];
				data = Integer.parseInt(String.valueOf(value), 16);
				data*=10000;
				data += Integer.parseInt(String.valueOf(value2), 16);
				data/=1000; // W to kWh 
				super.setInverterData(data, Inverter.Data.TOTALPOWER);
				value[0] = (char)src[20];
				value[1] = (char)src[21];
				value[2] = (char)src[22];
				value[3] = (char)src[23];
				data/=100; // W to fixed 1
				super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
				if(data>0) setInverterStatus(Inverter.Status.RUN);
				else setInverterStatus(Inverter.Status.STOP);
				value[0] = (char)src[28];
				value[1] = (char)src[29];
				value[2] = (char)src[30];
				value[3] = (char)src[31];				
				super.setInverterData(data, Inverter.Data.POWERFACTOR);
			}
			else if(this.mModel==InverterHEXP.Model.THREE || this.mModel==InverterHEXP.Model.H30xxS_ML) {
				value[0] = (char)src[8];
				value[1] = (char)src[9];
				value[2] = (char)src[10];
				value[3] = (char)src[11];
				data = Integer.parseInt(String.valueOf(value), 16);				
				super.setInverterData(data, Inverter.Data.PVP);
				char[] value2 = new char[8];
				value2[0] = (char)src[12];
				value2[1] = (char)src[13];
				value2[2] = (char)src[14];
				value2[3] = (char)src[15];
				value2[4] = (char)src[16];
				value2[5] = (char)src[17];
				value2[6] = (char)src[18];
				value2[7] = (char)src[19];
				data = Integer.parseInt(String.valueOf(value2), 16);
				super.setInverterData(data, Inverter.Data.TOTALPOWER);
				value[0] = (char)src[20];
				value[1] = (char)src[21];
				value[2] = (char)src[22];
				value[3] = (char)src[23];				
				super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
				if(data>0) setInverterStatus(Inverter.Status.RUN);
				else setInverterStatus(Inverter.Status.STOP);
				value[0] = (char)src[36];
				value[1] = (char)src[37];
				value[2] = (char)src[38];
				value[3] = (char)src[39];				
				super.setInverterData(data, Inverter.Data.POWERFACTOR);
					
			}
			break;
		default:
			mMsg = Inverter.ERROR_CMD;
			return false;
		}		
		return true;		
	}
	private Inverter.Fault mGetFaultCode(byte[] faultCode) {
		switch(mModel) {
		case SINGLE:
			if(faultCode[11]=='1') return Inverter.Fault.PV_OV;
			else if(faultCode[11]=='2') return Inverter.Fault.PV_OV;
			else if(faultCode[11]=='4') return Inverter.Fault.PV_UV;
			else if(faultCode[11]=='8') return Inverter.Fault.PV_UV;
			else if(faultCode[11]=='8') return Inverter.Fault.PV_UV;
			else if(faultCode[15]=='1') return Inverter.Fault.GRID_OC;
			else if(faultCode[15]=='2') return Inverter.Fault.GRID_OC;
			else if(faultCode[15]=='8') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[14]=='2') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[14]=='8') return Inverter.Fault.INV_OVERH;
			else if(faultCode[13]=='1') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[19]=='1') return Inverter.Fault.GRID_OV;
			else if(faultCode[19]=='2') return Inverter.Fault.GRID_UV;
			else if(faultCode[19]=='4') return Inverter.Fault.STANDALONE;
			else if(faultCode[18]=='1') return Inverter.Fault.GRID_OF;
			else if(faultCode[18]=='2') return Inverter.Fault.GRID_UF;
			break;
		case THREE:			
		case H30xxS_ML:
			if(faultCode[11]=='8') return Inverter.Fault.PV_OV;
			else if(faultCode[10]=='1') return Inverter.Fault.PV_UV;
			else if(faultCode[9]=='8') return Inverter.Fault.STANDALONE;
			else if(faultCode[13]=='8') return Inverter.Fault.GRID_OC;
			else if(faultCode[11]=='8') return Inverter.Fault.PV_UV;
			else if(faultCode[18]=='8') return Inverter.Fault.INV_OVERH;
			else if(faultCode[19]=='4') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[19]=='1') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[21]=='4') return Inverter.Fault.GRID_OF;
			else if(faultCode[21]=='2') return Inverter.Fault.GRID_OC;
			else if(faultCode[21]=='1') return Inverter.Fault.GRID_UF;
			else if(faultCode[22]=='8') return Inverter.Fault.GRID_OV;
			else if(faultCode[22]=='4') return Inverter.Fault.GRID_UV;
			else if(faultCode[22]=='2') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[22]=='1') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[23]=='8') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[23]=='4') return Inverter.Fault.ETC_FAULT;
			else if(faultCode[23]=='1') return Inverter.Fault.ETC_FAULT;
			break;
		}		
		return Inverter.Fault.NORMAL;
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
			initInverterData();
			txPacketbuff = getRequestPacket(id, InverterHEXP.Command.FAULT);
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
			if(verifyResponse(rxPacketbuff, id)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterHEXP.Command.FAULT)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterHEXP.Command.PV);
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
			if(verifyResponse(rxPacketbuff, id)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterHEXP.Command.PV)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterHEXP.Command.GRID);
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
			if(verifyResponse(rxPacketbuff, id)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterHEXP.Command.GRID)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterHEXP.Command.POWER);
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
			if(verifyResponse(rxPacketbuff, id)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterHEXP.Command.POWER)!=true) {
				threadHandler.post(runReault);
				return;
			}
			threadHandler.post(new Runnable() {
				public void run() {
					result.setText(getInverterData());				
				}						
			});
		}
	}
}
