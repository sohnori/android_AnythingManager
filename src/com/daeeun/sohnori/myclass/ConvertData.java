package com.daeeun.sohnori.myclass;

import java.util.Date;

import android.icu.text.SimpleDateFormat;

public class ConvertData {
	private final static char[] HEX_DIGITS = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
	public enum TYPE {
			BYTE, SHORT, INT
	};
	
	public ConvertData() {
		
	}
	
	public static String bytesToHexAsciiString(byte[] src) {
		return bytesToHexAsciiString(src, 0 ,src.length);
	}
	public static String bytesToHexAsciiString(byte[] src, int offset, int length) {
		StringBuilder result = new StringBuilder();
		byte[] line = new byte[256];
		if(src==null || (length+offset)>src.length) return null;
        result.append("HEX: ");        
        for (int i = offset; i < offset+length; i++) {
            byte b = src[i];
            result.append(" ");
            result.append(HEX_DIGITS[(b >>> 4) & 0x0F]);
            result.append(HEX_DIGITS[b & 0x0F]);

            line[i] = b;
        }
        result.append("\r\n");
        result.append("ASCII: ");
        for (int i = 0; i < length; i++) {
            if (line[i] >= ' ' && line[i] <= '~') {
                result.append(new String(line, i, 1));
            } else {
                result.append(".");
            }
        }		
		return result.toString();
	}
	
	public static String bytesToHexString(byte[] src) {
		return bytesToHexString(src, 0 ,src.length);
	}
	public static String bytesToHexString(byte[] src, int offset, int length) {
		StringBuilder result = new StringBuilder();
		byte[] line = new byte[256];
		if(src==null || (length+offset)>src.length) return null;
        result.append("HEX: ");        
        for (int i = offset; i < offset+length; i++) {
            byte b = src[i];
            result.append(" ");
            result.append(HEX_DIGITS[(b >>> 4) & 0x0F]);
            result.append(HEX_DIGITS[b & 0x0F]);

            line[i] = b;
        }
        result.append("\r\n");
		return result.toString();
	}
	
	public static String bytesToAsciiString(byte[] src) {
		return bytesToAsciiString(src, 0 ,src.length);
	}
	public static String bytesToAsciiString(byte[] src, int offset, int length) {
		StringBuilder result = new StringBuilder();
		byte[] line = new byte[256];
		if(src==null || (length+offset)>src.length) return null;        
        result.append("ASCII: ");
        for (int i = 0; i < length; i++) {
            if (line[i] >= ' ' && line[i] <= '~') {
                result.append(new String(line, i, 1));
            } else {
                result.append(".");
            }
        }		
		return result.toString();
	}
	
	public static int hexStringToInt(String src) {
		return hexStringToInt(src, 0, src.length());
	}
	public static int hexStringToInt(String src, int offset, int length) {
		if(src==null || (length+offset)>src.length()) return -1;
		String str = src.substring(offset, offset+length);
		int result = Integer.parseInt(str, 16);
		return result;		
	}
	
	public static int bytesToInt(byte[] src, int offset, ConvertData.TYPE type) {
		int result = 0;
		byte[] srcCopy = new byte[4];			
		switch(type) {
		case BYTE:
			if(offset+1>src.length) return -1;
			if(src.length<1) return -1;
			System.arraycopy(src, offset, srcCopy, 0, 1);
			result = (srcCopy[0]&0xFF);
			break;
		case SHORT:
			if(offset+2>src.length) return -1;
			if(src.length<2) return -1;
			System.arraycopy(src, offset, srcCopy, 0, 2);
			result = (srcCopy[0]&0xFF)<<8 | (srcCopy[1]&0xFF);
			break;
		case INT:
			if(offset+4>src.length) return -1;
			if(src.length<4) return -1;
			System.arraycopy(src, offset, srcCopy, 0, 4);
			result = (srcCopy[0]&0xFF)<<24 | (srcCopy[1]&0xFF)<<16 | (srcCopy[2]&0xFF)<<8 | (srcCopy[3]&0xFF);
			break;			
		}		
		return result;
	}
	public static long bytesToLong(byte[] src, int offset) {		
		if(src==null) return -1;
		int value1 = 0;
		int value2 = 0;
		byte[] srcCopy = new byte[8];
		if(offset+8>src.length) return -1;
		if(src.length<8) return -1;
		System.arraycopy(src, offset, srcCopy, 0, 8);
		value1 = (srcCopy[0]&0xFF)<<24 | (srcCopy[1]&0xFF)<<16 | (srcCopy[2]&0xFF)<<8 | (srcCopy[3]&0xFF);
		value2 = (srcCopy[4]&0xFF)<<24 | (srcCopy[5]&0xFF)<<16 | (srcCopy[6]&0xFF)<<8 | (srcCopy[7]&0xFF);
		return (long)((value1&0xFFFFFFFF)<<32 | value2&0xFFFFFFFF);
	}
	public static String bytesToStringLog(byte[] src) {
		if(src.length==0 || src==null) return null;
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");				
		String str = simpleDate.format(new Date()) + "Transmit " + src.length + " bytes: \n"
		        + ConvertData.bytesToHexAsciiString(src) + "\r\n";
		return str;
	}
	public static char[] bytesAsciiToCharArray(byte[] src) {
		return bytesAsciiToCharArray(src, 0, src.length);
	}
	public static char[] bytesAsciiToCharArray(byte[] src, int length) {		
		return bytesAsciiToCharArray(src, 0, length);
	}
	public static char[] bytesAsciiToCharArray(byte[] src, int offset, int length) {
		if(src==null) return null;
		char[] retValue = new char[length];
		for(int cnt=0;cnt<length;cnt++) {
			retValue[cnt] = (char)src[cnt+offset]; 
		}
		return retValue;
	}
	public static byte[] byteArraysAdd(byte[] src1, byte[] src2) {
		if(src1==null || src2==null) return null;
		byte[] retValue = new byte[src1.length+src2.length];
		System.arraycopy(src1, 0, retValue, 0, src1.length);
		System.arraycopy(src2, 0, retValue, src1.length, src2.length);		
		return retValue;
	}
	public static byte[] bytesAdd(byte[] src1, byte src2) {
		if(src1==null) return null;
		byte[] retValue = new byte[src1.length+1];
		System.arraycopy(src1, 0, retValue, 0, src1.length);
		retValue[src1.length] = src2;
		return retValue;
	}
}
