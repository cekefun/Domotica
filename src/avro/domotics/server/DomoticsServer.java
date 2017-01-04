package avro.domotics.server;

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
import avro.domotics.proto.smartFridge.fridge;
import avro.domotics.proto.user.User;
import avro.domotics.smartFridge.SmartFridge.clientpinger;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;


public class DomoticsServer implements DomServer{
	Map<String,Set<Integer> > clients = new ConcurrentHashMap<String,Set<Integer> >();
	Map<Integer,SimpleEntry<CharSequence,Boolean>> users = new ConcurrentHashMap<Integer,SimpleEntry<CharSequence,Boolean> >();
	private Thread PingingEveryone = new Thread(new pinger(this));
	HashMap<Integer, pinginfo> PingMap = new HashMap<Integer, pinginfo>();
	public static void log(String s){
		System.err.println(s);
	}
	
	public class pinginfo{
		public int missedpings = 0;
		}
	
	public static void main(String[] args){
		
		
		int ID = 6789;
		
		if ( args.length > 0 ){
			ID = Integer.valueOf(args[0]);
		}
		
		DomoticsServer myServer = new DomoticsServer();
		myServer.run(ID);
		
		
	}
	public class pinger implements Runnable{
		DomoticsServer ptr;
		int sleeper= 3000;
		boolean _run = true;
		int threshold = 3;
		
		public pinger(DomoticsServer owner){
			ptr = owner;
		}
	
		public void run(){
			while(_run){
				try{
					
				Thread.sleep(sleeper);
				
				}
				
				catch(InterruptedException e){}
				int successes = 0;
				int fails = 0;
				for(String key: clients.keySet()){
					for(Integer ID: clients.get(key)){
						boolean runningsmooth = false;
						if(PingMap.get(ID) == null){
							pinginfo newinfo = new pinginfo();
							PingMap.put(ID, newinfo);
						}
						Transceiver client = null;
						try{
							
							client = new SaslSocketTransceiver(new InetSocketAddress(ID));
							switch (key) {
							case "server":
								DomServer proxyS = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
								runningsmooth = proxyS.IsAlive();
								break;
							case "users":
								User proxyU = (User) SpecificRequestor.getClient(User.class, client);
								runningsmooth = proxyU.IsAlive();
								break;
							case "lights":
								Lights proxyL = (Lights) SpecificRequestor.getClient(Lights.class, client);
								runningsmooth = proxyL.IsAlive();
								break;
							case "fridges":
								fridge proxyF = (fridge) SpecificRequestor.getClient(fridge.class,client);
								runningsmooth = proxyF.IsAlive();
								log("Pingsfridge");
								break;
							/*case "Thermostat":
								Thermostat proxyT = (Thermostat) SpecificRequestor.getClient(Thermostat.class,client);
								proxyT.IsAlive();
								break;*/
							}
							
							client.close();
							client = null;
							if(runningsmooth != true){
								throw new Exception("Notrunning smooth");
							}
							successes++;
							
							
						}
						catch(Exception e){
							
							if(client != null){
								try{
									client.close();
								}
								catch(IOException e2){
									log("Warning: problem closing client after exception " + e2);
								}
							}
							fails++;
							log("objectID: " + ID + " Pinger Error :" +e);
							pinginfo missping = PingMap.get(ID);
							missping.missedpings ++;
							if(missping.missedpings >= threshold){
								PingMap.remove(ID);
								if (key != "server"){
									clients.remove(key);
								}
								try{LeaveHouse(key);}
								catch(IOException woops){
									log("User not in users,... huh?");
								}
								
							}
							
						}
					}
				}
				log("Pinging called: successes: " + successes + " fails: " + fails);
			}
		}
	}
	public void run(Integer ID){
		if (clients.get("server") == null){
			Set<Integer> values = new HashSet<Integer>();
			clients.put("server", values);
		}
		if(!clients.get("server").isEmpty() ){
			System.err.println("[error] Failed to start server");
			System.err.println("Tried to make a second instace");
			return;
		}
		clients.get("server").add(Integer.valueOf(ID));
		Server server = null;
		try{
			server = new SaslSocketServer(new SpecificResponder(DomServer.class, this),new InetSocketAddress(ID));
		} catch(IOException e){
			System.err.println("[error] Failed to start server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		server.start();
		PingingEveryone.start();
		try{
			server.join();
		} catch(InterruptedException e){}
	}
	
	private boolean find(int ID){
		for(String key: clients.keySet()){
			for(Integer entry: clients.get(key)){
				if (entry.intValue() == ID){
					return true;
				}
			}
		}
		return false;
	}
	
	private int getFreeID(){
		int result = 0;
		for(String key: clients.keySet()){
			for(Integer entry: clients.get(key)){
				if (entry.intValue() >= result){
					result = entry.intValue()+1;
				}
			}
		}	
		return result;
	}

	@Override
	public int ConnectLight(int LightID) throws AvroRemoteException{
		if (clients.get("lights") == null){
			Set<Integer> values = new HashSet<Integer>();
			clients.put("lights", values);
		}
		if( find(LightID) ){
			LightID = getFreeID();
		}
		clients.get("lights").add(LightID);
		return LightID;
	}
	
	@Override
	public int ConnectFridge(int FridgeID) throws AvroRemoteException {
		if (clients.get("fridges") == null){
			Set<Integer> values = new HashSet<Integer>();
			clients.put("fridges", values);
		}
		if( find(FridgeID) ){
			FridgeID = getFreeID();
		}
		clients.get("fridges").add(FridgeID);
		return FridgeID;
	}
	
	
	@Override
	public int ConnectUser(CharSequence username) throws AvroRemoteException {
		if (users.containsValue(new SimpleEntry<CharSequence,Boolean>(username,false)) || users.containsValue(new SimpleEntry<CharSequence,Boolean>(username,true))){
			for (Integer ID :users.keySet()){
				if(users.get(ID).getKey() == username){
					users.get(ID).setValue(true);
					return ID;
				}
			}
		}
		int ID = getFreeID();
		SimpleEntry<CharSequence,Boolean> tuple = new SimpleEntry<CharSequence,Boolean>(username,true);
		users.put(ID, tuple);
		if(clients.get("users") == null){
			Set<Integer> values = new HashSet<Integer>();
			clients.put("users", values);
		}
		clients.get("users").add(ID);
		return ID;
	}

	@Override
	public boolean Switch(int lightID) throws AvroRemoteException {
		if (clients.get("lights")==null || !clients.get("lights").contains(lightID)){
			throw new AvroRemoteException("Exist");
		}
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(lightID));
			Lights proxy = (Lights) SpecificRequestor.getClient(Lights.class, client);
			proxy.LightSwitch();
			client.close();
		} catch(IOException e){
			throw new AvroRemoteException("Connect");
		}
		return true;
	}

