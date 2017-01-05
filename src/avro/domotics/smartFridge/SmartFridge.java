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

import avro.domotics.Electable;
import avro.domotics.proto.Electable.electable;
import avro.domotics.proto.smartFridge.fridge;
import avro.domotics.user.UserClient;
import avro.domotics.util.NetAddress;


public class SmartFridge extends Electable implements fridge {
	private Server server = null;
	private NetAddress ServerID = null;
	private List<CharSequence> contents = new Vector<CharSequence>();
	private boolean Open = false;
	public NetAddress CurrentuserID = null;
	private Thread serverThread = null;
	private Thread Pinginguser = new Thread(new clientpinger(this));

	//Map<String,Set<Integer> > clients = null;
	//Map<Integer,SimpleEntry<CharSequence,Boolean>> users = null;
	
	
	SmartFridge(NetAddress server, NetAddress thisAddr){
		ServerID = server;
		SelfID = thisAddr;
	}

	/*public void _Sync(Map<String,Set<Integer> > _clients,Map<Integer,SimpleEntry<CharSequence,Boolean>> _users ){
		clients = new HashMap<String,Set<Integer> >(_clients);// _clients.clone();
		users = new HashMap<Integer,SimpleEntry<CharSequence,Boolean>>(_users);//_users.clone();
	}*/
	public int getID(){
		return SelfID.getPort();
	}
	public  String getName(){
		return "SmartFridge";
	}
	public class RunServer implements Runnable{
		NetAddress ID;
		SmartFridge ptr;
		public RunServer(NetAddress aboveID, SmartFridge above){
			ID = aboveID;
			ptr = above;
		}
		public void run(){
			try{
				server = new SaslSocketServer(new SpecificResponder(fridge.class, ptr),new InetSocketAddress(ID.getIP(),ID.getPort()));
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
	
	public class clientpinger implements Runnable{
		SmartFridge ptr;
		boolean stop  = false;
		int missescounter = 0;
		long sleeper = 3000;
		int missesallowed = 2;
		
		public clientpinger(SmartFridge owner){
			ptr = owner;
		}
		
		public void run(){
			//ptr.CurrentuserID;
			while(! stop){
				missescounter++;
				try{
					
				Thread.sleep(sleeper);
				
				}
				
				catch(InterruptedException e){}
				try{
					Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ptr.CurrentuserID.getIP(),ptr.CurrentuserID.getPort()));
					UserClient proxy = (UserClient) SpecificRequestor.getClient(UserClient.class, client);

					if(proxy.IsAlive()){
						missescounter--;
					}
					client.close();
				}
				catch(IOException e){}
				if(missescounter >= missesallowed){
					stop = true;
					ptr.CloseFridge(ptr.CurrentuserID.getPort());
					
				}
			}
		}
		
	}
	
	@Override
	public List<CharSequence> GetContents() throws AvroRemoteException{
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		log("Smartfridge list");
		List<CharSequence> allContents = this.contents;
		log("Smartfridge list returning " + allContents );
		return allContents;
	}
	
	@Override
	public synchronized boolean OpenFridge(int UserID,CharSequence IP){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.Open == false){
			this.Open = true;
			this.Pinginguser.start();
		}
		else{
			return false;
		}
		NetAddress userAddr = new NetAddress(UserID,String.valueOf(IP));
		this.CurrentuserID = userAddr;
		return true;
	}
	
	@Override
	public Void AddItem(int UserID, CharSequence item){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID.getPort() == UserID){
			this.contents.add(item);
		}
		return null;
	}
	
	@Override
	public Void RemoveItem(int UserID, CharSequence item){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID.getPort() == UserID){
			if(contents.contains(item)){
				contents.remove(item);
				if(this.contents.isEmpty()){
					//send message to controller
					try{
						Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID.getIP(),ServerID.getPort()));
						electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
						proxy.FridgeIsEmpty(SelfID.getPort());
						client.close();
					} catch(IOException e){
						System.err.println("Error connecting to server");
						e.printStackTrace(System.err);
						System.exit(1);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public Void CloseFridge(int UserID){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID.getPort() == UserID){
			this.Open= false;
			this.CurrentuserID = null;
		}
		return null;
	}
	
	public void start(){
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID.getIP(),ServerID.getPort()));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			SelfID.setPort(proxy.ConnectFridge(SelfID.getPort(),SelfID.getIPStr()));
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		System.out.println("You have ID: "+Integer.toString(SelfID.getPort()));
		serverThread = new Thread(new RunServer(SelfID,this));
		serverThread.start();
	}
	
	public void stop(){
		log("stop");
		server.close();
	}
	
	public static void main(String[] args){
		Integer ServerID = 6789;
		String ServerIP = "127.0.0.1";
		Integer FridgeID = 7777;
		String FridgeIP = "127.0.0.1";
		if (args.length>0){
			ServerID = Integer.valueOf(args[0]);
		}
		if (args.length>1){
			ServerIP = args[1];
		}
		if (args.length>2){
			FridgeID = Integer.valueOf(args[2]);
		}
		if (args.length>3){
			FridgeIP = args[3];
		}

		NetAddress ServerAddr = new NetAddress(ServerID,ServerIP);
		if(ServerAddr.getIP() == null){
			System.out.println("Invalid serverIP");
			System.exit(-1);
		}
		NetAddress FridgeAddr = new NetAddress(FridgeID,FridgeIP);
		if(FridgeAddr.getIP() == null){
			System.out.println("Invalid fridgeIP");
			System.exit(-1);
		}
		SmartFridge fridge = new SmartFridge(ServerAddr,FridgeAddr);
		fridge.start();
		
		fridge.standby();
		
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
}