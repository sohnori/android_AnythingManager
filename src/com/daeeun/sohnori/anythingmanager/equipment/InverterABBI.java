package com.daeeun.sohnori.anythingmanager.equipment;

import java.util.Date;

import com.daeeun.sohnori.anythingmanager.terminal.Terminal;
import com.hoho.android.usbserial.util.HexDump;

import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

public class InverterABBI extends Inverter {
	public enum Command {
		STATE, MESUREMENT
	}
	private Thread mThread;
	public InverterABBI(Inverter.Phase phase) {
		super(phase);
		super.setEquipInfo("ABB", Equipment.EquipInfo.MANUFACTURER);
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
	public byte[] getRequestPacket(int id, int cmdCode, int subCode) {
		// Aurora protocol
		byte[] packet = new byte[10];		
		if(id>99) {
			mMsg = Inverter.ERROR_ID;
			return null;
		}
		packet[0] = (byte)id; // id
		packet[1] = (byte)cmdCode; //
		packet[2] = (byte)subCode; //
		packet[3] = 0x00; // option
		packet[4] = 0x00; // option
		packet[5] = 0x00; // option
		packet[6] = 0x00; // option
		packet[7] = 0x00; // option		
		CyclicalRedundancyCheck crc16X25 = new CyclicalRedundancyCheck();
		byte[] crc = crc16X25.getCRC16Bytes(packet, 8, CyclicalRedundancyCheck.Crc16.CRC16_X_25);
		packet[8] = (byte)crc[0];
		packet[9] = (byte)crc[1];
		return packet;
	}	
	public boolean verifyResponse(byte[] src) {		
		CyclicalRedundancyCheck crc16X25 = new CyclicalRedundancyCheck();
		byte[] crc = crc16X25.getCRC16Bytes(src, src.length-2, CyclicalRedundancyCheck.Crc16.CRC16_X_25);

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
	public Inverter.Fault getFaultCode(byte[] faultCode){
		if(faultCode[3]==4 || faultCode[4]==4) return Inverter.Fault.PV_OC;
		else if(faultCode[3]==5 || faultCode[4]==5) return Inverter.Fault.PV_UV;
		else if(faultCode[3]==6 || faultCode[4]==6) return Inverter.Fault.PV_OV;
		else if(faultCode[3]==9 || faultCode[4]==9) return Inverter.Fault.PV_OV;
		else if(faultCode[3]==14 || faultCode[4]==14) return Inverter.Fault.EARTH_FAULT;
		else if(faultCode[3]==16 || faultCode[4]==16) return Inverter.Fault.INV_IGBT;
		else if(faultCode[2]==3) return Inverter.Fault.GRID_OV;
		else if(faultCode[2]==4) return Inverter.Fault.GRID_OC;
		else if(faultCode[2]==5) return Inverter.Fault.INV_IGBT;
		else if(faultCode[2]==6) return Inverter.Fault.GRID_UV;
		else if(faultCode[2]==9) return Inverter.Fault.GRID_UV;
		else if(faultCode[2]==10) return Inverter.Fault.GRID_OV;
		else if(faultCode[2]==14) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[2]==15) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[2]==16) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[2]==30) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[2]==31) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[2]==41) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[2]==46) return Inverter.Fault.ETC_FAULT;
		else if(faultCode[2]==47) return Inverter.Fault.ETC_FAULT;		
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
			float rxData = 0;
			int data = 0;
			initInverterData();
			txPacketbuff = getRequestPacket(id, 50, 0);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}			
			setInverterData(getFaultCode(rxPacketbuff));
			//
			txPacketbuff = getRequestPacket(id, 59, 5);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)rxData;
			setInverterData(data, Inverter.Data.PVV);
			//
			txPacketbuff = getRequestPacket(id, 59, 4);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)(rxData*10); // to fixed 1
			setInverterData(data, Inverter.Data.FREQUENCY);
			//
			txPacketbuff = getRequestPacket(id, 59, 3);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)(rxData/100); // W to kW fixed 1
			setInverterData(data, Inverter.Data.GRIDREALPOWER);
			if(data>0) setInverterStatus(Inverter.Status.RUN);
			else setInverterStatus(Inverter.Status.STOP);
			//
			txPacketbuff = getRequestPacket(id, 59, 25);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)(rxData*10); // to fixed 1
			setInverterData(data, Inverter.Data.PVI);
			//
			txPacketbuff = getRequestPacket(id, 59, 39);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)(rxData*10); // to fixed 1
			setInverterData(data, Inverter.Data.GRIDRI);
			//
			txPacketbuff = getRequestPacket(id, 59, 40);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)(rxData*10); // to fixed 1
			setInverterData(data, Inverter.Data.GRIDSI);
			//
			txPacketbuff = getRequestPacket(id, 59, 41);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)(rxData*10); // to fixed 1
			setInverterData(data, Inverter.Data.GRIDTI);
			//
			txPacketbuff = getRequestPacket(id, 59, 61);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)rxData; // 
			setInverterData(data, Inverter.Data.GRIDRV);
			//
			txPacketbuff = getRequestPacket(id, 59, 62);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)rxData; // 
			setInverterData(data, Inverter.Data.GRIDSV);
			//
			txPacketbuff = getRequestPacket(id, 59, 63);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)rxData; // 
			setInverterData(data, Inverter.Data.GRIDTV);
			//
			txPacketbuff = getRequestPacket(id, 68, 6);
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
			if(verifyResponse(rxPacketbuff)!=true) {				
				threadHandler.post(runReault);
				return;
			}
			rxData = Float.intBitsToFloat((rxPacketbuff[2]&0xFF)<<24 | (rxPacketbuff[3]&0xFF)<<16 | (rxPacketbuff[4]&0xFF)<<8 | (rxPacketbuff[5]&0xFF));
			data = (int)(rxData/1000); // Wh to kWh 
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