	@Override
	public Map<CharSequence, List<Integer>> GetClients() throws AvroRemoteException {
		Map<CharSequence, List<Integer>> result = new HashMap<CharSequence, List<Integer>>();
		for(String key: clients.keySet()){
			List<Integer> ValueList = new Vector<Integer>();
			result.put(key, ValueList);
			for(Integer ID: clients.get(key)){
				try{
					Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
					switch (key) {
					case "server":
						DomServer proxyS = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
						proxyS.IsAlive();
						break;
					case "users":
						User proxyU = (User) SpecificRequestor.getClient(User.class, client);
						proxyU.IsAlive();
						break;
					case "lights":
						Lights proxyL = (Lights) SpecificRequestor.getClient(Lights.class, client);
						proxyL.IsAlive();
						break;
					case "fridges":
						fridge proxyF = (fridge) SpecificRequestor.getClient(fridge.class,client);
						proxyF.IsAlive();
						break;
					}
					client.close();
				}
				catch(IOException e){
					continue;
				}
				result.get(key).add(ID);
			}
		}
		return result;
	}
	
	@Override
	public Map<CharSequence, Boolean> GetServers() throws AvroRemoteException {
		Map<CharSequence, Boolean> result = new HashMap<CharSequence, Boolean>();
		for(Integer ID: clients.get("server")){
			boolean connected = true;
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
				DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
				proxy.IsAlive();
				client.close();
			}
			catch(Exception e){
				connected = false;
			}
			result.put(ID.toString(), connected);
		}
		return result;
	}

	@Override
	public Map<CharSequence, Boolean> GetUsers() throws AvroRemoteException {
		Map<CharSequence, Boolean> result = new HashMap<CharSequence, Boolean>();
		for(SimpleEntry<CharSequence,Boolean> value: users.values()){
			result.put(value.getKey(), value.getValue());
			
		}
		return result;
	}

	@Override
	public Map<CharSequence, Boolean> GetLights() throws AvroRemoteException {
		Map<CharSequence, Boolean> result = new HashMap<CharSequence, Boolean>();
		if(clients.get("lights") == null){
			return result;
		}
		for(Integer ID: clients.get("lights")){
			boolean on;
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
				Lights proxy = (Lights) SpecificRequestor.getClient(Lights.class, client);
				on = proxy.GetLightState();
				client.close();
			}
			catch(IOException e){
				continue;
			}
			result.put(ID.toString(), on);
		}
		return result;
	}

	@Override
	public Void LeaveHouse(CharSequence username) throws AvroRemoteException {
		for(Integer ID : users.keySet()){
			if (users.get(ID).getKey() == username) {
				users.get(ID).setValue(false);
			}
		}
		return null;
	}
	
	@Override
	public boolean ConnectUserToFridge(int userID, int fridgeID) throws AvroRemoteException {
		//Vector<String> result = new Vector<String>();
		if(clients.get("fridges") == null){
			return false;
		}
		if(!clients.get("fridges").contains(fridgeID)){
			return false;
		}
		boolean success = false;
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(fridgeID));
			fridge proxy = (fridge) SpecificRequestor.getClient(fridge.class, client);
			success = proxy.OpenFridge(userID);
			//result = proxy.getContents();
			client.close();
		}
		catch(Exception e){
			
		}
		return success;
	}
	
	@Override
	public Map<CharSequence, List<CharSequence>> GetFridges() throws AvroRemoteException {
		Map<CharSequence, List<CharSequence>>  result = new HashMap<CharSequence, List<CharSequence> >() ;
		if(clients.get("fridges") == null){
			return result;
		}
		for(Integer ID: clients.get("fridges")){

			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
				fridge proxy = (fridge) SpecificRequestor.getClient(fridge.class, client);
				List<CharSequence> Contents = proxy.GetContents();
				client.close();
				result.put(ID.toString(), Contents);
			}
			catch(IOException e){
				continue;
			}
		}
		return result;
	}
	
	@Override
	public Void FridgeIsEmpty(int fridgeID) throws AvroRemoteException {
		// TODO Auto-generated method stub
		//needs to inform users somehow.
		return null;
	}
	
	@Override
	public boolean IsAlive() throws AvroRemoteException {
		return true;
	}
	
}
