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

import avro.domotics.proto.Electable.electable;
import avro.domotics.proto.lights.Lights;
import avro.domotics.util.NetAddress;


public class LightClient implements Lights{
	private boolean state;
	private Server server = null;
	private NetAddress lightID;
	private Thread serverRunning = null;
	
	private class ServerThread implements Runnable {
		NetAddress ID;
		LightClient ptr;
		public ServerThread(NetAddress aboveID, LightClient above){
			ID = aboveID;
			ptr = above;
		}
		public void run(){
			try{
				server = new SaslSocketServer(new SpecificResponder(Lights.class, ptr),new InetSocketAddress(ID.getIP(),ID.getPort()));
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
	
	public LightClient(){
		state = false;
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
	
	public void run(NetAddress serverAddress, NetAddress ID){
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(serverAddress.getIP(),serverAddress.getPort()));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			ID.setPort(proxy.ConnectLight(ID.getPort(),ID.getIPStr()));
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		lightID = ID;
		System.out.println("You have ID: "+Integer.toString(ID.getPort()));
		serverRunning = new Thread(new ServerThread(lightID,this));
		serverRunning.start();
		
	}

	public void stop(){
		serverRunning.interrupt();
	}
	
	public static void main(String[] args){
		int serverAddress = 6789;
		String serverIP = "127.0.0.1";
		int ID = 7891;
		String thisIP = "127.0.0.1";
		if(args.length > 0){
			serverAddress = Integer.valueOf(args[0]);
		}
		if(args.length > 1){
			serverIP = args[1];
		}
		if(args.length > 2){
			ID = Integer.valueOf(args[2]);
		}
		if(args.length > 3){
			thisIP = args[3];
		}
		
		NetAddress ServerAddr = new NetAddress(serverAddress,serverIP);
		NetAddress thisAddr = new NetAddress(ID,thisIP);
		
		LightClient thisLight = new LightClient();
		thisLight.run(ServerAddr, thisAddr);
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

	@Override
	public boolean IsAlive() throws AvroRemoteException {
		return true;
	}
}