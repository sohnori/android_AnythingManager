package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterVELT extends Inverter {
	private Thread mThread;
	public InverterVELT(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("(주)헵시바", Equipment.EquipInfo.MANUFACTURER);
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
	public byte[] getRequestPacket(int id) {
		// MODBUS-RTU protocol
		byte idByte;
		byte[] packet = new byte[8];		
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		packet[0] = '#';
		packet[1] = 'W';
		packet[2] = 'R'; // header
		packet[3] = '0'; // id
		idByte = (byte)(id/10+0x30); 
		packet[4] = idByte; // id
		idByte = (byte)(id%10+0x30);
		packet[5] = idByte;	// id
		packet[6] = 'R';	// command - R_read	S_force stop G_force run 
		packet[7] = 'X'; // tail
		return packet;
	}
	public boolean verifyResponse(byte[] src, int id) {
		// verify header1, 2
		if (src[0]!='#' | src[1]!='W' | src[2]!='R') {
			mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if (src[4]!=(id/10+0x30) || src[5]!=(id%10+0x30)) {
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
	public boolean setInverterData(byte[] src) {		
		int data = 0;
		int data2 = 0;
		char[] dataChar = new char[3];
		dataChar[0] = (char)src[18];
		dataChar[1] = (char)src[19];
		dataChar[2] = (char)src[20];
		data = Integer.parseInt(String.valueOf(dataChar));
		super.setInverterData(this.mGetFaultCode(data));
		dataChar[0] = (char)src[22];
		dataChar[1] = (char)src[23];
		dataChar[2] = (char)src[24];
		data = Integer.parseInt(String.valueOf(dataChar));
		super.setInverterData(data, Inverter.Data.PVV);
		dataChar[0] = (char)src[26];
		dataChar[1] = (char)src[27];
		dataChar[2] = (char)src[28];
		data2 = Integer.parseInt(String.valueOf(dataChar)); // fix 1
		super.setInverterData(data2, Inverter.Data.PVI);		
		data *= data2;
		data = data/10/100; // W to kW fix 1
		super.setInverterData(data, Inverter.Data.PVP);
		dataChar[0] = (char)src[30];
		dataChar[1] = (char)src[31];
		dataChar[2] = (char)src[32];
		data = Integer.parseInt(String.valueOf(dataChar));
		super.setInverterData(data, Inverter.Data.GRIDRV);
		dataChar[0] = (char)src[34];
		dataChar[1] = (char)src[35];
		dataChar[2] = (char)src[36];
		data2 = Integer.parseInt(String.valueOf(dataChar)); // fix 1
		super.setInverterData(data2, Inverter.Data.GRIDRI);
		data *= data2;
		if(data>0) super.setInverterStatus(Inverter.Status.RUN);
		else super.setInverterStatus(Inverter.Status.STOP);
		data = data/10/100; // W to kW fix 1
		super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
		dataChar[0] = (char)src[38];
		dataChar[1] = (char)src[39];
		dataChar[2] = (char)src[40];
		data2 = Integer.parseInt(String.valueOf(dataChar)); // fix 1
		super.setInverterData(data2, Inverter.Data.FREQUENCY);
		dataChar = new char[5];
		dataChar[0] = (char)src[50];
		dataChar[1] = (char)src[51];
		dataChar[2] = (char)src[52];
		dataChar[3] = (char)src[53];
		dataChar[4] = (char)src[54];
		data = Integer.parseInt(String.valueOf(dataChar));
		data /= 10; // kWh fix 1 to fix 0
		super.setInverterData(data, Inverter.Data.TOTALPOWER);		
		return true;		
	}
	private Inverter.Fault mGetFaultCode(int faultCode){
//		if(faultCode==1) return Inverter.Fault.PV_OV;
//		else if(faultCode==2) return Inverter.Fault.PV_UV;
//		else if(faultCode==3) return Inverter.Fault.GRID_OC;
//		else if(faultCode==4) return Inverter.Fault.PV_OC;
//		else if(faultCode==5) return Inverter.Fault.PV_OV;
//		else if(faultCode==8) return Inverter.Fault.GRID_OF;
//		else if(faultCode==10) return Inverter.Fault.STANDALONE;
//		else if(faultCode==11) return Inverter.Fault.GRID_UV;
//		else if(faultCode==12) return Inverter.Fault.GRID_OV;
//		else if(faultCode==15) return Inverter.Fault.ETC_FAULT;
//		else if(faultCode==30) return Inverter.Fault.INV_OVERH;
//		else if(faultCode==40) return Inverter.Fault.EARTH_FAULT;
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
			txPacketbuff = getRequestPacket(id);
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
			if(setInverterData(rxPacketbuff)!=true) {
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
