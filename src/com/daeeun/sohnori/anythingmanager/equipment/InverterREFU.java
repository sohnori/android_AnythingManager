package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterREFU extends Inverter {
	private Thread mThread;
	public InverterREFU(Inverter.Phase phase) {
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
	public byte[] getRequestPacket(int id, byte ak, int parameter, int index) {
		if(id>31) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		byte[] packet = new byte[7];
		int bcc = 0;
		int pke = 0;
		id &= 0b00011111; // 7_special, 6_mirror, 5_broadcast telegram 4~0_ID(0~31)
		pke = ak;
		pke <<= 8;
		pke |= parameter;
		packet[0] = 0x02; // STX
		packet[1] = 0x16;// length type2_22byte fixed
		packet[2] = (byte)(id&0xFF);
		packet[3] = (byte)(pke>>8); // parameter ID high byte
		packet[4] = (byte)pke;  // parameter ID low byte
		packet[5] = (byte)(index>>8); // index word high byte
		packet[6] = (byte)index; // index word low byte
		packet[7] = 0x00; // PZD1(process data)_control word
		packet[8] = 0x00; // PZD2 main set value
		packet[9] = 0x00; // PZD3 addtional set value
		packet[10] = 0x00; // PZD4 addtional set value
		packet[11] = 0x00; // PZD5 addtional set value
		packet[12] = 0x00; // PZD6 addtional set value
		packet[13] = 0x00;
		packet[14] = 0x00;
		packet[15] = 0x00;
		packet[16] = 0x00;
		packet[17] = 0x00;
		packet[18] = 0x00;
		packet[19] = 0x00;
		packet[20] = 0x00;
		packet[21] = 0x00;
		packet[22] = 0x00;
		for(int cnt=0;cnt<23;cnt++){
			bcc ^=packet[cnt];	
		}
		packet[23] = (byte)(bcc&0xFF); // XOR all		
		return packet;
	}
	public boolean verifyResponse(byte[] src, int id, int parameter) {				
		int bcc = 0;
		int length = src[1];
		// verify header1, 2
		if(!(src[0]==0x02 && src[4]==(byte)(parameter&0xFF))) {
			mMsg = Inverter.ERROR_PACKET;
			return false;
		}
		if ((src[2]&0b0001111)!=id) {
			mMsg = Inverter.ERROR_ID;
			return false;
		}
		for(int cnt=0;cnt<(length+1);cnt++) { // calculate check sum
			bcc ^= src[cnt];	
		}

		if ((byte)bcc!=(byte)src[length+1]) {
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
	public Inverter.Fault getFaultCode(byte[] faultCode){
		int fault = (faultCode[7]&0xFF)<<24 | (faultCode[8]&0xFF)<<16 | (faultCode[9]&0xFF)<<8 | (faultCode[10]&0xFF);		
		if(fault==0x00000000) return Inverter.Fault.NORMAL;
		else if(fault==0x00090006) return Inverter.Fault.GRID_OV;
		else if(fault==0x00090007) return Inverter.Fault.GRID_UV;
		else if(fault==0x00090008) return Inverter.Fault.GRID_OF;
		else if(fault==0x00090009) return Inverter.Fault.GRID_UF;
		else return Inverter.Fault.ETC_FAULT;		
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
			float rxData = 0;
			int data = 0;
			initInverterData();
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 500, 0);
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
			if(verifyResponse(rxPacketbuff, id, 500)!=true) {				
				threadHandler.post(runReault);
				return;
			}			
			setInverterData(getFaultCode(rxPacketbuff));					
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1104, 0);
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
			if(verifyResponse(rxPacketbuff, id, 1104)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)rxData;
			setInverterData(data, Inverter.Data.PVV);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1105, 0);
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
			if(verifyResponse(rxPacketbuff, id, 1105)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)(rxData*10);
			setInverterData(data, Inverter.Data.PVI);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1107, 0);
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
			if(verifyResponse(rxPacketbuff, id, 1107)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)(rxData/100);
			setInverterData(data, Inverter.Data.PVP);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1121, 0);
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
			if(verifyResponse(rxPacketbuff, id, 1121)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)rxData;
			setInverterData(data, Inverter.Data.GRIDRV);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1121, 1);
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
			if(verifyResponse(rxPacketbuff, id, 1121)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)rxData;
			setInverterData(data, Inverter.Data.GRIDSV);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1121, 3);
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
			if(verifyResponse(rxPacketbuff, id, 1121)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)rxData;
			setInverterData(data, Inverter.Data.GRIDTV);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1141, 0);
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
			if(verifyResponse(rxPacketbuff, id, 1141)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)(rxData*10);
			setInverterData(data, Inverter.Data.GRIDRI);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1141, 1);
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
			if(verifyResponse(rxPacketbuff, id, 1141)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)(rxData*10);
			setInverterData(data, Inverter.Data.GRIDSI);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1141, 2);
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
			if(verifyResponse(rxPacketbuff, id, 1141)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)(rxData*10); // convert one decimal place(A)
			setInverterData(data, Inverter.Data.GRIDTI);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1106, 0);
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
			if(verifyResponse(rxPacketbuff, id, 1106)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			if(rxData>0) setInverterStatus(Inverter.Status.RUN);
			else setInverterStatus(Inverter.Status.STOP);
			data = (int)(rxData/100); // convert W to kW_one decimal place
			setInverterData(data, Inverter.Data.GRIDREALPOWER);
			//
			txPacketbuff = getRequestPacket(id, (byte)0b01100000, 1151, 0);
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
			if(verifyResponse(rxPacketbuff, id, 1151)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[7]&0xFF)<<24 | (rxPacketbuff[8]&0xFF)<<16 | (rxPacketbuff[9]&0xFF)<<8 | (rxPacketbuff[10]&0xFF));
			data = (int)(rxData/10); // convert 10^2W to kW
			setInverterData(data, Inverter.Data.TOTALPOWER);
			//
			threadHandler.post(new Runnable() {
				public void run() {
					result.setText(getInverterData());				
				}						
			});
		}
	}
}
