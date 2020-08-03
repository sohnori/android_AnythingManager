package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterREMS extends Inverter {
	private Thread mThread;
	public InverterREMS(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("REMS", Equipment.EquipInfo.MANUFACTURER);
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
		byte[] packet = new byte[5];		
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		packet[0] = 0x7E; // header
		packet[1] = (byte)id; // id		
		if(super.mPhase==Inverter.Phase.SINGLE) packet[2] = 0x01;
		else if(super.mPhase==Inverter.Phase.THREE) packet[2] = 0x07;		
		CyclicalRedundancyCheck crcModbus = new CyclicalRedundancyCheck();
		byte[] crc = crcModbus.getCRC16Bytes(packet, 3, CyclicalRedundancyCheck.Crc16.CRC16_MODBUS);
		packet[3] = (byte)crc[0];
		packet[4] = (byte)crc[1];
		return packet;
	}
	public Inverter.Phase getInvPhase() {
		return mPhase;
	}
	public boolean verifyResponse(byte[] src, int id) {
		// verify header1, 2
		if ((this.mPhase==Inverter.Phase.SINGLE && !(src[0]==0x7E && src[1]==id && src[2]==0x02))
				|| (this.mPhase==Inverter.Phase.THREE && !(src[0]==0x7E && src[1]==id && src[2]==0x08))) {
			mMsg = Inverter.ERROR_PACKET;
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
	public boolean setInverterData(byte[] src) {
		int data = 0;
		int data2 = 0;
		long dataTotal = 0;
		if(this.mPhase==Inverter.Phase.SINGLE) {
			data = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			this.setInverterData(data, Inverter.Data.PVV);
			data = (src[7]&0xFF)<<8 | (src[8]&0xFF);
			data *= 10; // to fixed 1
			this.setInverterData(data, Inverter.Data.PVI);
			data = (src[9]&0xFF)<<8 | (src[10]&0xFF);
			data /= 100; // W to kW fixed 1
			this.setInverterData(data, Inverter.Data.PVP);
			data = (src[11]&0xFF)<<8 | (src[12]&0xFF);
			this.setInverterData(data, Inverter.Data.GRIDRV);
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);
			data *= 10; // to fixed 1
			this.setInverterData(data, Inverter.Data.GRIDRI);
			data = (src[15]&0xFF)<<8 | (src[16]&0xFF);
			if(data>0) this.setInverterStatus(Inverter.Status.RUN);
			else this.setInverterStatus(Inverter.Status.STOP);
			data /= 100; // W to kW fixed 1
			this.setInverterData(data, Inverter.Data.GRIDREALPOWER);			
			data = (src[17]&0xFF)<<8 | (src[18]&0xFF);
			this.setInverterData(data, Inverter.Data.POWERFACTOR);
			data = (src[19]&0xFF)<<8 | (src[20]&0xFF);
			this.setInverterData(data, Inverter.Data.FREQUENCY);
			data = (src[21]&0xFF)<<24 |(src[22]&0xFF)<<16 | (src[23]&0xFF)<<8 | (src[24]&0xFF);				
			data2 = (src[25]&0xFF)<<24 | (src[26]&0xFF)<<16 | (src[27]&0xFF)<<8 | (src[28]&0xFF);
			dataTotal = (data&0xFFFFFFFF)<<32 | data2&0xFFFFFFFF;
			dataTotal /= 1000;
			this.setInverterData((int)dataTotal, Inverter.Data.TOTALPOWER);
			this.setInverterFault(src[29], src[30]);
		}
		else if(this.mPhase==Inverter.Phase.THREE) {
			data = (src[5]&0xFF)<<8 | (src[6]&0xFF);
			this.setInverterData(data, Inverter.Data.PVV);
			data = (src[7]&0xFF)<<8 | (src[8]&0xFF);
			data *= 10; // to fixed 1
			this.setInverterData(data, Inverter.Data.PVI);
			data = (src[9]&0xFF)<<24 | (src[10]&0xFF)<<16 | (src[11]&0xFF)<<8 | src[12]&0xFF;			
			data /= 100; // W to kW fixed 1
			this.setInverterData(data, Inverter.Data.PVP);
			data = (src[13]&0xFF)<<8 | (src[14]&0xFF);
			this.setInverterData(data, Inverter.Data.GRIDRV);
			data = (src[15]&0xFF)<<8 | (src[16]&0xFF);
			this.setInverterData(data, Inverter.Data.GRIDSV);
			data = (src[17]&0xFF)<<8 | (src[18]&0xFF);
			this.setInverterData(data, Inverter.Data.GRIDTV);
			data = (src[19]&0xFF)<<8 | (src[20]&0xFF);
			data *= 10; // to fixed 1
			this.setInverterData(data, Inverter.Data.GRIDRI);
			data = (src[21]&0xFF)<<8 | (src[22]&0xFF);
			data *= 10; // to fixed 1
			this.setInverterData(data, Inverter.Data.GRIDSI);
			data = (src[23]&0xFF)<<8 | (src[24]&0xFF);
			data *= 10; // to fixed 1
			this.setInverterData(data, Inverter.Data.GRIDTI);
			data = (src[25]&0xFF)<<24 | (src[26]&0xFF)<<16 | (src[27]&0xFF)<<8 | src[28]&0xFF;
			if(data>0) this.setInverterStatus(Inverter.Status.RUN);
			else this.setInverterStatus(Inverter.Status.STOP);
			data /= 100; // W to kW fixed 1
			this.setInverterData(data, Inverter.Data.GRIDREALPOWER);
			data = (src[29]&0xFF)<<8 | (src[30]&0xFF);
			this.setInverterData(data, Inverter.Data.POWERFACTOR);
			data = (src[31]&0xFF)<<8 | (src[32]&0xFF);
			this.setInverterData(data, Inverter.Data.FREQUENCY);
			data = (src[33]&0xFF)<<24 |(src[34]&0xFF)<<16 | (src[35]&0xFF)<<8 | (src[36]&0xFF);				
			data2 = (src[37]&0xFF)<<24 | (src[38]&0xFF)<<16 | (src[39]&0xFF)<<8 | (src[40]&0xFF);
			dataTotal = (data&0xFFFFFFFF)<<32 | data2&0xFFFFFFFF;
			dataTotal /= 1000;
			this.setInverterData((int)dataTotal, Inverter.Data.TOTALPOWER);
			this.setInverterFault(src[41], src[42]);
		}
		else return false;
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
