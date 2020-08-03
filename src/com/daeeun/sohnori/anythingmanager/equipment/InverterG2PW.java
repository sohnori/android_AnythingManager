package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterG2PW extends Inverter{
	private Thread mThread;
	public InverterG2PW(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("지투파워", Equipment.EquipInfo.MANUFACTURER);
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
	public boolean setInverterData(byte[] src) {				
		int data = 0;
		int data2 = 0;
		float valueFloat;		
		if(src.length<201) return false;
		data = (src[57]&0xFF)<<24 | (src[58]&0xFF)<<16 | (src[59]&0xFF)<<8 | src[60]&0xFF;
		super.setInverterData(this.mGetFaultCode(data));
		valueFloat = Float.intBitsToFloat((src[71]&0xFF)<<24 | (src[72]&0xFF)<<16 | (src[73]&0xFF)<<8 | src[74]&0xFF);		
		data = (int)valueFloat; 
		super.setInverterData(data, Inverter.Data.PVV);
		valueFloat = Float.intBitsToFloat((src[79]&0xFF)<<24 | (src[80]&0xFF)<<16 | (src[81]&0xFF)<<8 | src[82]&0xFF);
		data = (int)(valueFloat*10); // to fix 1 
		super.setInverterData(data, Inverter.Data.PVI);
		valueFloat = Float.intBitsToFloat((src[83]&0xFF)<<24 | (src[84]&0xFF)<<16 | (src[85]&0xFF)<<8 | src[86]&0xFF);
		data = (int)(valueFloat/100); // W to kW fix 1 
		super.setInverterData(data, Inverter.Data.PVP);
		valueFloat = Float.intBitsToFloat((src[95]&0xFF)<<24 | (src[96]&0xFF)<<16 | (src[97]&0xFF)<<8 | src[98]&0xFF);
		data = (int)(valueFloat/100); // W to kW fix 1
		if(data>0) super.setInverterStatus(Inverter.Status.RUN);
		else super.setInverterStatus(Inverter.Status.STOP);
		super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
		valueFloat = Float.intBitsToFloat((src[103]&0xFF)<<24 | (src[104]&0xFF)<<16 | (src[105]&0xFF)<<8 | src[106]&0xFF);
		if(valueFloat<0) valueFloat *= -1;
		data = (int)(valueFloat*10); // to fix 1 
		super.setInverterData(data, Inverter.Data.POWERFACTOR);
		valueFloat = Float.intBitsToFloat((src[107]&0xFF)<<24 | (src[108]&0xFF)<<16 | (src[109]&0xFF)<<8 | src[110]&0xFF);
		data = (int)(valueFloat*10); // W to kW fix 1 
		super.setInverterData(data, Inverter.Data.FREQUENCY);
		valueFloat = Float.intBitsToFloat((src[119]&0xFF)<<24 | (src[120]&0xFF)<<16 | (src[121]&0xFF)<<8 | src[122]&0xFF);
		data = (int)valueFloat; //to fix 0 
		super.setInverterData(data, Inverter.Data.GRIDRV);
		valueFloat = Float.intBitsToFloat((src[123]&0xFF)<<24 | (src[124]&0xFF)<<16 | (src[125]&0xFF)<<8 | src[126]&0xFF);
		data = (int)valueFloat; //to fix 0 
		super.setInverterData(data, Inverter.Data.GRIDSV);
		valueFloat = Float.intBitsToFloat((src[127]&0xFF)<<24 | (src[128]&0xFF)<<16 | (src[129]&0xFF)<<8 | src[130]&0xFF);
		data = (int)valueFloat; //to fix 0 
		super.setInverterData(data, Inverter.Data.GRIDTV);
		valueFloat = Float.intBitsToFloat((src[131]&0xFF)<<24 | (src[132]&0xFF)<<16 | (src[133]&0xFF)<<8 | src[134]&0xFF);
		data = (int)(valueFloat*10); //to fix 1 
		super.setInverterData(data, Inverter.Data.GRIDRI);
		valueFloat = Float.intBitsToFloat((src[135]&0xFF)<<24 | (src[136]&0xFF)<<16 | (src[137]&0xFF)<<8 | src[138]&0xFF);
		data = (int)(valueFloat*10); //to fix 1 
		super.setInverterData(data, Inverter.Data.GRIDSI);
		valueFloat = Float.intBitsToFloat((src[139]&0xFF)<<24 | (src[140]&0xFF)<<16 | (src[141]&0xFF)<<8 | src[142]&0xFF);
		data = (int)(valueFloat*10); //to fix 1 
		super.setInverterData(data, Inverter.Data.GRIDTI);
		data = (src[191]&0xFF)<<24 | (src[192]&0xFF)<<16 | (src[193]&0xFF)<<8 | src[194]&0xFF;
		data2 = (src[195]&0xFF)<<24 | (src[196]&0xFF)<<16 | (src[197]&0xFF)<<8 | src[198]&0xFF;
		data *= 1000; // MWh to kWh
		data2 /= 1000; // Wh to kWh
		data += data2;
		super.setInverterData(data, Inverter.Data.TOTALPOWER);
		return true;
	}
	private Inverter.Fault mGetFaultCode(int code) {
		if((code&0x01000000)>0) return Inverter.Fault.ETC_FAULT;
		else if((code&0x00800000)>0) return Inverter.Fault.ETC_FAULT;
		else if((code&0x00400000)>0) return Inverter.Fault.ETC_FAULT;
		else if((code&0x00200000)>0) return Inverter.Fault.GRID_OV;
		else if((code&0x00100000)>0) return Inverter.Fault.GRID_UV;
		else if((code&0x00080000)>0) return Inverter.Fault.PV_OV;
		else if((code&0x00040000)>0) return Inverter.Fault.PV_UV;
		else if((code&0x00020000)>0) return Inverter.Fault.PV_OV;
		else if((code&0x00010000)>0) return Inverter.Fault.PV_UV;
		else if((code&0x00000020)>0) return Inverter.Fault.STANDALONE;
		else if((code&0x00000010)>0) return Inverter.Fault.INV_OVERH;
		else if((code&0x00000008)>0) return Inverter.Fault.ETC_FAULT;
		else if((code&0x00000004)>0) return Inverter.Fault.GRID_OF;
		else if((code&0x00000002)>0) return Inverter.Fault.GRID_OC;
		else if((code&0x00000001)>0) return Inverter.Fault.PV_OC;
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
			txPacketbuff = getRequestPacket(id, (byte)0x04, 0, 98);
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
