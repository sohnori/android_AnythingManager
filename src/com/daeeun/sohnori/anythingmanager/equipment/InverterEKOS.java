package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterEKOS extends Inverter {
	public enum Command {
		PV, GRID_PW_PF_FR, GRID_VI, TOTAL, FAULT  
	}	
	private Thread mThread;
	public InverterEKOS(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("(주)에코스", Equipment.EquipInfo.MANUFACTURER);
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
	public byte[] getRequestPacket(int id, int address, int length) {
		// Modbus-RTU protocol
		byte[] packet = new byte[8];		
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		packet[0] = (byte)id; // id
		packet[1] = (byte)0x04; // function code_read input register		
		packet[2] = (byte)((address>>8)&0xFF); // address high
		packet[3] = (byte)(address&0xFF); // address low
		packet[4] = (byte)((length>>8)&0xFF); // length high
		packet[5] = (byte)(length&0xFF); // length low
		CyclicalRedundancyCheck crcModbus = new CyclicalRedundancyCheck();
		byte[] crc = crcModbus.getCRC16Bytes(packet, 6, CyclicalRedundancyCheck.Crc16.CRC16_MODBUS);
		packet[6] = (byte)crc[0];
		packet[7] = (byte)crc[1];
		return packet;
	}
	public byte[] getRequestPacket(int id, InverterEKOS.Command cmd) {
		// Modbus-RTU protocol
		byte[] packet = new byte[8];
		short address = 0;
		short length = 0;
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}		
		packet[0] = (byte)id; // id
		packet[1] = (byte)0x04; // function code_read input register
		switch(cmd) {
		case PV:
			address = 34;
			length = 8;			
			break;
		case GRID_PW_PF_FR:
			address = 46;
			length = 8;	
			break;
		case GRID_VI:
			address = 58;
			length = 12;	
			break;
		case TOTAL:
			address = 94;
			length = 4;	
			break;
		case FAULT:
			address = 98;
			length = 6;	
			break;	
		default:
			mMsg = Inverter.ERROR_REQUEST;
			return null;
		}
		packet[2] = (byte)((address>>8)&0xFF); // address high
		packet[3] = (byte)(address&0xFF); // address low			
		packet[4] = (byte)((length>>8)&0xFF); // length high
		packet[5] = (byte)(length&0xFF); // length low
		CyclicalRedundancyCheck crcModbus = new CyclicalRedundancyCheck();
		byte[] crc = crcModbus.getCRC16Bytes(packet, 6, CyclicalRedundancyCheck.Crc16.CRC16_MODBUS);
		packet[6] = (byte)crc[0];
		packet[7] = (byte)crc[1];
		return packet;
	}
	public boolean verifyResponse(byte[] src, int id, byte functionCode) {
		// verify header1, 2
		if (src[0]!=(byte)0x04 || src.length==src[3]+4) {
			mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if (src[2]!=id) {
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
	public boolean setInverterData(byte[] src, InverterEKOS.Command cmd) {				
		int data = 0;
		float data2=0;
		switch(cmd) {		
		case PV:
			data2 = Float.intBitsToFloat((src[3]&0xFF)<<24 | (src[4]&0xFF)<<16 | (src[5]&0xFF)<<8 | (src[6]&0xFF));
			data = (int)data2;			
			super.setInverterData(data, Inverter.Data.PVV);
			data2 = Float.intBitsToFloat((src[11]&0xFF)<<24 | (src[12]&0xFF)<<16 | (src[13]&0xFF)<<8 | (src[14]&0xFF));
			data = (int)(data2*10); // to fixed 1
			super.setInverterData(data, Inverter.Data.PVI);
			data2 = Float.intBitsToFloat((src[15]&0xFF)<<24 | (src[16]&0xFF)<<16 | (src[17]&0xFF)<<8 | (src[18]&0xFF));
			data = (int)(data2/100); // W to kW fixed 1
			super.setInverterData(data, Inverter.Data.PVP);			
			break;
		case GRID_PW_PF_FR:
			data2 = Float.intBitsToFloat((src[3]&0xFF)<<24 | (src[4]&0xFF)<<16 | (src[5]&0xFF)<<8 | (src[6]&0xFF));
			data = (int)(data2/100); // W to kW fixed 1		
			super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
			if(data>0) setInverterStatus(Inverter.Status.RUN);
			else setInverterStatus(Inverter.Status.STOP);
			data2 = Float.intBitsToFloat((src[11]&0xFF)<<24 | (src[12]&0xFF)<<16 | (src[13]&0xFF)<<8 | (src[14]&0xFF));
			data = (int)(data2*10); // to fixed 1		
			super.setInverterData(data, Inverter.Data.POWERFACTOR);
			data2 = Float.intBitsToFloat((src[15]&0xFF)<<24 | (src[16]&0xFF)<<16 | (src[17]&0xFF)<<8 | (src[18]&0xFF));
			data = (int)(data2*10); // to fixed 1		
			super.setInverterData(data, Inverter.Data.FREQUENCY);			
			break;
		case GRID_VI:			
			data2 = Float.intBitsToFloat((src[3]&0xFF)<<24 | (src[4]&0xFF)<<16 | (src[5]&0xFF)<<8 | (src[6]&0xFF));
			data = (int)data2;
			super.setInverterData(data, Inverter.Data.GRIDRV);
			data2 = Float.intBitsToFloat((src[7]&0xFF)<<24 | (src[8]&0xFF)<<16 | (src[9]&0xFF)<<8 | (src[10]&0xFF));
			data = (int)data2;
			super.setInverterData(data, Inverter.Data.GRIDSV);
			data2 = Float.intBitsToFloat((src[11]&0xFF)<<24 | (src[12]&0xFF)<<16 | (src[13]&0xFF)<<8 | (src[14]&0xFF));
			data = (int)data2;
			super.setInverterData(data, Inverter.Data.GRIDTV);
			data2 = Float.intBitsToFloat((src[15]&0xFF)<<24 | (src[16]&0xFF)<<16 | (src[17]&0xFF)<<8 | (src[18]&0xFF));
			data = (int)(data2*10); // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDRI);
			data2 = Float.intBitsToFloat((src[19]&0xFF)<<24 | (src[20]&0xFF)<<16 | (src[21]&0xFF)<<8 | (src[22]&0xFF));
			data = (int)(data2*10); // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDSI);
			data2 = Float.intBitsToFloat((src[23]&0xFF)<<24 | (src[24]&0xFF)<<16 | (src[25]&0xFF)<<8 | (src[26]&0xFF));
			data = (int)(data2*10); // to fixed 1
			super.setInverterData(data, Inverter.Data.GRIDTI);
			break;
		case TOTAL:
			int dataTotal;
			dataTotal = (src[3]&0xFF)<<24 | (src[4]&0xFF)<<16 | (src[5]&0xFF)<<8 | (src[6]&0xFF);
			data = (src[7]&0xFF)<<24 | (src[8]&0xFF)<<16 | (src[9]&0xFF)<<8 | (src[10]&0xFF);
			dataTotal *= 1000; // MWh to kWh 
			data /= 1000; // Wh to kWh
			dataTotal += data;
			super.setInverterData(dataTotal, Inverter.Data.TOTALPOWER);
			break;
		case FAULT:
			setInverterData(this.mGetFaultCode(src));
			break;
		default:
			mMsg = Inverter.ERROR_CMD;
			return false;
		}		
		return true;		
	}
	private Inverter.Fault mGetFaultCode(byte[] faultCode){
		if(faultCode[3]==0b01000000) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[3]==0b00100000) return Inverter.Fault.EARTH_FAULT;
		else if(faultCode[3]==0b00010000) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[3]==0b00001000) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[4]==0b00000100) return Inverter.Fault.PV_OV;
		else if(faultCode[4]==0b00000001) return Inverter.Fault.PV_UV;
		else if(faultCode[9]==0b00001000) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[9]==0b00000100) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[9]==0b00000010) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[9]==0b00000001) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[10]==0b10000000) return Inverter.Fault.INV_OVERH;
		else if(faultCode[10]==0b00010000) return Inverter.Fault.GRID_OC;
		else if(faultCode[10]==0b00001000) return Inverter.Fault.GRID_OC;
		else if(faultCode[11]==0b00010000) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[11]==0b00001000) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[11]==0b00000100) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[11]==0b00000010) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[11]==0b00000001) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[12]==0b00100000) return Inverter.Fault.EARTH_FAULT;
		else if(faultCode[12]==0b00010000) return Inverter.Fault.STANDALONE;
		else if(faultCode[12]==0b00001000) return Inverter.Fault.GRID_UF;
		else if(faultCode[12]==0b00000100) return Inverter.Fault.GRID_OF;
		else if(faultCode[12]==0b00000010) return Inverter.Fault.GRID_UV;
		else if(faultCode[12]==0b00000001) return Inverter.Fault.GRID_OV;
		else if(faultCode[13]==0b00000010) return Inverter.Fault.INV_OVERH;
		else if(faultCode[13]==0b00000001) return Inverter.Fault.GRID_OV;
		else if(faultCode[14]==0b00100000) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[14]==0b00010000) return Inverter.Fault.GRID_OV;
		else if(faultCode[14]==0b00000010) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[14]==0b00000001) return Inverter.Fault.GRID_OC;
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
			txPacketbuff = getRequestPacket(id, InverterEKOS.Command.PV);
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
			if(setInverterData(rxPacketbuff, InverterEKOS.Command.PV)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterEKOS.Command.GRID_PW_PF_FR);
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
			if(setInverterData(rxPacketbuff, InverterEKOS.Command.GRID_PW_PF_FR)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterEKOS.Command.GRID_VI);
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
			if(setInverterData(rxPacketbuff, InverterEKOS.Command.GRID_VI)!=true) {
				threadHandler.post(runReault);
				return;
			}
			txPacketbuff = getRequestPacket(id, InverterEKOS.Command.TOTAL);
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
			if(setInverterData(rxPacketbuff, InverterEKOS.Command.TOTAL)!=true) {
				threadHandler.post(runReault);
				return;
			}
			//
			txPacketbuff = getRequestPacket(id, InverterEKOS.Command.FAULT);
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
			if(setInverterData(rxPacketbuff, InverterEKOS.Command.FAULT)!=true) {
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
