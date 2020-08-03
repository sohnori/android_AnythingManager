package com.daeeun.sohnori.anythingmanager.equipment;

public class CyclicalRedundancyCheck {
	public enum Crc8 {
		CRC8, CRC8_CDMA2000, CRC8_DARC, CRC8_DVB_S2, CRC8_EBU, CRC8_I_CODE, CRC8_ITU, CRC8_MAXIM,
		CRC8_ROHC, CRC8_WCDMA, CRC8_AUTOSAR, CRC8_BLUETOOTH, CRC8_LTE
	}
	public enum Crc16 {
		CRC16_CCITT_FALSE, CRC16_ARC, CRC16_AUR_CCITT, CRC16_BUYPASS, CRC16_CDMA2000, CRC16_DDS_110,
		CRC16_DECT_R, CRC16_DECT_X, CRC16_DNP, CRC16_EN_13757, CRC16_GENIBUS, CRC16_MAXIM, CRC16_MCRF4XX,
		CRC16_RIELLO, CRC16_T10_DIF, CRC16_TELEDISK, CRC16_TMS37157, CRC16_USB, CRC_A, CRC16_KERMIT,
		CRC16_MODBUS, CRC16_X_25, CRC16_XMODEM
	}
	public enum Crc32 {
		CRC32, CRC32_BZIP2, CRC32_C, CRC32_D, CRC32_MPEG2, CRC32_POSIX, CRC32_Q, CRC32_JAMCRC, CRC32_XFER
	}
	
	private String mMsg;
	
	public CyclicalRedundancyCheck() {
		this.mMsg = "None";
	}
	
	public String getMessage() {
		return this.mMsg;
	}
	
	public byte getCRC8(byte[] src, int srcLength, Crc8 category) {
		byte polynomial;
		byte crc8;
		byte srcCnt, bitCnt; 
		switch(category) {
		case CRC8: // reflected_in_out_false
			polynomial = 0x07;
			crc8 = 0x00;
			break;
		case CRC8_CDMA2000: // reflected_in_out_false
			polynomial = (byte)0x9B;
			crc8 = (byte)0xFF;
			break;
		case CRC8_DARC: // reflected_in_out_true
			polynomial = (byte)0x9C;			
			crc8 = 0x00;
			break;
		case CRC8_DVB_S2: // reflected_in_out_false
			polynomial = (byte)0xD5;
			crc8 = 0x00;
			break;
		case CRC8_EBU: // reflected_in_out_true
			polynomial = (byte)0xB8;
			crc8 = (byte)0xFF;
			break;
		case CRC8_I_CODE: // reflected_in_out_false
			polynomial = 0x1D;
			crc8 = (byte)0xFD;
			break;
		case CRC8_ITU: // reflected_in_out_false
			polynomial = (byte) 0x07;
			crc8 = 0x00;
			break;
		case CRC8_MAXIM: // reflected_in_out_true
			polynomial = (byte)0x8C;
			crc8 = 0x00;
			break;
		case CRC8_ROHC: // reflected_in_out_true
			polynomial = (byte)0xE0;
			crc8 = (byte)0xFF;
			break;
		case CRC8_WCDMA: // reflected_in_out_true
			polynomial = (byte)0xD9;
			crc8 = 0x00;
			break;
		case CRC8_AUTOSAR: // reflected_in_out_false
			polynomial = (byte) 0x2F;
			crc8 = (byte)0xFF;
			break;
		case CRC8_BLUETOOTH: // reflected_in_out_true
			polynomial = (byte)0xE5;
			crc8 = 0x00;
			break;
		case CRC8_LTE: // reflected_in_out_false
			polynomial = (byte)0x9B;
			crc8 = 0x00;
			break;
		default:
			this.mMsg = "Invalid CRC8 request.";
			return 0;
		}
		if(category==Crc8.CRC8 || category==Crc8.CRC8_CDMA2000 ||  category==Crc8.CRC8_DVB_S2 || category==Crc8.CRC8_I_CODE
				|| category==Crc8.CRC8_ITU || category==Crc8.CRC8_AUTOSAR || category==Crc8.CRC8_LTE) 
		{ // reflected_in_out_false
			for(srcCnt=0;srcCnt<srcLength;srcCnt++)
			{
				crc8 ^= src[srcCnt];
				
				for(bitCnt=0;bitCnt<8;bitCnt++)
				{
					if((crc8 & 0b10000000)==0x80)
					{
						crc8 <<=1;
						crc8 ^= polynomial;
					}
					else
					{
						crc8 <<= 1;	
					}
				}
			}
		}
		if(category==Crc8.CRC8_DARC || category==Crc8.CRC8_EBU ||  category==Crc8.CRC8_MAXIM ||  
				category==Crc8.CRC8_ROHC || category==Crc8.CRC8_WCDMA || category==Crc8.CRC8_BLUETOOTH ) 	
		{ // reflected_in_out_true
			for(srcCnt=0;srcCnt<srcLength;srcCnt++)
			{
				crc8 ^= src[srcCnt];
				
				for(bitCnt=0;bitCnt<8;bitCnt++)
				{ // 자바는 비트 쉬프트 연산시 integer 타입으로 자동전환된다.
					if((crc8 & (byte)1)==0x01)
					{
						crc8 >>>=1;
						if(crc8<0) crc8^=0b10000000;
						crc8 ^= polynomial;
					}
					else
					{
						crc8 >>>=1;
						if(crc8<0) crc8^=0b10000000;
					}
				}
			}
		}
		// XOR out
		if(category==Crc8.CRC8_ITU)		crc8 ^= 0x55;
		if(category==Crc8.CRC8_AUTOSAR)	crc8 ^= 0xFF;
		return crc8;
	}
	
