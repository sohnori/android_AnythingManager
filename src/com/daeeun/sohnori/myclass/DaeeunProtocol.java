package com.daeeun.sohnori.myclass;

public class DaeeunProtocol {
	public final static String faultNormal = "정상";
	public final static String faultEtc = "기타에러";
	public final static String faultEarth = "지락(누전)";
	public final static String faultStandAlone = "지락(누전)";
	public final static String faultUF = "계통 저주파수";
	public final static String faultOF = "계통 과주파수";
	public final static String faultOC = "계통 과전류";
	public final static String faultUV = "계통 저전압";
	public final static String faultOV = "계통 과전압";
	public final static String faultOH = "과열";
	public final static String faultIGBT = "IGBT에러";
	public final static String faultPVOC = "PV과전류";
	public final static String faultPVUV = "PV저전압";
	public final static String faultPVOV = "PV과전압";
	public DaeeunProtocol(){
		
	}
	public static String getResult(String packet) {
		StringBuilder retStr = new StringBuilder();
		String cmd;
		String status;
		String str;
//		int length;
		int data;
		int phase=0;
		//char[] buffChar = new char[4];
		str = packet.substring(0, 2);		
		if(str.equals("de")!=true) return null;
		if(packet.length()<20) return null;
		retStr.append("패킷: Daeeun 프로토콜\r\n");
		retStr.append("명령구분: ");
//		str = packet.substring(2, 6);
//		length = Integer.parseInt(str, 16);
//		length *= 2; // 문자열 길이로 변환
		cmd = packet.substring(8, 10);		
		if(cmd.equals("31")==true) retStr.append("모듈센서 1채널\r\n");
		else if(cmd.equals("32")==true) retStr.append("모듈센서 2채널\r\n");
		else if(cmd.equals("34")==true) retStr.append("모듈센서 4채널\r\n");
		else if(cmd.equals("45")==true) retStr.append("환경센서\r\n");
		else if(cmd.equals("49")==true) retStr.append("인버터\r\n");
		else retStr.append("알수없음\r\n");
		retStr.append("상태: ");
		status = packet.substring(10, 12);		
		if(status.equals("00")==true) retStr.append("정상\r\n");
		else if(status.equals("80")==true) retStr.append("응답없음\r\n");
		else if(status.equals("81")==true) retStr.append("모드설정에러\r\n");
		else if(status.equals("82")==true) retStr.append("ZB갯수 설정에러\r\n");		
		else retStr.append("알수없음\r\n");
		if(cmd.equals("31")==true || cmd.equals("32")==true || cmd.equals("34")==true) {
			retStr.append("중계기 페이지번호: ");
			str = packet.substring(12, 14);
			data = Integer.parseInt(str, 16);
			retStr.append(data+"\r\n");
			retStr.append("모듈센서 번호: ");
			str = packet.substring(14, 18);
			data = Integer.parseInt(str, 16);
			retStr.append(data+"\r\n");
		}
		else if(cmd.equals("49")==true) {
			retStr.append("인버터 EEPROM번호: ");
			str = packet.substring(12, 14);
			retStr.append(str+"\r\n");
			retStr.append("Phase: ");
			str = packet.substring(14, 18);
			phase = Integer.parseInt(str, 16);
			retStr.append(phase+"\r\n");
		}
		if(status.equals("00")==true) {
			if(cmd.equals("31")==true || cmd.equals("32")==true || cmd.equals("34")==true) {
				retStr.append("모듈센서값: ");
				str = packet.substring(18, packet.length()-2);
				retStr.append(str+"\r\n");
			}
			if(cmd.equals("45")==true) {
				retStr.append("수평일사량: ");
				str = packet.substring(18, 22);
				data = Integer.parseInt(str, 16);
				retStr.append(data+"\r\n");
				retStr.append("경사일사량: ");
				str = packet.substring(22, 26);
				data = Integer.parseInt(str, 16);
				retStr.append(data+"\r\n");
				retStr.append("외기온도: ");
				str = packet.substring(26, 30);
				data = Integer.parseInt(str, 16);
				retStr.append(data+"\r\n");
				retStr.append("모듈온도: ");
				str = packet.substring(30, 34);
				data = Integer.parseInt(str, 16);
				retStr.append(data+"\r\n");
			}
			if(cmd.equals("49")==true) {
				retStr.append("인버터타입: ");
				str = packet.substring(18, 20);
				data = Integer.parseInt(str, 16);
				if(data==1) retStr.append("다쓰테크"+"\r\n");
				else if(data==2) retStr.append("동양E&P 3kW"+"\r\n");
				else if(data==3) retStr.append("동양E&P 5kW"+"\r\n");
				else if(data==4) retStr.append("한솔"+"\r\n");
				else if(data==5) retStr.append("헥스파워"+"\r\n");				
				else if(data==6) retStr.append("에코스"+"\r\n");
				else if(data==7) retStr.append("윌링스"+"\r\n");
				else if(data==8) retStr.append("ABB"+"\r\n");
				else if(data==9) retStr.append("REFU sol"+"\r\n");
				else if(data==10) retStr.append("REMS"+"\r\n");
				else if(data==11) retStr.append("썬그로우"+"\r\n");
				else if(data==12) retStr.append("동이에코스"+"\r\n");
				else if(data==13) retStr.append("SMA"+"\r\n");
				else if(data==14) retStr.append("벨츠(헵시바)"+"\r\n");
				else retStr.append(data+"\r\n");
				retStr.append("인버터ID: ");
				str = packet.substring(20, 22);
				data = Integer.parseInt(str, 16);
				retStr.append(data+"\r\n");
				retStr.append("PV전압: ");
				str = packet.substring(22, 26);
				data = Integer.parseInt(str, 16);
				retStr.append(data+" V\r\n");
				retStr.append("PV전류: ");
				str = packet.substring(26, 30);
				data = Integer.parseInt(str, 16);
				retStr.append(data/10+"."+data%10+" A\r\n");
				retStr.append("PV전력: ");
				str = packet.substring(30, 34);
				data = Integer.parseInt(str, 16);
				retStr.append(data/10+"."+data%10+" kW\r\n");
				if(phase==1) {
					retStr.append("Grid전압: ");
					str = packet.substring(34, 38);
					data = Integer.parseInt(str, 16);
					retStr.append(data+" V\r\n");
					retStr.append("Grid전류: ");
					str = packet.substring(38, 42);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+" A\r\n");
					retStr.append("Grid전력: ");
					str = packet.substring(42, 46);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+" kW\r\n");
					retStr.append("역률: ");
					str = packet.substring(46, 50);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+"\r\n");
					retStr.append("주파수: ");
					str = packet.substring(50, 54);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+" Hz\r\n");
					retStr.append("누적발전량: ");
					str = packet.substring(54, 62);
					data = Integer.parseInt(str, 16);
					retStr.append(data+" kWh\r\n");
					retStr.append("상태코드: ");
					str = packet.substring(62, 66);
					data = Integer.parseInt(str, 16);
					if(data==0) retStr.append(faultNormal+"\r\n");					
					else if(data==2) retStr.append(faultPVOV+"\r\n");
					else if(data==4) retStr.append(faultPVUV+"\r\n");
					else if(data==8) retStr.append(faultPVOC+"\r\n");
					else if(data==16) retStr.append(faultIGBT+"\r\n");
					else if(data==32) retStr.append(faultOH+"\r\n");
					else if(data==64) retStr.append(faultOV+"\r\n");
					else if(data==128) retStr.append(faultUV+"\r\n");
					else if(data==256) retStr.append(faultOC+"\r\n");
					else if(data==512) retStr.append(faultOF+"\r\n");
					else if(data==1024) retStr.append(faultUF+"\r\n");
					else if(data==2048) retStr.append(faultStandAlone+"\r\n");
					else if(data==4096) retStr.append(faultEarth+"\r\n");
					else if(data==32768) retStr.append(faultEtc+"\r\n");
					else retStr.append("\r\n");
					retStr.append("동작유무: ");
					str = packet.substring(66, 68);
					data = Integer.parseInt(str, 16);
					if(data==1) retStr.append("RUN\r\n");
					else if(data==0) retStr.append("STOP\r\n");
				}
				if(phase==3) {
					retStr.append("Grid R전압: ");
					str = packet.substring(34, 38);
					data = Integer.parseInt(str, 16);
					retStr.append(data+" V\r\n");
					retStr.append("Grid S전압: ");
					str = packet.substring(38, 42);
					data = Integer.parseInt(str, 16);
					retStr.append(data+" V\r\n");
					retStr.append("Grid T전압: ");
					str = packet.substring(42, 46);
					data = Integer.parseInt(str, 16);
					retStr.append(data+" V\r\n");
					retStr.append("Grid R전류: ");
					str = packet.substring(46, 50);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+" A\r\n");
					retStr.append("Grid S전류: ");
					str = packet.substring(50, 54);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+" A\r\n");
					retStr.append("Grid T전류: ");
					str = packet.substring(54, 58);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+" A\r\n");
					retStr.append("Grid전력: ");
					str = packet.substring(58, 62);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+" kW\r\n");
					retStr.append("역률: ");
					str = packet.substring(62, 66);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+"\r\n");
					retStr.append("주파수: ");
					str = packet.substring(66, 70);
					data = Integer.parseInt(str, 16);
					retStr.append(data/10+"."+data%10+" Hz\r\n");
					retStr.append("누적발전량: ");
					str = packet.substring(70, 78);
					data = Integer.parseInt(str, 16);
					retStr.append(data+" kWh\r\n");
					retStr.append("상태코드: ");
					str = packet.substring(78, 82);
					data = Integer.parseInt(str, 16);
					if(data==0) retStr.append(faultNormal+"\r\n");					
					else if(data==2) retStr.append(faultPVOV+"\r\n");
					else if(data==4) retStr.append(faultPVUV+"\r\n");
					else if(data==8) retStr.append(faultPVOC+"\r\n");
					else if(data==16) retStr.append(faultIGBT+"\r\n");
					else if(data==32) retStr.append(faultOH+"\r\n");
					else if(data==64) retStr.append(faultOV+"\r\n");
					else if(data==128) retStr.append(faultUV+"\r\n");
					else if(data==256) retStr.append(faultOC+"\r\n");
					else if(data==512) retStr.append(faultOF+"\r\n");
					else if(data==1024) retStr.append(faultUF+"\r\n");
					else if(data==2048) retStr.append(faultStandAlone+"\r\n");
					else if(data==4096) retStr.append(faultEarth+"\r\n");
					else if(data==32768) retStr.append(faultEtc+"\r\n");
					retStr.append("동작유무: ");
					str = packet.substring(82, 84);
					data = Integer.parseInt(str, 16);
					if(data==1) retStr.append("RUN\r\n");
					else if(data==0) retStr.append("STOP\r\n");
				}
			}			
		}		
		return retStr.toString();
	}
}
