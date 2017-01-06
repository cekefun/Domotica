package avro.domotics.server;



import avro.domotics.Electable;

import avro.domotics.proto.server.DomServer;
import avro.domotics.util.NetAddress;



public class DomoticsServer extends Electable implements DomServer{
	
	public int getID(){
		return SelfID.getPort();
	}
	
	public String getName(){
		return "server";	
	}
	
	public static void main(String[] args){
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
		DomoticsServer myServer = new DomoticsServer();
		Integer ID = 6789;
		if ( args.length > 0 ){
			ID = Integer.valueOf(args[0]);
		}
		String IP = "127.0.0.1";
		if ( args.length > 1 ){
			ID = Integer.valueOf(args[1]);
		}
		myServer.SelfID = new NetAddress(ID,IP);
		if (myServer.SelfID.getIP() == null){
			System.out.println("invallid IP");
			System.exit(-1);
		}
		myServer.run();
	}
	@Override
	public boolean IsAlive(){
		return true;
	}
	

	
}
