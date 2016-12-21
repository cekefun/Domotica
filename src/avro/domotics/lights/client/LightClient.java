package avro.domotics.lights.client;


import java.io.IOException ;
import java.net.InetSocketAddress;
import java.util.Scanner;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import avro.domotics.proto.lights.Lights;
import avro.domotics.proto.server.DomServer;


public class LightClient implements Lights{
	protected boolean state;
	protected Server server = null;
	protected ServerThread serverRunning = new ServerThread();
	
	private class ServerThread implements Runnable {
		public void run(){
			this.run(6789);
		}
		public void run(Integer ID){
			try{
				server = new SaslSocketServer(new SpecificResponder(Lights.class, new LightClient()),new InetSocketAddress(ID));
			} catch(IOException e){
				System.err.println("[error] Failed to start server");
				e.printStackTrace(System.err);
				System.exit(1);
			}
			server.start();
			try{
				server.join();
			} catch(InterruptedException e){}
		}
		public void stop(){
			server.close();
		}
	}
	
	@Override
	public Void LightSwitch() throws AvroRemoteException{
		state = ! state;
		if(state){
			System.out.println("The light is on.");
		}
		else{
			System.out.println("The light is off.");
		}
		return null;
	}
	
	@Override
	public boolean GetLightState() throws AvroRemoteException{
		return state;
	}
	
	
	public static void main(String[] args){
		int serverAddress = 6789;
		int ID = 7891;
		if(args.length > 1){
			serverAddress = Integer.valueOf(args[0]);
		}
		if(args.length > 2){
			ID = Integer.valueOf(args[1]);
		}
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(serverAddress));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			ID = proxy.ConnectLight(ID);
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		System.out.println(ID);
		
		LightClient Light = new LightClient();
		Light.serverRunning.run(ID);
		while(true){
			String input = "";
			Scanner s = new Scanner(System.in);
			input = s.next();
			s.close();
			if (input =="exit"){
				Light.serverRunning.stop();
				return;
			}
		}
		
	}
}
