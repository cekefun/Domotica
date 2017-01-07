package avro.domotics;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import avro.domotics.proto.Electable.electable;
import avro.domotics.proto.lights.Lights;
import avro.domotics.util.NetAddress;

public abstract class SimpleClient extends Client {
	protected Thread serverRunning = null;
	private Server server = null;
	private NetAddress OwnID;
	
	public boolean IsAlive() throws AvroRemoteException {
		return true;
	}
	
	public abstract NetAddress getAddress();
	public abstract Class getClientClass();
	public class ServerThread implements Runnable {
		NetAddress ID;
		SimpleClient ptr;
		public ServerThread(NetAddress aboveID, SimpleClient above){
			ID = aboveID;
			ptr = above;
		}
		public void run(){
			try{
				server = new SaslSocketServer(new SpecificResponder(getClientClass(), ptr),new InetSocketAddress(ID.getIP(),ID.getPort()));
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
	
	


	public void stop(){
		serverRunning.interrupt();
	}

	
}
