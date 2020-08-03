package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterWILL extends Inverter {
	public enum Command {
		SYSTEM_FAULT, PV_ALL, GRID_V_ALL, GRID_I_ALL, GRID_FR_PF_PW, TOTALPOWER 
	}
	
	public enum Model {
		UVHT_TYPE, M_TYPE, SL_TYPE
	}
	private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
	
	public static final int ADDR_PV_STATE = 0;
	public static final int ADDR_PVV = 32;
	public static final int ADDR_GRID_RV = 64;
	public static final int ADDR_GRID_RI = 67;
	public static final int ADDR_GRID_FREQUENCY = 70;
	public static final int ADDR_TOTALPOWER = 73;
	private Thread mThread;
	private Model mModel;	
	public InverterWILL(Inverter.Phase phase , Model model) {
		super(phase);				
		this.mModel = model;
		super.setEquipInfo("Willings", Equipment.EquipInfo.MANUFACTURER);		
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
	
	public byte[] getRequestPacket(int id, int address, int dataLength) {
		byte[] packet = new byte[20];
		int bcc = 0;
		if(id>99 || dataLength>4) {
			super.mMsg = Inverter.ERROR_REQUEST;
			return null;
		}		
		packet[0]=0x05; // fixed header 
		packet[1]=(byte)(id/10+0x30);
		packet[2]=(byte)(id%10+0x30); // id_ASCII
		packet[3]='r'; // read command
		packet[4]='S';
		packet[5]='B'; // type
		packet[6]='0'; // variable length high
		packet[7]='7'; // variable length low
		packet[8]='%';
		packet[9]='M';
		packet[10]='W'; // variable name
		packet[11]=(byte)(address/1000+0x30); // address MSB
		packet[12]=(byte)(address/100+0x30);
		packet[13]=(byte)(address/10+0x30);
		packet[14]=(byte)(address%10+0x30); // address LSB
		packet[15]=(byte)(dataLength/10+0x30); // data counter high
		packet[16]=(byte)(dataLength%10+0x30); // data counter low
		packet[17]=0x04; // tail
		for(int cnt=0;cnt<18;cnt++) {			
			bcc +=(int)(packet[cnt]&0xFF);
		}
		packet[18] = (byte)HEX_DIGITS[(bcc>>>4)&0x0F];
		packet[19] = (byte)HEX_DIGITS[bcc&0x0F];		
//		if(ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN) {
//			packet[18]=(byte)(bcc>>8);
//			packet[19]=(byte)bcc;
//		}
//		else if(ByteOrder.nativeOrder()==ByteOrder.LITTLE_ENDIAN) {
//			packet[18]=(byte)bcc;
//			packet[19]=(byte)(bcc>>8);
//		}		
		return packet;
	}
	
	public byte[] getRequestPacket(int id, InverterWILL.Command cmd) {
		byte[] retByteArray;
		switch(cmd) {
		case SYSTEM_FAULT:
			retByteArray = this.getRequestPacket(id, ADDR_PV_STATE, 4);
			return retByteArray;
		case PV_ALL:
			retByteArray = this.getRequestPacket(id, ADDR_PVV, 3);
			return retByteArray;
		case GRID_V_ALL:
			retByteArray = this.getRequestPacket(id, ADDR_GRID_RV, 3);
			return retByteArray;
		case GRID_I_ALL:
			retByteArray = this.getRequestPacket(id, ADDR_GRID_RI, 3);
			return retByteArray;
		case GRID_FR_PF_PW:
			retByteArray = this.getRequestPacket(id, ADDR_GRID_FREQUENCY, 3);
			return retByteArray;
		case TOTALPOWER:
			retByteArray = this.getRequestPacket(id, ADDR_TOTALPOWER, 2);
			return retByteArray;
		}
		return null;
	}
	
	public boolean verifyResponse(byte[] src, int id) {
		int cnt=0;
		int bcc = 0;
		byte bccHigh=0;
		byte bccLow=0;
		int dataLength = (int)((src[8]-0x30)*10);
		dataLength += (int)(src[9]-0x30);
		if(!(src[0]==0x06 && src[10+dataLength*2]==0x03)) {
			super.mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if(!(src[1]==((byte)(id/10)+0x30) && src[2]==((byte)id%10+0x30))) {
			super.mMsg = Inverter.ERROR_ID;
			return false;
		}
		for(;cnt<(11+dataLength*2);cnt++){
			//bcc += src[cnt];
			bcc +=(int)(src[cnt]&0xFF);
		}
		bccHigh = (byte)HEX_DIGITS[(bcc>>>4)&0x0F];
		bccLow = (byte)HEX_DIGITS[bcc&0x0F];
		if(!(src[cnt]==bccHigh) && src[cnt+1]==bccLow) {
			super.mMsg = Inverter.ERROR_CKSUM;
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
	
	public boolean setInverterData(byte[] src, InverterWILL.Command cmd) {
		int data;
		String str;
		char[] buffChar = new char[4];
		switch(cmd) {
		case SYSTEM_FAULT:
			if(src.length<29) {
				mMsg = "source length error.";
				return false;
			}
			byte[] buffByte = new byte[16];
			System.arraycopy(src, 11, buffByte, 0, 16);
			super.setInverterData(this.mGetFaultCode(buffByte));
			break;
		case PV_ALL:
			if(src.length<25) {
				mMsg = "source length error.";
				return false;
			}			
			buffChar[0] = (char)src[10];
			buffChar[1] = (char)src[11];
			buffChar[2] = (char)src[12];
			buffChar[3] = (char)src[13];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.PVV);
			buffChar[0] = (char)src[14];
			buffChar[1] = (char)src[15];
			buffChar[2] = (char)src[16];
			buffChar[3] = (char)src[17];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.PVI);
			buffChar[0] = (char)src[18];
			buffChar[1] = (char)src[19];
			buffChar[2] = (char)src[20];
			buffChar[3] = (char)src[21];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			if(this.mModel==InverterWILL.Model.UVHT_TYPE) data/=100; // W to kWh(0.1)
			super.setInverterData(data, Inverter.Data.PVP);
			break;
		case GRID_V_ALL:
			if(src.length<25) {
				mMsg = "source length error.";
				return false;
			}
			buffChar[0] = (char)src[10];
			buffChar[1] = (char)src[11];
			buffChar[2] = (char)src[12];
			buffChar[3] = (char)src[13];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.GRIDRV);
			buffChar[0] = (char)src[14];
			buffChar[1] = (char)src[15];
			buffChar[2] = (char)src[16];
			buffChar[3] = (char)src[17];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.GRIDSV);
			buffChar[0] = (char)src[18];
			buffChar[1] = (char)src[19];
			buffChar[2] = (char)src[20];
			buffChar[3] = (char)src[21];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.GRIDTV);			
			break;
		case GRID_I_ALL:
			if(src.length<25) {
				mMsg = "source length error.";
				return false;
			}
			buffChar[0] = (char)src[10];
			buffChar[1] = (char)src[11];
			buffChar[2] = (char)src[12];
			buffChar[3] = (char)src[13];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.GRIDRI);
			buffChar[0] = (char)src[14];
			buffChar[1] = (char)src[15];
			buffChar[2] = (char)src[16];
			buffChar[3] = (char)src[17];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.GRIDSI);
			buffChar[0] = (char)src[18];
			buffChar[1] = (char)src[19];
			buffChar[2] = (char)src[20];
			buffChar[3] = (char)src[21];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.GRIDTI);			
			break;
		case GRID_FR_PF_PW:
			if(src.length<25) {
				mMsg = "source length error.";
				return false;
			}
			buffChar[0] = (char)src[10];
			buffChar[1] = (char)src[11];
			buffChar[2] = (char)src[12];
			buffChar[3] = (char)src[13];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.FREQUENCY);
			buffChar[0] = (char)src[14];
			buffChar[1] = (char)src[15];
			buffChar[2] = (char)src[16];
			buffChar[3] = (char)src[17];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			super.setInverterData(data, Inverter.Data.POWERFACTOR);
			buffChar[0] = (char)src[18];
			buffChar[1] = (char)src[19];
			buffChar[2] = (char)src[20];
			buffChar[3] = (char)src[21];
			str = String.valueOf(buffChar);
			data = Integer.parseInt(str, 16);
			if(this.mModel==InverterWILL.Model.UVHT_TYPE) data/=100; // W to kWh(0.1)
			super.setInverterData(data, Inverter.Data.GRIDREALPOWER);
			if(data>0) super.setInverterStatus(Status.RUN);
			else super.setInverterStatus(Status.STOP);
			break;
		case TOTALPOWER:
			if(src.length<21) {
				mMsg = "source length error.";
				return false;
			}
			char[] buffChar2 = new char[8]; 
			buffChar2[0] = (char)src[14];
			buffChar2[1] = (char)src[15];
			buffChar2[2] = (char)src[16];
			buffChar2[3] = (char)src[17];
			buffChar2[4] = (char)src[10];
			buffChar2[5] = (char)src[11];
			buffChar2[6] = (char)src[12];
			buffChar2[7] = (char)src[13];
			str = String.valueOf(buffChar2);
			data = Integer.parseInt(str, 16);
			data/=1000; // Wh to kWh
			super.setInverterData(data, Inverter.Data.TOTALPOWER);				
			break;		
		default:
			mMsg = "Inverter data setting error: Requested invalid category";
			return false;
		}
		return true;
	}
	
	private Inverter.Fault mGetFaultCode(byte[] faultCode) {		
		switch(this.mModel) {
		case UVHT_TYPE:
			if( faultCode[3]=='1' || faultCode[4]=='2' || faultCode[4]=='3') return Inverter.Fault.PV_OV;			
			if(faultCode[3]=='4') return Inverter.Fault.PV_UV;
			if(faultCode[7]=='4' || faultCode[7]=='8' || faultCode[7]=='C') return Inverter.Fault.PV_OC;
			if(faultCode[6]=='2') return Inverter.Fault.EARTH_FAULT;
			if(faultCode[6]=='4') return Inverter.Fault.ETC_FAULT;
			if(faultCode[6]=='8') return Inverter.Fault.INV_OVERH;
			if(faultCode[5]=='1') return Inverter.Fault.ETC_FAULT;
			if(faultCode[5]=='8') return Inverter.Fault.ETC_FAULT;
			if(faultCode[11]=='1') return Inverter.Fault.GRID_OV;
			if(faultCode[11]=='2') return Inverter.Fault.GRID_UV;
			if(faultCode[11]=='4') return Inverter.Fault.GRID_OF;
			if(faultCode[11]=='8') return Inverter.Fault.GRID_UF;
			if(faultCode[10]=='1') return Inverter.Fault.STANDALONE;
			if(faultCode[10]=='2') return Inverter.Fault.EARTH_FAULT;
			if(faultCode[12]=='8') return Inverter.Fault.ETC_FAULT;
			return Inverter.Fault.NORMAL;
		case M_TYPE:
			if(faultCode[3]=='1') return Inverter.Fault.PV_OV;			
			if(faultCode[3]=='4') return Inverter.Fault.PV_UV;			
			if(faultCode[7]=='1') return Inverter.Fault.GRID_OV;
			if(faultCode[7]=='2') return Inverter.Fault.GRID_UV;
			if(faultCode[7]=='4') return Inverter.Fault.GRID_OF;
			if(faultCode[7]=='8') return Inverter.Fault.GRID_UF;
			if(faultCode[6]=='1') return Inverter.Fault.STANDALONE;
			if(faultCode[6]=='4') return Inverter.Fault.EARTH_FAULT;			
			return Inverter.Fault.NORMAL;
		case SL_TYPE:
			if(faultCode[3]=='1') return Inverter.Fault.PV_OV;
			if(faultCode[3]=='2') return Inverter.Fault.PV_OV;
			if(faultCode[3]=='4') return Inverter.Fault.PV_UV;
			if(faultCode[1]=='8') return Inverter.Fault.ETC_FAULT;
			if(faultCode[0]=='1') return Inverter.Fault.ETC_FAULT;
			if(faultCode[0]=='2') return Inverter.Fault.ETC_FAULT;
			if(faultCode[0]=='4') return Inverter.Fault.ETC_FAULT;
			if(faultCode[0]=='8') return Inverter.Fault.ETC_FAULT;
			if(faultCode[4]=='8') return Inverter.Fault.ETC_FAULT;
			if(faultCode[11]=='1') return Inverter.Fault.GRID_OV;
			if(faultCode[11]=='2') return Inverter.Fault.GRID_UV;
			if(faultCode[11]=='4') return Inverter.Fault.GRID_OF;
			if(faultCode[11]=='8') return Inverter.Fault.GRID_UF;
			if(faultCode[10]=='1') return Inverter.Fault.STANDALONE;
			if(faultCode[10]=='2') return Inverter.Fault.EARTH_FAULT;
			if(faultCode[8]=='8') return Inverter.Fault.ETC_FAULT;
			return Inverter.Fault.NORMAL;		
		default:
			return Inverter.Fault.NORMAL;	
		}	
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
			txPacketbuff = getRequestPacket(id, InverterWILL.Command.SYSTEM_FAULT);
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
			if(setInverterData(rxPacketbuff, InverterWILL.Command.SYSTEM_FAULT)!=true) {
				threadHandler.post(runReault);
				return;
			}
			// 명령 구분점
			txPacketbuff = getRequestPacket(id, InverterWILL.Command.PV_ALL);
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
			if(setInverterData(rxPacketbuff, InverterWILL.Command.PV_ALL)!=true) {
				threadHandler.post(runReault);
				return;
			}
			// 명령 구분점			
			txPacketbuff = getRequestPacket(id, InverterWILL.Command.GRID_V_ALL);
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
			if(setInverterData(rxPacketbuff, InverterWILL.Command.GRID_V_ALL)!=true) {
				threadHandler.post(runReault);
				return;
			}
			// 구분점			
			txPacketbuff = getRequestPacket(id, InverterWILL.Command.GRID_I_ALL);
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
			if(setInverterData(rxPacketbuff, InverterWILL.Command.GRID_I_ALL)!=true) {
				threadHandler.post(runReault);
				return;
			}
			// 구분점
			txPacketbuff = getRequestPacket(id, InverterWILL.Command.GRID_FR_PF_PW);
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
			if(setInverterData(rxPacketbuff, InverterWILL.Command.GRID_FR_PF_PW)!=true) {
				threadHandler.post(runReault);
				return;
			}
			// 구분점
			txPacketbuff = getRequestPacket(id, InverterWILL.Command.TOTALPOWER);
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
			if(setInverterData(rxPacketbuff, InverterWILL.Command.TOTALPOWER)!=true) {
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
