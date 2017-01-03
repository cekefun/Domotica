package avro.domotics.smartFridge;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Vector;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import avro.domotics.proto.server.DomServer;
import avro.domotics.proto.smartFridge.fridge;


public class SmartFridge implements fridge {
	private Server server = null;
	private Integer ServerID = 6789;
	private Integer SelfID = 6789;
	private List<CharSequence> contents = new Vector<CharSequence>();
	private boolean Open = false;
	private Integer CurrentuserID = null;
	private Thread serverThread = null;
	
	SmartFridge(Integer server){
		server = ServerID;
	}
	
	public class RunServer implements Runnable{
		Integer ID;
		SmartFridge ptr;
		public RunServer(Integer aboveID, SmartFridge above){
			ID = aboveID;
			ptr = above;
		}
		public void run(){
			try{
				server = new SaslSocketServer(new SpecificResponder(fridge.class, ptr),new InetSocketAddress(ID));
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
	public List<CharSequence> GetContents() throws AvroRemoteException{
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		List<CharSequence> allContents = this.contents;
	
		return allContents;
	}
	
	@Override
	public boolean OpenFridge(int UserID){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.Open == false){
			this.Open = true;
		}
		else{
			return false;
		}
		
		this.CurrentuserID = UserID;
		return true;
	}
	
	@Override
	public Void AddItem(int UserID, CharSequence item){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID == UserID){
			this.contents.add(item);
		}
		return null;
	}
	
	@Override
	public Void RemoveItem(int UserID, CharSequence item){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID == UserID){
			for(int i = 0;  i < this.contents.size(); i++){
				if(item == this.contents.get(i)){
					this.contents.remove(i);
						if(this.contents.isEmpty()){
							//send message to controller
							try{
								Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
								DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
								proxy.FridgeIsEmpty(SelfID);
								client.close();
							} catch(IOException e){
								System.err.println("Error connecting to server");
								e.printStackTrace(System.err);
								System.exit(1);
							}
						}
						return null;
				}
			}
		}
		return null;
	}
	
	//TODO something
	@Override
	public Void CloseFridge(int UserID){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID == UserID){
			this.Open= false;
			this.CurrentuserID = null;
		}
		return null;
	}
	
	public void start(){
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			SelfID = proxy.ConnectFridge(SelfID);
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		System.out.println("You have ID: "+Integer.toString(SelfID));
		serverThread = new Thread(new RunServer(SelfID,this));
		serverThread.start();
	}
	
	public void stop(){
		serverThread.interrupt();
	}
	
	public static void main(String[] args){
		Integer ServerID = 6789;

		if (args.length>0){
			ServerID = Integer.valueOf(args[0]);
		}

		
		SmartFridge fridge = new SmartFridge(ServerID);
		
		fridge.start();
		
		while(true){
			int input = 0;
			try{
				input = System.in.read();
			} catch(Exception e){
				
			}
			if (input =='e'){ 
				fridge.stop();
				break;
			}
		}
	}
	

	@Override
	public boolean ConnectToClient(int ClientID) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean IsAlive() throws AvroRemoteException {
		return true;
	}

}