package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class LoraModemSKT extends Equipment{
	public enum Model {
		IPL, F1M
	}
	public enum CommandWisol {
		GET_DEV_EUI, GET_APP_EUI, GET_TX_DATARATE, GET_ADR, GET_RETX, GET_RX1_DELAY, GET_LAST_RSSI_SNR,
		GET_CLASS_TYPE, GET_FIRM_VERSION, GET_APP_KEY, GET_ATTEN_GAIN, GET_UNCONFIRMED_MSG_RET_NUMBER,
		GET_RX1_DATATATE_OFFSET, GET_UPLINK_CYCLE, GET_CHANNEL_TXPOWER, GET_UART_BAUDRATE,
		TX_MSG
	}
	public enum CommandIPL {
		GET_FIRM_VERSION
	}
	public enum CommandF1M {
		GET_FIRM_VERSION
	}
	public static final int COMMUNICATION_DELAY_MS = 600;
	private Thread mThread;
	public static final String ERROR_NO_RESPONSE = "응답 없음.";	
	public static final String ERROR_CMD = "Invalid modem Command";
	public LoraModemSKT.Model mModel;
	private String mMsg = "";
	public LoraModemSKT(LoraModemSKT.Model model) {
		super.setEquipInfo("Lora Modem", Equipment.EquipInfo.EQUIPTYPE);
		this.mModel = model;
		if(model==LoraModemSKT.Model.IPL) super.setEquipInfo("아이피엘", Equipment.EquipInfo.MANUFACTURER);
		else if(model==LoraModemSKT.Model.F1M)super.setEquipInfo("에프원미디어", Equipment.EquipInfo.MANUFACTURER);
		super.setEquipInfo("SKT 로라모뎀", Equipment.EquipInfo.ETCINFO);
	}
	public String getMessage() {
		return this.mMsg;
	}
	public void runCommunicationThread(Terminal terminal, EditText log, EditText result) {
		mThread = new RequestDataThread(terminal, log, result);
		mThread.setDaemon(true);
		mThread.start();
	}
	public byte[] getRequestPacket(LoraModemSKT.CommandIPL cmd) {
		byte[] packet = new byte[9];
		packet[0] = 'I';
		packet[1] = 'P';
		packet[2] = 'L';
		packet[3] = ' ';
		switch(cmd) {
		case GET_FIRM_VERSION:
			packet[4] = 'I';
			packet[5] = 'V';
			break;
		default:
			return null;
		}
		packet[6] = ' ';
		packet[7] = '\r';
		packet[8] = '\n';
		return packet;
	}
	public byte[] getRequestPacket(LoraModemSKT.CommandF1M cmd) {
		byte[] packet = new byte[8];
		packet[0] = 'F';
		packet[1] = '1';
		packet[2] = 'M';
		packet[3] = ' ';
		switch(cmd) {
		case GET_FIRM_VERSION:
			packet[4] = 'I';
			packet[5] = 'V';
			break;
		default:
			return null;
		}		
		packet[6] = '\r';
		packet[7] = '\n';
		return packet;
	}
	public byte[] getRequestPacket(LoraModemSKT.CommandWisol cmd) {
		byte[] packet = new byte[9];
		packet[0] = 'L';
		packet[1] = 'R';
		packet[2] = 'W';
		packet[3] = ' ';
		switch(cmd) {
		case GET_DEV_EUI:
			packet[4] = '3';
			packet[5] = 'F';
			break;
		case GET_APP_EUI:
			packet[4] = '4';
			packet[5] = '0';
			break;
		case GET_TX_DATARATE:
			packet[4] = '4';
			packet[5] = '2';
			break;
		case GET_ADR:
			packet[4] = '4';
			packet[5] = '4';
			break;
		case GET_RETX:
			packet[4] = '4';
			packet[5] = '5';
			break;
		case GET_RX1_DELAY:
			packet[4] = '4';
			packet[5] = '6';
			break;
		case GET_LAST_RSSI_SNR:
			packet[4] = '4';
			packet[5] = 'A';
			break;
		case GET_CLASS_TYPE:
			packet[4] = '4';
			packet[5] = 'C';
			break;
		case GET_FIRM_VERSION:
			packet[4] = '4';
			packet[5] = 'F';
			break;
		case GET_APP_KEY:
			packet[4] = '5';
			packet[5] = '2';
			break;
		case GET_ATTEN_GAIN:
			packet[4] = '6';
			packet[5] = '3';
			break;
		case GET_UNCONFIRMED_MSG_RET_NUMBER:
			packet[4] = '5';
			packet[5] = '5';
			break;
		case GET_RX1_DATATATE_OFFSET:
			packet[4] = '5';
			packet[5] = '6';
			break;
		case GET_UPLINK_CYCLE:
			packet[4] = '5';
			packet[5] = '9';
			break;
		case GET_CHANNEL_TXPOWER:
			packet[4] = '5';
			packet[5] = 'F';
			break;
		case GET_UART_BAUDRATE:
			packet[4] = '6';
			packet[5] = '2';
			break;
		case TX_MSG:
			packet = new byte[23];
			packet[0] = 'L';
			packet[1] = 'R';
			packet[2] = 'W';
			packet[3] = ' ';
			packet[4] = '3';
			packet[5] = '1';
			packet[6] = ' ';
			packet[7] = 'A';
			packet[8] = 'B';
			packet[9] = 'C';
			packet[10] = 'D';
			packet[11] = 'E';
			packet[12] = 'F';
			packet[13] = 'G';
			packet[14] = ' ';
			packet[15] = 'c';
			packet[16] = 'n';
			packet[17] = 'f';
			packet[18] = ' ';
			packet[19] = '1';
			packet[20] = ' ';
			packet[21] = '\r';
			packet[22] = '\n';
			return packet;
		default:
			return null;
		}
		packet[6] = ' ';
		packet[7] = '\r';
		packet[8] = '\n';
		return packet;
	}
	private class RequestDataThread extends Thread {		
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
		RequestDataThread(Terminal terminal, EditText log, EditText result){			
			this.terminal = terminal;
			this.log = log;			
			this.result = result;
		}
		public void run() {
			if(mModel==LoraModemSKT.Model.IPL) txPacketbuff = getRequestPacket(LoraModemSKT.CommandIPL.GET_FIRM_VERSION);
			else if(mModel==LoraModemSKT.Model.F1M) txPacketbuff = getRequestPacket(LoraModemSKT.CommandF1M.GET_FIRM_VERSION);
			else return ;
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
				Thread.sleep(COMMUNICATION_DELAY_MS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mMsg = e.getMessage();
				threadHandler.post(runReault);
			}
//			if(terminal.isReceivedData()!=true) {
//				mMsg = ERROR_NO_RESPONSE;
//				threadHandler.post(runReault);		
//				return;
//			}			
//			rxPacketbuff = terminal.getReceicedData();
			//
//			txPacketbuff = getRequestPacket(LoraModemSKT.CommandWisol.GET_DEV_EUI);
//			if(txPacketbuff==null) return ;
//			terminal.initReceivedData();
//			try {
//				terminal.writeBytes(txPacketbuff, 300);
//			}		
//			catch(Exception e) {
//				mMsg = e.getMessage();
//				threadHandler.post(runReault);
//			}			
//			simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
//			strPacket = simpleDate.format(new Date()) + "Transmit " + txPacketbuff.length + " bytes: \n"
//			        + HexDump.dumpHexString(txPacketbuff, txPacketbuff.length) + "\r\n";
//			threadHandler.post(new Runnable() {
//				public void run() {
//					edit = log.getText();
//					edit.append(strPacket);					
//				}						
//			});
//			try {
//				Thread.sleep(COMMUNICATION_DELAY_MS);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				mMsg = e.getMessage();
//				threadHandler.post(runReault);
//			}
//			if(terminal.isReceivedData()!=true) {
//				mMsg = ERROR_NO_RESPONSE;
//				threadHandler.post(runReault);		
//				return;
//			}			
//			rxPacketbuff = terminal.getReceicedData();										
			//
			txPacketbuff = getRequestPacket(LoraModemSKT.CommandWisol.GET_FIRM_VERSION);
			if(txPacketbuff==null) return ;
			//terminal.initReceivedData();
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
//			if(terminal.isReceivedData()!=true) {
//				mMsg = ERROR_NO_RESPONSE;
//				threadHandler.post(runReault);		
//				return;
//			}			
			rxPacketbuff = terminal.getReceicedData();										
			//	
												
			//	
		}
	}
}