package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterDASS extends Inverter{
	public enum Command {
		DASS_CMD_MOD, DASS_CMD_ST1, DASS_CMD_ST2, DASS_CMD_ST3, DASS_CMD_ST4 ,DASS_CMD_ST5 ,DASS_CMD_ST6 
	}	
		
	Thread mThread;	
	public InverterDASS(Phase phase) {
		super(phase);
		super.setEquipInfo("DASSTech", Equipment.EquipInfo.MANUFACTURER);		
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
	
	public byte[] getRequestPacket(int id, InverterDASS.Command cmd) {
		byte[] bufferByte = new byte[8];
		byte idByte;		
		if(id>99) {
			super.mMsg = Inverter.ERROR_ID;
			return null;
		}		
		bufferByte[0] = '^';
		bufferByte[1] = 'P';
		bufferByte[2] = '0';
		idByte = (byte)(id/10+0x30);
		bufferByte[3] = idByte;
		idByte = (byte)(id%10+0x30);
		bufferByte[4] = idByte;				
		switch (cmd){
		case DASS_CMD_MOD:		
			bufferByte[5] = 'M';
			bufferByte[6] = 'O';
			bufferByte[7] = 'D';
			break;
		case DASS_CMD_ST1:		
			bufferByte[5] = 'S';
			bufferByte[6] = 'T';
			bufferByte[7] = '1';
			break;
		case DASS_CMD_ST2:		
			bufferByte[5] = 'S';
			bufferByte[6] = 'T';
			bufferByte[7] = '2';
			break;
		case DASS_CMD_ST3:		
			bufferByte[5] = 'S';
			bufferByte[6] = 'T';
			bufferByte[7] = '3';
			break;
		case DASS_CMD_ST4:
			bufferByte[5] = 'S';
			bufferByte[6] = 'T';
			bufferByte[7] = '4';
			break;
		case DASS_CMD_ST5:		
			bufferByte[5] = 'S';
			bufferByte[6] = 'T';
			bufferByte[7] = '5';
			break;
		case DASS_CMD_ST6:
			bufferByte[5] = 'S';
			bufferByte[6] = 'T';
			bufferByte[7] = '6';
			break;
		default:
			super.mMsg = "Invalid inverter cmd.";
			return null;
		}		
		return bufferByte;
	}
	
	public boolean verifyResponse(byte[] src, int id, InverterDASS.Command cmd)
	{
		byte cksumReceivedHigh;
		byte cksumReceivedLow;
		int length;
		int cksumCompare = 0;
		byte cksumCompareHigh;
		byte cksumCompareLow;
		if(id>99) {
			super.mMsg = Inverter.ERROR_ID;
			return false;
		}
				
		if(!(src[5]=='0' && src[6]==(byte)(id/10+0x30) && src[7]==(byte)(id%10+0x30))) {
			super.mMsg = Inverter.ERROR_ID;
			return false;
		}
		length = (src[3] - 0x30)*10;
		length += (src[4] -0x30);
		cksumReceivedHigh = src[length+3];
		cksumReceivedLow = src[length+4];
		switch (cmd){
		case DASS_CMD_MOD:
			if(!(src[0]=='^' && src[1]=='D' && src[2]=='0')) {
				super.mMsg = Inverter.ERROR_PACKET;
				return false;
			}
			cksumCompare = src[2]-0x30;
			cksumCompare += src[3]-0x30;
			cksumCompare += src[4]-0x30;
			cksumCompare += src[5]-0x30;
			cksumCompare += src[6]-0x30;
			cksumCompare += src[7]-0x30;
			cksumCompare += src[9]-0x30;
			cksumCompare += src[11]-0x30;
			cksumCompare += src[12]-0x30;
			cksumCompare += src[13]-0x30;
			cksumCompare += src[14]-0x30;
			cksumCompare += src[16]-0x30;
			cksumCompare += src[17]-0x30;
			cksumCompare += src[18]-0x30;			
			break;
		case DASS_CMD_ST1:
			if(!(src[0]=='^' && src[1]=='D' && src[2]=='1')) {
				super.mMsg = Inverter.ERROR_PACKET;
				return false;
			}
			cksumCompare = src[2]-0x30;
			cksumCompare += src[3]-0x30;
			cksumCompare += src[4]-0x30;
			cksumCompare += src[5]-0x30;
			cksumCompare += src[6]-0x30;
			cksumCompare += src[7]-0x30;
			cksumCompare += src[9]-0x30;
			cksumCompare += src[10]-0x30;
			cksumCompare += src[11]-0x30;
			cksumCompare += src[13]-0x30;
			cksumCompare += src[14]-0x30;
			cksumCompare += src[15]-0x30;
			cksumCompare += src[16]-0x30;
			cksumCompare += src[18]-0x30;
			cksumCompare += src[19]-0x30;
			cksumCompare += src[20]-0x30;
			cksumCompare += src[21]-0x30;		
			break;
		case DASS_CMD_ST2:
			if(!(src[0]=='^' && src[1]=='D' && src[2]=='2')) {
				super.mMsg = Inverter.ERROR_PACKET;
				return false;
			}
			cksumCompare = src[2]-0x30;
			cksumCompare += src[3]-0x30;
			cksumCompare += src[4]-0x30;
			cksumCompare += src[5]-0x30;
			cksumCompare += src[6]-0x30;
			cksumCompare += src[7]-0x30;
			cksumCompare += src[9]-0x30;
			cksumCompare += src[10]-0x30;
			cksumCompare += src[11]-0x30;
			cksumCompare += src[13]-0x30;
			cksumCompare += src[14]-0x30;
			cksumCompare += src[15]-0x30;
			cksumCompare += src[17]-0x30;
			cksumCompare += src[18]-0x30;
			cksumCompare += src[19]-0x30;
			cksumCompare += src[21]-0x30;
			cksumCompare += src[22]-0x30;
			cksumCompare += src[23]-0x30;
			break;
		case DASS_CMD_ST3:
			if(!(src[0]=='^' && src[1]=='D' && src[2]=='3')) {
				super.mMsg = Inverter.ERROR_PACKET;
				return false;
			}
			cksumCompare = src[2]-0x30;
			cksumCompare += src[3]-0x30;
			cksumCompare += src[4]-0x30;
			cksumCompare += src[5]-0x30;
			cksumCompare += src[6]-0x30;
			cksumCompare += src[7]-0x30;
			cksumCompare += src[9]-0x30;
			cksumCompare += src[10]-0x30;
			cksumCompare += src[11]-0x30;
			cksumCompare += src[12]-0x30;
			cksumCompare += src[14]-0x30;
			cksumCompare += src[15]-0x30;
			cksumCompare += src[16]-0x30;
			cksumCompare += src[17]-0x30;
			cksumCompare += src[19]-0x30;
			cksumCompare += src[20]-0x30;
			cksumCompare += src[21]-0x30;
			cksumCompare += src[22]-0x30;
			break;
		case DASS_CMD_ST4:
			if(!(src[0]=='^' && src[1]=='D' && src[2]=='4')) {
				super.mMsg = Inverter.ERROR_PACKET;
				return false;
			}
			cksumCompare = src[2]-0x30;
			cksumCompare += src[3]-0x30;
			cksumCompare += src[4]-0x30;
			cksumCompare += src[5]-0x30;
			cksumCompare += src[6]-0x30;
			cksumCompare += src[7]-0x30;
			cksumCompare += src[9]-0x30;
			cksumCompare += src[10]-0x30;
			cksumCompare += src[11]-0x30;
			cksumCompare += src[12]-0x30;
			cksumCompare += src[14]-0x30;
			cksumCompare += src[15]-0x30;
			cksumCompare += src[16]-0x30;
			cksumCompare += src[17]-0x30;
			cksumCompare += src[18]-0x30;
			cksumCompare += src[19]-0x30;
			cksumCompare += src[20]-0x30;		
			break;
		case DASS_CMD_ST5:
			if(!(src[0]=='^' && src[1]=='D' && src[2]=='5')) {
				super.mMsg = Inverter.ERROR_PACKET;
				return false;
			}
			cksumCompare = src[2]-0x30;
			cksumCompare += src[3]-0x30;
			cksumCompare += src[4]-0x30;
			cksumCompare += src[5]-0x30;
			cksumCompare += src[6]-0x30;
			cksumCompare += src[7]-0x30;
			cksumCompare += src[9]-0x30;
			cksumCompare += src[10]-0x30;
			cksumCompare += src[11]-0x30;
			cksumCompare += src[12]-0x30;
			cksumCompare += src[14]-0x30;
			cksumCompare += src[15]-0x30;
			cksumCompare += src[16]-0x30;
			cksumCompare += src[17]-0x30;
			cksumCompare += src[19]-0x30;
			cksumCompare += src[20]-0x30;
			cksumCompare += src[21]-0x30;
			cksumCompare += src[22]-0x30;
			cksumCompare += src[24]-0x30;
			cksumCompare += src[25]-0x30;
			cksumCompare += src[26]-0x30;
			cksumCompare += src[27]-0x30;
			break;
		case DASS_CMD_ST6: // cksum_calcurate fault code A~Z to 10~35(decimal)
			if(!(src[0]=='^' && src[1]=='D' && src[2]=='6')) {
				super.mMsg = Inverter.ERROR_PACKET;
				return false;
			}
			// 계산이 불분명하여 제외함
//			cksumCompare = src[2]-0x30;
//			cksumCompare += src[3]-0x30;
//			cksumCompare += src[4]-0x30;
//			cksumCompare += src[5]-0x30;
//			cksumCompare += src[6]-0x30;
//			cksumCompare += src[7]-0x30;
//			cksumCompare += src[9]-0x30;
//			cksumCompare += src[11]-0x30;
//			cksumCompare += src[13]-0x30;
			break;
		default:
			super.mMsg = Inverter.ERROR_CMD;
			return false;
		}
		cksumCompareHigh = (byte)((cksumCompare%100)/10+0x30);
		cksumCompareLow = (byte)((cksumCompare%100)%10+0x30);
		if (cmd!=InverterDASS.Command.DASS_CMD_ST6 && (cksumReceivedHigh!=cksumCompareHigh || cksumReceivedLow!=cksumCompareLow)) {
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
		
	public boolean requestDataAll(Terminal terminal, int id, EditText log) {
		this.initInverterData();
		if(mTryCommunicationDASS(terminal, id, log, InverterDASS.Command.DASS_CMD_ST1)!=true) return false;
		if(mTryCommunicationDASS(terminal, id, log, InverterDASS.Command.DASS_CMD_ST2)!=true) return false;
		if(mTryCommunicationDASS(terminal, id, log, InverterDASS.Command.DASS_CMD_ST3)!=true) return false;
		if(mTryCommunicationDASS(terminal, id, log, InverterDASS.Command.DASS_CMD_ST4)!=true) return false;
		if(mTryCommunicationDASS(terminal, id, log, InverterDASS.Command.DASS_CMD_ST5)!=true) return false;
		return true;		
	}
	
	private boolean mTryCommunicationDASS(Terminal terminal, int id, EditText log, InverterDASS.Command cmd) {			
		SimpleDateFormat imSimpleDate;
		String strPacket;
		Editable editLog = log.getText();		
		byte[] txPacket = this.getRequestPacket(id, cmd);
		if(txPacket==null) return false;
		terminal.initReceivedData();
		try {
			terminal.writeBytes(txPacket, 300);
		}		
		catch(Exception e) {
			editLog.append(e.getMessage());
		}
		
		imSimpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
		strPacket = imSimpleDate.format(new Date()) + "Transmit " + txPacket.length + " bytes: \n"
		        + HexDump.dumpHexString(txPacket, txPacket.length) + "\r\n";		
		editLog.append(strPacket);
//		this.mDelayFlag = false;
//		this.mHandler.postDelayed(new Runnable() {
//			public void run() {
//				mDelayFlag = true;
//			}
//		}, 1000);
//		while(this.mDelayFlag==false) ;
		this.delayTime(2000);
		if(terminal.isReceivedData()!=true) {
			this.mMsg = "no response\r\n";
			return false;
		}
		byte[] buff = terminal.getReceicedData();
		if(this.verifyResponse(buff, id, cmd)!=true) return false;
		this.setInverterData(buff, cmd);
		return true;
	}
	
	public boolean setInverterData(byte[] src, InverterDASS.Command cmd) {
		int data;
		String str;		
		char[] buff  = new char[4];
		switch(cmd) {
		case DASS_CMD_MOD:			
			break;
		case DASS_CMD_ST1:
			if(src.length!=25) {
				super.mMsg = "source length error.";
				return false;
			}			
			buff[0] = '0';
			buff[1] = (char)src[9];
			buff[2] = (char)src[10];
			buff[3] = (char)src[11];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.PVV);
			buff[0] = (char)src[13];
			buff[1] = (char)src[14];
			buff[2] = (char)src[15];
			buff[3] = (char)src[16];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.PVI);
			buff[0] = (char)src[18];
			buff[1] = (char)src[19];
			buff[2] = (char)src[20];
			buff[3] = (char)src[21];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.PVP);						
			break;
		case DASS_CMD_ST2:
			if(src.length!=27) {
				super.mMsg = "source length error.";
				return false;
			}
			buff[0] = '0';
			buff[1] = (char)src[9];
			buff[2] = (char)src[10];
			buff[3] = (char)src[11];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.GRIDRV);
			buff[0] = '0';
			buff[1] = (char)src[13];
			buff[2] = (char)src[14];
			buff[3] = (char)src[15];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.GRIDSV);
			buff[0] = '0';
			buff[1] = (char)src[17];
			buff[2] = (char)src[18];
			buff[3] = (char)src[19];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.GRIDTV);
			buff[0] = '0';
			buff[1] = (char)src[21];
			buff[2] = (char)src[22];
			buff[3] = (char)src[23];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.FREQUENCY);
			break;
		case DASS_CMD_ST3:
			if(src.length!=26) {
				super.mMsg = "source length error.";
				return false;
			}
			buff[0] = (char)src[9];
			buff[1] = (char)src[10];
			buff[2] = (char)src[11];
			buff[3] = (char)src[12];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.GRIDRI);
			buff[0] = (char)src[14];
			buff[1] = (char)src[15];
			buff[2] = (char)src[16];
			buff[3] = (char)src[17];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.GRIDSI);
			buff[0] = (char)src[19];
			buff[1] = (char)src[20];
			buff[2] = (char)src[21];
			buff[3] = (char)src[22];
			str = String.valueOf(buff);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.GRIDTI);			
			break;
		case DASS_CMD_ST4:
			if(src.length!=24) {
				super.mMsg = "source length error.";
				return false;
			}
			char[] buffTotal = new char[7];
			buffTotal[0] = '0';
			buffTotal[1] = '0';
			buffTotal[2] = '0';
			buffTotal[3] = (char)src[9];
			buffTotal[4] = (char)src[10];
			buffTotal[5] = (char)src[11];
			buffTotal[6] = (char)src[12];
			str = String.valueOf(buffTotal);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.GRIDREALPOWER);			
			buffTotal[0] = (char)src[14];
			buffTotal[1] = (char)src[15];
			buffTotal[2] = (char)src[16];
			buffTotal[3] = (char)src[17];
			buffTotal[4] = (char)src[18];
			buffTotal[5] = (char)src[19];
			buffTotal[6] = (char)src[20];			
			str = String.valueOf(buffTotal);
			data = Integer.parseInt(str);
			super.setInverterData(data, Inverter.Data.TOTALPOWER);				
			break;
		case DASS_CMD_ST5: // 환경센서 데이터 (불필요하여 구현안함)
			break;
		case DASS_CMD_ST6:
			if(src.length!=17) {
				super.mMsg = "source length error.";
				return false;
			}			
			if(src[11]=='0') super.setInverterStatus(Status.RUN);
			else super.setInverterStatus(Status.STOP);						
			super.setInverterData(this.mGetFaultCode(src[13]));						
			break;
		default:
			super.mMsg = "Inverter data setting error: Requested invalid category";
			return false;
		}
		return true;
	}
	
	private Inverter.Fault mGetFaultCode(byte faultCode) {
		switch(faultCode) {
		case '1':
			return Inverter.Fault.EARTH_FAULT;
		case '2':
			return Inverter.Fault.INV_OVERH;
		case '3':
			return Inverter.Fault.PV_OV;
		case '4':
			return Inverter.Fault.ETC_FAULT;
		case '5':
			return Inverter.Fault.ETC_FAULT;
		case '6':
			return Inverter.Fault.GRID_OC;
		case '7':
			return Inverter.Fault.ETC_FAULT;
		case '8':
			return Inverter.Fault.STANDALONE;
		case '9':
			return Inverter.Fault.INV_IGBT;
		case 'A':
			return Inverter.Fault.INV_IGBT;
		case 'B':
			return Inverter.Fault.INV_IGBT;
		case 'C':
			return Inverter.Fault.INV_IGBT;
		case 'D':
			return Inverter.Fault.ETC_FAULT;
		case 'K':
			return Inverter.Fault.GRID_OF;
		case 'L':
			return Inverter.Fault.GRID_UF;
		case 'M':
			return Inverter.Fault.GRID_OV;
		case 'N':
			return Inverter.Fault.GRID_UV;
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
			txPacketbuff = getRequestPacket(id, InverterDASS.Command.DASS_CMD_ST1);
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
			if(verifyResponse(rxPacketbuff, id, InverterDASS.Command.DASS_CMD_ST1)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterDASS.Command.DASS_CMD_ST1)!=true) {
				threadHandler.post(runReault);
				return;
			}
			// 명령시퀀스 구분점			
			txPacketbuff = getRequestPacket(id, InverterDASS.Command.DASS_CMD_ST2);
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
			if(verifyResponse(rxPacketbuff, id, InverterDASS.Command.DASS_CMD_ST2)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterDASS.Command.DASS_CMD_ST2)!=true) {
				threadHandler.post(runReault);
				return;
			}								
			// 명령시퀀스 구분점
			txPacketbuff = getRequestPacket(id, InverterDASS.Command.DASS_CMD_ST3);
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
			if(verifyResponse(rxPacketbuff, id, InverterDASS.Command.DASS_CMD_ST3)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterDASS.Command.DASS_CMD_ST3)!=true) {
				threadHandler.post(runReault);
				return;
			}
			// 명령시퀀스 구분점
			txPacketbuff = getRequestPacket(id, InverterDASS.Command.DASS_CMD_ST4);
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
			if(verifyResponse(rxPacketbuff, id, InverterDASS.Command.DASS_CMD_ST4)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterDASS.Command.DASS_CMD_ST4)!=true) {
				threadHandler.post(runReault);
				return;
			}
			// 명령시퀀스 구분점
			txPacketbuff = getRequestPacket(id, InverterDASS.Command.DASS_CMD_ST6);
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
			if(verifyResponse(rxPacketbuff, id, InverterDASS.Command.DASS_CMD_ST6)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			if(setInverterData(rxPacketbuff, InverterDASS.Command.DASS_CMD_ST6)!=true) {
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
