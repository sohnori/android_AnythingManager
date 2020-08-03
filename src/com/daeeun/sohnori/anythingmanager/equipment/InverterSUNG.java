package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterSUNG extends Inverter {
	public enum Command {
		PV, GRID_VI, GRID_PW_PF_FR, TOTAL, FAULT
	}
	private Thread mThread;
	public InverterSUNG(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("REFU", Equipment.EquipInfo.MANUFACTURER);
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
		// MODBUS-RTU protocol
		byte[] packet = new byte[8];		
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		packet[0] = (byte)id; // ID
		packet[1] = functionCode;		
		packet[2] = (byte)(address>>8);
		packet[3] = (byte)address; 
		packet[4] = (byte)(length>>8);
		packet[5] = (byte)length;
		CyclicalRedundancyCheck crc16X25 = new CyclicalRedundancyCheck();
		byte[] crc = crc16X25.getCRC16Bytes(packet, 6, CyclicalRedundancyCheck.Crc16.CRC16_MODBUS);
		packet[6] = (byte)crc[0];
		packet[7] = (byte)crc[1];					
		return packet;
	}
	public byte[] getRequestPacket(int id, InverterSUNG.Command cmd) {		
		byte[] packet = null;		
		switch(cmd) {
		case PV:
			packet = getRequestPacket(id, (byte)0x04, 5011, 8);
			break;
		case GRID_VI:
			packet = getRequestPacket(id, (byte)0x04, 5019, 6);
			break;
		case GRID_PW_PF_FR:
			packet = getRequestPacket(id, (byte)0x04, 5031, 6);
			break;
		case TOTAL:
			packet = getRequestPacket(id, (byte)0x04, 5004, 2);
			break;
		case FAULT:
			packet = getRequestPacket(id, (byte)0x04, 5045, 1);
			break;
		default:
			return null;
		}
		return packet;
	}	
	public boolean verifyResponse(byte[] src, int id, byte functionCode) {
		// verify header1, 2
		if (src[1]!=functionCode) {
			mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if (src[0]!=id) {
			mMsg = Inverter.ERROR_ID;
			return false;
		}
		CyclicalRedundancyCheck crcModbus = new CyclicalRedundancyCheck();
		byte[] crc = crcModbus.getCRC16Bytes(src, src.length-2, CyclicalRedundancyCheck.Crc16.CRC16_MODBUS);

		if (crc[0]!=(byte)src[src.length-2] || crc[1]!=(byte)src[src.length-1]) {
			mMsg =  Inverter.ERROR_CKSUM;
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
	public boolean setInverterData(byte[] src, InverterSUNG.Command cmd) {				
		int data = 0;
		int data2 = 0;
		switch(cmd) {		
		case PV:			
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			data /= 10; // to fixed 0
			super.setInverterData(data, Inverter.Data.PVV);			
			data2 = (src[17]&0xFF)<<24 | (src[18]&0xFF)<<16 | (src[15]&0xFF)<<8 | (src[16]&0xFF);
			data = data2/data; // calculate DC current
			data2 /= 100; // W to kW fixed 1
			super.setInverterData(data2, Inverter.Data.PVP);			
			super.setInverterData(data, Inverter.Data.PVI);
			break;
		case GRID_VI:
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			data /= 10; // to fixed 0
			super.setInverterData(data, Inverter.Data.GRIDRV);	
			data = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			data /= 10; // to fixed 0
			super.setInverterData(data, Inverter.Data.GRIDSV);	
			data = (src[7]&0xFF)<<8 | (src[8]&0xFF);
			data /= 10; // to fixed 0
			super.setInverterData(data, Inverter.Data.GRIDTV);
			data = (src[9]&0xFF)<<8 | (src[10]&0xFF);			
			super.setInverterData(data, Inverter.Data.GRIDRI);
			data = (src[11]&0xFF)<<8 | (src[12]&0xFF);			
			super.setInverterData(data, Inverter.Data.GRIDSI);
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);			
			super.setInverterData(data, Inverter.Data.GRIDTI);
			break;
		case GRID_PW_PF_FR:
			data = (src[5]&0xFF)<<24 | (src[6]&0xFF)<<16 | (src[3]&0xFF)<<8 | (src[4]&0xFF);
			if(data>0) super.setInverterStatus(Inverter.Status.RUN);
			else super.setInverterStatus(Inverter.Status.STOP);
			data /= 100; // W to kWh fixed 1
			super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
			data = (src[11]&0xFF)<<8 | (src[12]&0xFF);
			data /= 100; // to fixed 1
			super.setInverterData(data, Inverter.Data.POWERFACTOR);
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);			
			super.setInverterData(data, Inverter.Data.FREQUENCY);
			break;
		case TOTAL:
			data = (src[5]&0xFF)<<24 | (src[6]&0xFF)<<16 | (src[3]&0xFF)<<8 | (src[4]&0xFF);
			super.setInverterData(data, Inverter.Data.TOTALPOWER);
			break;
		case FAULT:
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			super.setInverterData(mGetFaultCode(data));
			break;
		}		
		return true;
	}
	private Inverter.Fault mGetFaultCode(int code) {
		if(code==0x0002) return Inverter.Fault.GRID_OV;
		else if(code==0x0003) return Inverter.Fault.GRID_OV;
		else if(code==0x0004) return Inverter.Fault.GRID_UV;
		else if(code==0x0005) return Inverter.Fault.GRID_UV;
		else if(code==0x0006) return Inverter.Fault.ETC_FAULT;
		else if(code==0x0007) return Inverter.Fault.GRID_OC;
		else if(code==0x0008) return Inverter.Fault.GRID_OF;
		else if(code==0x0009) return Inverter.Fault.GRID_UF;
		else if(code==0x000A) return Inverter.Fault.STANDALONE;
		else if(code==0x000C) return Inverter.Fault.EARTH_FAULT;
		else if(code==0x000E) return Inverter.Fault.EARTH_FAULT;
		else if(code==0x000F) return Inverter.Fault.GRID_OV;
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
			txPacketbuff = getRequestPacket(id, InverterSUNG.Command.PV);
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
			if(setInverterData(rxPacketbuff, InverterSUNG.Command.PV)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterSUNG.Command.GRID_VI);
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
			if(setInverterData(rxPacketbuff, InverterSUNG.Command.GRID_VI)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterSUNG.Command.GRID_PW_PF_FR);
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
			if(setInverterData(rxPacketbuff, InverterSUNG.Command.GRID_PW_PF_FR)!=true) {
				threadHandler.post(runReault);
				return;
			}
			txPacketbuff = getRequestPacket(id, InverterSUNG.Command.TOTAL);
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
			if(setInverterData(rxPacketbuff, InverterSUNG.Command.TOTAL)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterSUNG.Command.FAULT);
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
			if(setInverterData(rxPacketbuff, InverterSUNG.Command.FAULT)!=true) {
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
