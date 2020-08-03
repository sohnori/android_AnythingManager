package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterE_P5 extends Inverter{
	private Thread mThread;
	public InverterE_P5(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("동양애앤피(주)", Equipment.EquipInfo.MANUFACTURER);
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
		byte[] packet = new byte[7];		
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		packet[0] = 0x0A; // header
		packet[1] = (byte)0x96; // header2
		packet[2] = (byte)id; // station ID
		packet[3] = 0x54; // command
		packet[4] = 0x18; // length
		packet[5] = 0x05; // tail		
		packet[6] = (byte)(packet[2]+packet[3]+packet[4]); // checksum			
		return packet;
	}
	public boolean verifyResponse(byte[] src, int id) {
		int cnt=0;
		byte cksum=0;		
		// verify header1, 2
		if (!((src[0]==(byte)0xB1) && (src[1]==(byte)0xB5))) {
			mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if (src[2]!=id) {
			mMsg = Inverter.ERROR_ID;
			return false;
		}
		for(cnt=0;cnt<39;cnt++) { // calculate check sum
			cksum ^= src[cnt];	
		}

		if ((byte)cksum!=(byte)src[39]) {
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
		int data1=0;
		int data2=0;					
		data1=(src[4]&0xFF)<<8|(src[3]&0xFF);
		data2=(src[10]&0xFF)<<8|(src[9]&0xFF);		
		if(data1!=0 && data2!=0) data1 =(short) ((short) (data1+data2)/2/10);
		else if(data1!=0 && data2==0) data1/=10;
		else if(data1==0 && data2!=0) data1 = (short) (data2/10);
		else data1 = 0;
		super.setInverterData(data1, Inverter.Data.PVV);
		data1=(src[6]&0xFF)<<8|(src[5]&0xFF);
		data2=(src[12]&0xFF)<<8|(src[11]&0xFF);	
		data1 = (data1+data2)/10; // fixed 2 to fixed 1
		super.setInverterData(data1, Inverter.Data.PVI);
		data1=(src[8]&0xFF)<<8|(src[7]&0xFF);
		data2=(src[14]&0xFF)<<8|(src[13]&0xFF);	
		data1 = (data1+data2)/100; // W to kW fixed 1
		super.setInverterData(data1, Inverter.Data.PVP);		
		data1=(src[16]&0xFF)<<8|(src[15]&0xFF);
		data2=(src[18]&0xFF)<<8|(src[17]&0xFF);			
		super.setInverterData(data1, Inverter.Data.GRIDRV);		
		super.setInverterData(data2, Inverter.Data.GRIDRI);
		data1=(src[20]&0xFF)<<8|(src[19]&0xFF);
		data2=data1/100; // W to kW fixed 1
		super.setInverterData(data2, Inverter.Data.GRIDREALPOWER);
		if(data1>0) super.setInverterStatus(Inverter.Status.RUN);
		else super.setInverterStatus(Inverter.Status.STOP);							
		data1=(src[25]&0xFF)<<16|(src[24]&0xFF)<<8|(src[23]&0xFF);		
		super.setInverterData(data1, Inverter.Data.TOTALPOWER);
		data1=(src[22]&0xFF)<<8|(src[21]&0xFF);
		super.setInverterData(data1, Inverter.Data.FREQUENCY);	
		super.setInverterData(this.mGetFaultCode(src));
		return true;		
	}
	private Inverter.Fault mGetFaultCode(byte[] faultCode){
		if(faultCode[35]==0b00000010) return Inverter.Fault.GRID_OV;
		else if(faultCode[35]==0b00010000) return Inverter.Fault.GRID_UF;
		else if(faultCode[35]==0b00100000) return Inverter.Fault.GRID_OF;
		else if(faultCode[35]==0b01000000) return Inverter.Fault.GRID_UV;
		else if(faultCode[35]==0b10000000) return Inverter.Fault.GRID_OV;
		else if(faultCode[36]==0b00000001) return Inverter.Fault.INV_OVERH;
		else if(faultCode[36]==0b00000010) return Inverter.Fault.EARTH_FAULT;
		else if(faultCode[36]==0b00000100) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[36]==0b00001000) return Inverter.Fault.PV_OV;
		else if(faultCode[36]==0b00010000) return Inverter.Fault.GRID_OC;
		else if(faultCode[36]==0b01000000) return Inverter.Fault.PV_UV;
		else if(faultCode[36]==0b10000000) return Inverter.Fault.PV_OV;
		else if(faultCode[37]==0b00100000) return Inverter.Fault.ETC_FAULT;		
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
