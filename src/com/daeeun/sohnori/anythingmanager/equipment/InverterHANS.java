package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterHANS extends Inverter {
	private Thread mThread;
	public InverterHANS(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("HANSOL", Equipment.EquipInfo.MANUFACTURER);
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
		byte[] packet = new byte[12];
		short cksum = 0;
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		packet[0] = 0x02; // STX
		packet[1] = 0x0C; // packet length(all)
		packet[2] = 0x00; // frame number
		packet[3] = 0x00; // option high
		packet[4] = 0x00; // option low
		packet[5] = (byte)id; // slave ID
		packet[6] = 0x00; // master ID
		packet[7] = 0x05; // ENQ(cmd)
		if(super.mPhase==Inverter.Phase.SINGLE) packet[8] = 0x11;
		else if(super.mPhase==Inverter.Phase.THREE) packet[8] = 0x33;
		else {
			mMsg = Inverter.ERROR_REQUEST;
			return null;
		}
		cksum = (short)(packet[5] + packet[6] + packet[7] + packet[8]);
		packet[9] = (byte)(cksum>>8);
		packet[10] = (byte)cksum;
		packet[11] = 0x03; // ETX
		return packet;
	}
	
	public boolean verifyResponse(byte[] src, int id) {
		int cnt=0;
		int cksum=0;		
		// verify STX, length, ACK code
		if (!((src[0]==0x02) && (src[1]==0x2E) && (src[7]==0x06))) {
			mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if (src[6]!=id) {
			mMsg = Inverter.ERROR_ID;
			return false;
		}
		for(cnt=5;cnt<43;cnt++) { // calculate check sum
			cksum += (int)(src[cnt]&0xFF);	
		}

		if (!((byte)(cksum>>8)==(byte)src[43] && (byte)cksum==(byte)src[44])) {
			mMsg =  Inverter.ERROR_CKSUM;
			return false;
		}
//		if (ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN && !((byte)(cksum>>8)==(byte)src[43] && (byte)cksum==(byte)src[44])) {
//			mMsg =  Inverter.ERROR_CKSUM;
//			return false;
//		}
//		if (ByteOrder.nativeOrder()==ByteOrder.LITTLE_ENDIAN && !((byte)(cksum>>8)==(byte)src[44] && (byte)cksum==(byte)src[43])) {
//			mMsg =  Inverter.ERROR_CKSUM;
//			return false;
//		}
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
		int data1=0;
		int data2=0;
		
//		if(ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN) {
//			data=src[15]>>24;
//			data|=src[16]>>16;
//			data|=src[17]>>8;
//			data|=src[18];
//		}
//		else if(ByteOrder.nativeOrder()==ByteOrder.LITTLE_ENDIAN) {
//			data=src[18]>>24;
//			data|=src[17]>>16;
//			data|=src[16]>>8;
//			data|=src[15];
//		}
		data1=(src[15]&0xFF)<<24|(src[16]&0xFF)<<16|(src[17]&0xFF)<<8|(src[18]&0xFF);
		data1/=1000; // Wh to kWh
		super.setInverterData(data1, Inverter.Data.TOTALPOWER);
		if(super.mPhase==Inverter.Phase.SINGLE) {
			data1=(src[19]&0xFF)<<8|src[20]&0xFF;			
			data2=(src[21]&0xFF)<<8|src[22]&0xFF;								
			super.setInverterData(data1, Inverter.Data.PVV);			
			super.setInverterData(data2, Inverter.Data.PVI);			
		}
		else if(super.mPhase==Inverter.Phase.THREE) {		
			data1=(src[23]&0xFF)<<8|src[24]&0xFF;			
			data2=(src[25]&0xFF)<<8|src[26]&0xFF;							
			super.setInverterData(data1, Inverter.Data.PVV);			
			super.setInverterData(data2, Inverter.Data.PVI);			
		}
		else {
			mMsg = Inverter.ERROR_PHASE;
			return false;
		}
		super.setInverterData(data1*data2/1000, Inverter.Data.PVP);		
		data1=(src[27]&0xFF)<<8|src[28]&0xFF;			
		data2=(src[29]&0xFF)<<8|src[30]&0xFF;			
		super.setInverterData(data1, Inverter.Data.GRIDRV);		
		super.setInverterData(data2, Inverter.Data.GRIDRI);
		super.setInverterData(data1*data2/1000, Inverter.Data.GRIDREALPOWER);
		if((data1*data2)>0) super.setInverterStatus(Inverter.Status.RUN);
		else super.setInverterStatus(Inverter.Status.STOP);
		data1=(src[31]&0xFF)<<8|src[32]&0xFF;			
		data2=(src[33]&0xFF)<<8|src[34]&0xFF;			
		super.setInverterData(data1, Inverter.Data.GRIDSV);		
		super.setInverterData(data2, Inverter.Data.GRIDSI);
		data1=(src[35]&0xFF)<<8|src[36]&0xFF;			
		data2=(src[37]&0xFF)<<8|src[38]&0xFF;
		super.setInverterData(data1, Inverter.Data.GRIDTV);		
		super.setInverterData(data2, Inverter.Data.GRIDTI);
		super.setInverterData(this.mGetFaultCode(src));
		return true;		
	}
	
	private Inverter.Fault mGetFaultCode(byte[] faultCode) {
		if(faultCode[39]==0b10000000 || faultCode[41]==0b10000000) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[39]==0b00100000 || faultCode[41]==0b00100000) return Inverter.Fault.INV_OVERH;
		else if(faultCode[39]==0b00010000 || faultCode[41]==0b00010000) return Inverter.Fault.EARTH_FAULT;
		else if(faultCode[39]==0b00001000 || faultCode[41]==0b00001000) return Inverter.Fault.GRID_UF;
		else if(faultCode[39]==0b00000100 || faultCode[41]==0b00000100) return Inverter.Fault.GRID_OF;
		else if(faultCode[39]==0b00000010 || faultCode[41]==0b00000010) return Inverter.Fault.INV_IGBT;
		else if(faultCode[39]==0b00000001 || faultCode[41]==0b00000001) return Inverter.Fault.INV_IGBT;
		else if(faultCode[40]==0b10000000 || faultCode[42]==0b10000000) return Inverter.Fault.GRID_UV;
		else if(faultCode[40]==0b01000000 || faultCode[42]==0b01000000) return Inverter.Fault.GRID_OV;
		else if(faultCode[40]==0b00100000 || faultCode[42]==0b00100000) return Inverter.Fault.GRID_OC;
		else if(faultCode[40]==0b00010000 || faultCode[42]==0b00010000) return Inverter.Fault.PV_UV;
		else if(faultCode[40]==0b00001000 || faultCode[42]==0b00001000) return Inverter.Fault.PV_OV;
		else if(faultCode[40]==0b00000100 || faultCode[42]==0b00000100) return Inverter.Fault.PV_UV;
		else if(faultCode[40]==0b00000010 || faultCode[42]==0b00000010) return Inverter.Fault.PV_OV;
		else if(faultCode[40]==0b00000001 || faultCode[42]==0b00000001) return Inverter.Fault.PV_OC;
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
