package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterECOS extends Inverter {
	public enum Command {
		PV, GRID_VI_PW, FAULT, TOTAL, PF
	}
	private Thread mThread;
	public InverterECOS(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("(주)동이에코스", Equipment.EquipInfo.MANUFACTURER);
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
		CyclicalRedundancyCheck crc16 = new CyclicalRedundancyCheck();
		byte[] crc = crc16.getCRC16Bytes(packet, 6, CyclicalRedundancyCheck.Crc16.CRC16_MODBUS);
		packet[6] = (byte)crc[0];
		packet[7] = (byte)crc[1];					
		return packet;
	}
	public byte[] getRequestPacket(int id, InverterECOS.Command cmd) {		
		byte[] packet = null;		
		switch(cmd) {
		case PV:
			packet = getRequestPacket(id, (byte)0x04, 3000, 12);
			break;
		case GRID_VI_PW:
			packet = getRequestPacket(id, (byte)0x04, 3014, 11);
			break;
		case FAULT:
			packet = getRequestPacket(id, (byte)0x04, 3028, 2);
			break;
		case TOTAL:
			packet = getRequestPacket(id, (byte)0x04, 3038, 2);
			break;
		case PF:
			packet = getRequestPacket(id, (byte)0x04, 3056, 1);
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
	public boolean setInverterData(byte[] src, InverterECOS.Command cmd) {				
		int data = 0;
		int data2 = 0;
		int data3 = 0;
		switch(cmd) {		
		case PV:			
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			data2 = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			data3 = (src[7]&0xFF)<<8 | (src[8]&0xFF);
			data = (data+data2+data3)/3; // get average
			data /= 10; // from fixed 1 to fixed 0
			super.setInverterData(data, Inverter.Data.PVV);			
			data = (src[9]&0xFF)<<8 | (src[10]&0xFF);
			data2 = (src[11]&0xFF)<<8 | (src[12]&0xFF);
			data3 = (src[13]&0xFF)<<8 | (src[14]&0xFF);
			data = data+data2+data3; // get total
			data /= 10; // from fixed 2 to fixed 1
			super.setInverterData(data2, Inverter.Data.PVI);
			data = (src[15]&0xFF)<<24 | (src[16]&0xFF)<<16 | (src[17]&0xFF)<<8 | (src[18]&0xFF);
			data2 = (src[19]&0xFF)<<24 | (src[20]&0xFF)<<16 | (src[21]&0xFF)<<8 | (src[22]&0xFF);
			data3 = (src[23]&0xFF)<<24 | (src[24]&0xFF)<<16 | (src[25]&0xFF)<<8 | (src[26]&0xFF);
			data = data+data2+data3; // get total
			data /= 100; //from W to kW fixed 1
			super.setInverterData(data, Inverter.Data.PVP);
			break;
		case GRID_VI_PW:
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
			data /= 10; // to fixed 1
			super.setInverterData(data, Inverter.Data.FREQUENCY);
			data = (src[11]&0xFF)<<8 | (src[12]&0xFF);
			data /= 10; // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDRI);
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);
			data /= 10; // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDSI);
			data = (src[15]&0xFF)<<8 | (src[16]&0xFF);
			data /= 10; // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDTI);
			data = (src[17]&0xFF)<<24 | (src[18]&0xFF)<<16 | (src[19]&0xFF)<<8 | (src[20]&0xFF);
			if(data>0) super.setInverterStatus(Inverter.Status.RUN);
			else super.setInverterStatus(Inverter.Status.STOP);
			data /= 100; //from W to kW fixed 1
			super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
			break;
		case FAULT:			
			super.setInverterData(mGetFaultCode(src));
			break;		
		case TOTAL:
			data = (src[3]&0xFF)<<24 | (src[4]&0xFF)<<16 | (src[5]&0xFF)<<8 | (src[6]&0xFF);
			data /= 10; // to fixed 0
			super.setInverterData(data, Inverter.Data.TOTALPOWER);
			break;
		case PF:			
			data = (src[3]&0xFF)<<8 | (src[4]&0xFF);
			if(data>10000) data -= 10000;
			else if(data==0xFFFF) data = 0;
			super.setInverterData(data, Inverter.Data.POWERFACTOR);			
			break;
		
		}		
		return true;
	}
	private Inverter.Fault mGetFaultCode(byte[] src) {
		if(src[4]==0b10000000) return Inverter.Fault.ETC_FAULT;
		else if(src[4]==0b01000000) return Inverter.Fault.ETC_FAULT;
		else if(src[4]==0b00100000) return Inverter.Fault.ETC_FAULT;
		else if(src[4]==0b00010000) return Inverter.Fault.ETC_FAULT;
		else if(src[5]==0b01000000) return Inverter.Fault.ETC_FAULT;
		else if(src[5]==0b00100000) return Inverter.Fault.INV_OVERH;
		else if(src[5]==0b00010000) return Inverter.Fault.INV_OVERH;
		else if(src[5]==0b00001000) return Inverter.Fault.PV_OC;
		else if(src[5]==0b00000100) return Inverter.Fault.GRID_OC;
		else if(src[5]==0b00000010) return Inverter.Fault.GRID_OC;
		else if(src[5]==0b00000001) return Inverter.Fault.PV_OC;
		else if(src[6]==0b10000000) return Inverter.Fault.EARTH_FAULT;
		else if(src[6]==0b01000000) return Inverter.Fault.ETC_FAULT;
		else if(src[6]==0b00100000) return Inverter.Fault.ETC_FAULT;
		else if(src[6]==0b00010000) return Inverter.Fault.ETC_FAULT;
		else if(src[6]==0b00001000) return Inverter.Fault.GRID_OF;
		else if(src[6]==0b00000100) return Inverter.Fault.GRID_UF;
		else if(src[6]==0b00000010) return Inverter.Fault.GRID_OV;
		else if(src[6]==0b00000001) return Inverter.Fault.GRID_UV;
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
			txPacketbuff = getRequestPacket(id, InverterECOS.Command.PV);
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
			if(setInverterData(rxPacketbuff, InverterECOS.Command.PV)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterECOS.Command.GRID_VI_PW);
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
			if(setInverterData(rxPacketbuff, InverterECOS.Command.GRID_VI_PW)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterECOS.Command.FAULT);
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
			if(setInverterData(rxPacketbuff, InverterECOS.Command.FAULT)!=true) {
				threadHandler.post(runReault);
				return;
			}
			txPacketbuff = getRequestPacket(id, InverterECOS.Command.TOTAL);
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
			if(setInverterData(rxPacketbuff, InverterECOS.Command.TOTAL)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterECOS.Command.PF);
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
			if(setInverterData(rxPacketbuff, InverterECOS.Command.PF)!=true) {
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