	public short getCRC16(byte[] src, int srcLength, Crc16 category) {
		short polynomial;
		short crc16 = 0;		
		int srcCnt;
		int bitCnt;		
		
		// define polynomial
		if(category==Crc16.CRC16_CCITT_FALSE) 	polynomial = 0x1021; // reflected_in_out_false
		else if(category==Crc16.CRC16_ARC)	polynomial = (short)0xA001; // reflected_in_out_true
		else if(category==Crc16.CRC16_AUR_CCITT)polynomial = 0x1021; // reflected_in_out_false
		else if(category==Crc16.CRC16_BUYPASS)	polynomial = (short)0x8005; // reflected_in_out_false
		else if(category==Crc16.CRC16_CDMA2000)	polynomial = (short)0xC867; // reflected_in_out_false
		else if(category==Crc16.CRC16_DDS_110)	polynomial = (short)0x8005; // reflected_in_out_false
		else if(category==Crc16.CRC16_DECT_R)	polynomial = 0x0589; // reflected_in_out_false
		else if(category==Crc16.CRC16_DECT_X)	polynomial = 0x0589; // reflected_in_out_false
		else if(category==Crc16.CRC16_DNP)	polynomial = (short)0xA6BC; // reflected_in_out_true
		else if(category==Crc16.CRC16_EN_13757)	polynomial = 0x3D65; // reflected_in_out_false
		else if(category==Crc16.CRC16_GENIBUS)	polynomial = 0x1021; // reflected_in_out_false
		else if(category==Crc16.CRC16_MAXIM)	polynomial = (short)0xA001; // reflected_in_out_true
		else if(category==Crc16.CRC16_MCRF4XX)	polynomial = (short)0x8408; // reflected_in_out_true
		else if(category==Crc16.CRC16_RIELLO)	polynomial = (short)0x8408; // reflected_in_out_true
		else if(category==Crc16.CRC16_T10_DIF)	polynomial = (short)0x8BB7; // reflected_in_out_false
		else if(category==Crc16.CRC16_TELEDISK)	polynomial = (short)0xA097; // reflected_in_out_false
		else if(category==Crc16.CRC16_TMS37157)	polynomial = (short)0x8408; // reflected_in_out_true
		else if(category==Crc16.CRC16_USB)	polynomial = (short)0xA001; // reflected_in_out_true
		else if(category==Crc16.CRC_A)		polynomial = (short)0x8408; // reflected_in_out_true
		else if(category==Crc16.CRC16_KERMIT)	polynomial = (short)0x8408; // reflected_in_out_true
		else if(category==Crc16.CRC16_MODBUS)	polynomial = (short)0xA001; // reflected_in_out_true
		else if(category==Crc16.CRC16_X_25)	polynomial = (short)0x8408; // reflected_in_out_true
		else if(category==Crc16.CRC16_XMODEM)	polynomial = 0x1021; // reflected_in_out_false
		else return 0;
		
		// Initial value set
		if(category==Crc16.CRC16_CCITT_FALSE) 	crc16 = (short)0xFFFF; 
		else if(category==Crc16.CRC16_ARC)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_AUR_CCITT)crc16 = 0x1D0F;
		else if(category==Crc16.CRC16_BUYPASS)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_CDMA2000)	crc16 = (short)0xFFFF;
		else if(category==Crc16.CRC16_DDS_110)	crc16 = (short)0x800D;
		else if(category==Crc16.CRC16_DECT_R)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_DECT_X)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_DNP)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_EN_13757)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_GENIBUS)	crc16 = (short)0xFFFF;
		else if(category==Crc16.CRC16_MAXIM)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_MCRF4XX)	crc16 = (short)0xFFFF;
		else if(category==Crc16.CRC16_RIELLO)	crc16 = 0x554D;
		else if(category==Crc16.CRC16_T10_DIF)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_TELEDISK)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_TMS37157)	crc16 = 0x3791;
		else if(category==Crc16.CRC16_USB)	crc16 = (short)0xFFFF;
		else if(category==Crc16.CRC_A)		crc16 = 0x6363;
		else if(category==Crc16.CRC16_KERMIT)	crc16 = 0x0000;
		else if(category==Crc16.CRC16_MODBUS)	crc16 = (short)0xFFFF;
		else if(category==Crc16.CRC16_X_25)	crc16 = (short)0xFFFF;
		else if(category==Crc16.CRC16_XMODEM)	crc16 = 0x0000;
		
		if(category==Crc16.CRC16_CCITT_FALSE || category==Crc16.CRC16_AUR_CCITT ||  category==Crc16.CRC16_BUYPASS || category==Crc16.CRC16_CDMA2000
		|| category==Crc16.CRC16_DDS_110 || category==Crc16.CRC16_DECT_R || category==Crc16.CRC16_DECT_X || category==Crc16.CRC16_EN_13757
		|| category==Crc16.CRC16_GENIBUS || category==Crc16.CRC16_T10_DIF || category==Crc16.CRC16_TELEDISK || category==Crc16.CRC16_XMODEM)
		{ // reflected_in_out_false
			for(srcCnt=0;srcCnt<srcLength;srcCnt++)
			{
				crc16 ^= src[srcCnt]<<8;
				
				for(bitCnt=0;bitCnt<8;bitCnt++)
				{
					if((crc16 & 0x8000)==0x8000)
					{
						crc16 <<=1;
						crc16 ^= polynomial;
					}
					else
					{
						crc16 <<= 1;	
					}
				}
			}
		}
		
		if(category==Crc16.CRC16_ARC || category==Crc16.CRC16_DNP ||  category==Crc16.CRC16_MAXIM
		|| category==Crc16.CRC16_MCRF4XX || category==Crc16.CRC16_RIELLO || category==Crc16.CRC16_TMS37157
		|| category==Crc16.CRC16_USB || category==Crc16.CRC_A || category==Crc16.CRC16_KERMIT || category==Crc16.CRC16_MODBUS
		|| category==Crc16.CRC16_X_25)
		{ // reflected_in_out_true
			for(srcCnt=0;srcCnt<srcLength;srcCnt++)
			{
				crc16 ^= (short)(src[srcCnt]&0x00FF);			
				
				for(bitCnt=0;bitCnt<8;bitCnt++)
				{
					if((crc16 & 0x0001)==0x0001)
					{
						crc16 >>>=1;
						if(crc16<0) crc16^=0x8000;
						crc16 ^= polynomial;					
					}
					else
					{
						crc16 >>>= 1;
						if(crc16<0) crc16^=0x8000;
					}
				}
			}
		}
		
		// XOR out
		if(category==Crc16.CRC16_DNP || category==Crc16.CRC16_EN_13757 || category==Crc16.CRC16_GENIBUS || category==Crc16.CRC16_MAXIM
		|| category==Crc16.CRC16_USB || category==Crc16.CRC16_X_25) crc16 ^= 0xFFFF;	
		if(category==Crc16.CRC16_DECT_R) crc16 ^= 0x0001;
		
		return crc16;
	}
	
	public byte[] getCRC16Bytes(byte[] src, int srcLength, Crc16 category) {
		byte[] crc16 = new byte[2];
		short crc16Short = getCRC16(src, srcLength, category);
		crc16[0] = (byte)(crc16Short&0x00FF);
		crc16[1] = (byte)((crc16Short>>>8)&0x00FF);		
		return crc16;
	}
	
	public int getCRC32(byte[] src, int srcLength, Crc32 category)
	{
		int polynomial;
		int crc32 = 0;
		int srcCnt;
		int bitCnt;
		
		// define polynomial
		if(category==Crc32.CRC32) 		polynomial = 0xEDB88320; // reflected_in_out_true
		else if(category==Crc32.CRC32_BZIP2)	polynomial = 0x04C11DB7; // reflected_in_out_false
		else if(category==Crc32.CRC32_C)	polynomial = 0x82F63B78; // reflected_in_out_true
		else if(category==Crc32.CRC32_D)	polynomial = 0xD419CC15; // reflected_in_out_true
		else if(category==Crc32.CRC32_MPEG2)	polynomial = 0x04C11DB7; // reflected_in_out_false
		else if(category==Crc32.CRC32_POSIX)	polynomial = 0x04C11DB7; // reflected_in_out_false
		else if(category==Crc32.CRC32_Q)	polynomial = 0x814141AB; // reflected_in_out_false
		else if(category==Crc32.CRC32_JAMCRC)	polynomial = 0xEDB88320; // reflected_in_out_true
		else if(category==Crc32.CRC32_XFER)	polynomial = 0x000000AF; // reflected_in_out_false	
		else return 0x00;
		
		// Initial value set
		if(category==Crc32.CRC32) 		crc32 = 0xFFFFFFFF; 
		else if(category==Crc32.CRC32_BZIP2)	crc32 = 0xFFFFFFFF;
		else if(category==Crc32.CRC32_C)	crc32 = 0xFFFFFFFF;
		else if(category==Crc32.CRC32_D)	crc32 = 0xFFFFFFFF;
		else if(category==Crc32.CRC32_MPEG2)	crc32 = 0xFFFFFFFF;
		else if(category==Crc32.CRC32_POSIX)	crc32 = 0x00000000;
		else if(category==Crc32.CRC32_Q)	crc32 = 0x00000000;
		else if(category==Crc32.CRC32_JAMCRC)	crc32 = 0xFFFFFFFF;
		else if(category==Crc32.CRC32_XFER)	crc32 = 0x00000000;	
		
		if(category==Crc32.CRC32_BZIP2 || category==Crc32.CRC32_MPEG2 ||  category==Crc32.CRC32_POSIX || 
		category==Crc32.CRC32_Q || category==Crc32.CRC32_XFER)
		{ // reflected_in_out_false
			for(srcCnt=0;srcCnt<srcLength;srcCnt++)
			{
				crc32 ^= (int)(src[srcCnt]<<24);
				
				for(bitCnt=0;bitCnt<8;bitCnt++)
				{
					if((crc32 & 0x80000000)==0x80000000)
					{
						crc32 <<=1;						
						crc32 ^= polynomial;
					}
					else
					{
						crc32 <<=1;	
					}
				}
			}
		}
		
		if(category==Crc32.CRC32 || category==Crc32.CRC32_C ||  category==Crc32.CRC32_D || category==Crc32.CRC32_JAMCRC)
		{ // reflected_in_out_true
			for(srcCnt=0;srcCnt<srcLength;srcCnt++)
			{
				crc32 ^= (int)(src[srcCnt]&0x000000FF);			
				
				for(bitCnt=0;bitCnt<8;bitCnt++)
				{
					if((crc32 & 0x00000001)==0x00000001)
					{
						crc32 >>>=1;
						if(crc32<0) crc32^=0x80000000;
						crc32 ^= polynomial;					
					}
					else
					{
						crc32 >>>=1;
						if(crc32<0) crc32^=0x80000000;
					}
				}
			}
		}
		
		// XOR out
		if(category==Crc32.CRC32 || category==Crc32.CRC32_BZIP2 || category==Crc32.CRC32_C || category==Crc32.CRC32_D
		|| category==Crc32.CRC32_POSIX) crc32 ^= 0xFFFFFFFF;	
			
		return crc32;
	}
	
	public byte[] getCRC32Bytes(byte[] src, int srcLength, Crc32 category) {
		byte[] crc32 = new byte[4];
		int value = getCRC32(src, srcLength, category);
		crc32[0] = (byte)(value&0xFF);
		crc32[1] = (byte)((value>>8)&0xFF);
		crc32[2] = (byte)((value>>16)&0xFF);
		crc32[3] = (byte)((value>>24)&0xFF);
		return crc32;
	}
	
}
