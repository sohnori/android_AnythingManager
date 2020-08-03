package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterSMAI extends Inverter {
	public enum Command {
		FAULT, TOTAL, PV, GRID_VI_PW_FR, PF
	}
	private Thread mThread;
	public InverterSMAI(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("SMA Solar", Equipment.EquipInfo.MANUFACTURER);
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
	public byte[] getRequestPacket(int id, byte functionCode, int address, int length) {
		// MODBUS-TCP protocol
		byte[] packet = new byte[12];		
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		// Transaction ID is mean, working sequence number
		packet[0] = 0x00;;
		packet[1] = 0x01;
		// Protocol ID value fixed to 0x0000
		packet[2] = 0x00;
		packet[3] = 0x00;
		// packet length
		packet[4] = 0x00;
		packet[5] = 0x06;
		packet[6] = (byte)id; // ID
		packet[7] = functionCode;		
		packet[8] = (byte)(address>>8);
		packet[9] = (byte)address; 
		packet[10] = (byte)(length>>8);
		packet[11] = (byte)length;						
		return packet;
	}
	public byte[] getRequestPacket(int id, InverterSMAI.Command cmd) {		
		byte[] packet = null;		
		switch(cmd) {		
		case FAULT:
			packet = getRequestPacket(id, (byte)0x04, 30251, 2);
			break;
		case TOTAL:
			packet = getRequestPacket(id, (byte)0x04, 30531, 2);
			break;
		case PV:
			packet = getRequestPacket(id, (byte)0x04, 30769, 6);
			break;
		case GRID_VI_PW_FR:
			packet = getRequestPacket(id, (byte)0x04, 30775, 18);
			break;
		case PF:
			packet = getRequestPacket(id, (byte)0x04, 30821, 2);
			break;		
		default:
			return null;
		}
		return packet;
	}	
	public boolean verifyResponse(byte[] src, int id, byte functionCode) {
		// verify header1, 2
		if (src[7]!=functionCode) {
			mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if (src[6]!=id) {
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
	public boolean setInverterData(byte[] src, InverterSMAI.Command cmd) {				
		int data = 0;		
		switch(cmd) {				
		case FAULT:
			data = (src[8]&0xFF)<<24 | (src[9]&0xFF)<<16 | (src[10]&0xFF)<<8 | (src[11]&0xFF);
			super.setInverterData(mGetFaultCode(data));
			break;		
		case TOTAL:
			data = (src[8]&0xFF)<<24 | (src[9]&0xFF)<<16 | (src[10]&0xFF)<<8 | (src[11]&0xFF);			
			super.setInverterData(data, Inverter.Data.TOTALPOWER);
			break;
		case PV:			
			data = (src[8]&0xFF)<<24 | (src[9]&0xFF)<<16 | (src[10]&0xFF)<<8 | (src[11]&0xFF);			
			data /= 100; // from fix 3 to fix 1
			super.setInverterData(data, Inverter.Data.PVI);			
			data = (src[12]&0xFF)<<24 | (src[13]&0xFF)<<16 | (src[14]&0xFF)<<8 | (src[15]&0xFF);			
			data /= 100; // from fix 2 to fix 0
			super.setInverterData(data, Inverter.Data.PVV);
			data = (src[16]&0xFF)<<24 | (src[17]&0xFF)<<16 | (src[18]&0xFF)<<8 | (src[19]&0xFF);			
			data /= 100; // W to kW fix 1 
			super.setInverterData(data, Inverter.Data.PVP);
			break;
		case GRID_VI_PW_FR:
			data = (src[8]&0xFF)<<24 | (src[9]&0xFF)<<16 | (src[10]&0xFF)<<8 | (src[11]&0xFF);
			if(data>0) super.setInverterStatus(Inverter.Status.RUN);
			else super.setInverterStatus(Inverter.Status.STOP);
			data /= 100; // W to kW fix 1 
			super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
			data = (src[12]&0xFF)<<24 | (src[13]&0xFF)<<16 | (src[14]&0xFF)<<8 | (src[15]&0xFF);			
			data /= 100; // from fix 2 to fix 0
			super.setInverterData(data, Inverter.Data.GRIDRV);
			data = (src[16]&0xFF)<<24 | (src[17]&0xFF)<<16 | (src[18]&0xFF)<<8 | (src[19]&0xFF);			
			data /= 100; // from fix 2 to fix 0
			super.setInverterData(data, Inverter.Data.GRIDSV);	
			data = (src[20]&0xFF)<<24 | (src[21]&0xFF)<<16 | (src[22]&0xFF)<<8 | (src[23]&0xFF);			
			data /= 100; // from fix 2 to fix 0
			super.setInverterData(data, Inverter.Data.GRIDTV);			
			data = (src[28]&0xFF)<<24 | (src[29]&0xFF)<<16 | (src[30]&0xFF)<<8 | (src[31]&0xFF);			
			data /= 100; // from fix 3 to fix 1
			super.setInverterData(data, Inverter.Data.GRIDRI);
			data = (src[32]&0xFF)<<24 | (src[33]&0xFF)<<16 | (src[34]&0xFF)<<8 | (src[35]&0xFF);
			data /= 100; // from fix 3 to fix 1
			super.setInverterData(data, Inverter.Data.GRIDSI);
			data = (src[36]&0xFF)<<24 | (src[37]&0xFF)<<16 | (src[38]&0xFF)<<8 | (src[39]&0xFF);
			data /= 100; // from fix 3 to fix 1
			super.setInverterData(data, Inverter.Data.GRIDTI);
			data = (src[40]&0xFF)<<24 | (src[41]&0xFF)<<16 | (src[42]&0xFF)<<8 | (src[43]&0xFF);
			data /= 10; // to fixed 1
			super.setInverterData(data, Inverter.Data.FREQUENCY);			
			break;
		case PF:			
			data = (src[8]&0xFF)<<24 | (src[9]&0xFF)<<16 | (src[10]&0xFF)<<8 | (src[11]&0xFF);
			data /= 10; // to fixed 1
			super.setInverterData(data, Inverter.Data.POWERFACTOR);			
			break;
		
		}		
		return true;
	}
	private Inverter.Fault mGetFaultCode(int code) {
		if(code==2386) return Inverter.Fault.GRID_OV;
		else if(code==2387) return Inverter.Fault.GRID_UV;			
		else if(code==2388) return Inverter.Fault.GRID_OF;
		else if(code==2389) return Inverter.Fault.GRID_UF;
		else if(code==1690) return Inverter.Fault.ETC_FAULT;
		else if(code==2390) return Inverter.Fault.ETC_FAULT;
		else if(code==2490) return Inverter.Fault.ETC_FAULT;
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
			txPacketbuff = getRequestPacket(id, InverterSMAI.Command.FAULT);
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
			if(verifyResponse(rxPacketbuff, id, (byte)0x04)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterSMAI.Command.FAULT)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterSMAI.Command.TOTAL);
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
			if(verifyResponse(rxPacketbuff, id, (byte)0x04)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterSMAI.Command.TOTAL)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterSMAI.Command.GRID_VI_PW_FR);
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
			if(verifyResponse(rxPacketbuff, id, (byte)0x04)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterSMAI.Command.GRID_VI_PW_FR)!=true) {
				threadHandler.post(runReault);
				return;
			}
			txPacketbuff = getRequestPacket(id, InverterSMAI.Command.PF);
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
			if(verifyResponse(rxPacketbuff, id, (byte)0x04)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterSMAI.Command.PF)!=true) {
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
