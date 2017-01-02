package avro.domotics.lights.client;


import java.io.IOException ;
import java.net.InetSocketAddress;

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
	private boolean state;
	private Server server = null;
	private Thread serverRunning = new Thread(new ServerThread());
	private Integer lightID;
	
	private class ServerThread implements Runnable {
		public void run(){
			this.run(lightID);
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
	
	public void run(Integer serverAddress, Integer ID){
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
		lightID = ID;
		System.out.println(ID);
		serverRunning.start();
		
	}

	public void stop(){
		serverRunning.interrupt();
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
		LightClient thisLight = new LightClient();
		thisLight.run(serverAddress, ID);
		while(true){
			int input = 0;
			try{
				input = System.in.read();
			} catch(Exception e){
				
			}
			if (input =='e'){ 
				thisLight.stop();
				break;
			}
		}
	}
}

