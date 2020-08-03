package com.daeeun.sohnori.myclass;

public class IoTeepromHandler {
	private final static char[] HEX_DIGITS = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
	public final static byte[] HEADER = {'D','E'};
	public final static byte TAIL = '\r';
	public final static byte[] DISABLE = {'0','0'};
	public final static byte[] LORA_IPL = {'0','1'};
	public final static byte[] LORA_F1M = {'0','2'};
	public final static byte[] LORA_NODELINK = {'0','3'};
	public final static byte[] INMD_NORMAL = {'0','1'};
	public final static byte[] INMD_MASTER_LOCAL = {'1','1'};
	public final static byte[] INMD_MASTER_LOCAL_LORA = {'2','1'};
	public final static byte[] ZBMD_SLAVE_1CH = {'0','1'};
	public final static byte[] ZBMD_SLAVE_2CH = {'0','2'};
	public final static byte[] ZBMD_SLAVE_4CH = {'0','4'};
	public final static byte[] ZBMD_MASTER_LOCAL_1CH = {'1','1'};
	public final static byte[] ZBMD_MASTER_LOCAL_2CH = {'1','2'};
	public final static byte[] ZBMD_MASTER_LOCAL_4CH = {'1','4'};
	public final static byte[] ZBMD_MASTER_LOCAL_LORA_1CH = {'2','1'};
	public final static byte[] ZBMD_MASTER_LOCAL_LORA_2CH = {'2','2'};
	public final static byte[] ZBMD_MASTER_LOCAL_LORA_4CH = {'2','4'};
	public final static byte[] PVEM_SLAVE_INNER = {'0','1'};
	public final static byte[] PVEM_SLAVE_OUTER = {'0','2'};
	public final static byte[] PVEM_MASTER_LOCAL_INNER = {'1','1'};
	public final static byte[] PVEM_MASTER_LOCAL_OUTER = {'1','2'};
	public final static byte[] PVEM_MASTER_LOCAL_LORA_INNER = {'2','1'};
	public final static byte[] PVEM_MASTER_LOCAL_LORA_OUTER = {'2','2'};	
	public final static byte[] INV_DASS = {'D','A','S','S'};
	public final static byte[] INV_E_P3 = {'E','_','P','3'};
	public final static byte[] INV_E_P5 = {'E','_','P','5'};
	public final static byte[] INV_HANS = {'H','A','N','S'};
	public final static byte[] INV_HEXP = {'H','E','X','P'};
	public final static byte[] INV_EKOS = {'E','K','O','S'};
	public final static byte[] INV_WILL = {'W','I','L','L'};	
	public final static byte[] INV_ABBI = {'A','B','B','I'};	
	public final static byte[] INV_REFU = {'R','E','F','U'};
	public final static byte[] INV_SUNG = {'S','U','N','G'};
	public final static byte[] INV_REMS = {'R','E','M','S'};	
	public final static byte[] INV_ECOS = {'E','C','O','S'};
	public final static byte[] INV_SMAI = {'S','M','A','I'};
	public final static byte[] INV_VELT = {'V','E','L','T'};
	public final static byte[] INV_G2PW = {'G','2','P','W'};
	public final static byte[] EQP_PV1P = {'P','V','1','P'};
	public final static byte[] EQP_PV3P = {'P','V','3','P'};
	public final static byte[] EQP_PVHF = {'P','V','H','F'};
	public final static byte[] EQP_PVHN = {'P','V','H','N'};
	public final static byte[] EQP_GEOT = {'G','E','O','T'};
	public final static byte[] EQP_WIND = {'W','I','N','D'};
	public final static byte[] EQP_FUEL = {'F','U','E','L'};
	public final static byte[] EQP_ESSS = {'E','S','S','S'};	
	public enum Commandrems {
		ENTER, EXIT, EQUIP_ENABLE_H, EQUIP_ENABLE_L, EQUIP_TYPE, LORAMODEM, LORAMODEM_SUB, FVER, TXPK,
		RCNT, LRST, LSRS
	}
	public enum Command {
		ENTER, EXIT, INVERTER_ENABLE_H, INVERTER_ENABLE_L, INVERTERTYPE, LORAMODEM, INFO, TXPK,
		INVERTER_MODE, ZB_MODE, ENVIRONMENT_MODE, C1ES, C1CD, GETTIME, CAENNEL0READ, CAENNEL1READ,
		CAENNEL0SET, CAENNEL1SET, RCNT, LRST
	}
	private String mMsg;
	public IoTeepromHandler() {
		this.mMsg = "";
	}	
	public String getMessage() {
		return this.mMsg;
	}
	public boolean verifyResponse(byte[] response) {		
		if(response==null || response.length<3) return false;
		if(response[0]=='O' && response[1]=='K' && response[2]=='\r') return true;
		else return false;
	}
	public byte[] getCommand(IoTeepromHandler.Command command, int typeOption, byte[] writeData) {
		int cnt;
		byte[] result =null;
		if(writeData==null) result = new byte[7];
		else result = new byte[7+writeData.length];	
		switch(command) {
		case ENTER:
			result = new byte[3];
			result[0] = '#';
			result[1] = '#';
			result[2] = '#';
			return result;
		case INVERTER_ENABLE_H:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'I';
			result[3] = 'N';
			result[4] = 'E';
			result[5] = 'H';			
			break;
		case INVERTER_ENABLE_L:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'I';
			result[3] = 'N';
			result[4] = 'E';
			result[5] = 'L';			
			break;
		case INVERTERTYPE:
			if(typeOption>15 || typeOption<0) {
				this.mMsg = "장비 타입 번호가 올바르지 않습니다.";
				return null;
			}
			if(writeData!=null && writeData.length>16) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}			
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'I';
			result[3] = 'N';
			result[4] = 'V';
			result[5] = (byte)HEX_DIGITS[typeOption];			
			break;
		case C1ES: // 발전소 정보(모듈센서 갯수, 중계기 갯수)
			if(typeOption>15 || typeOption<0) {
				this.mMsg = "장비 타입 번호가 올바르지 않습니다.";
				return null;
			}
			if(writeData!=null && writeData.length>32) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}			
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'C';
			result[3] = '1';
			result[4] = 'E';
			result[5] = 'S';		
			break;
		case C1CD: // 코디네이터 정보
			if(typeOption>15 || typeOption<0) {
				this.mMsg = "장비 타입 번호가 올바르지 않습니다.";
				return null;
			}
			if(writeData!=null && writeData.length>32) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}			
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'C';
			result[3] = '1';
			result[4] = 'C';
			result[5] = 'D';		
			break;
		case LORAMODEM:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'L';
			result[3] = 'O';
			result[4] = 'M';
			result[5] = 'M';			
			break;
		case INVERTER_MODE:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'I';
			result[3] = 'N';
			result[4] = 'M';
			result[5] = 'D';			
			break;
		case ZB_MODE:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'Z';
			result[3] = 'B';
			result[4] = 'M';
			result[5] = 'D';			
			break;
		case ENVIRONMENT_MODE:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'P';
			result[3] = 'V';
			result[4] = 'E';
			result[5] = 'M';			
			break;
		case GETTIME:
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'T';
			result[3] = 'I';
			result[4] = 'M';
			result[5] = 'E';
			result[6] = '\r';
			return result;
		case CAENNEL0READ: // 수평일사량
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'C';
			result[3] = 'H';
			result[4] = '0';
			result[5] = 'R';
			result[6] = '\r';
			return result;
		case CAENNEL1READ: // 경사일사량
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'C';
			result[3] = 'H';
			result[4] = '1';
			result[5] = 'R';
			result[6] = '\r';
			return result;
		case CAENNEL0SET: // 수평일사량
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'C';
			result[3] = 'H';
			result[4] = '0';
			result[5] = 'S';
			result[6] = '\r';
			return result;
		case CAENNEL1SET: // 경사일사량
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'C';
			result[3] = 'H';
			result[4] = '1';
			result[5] = 'S';
			result[6] = '\r';
			return result;			
		case TXPK: // transmit packet
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'T';
			result[3] = 'X';
			result[4] = 'P';
			result[5] = 'K';
			result[6] = '\r';
			return result;
		case INFO: // 펌웨어 버전 읽기
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'I';
			result[3] = 'N';
			result[4] = 'F';
			result[5] = 'O';
			result[6] = '\r';
			return result;
		case RCNT: // 리셋 카운터 얻기
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'R';
			result[3] = 'C';
			result[4] = 'N';
			result[5] = 'T';
			result[6] = '\r';
			return result;
		case LRST: // 로라모뎀 리셋 강제
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'L';
			result[3] = 'R';
			result[4] = 'S';
			result[5] = 'T';
			result[6] = '\r';
			return result;		
		case EXIT:
			result = new byte[3];
			result[0] = '*';
			result[1] = '*';
			result[2] = '*';
			return result;
		default:
			this.mMsg = "유효하지 않은 커멘드 요청";
			return null;
		}
		if(writeData!=null) {
			for(cnt=0;cnt<writeData.length;cnt++) {
				result[6+cnt] = writeData[cnt];
			}
			result[6+cnt] = '\r';
		}
		else result[6] = '\r';	
		return result;
	}
	public byte[] getCommand(IoTeepromHandler.Commandrems command, int typeOption, byte[] writeData) {
		int cnt;
		byte[] result = null;
		if(writeData==null) result = new byte[7];
		else result = new byte[7+writeData.length];		
		switch(command) {
		case ENTER:
			result = new byte[3];
			result[0] = '#';
			result[1] = '#';
			result[2] = '#';
			return result;			
		case EQUIP_ENABLE_H:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'E';
			result[3] = 'Q';
			result[4] = 'E';
			result[5] = 'H';			
			break;
		case EQUIP_ENABLE_L:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}			
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'E';
			result[3] = 'Q';
			result[4] = 'E';
			result[5] = 'L';			
			break;
		case EQUIP_TYPE:
			if(typeOption>15 || typeOption<0) {
				this.mMsg = "장비 타입 번호가 올바르지 않습니다.";
				return null;
			}
			if(writeData!=null && writeData.length>16) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}			
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'E';
			result[3] = 'Q';
			result[4] = 'P';
			result[5] = (byte)HEX_DIGITS[typeOption];			
			break;
		case LORAMODEM:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}			
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'L';
			result[3] = 'O';
			result[4] = 'M';
			result[5] = 'M';			
			break;
		case LORAMODEM_SUB:
			if(writeData!=null && writeData.length>2) {
				this.mMsg = "쓰려고 하는 데이터 길이가 형식에 맞지 않습니다.";
				return null;
			}			
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'L';
			result[3] = 'S';
			result[4] = 'U';
			result[5] = 'B';			
			break;
		case FVER: // 펌웨어 버전 읽기			
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'F';
			result[3] = 'V';
			result[4] = 'E';
			result[5] = 'R';
			result[6] = '\r';
			return result;
		case TXPK: // transmit packet
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'T';
			result[3] = 'X';
			result[4] = 'P';
			result[5] = 'K';
			result[6] = '\r';
			return result;
		case RCNT: // 리셋 카운터 얻기
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'R';
			result[3] = 'C';
			result[4] = 'N';
			result[5] = 'T';
			result[6] = '\r';
			return result;
		case LRST: // 로라모뎀 리셋 강제
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'L';
			result[3] = 'R';
			result[4] = 'S';
			result[5] = 'T';
			result[6] = '\r';
			return result;
		case LSRS: // 로라서브모뎀 리셋 강제
			result = new byte[7];
			result[0] = 'D';
			result[1] = 'E';
			result[2] = 'L';
			result[3] = 'S';
			result[4] = 'R';
			result[5] = 'S';
			result[6] = '\r';
			return result;
		case EXIT:
			result = new byte[3];
			result[0] = '*';
			result[1] = '*';
			result[2] = '*';
			return result;
		default:
			this.mMsg = "유효하지 않은 커멘드 요청";
			return null;
		}
		if(writeData!=null) {
			for(cnt=0;cnt<writeData.length;cnt++) {
				result[6+cnt] = writeData[cnt];
			}
			result[6+cnt] = '\r';
		}
		else result[6] = '\r';		
		return result;
	}	
}
