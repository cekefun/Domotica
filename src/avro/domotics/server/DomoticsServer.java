package avro.domotics.server;



import avro.domotics.Electable;

import avro.domotics.proto.server.DomServer;



public class DomoticsServer extends Electable implements DomServer{
	private int SelfID = 6789;
	
	public int getID(){
		return SelfID;
	}
	
	public String getName(){
		return "server";	
	}
	
	public static void main(String[] args){
		DomoticsServer myServer = new DomoticsServer();
		if ( args.length > 0 ){
			myServer.SelfID = Integer.valueOf(args[0]);
		}
		myServer.run();
	}
	

	
}
