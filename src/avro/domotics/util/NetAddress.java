package avro.domotics.util;

import java.net.InetAddress;

public class NetAddress {
	int port;
	InetAddress ip;
	public NetAddress(int inPort, String inIP){
		port = inPort;
		try{
			ip = InetAddress.getByName(inIP);
		} catch (Exception e){
			ip = null;
		}
	}
	
	public Integer getPort(){
		return port;
	}
	
	public InetAddress getIP(){
		return ip;
	}
	
	public void setPort(int input){
		port = input;
	}
	
	public String getIPStr(){
		if (ip == null){
			return "";
		}
		String result = "";
		for(byte a : ip.getAddress()){
			int b = a;
			result += String.valueOf(b)+'.';
		}
		result = result.substring(0, result.length()-1);
		return result;
	}
}
