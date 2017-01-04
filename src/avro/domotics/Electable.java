package avro.domotics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.AbstractMap.SimpleEntry;

import avro.domotics.proto.Electable.electable;
import avro.domotics.proto.lights.Lights;
import avro.domotics.proto.server.DomServer;
import avro.domotics.proto.smartFridge.fridge;
import avro.domotics.proto.user.User;

public abstract class Electable implements electable, Runnable {
	public Map<CharSequence,List<Integer>> clientlist = null;
	public Map<CharSequence, Map<CharSequence, Boolean>> userlist = null;
	public Map<String,Set<Integer> > clients = new ConcurrentHashMap<String,Set<Integer> >();
	public Map<Integer,SimpleEntry<CharSequence,Boolean>> users = new ConcurrentHashMap<Integer,SimpleEntry<CharSequence,Boolean>>();
	public int UPPERBOUND = 64*1024 + 1;
	//private Integer SelfID = null;
	private int electedID = 0;
	private Thread PingingEveryone = new Thread(new pinger(this));
	HashMap<Integer, pinginfo> PingMap = new HashMap<Integer, pinginfo>();
	private Timer syncTimer = new Timer();
	public Synchronizer sync = new Synchronizer();
	public final long countdown = 10000;
	Thread ServerThread = null;
	private Timer deadservertimer = new Timer();
	public StartElection electiontimertask = new StartElection();
	
	public abstract int getID();
	public abstract String getName();
	
	public void stop(){}
	
	public void log(String s){
		System.err.println(this.getName() + " " + this.getID() + " says: " +s);
	}
	
	public void SetElected(int _electedID){

		electedID = _electedID;
		
		int OwnID = this.getID();
		log("SetElected, ElectedID: " + _electedID + " Own ID: " + OwnID );
		if(OwnID == _electedID){
			stop();
			ServerThread = new Thread(this);
			ServerThread.start();
		}
		else{
			log("TODO: stop server thread");
		}

	}
	
	public Void _sync(Map<CharSequence,List<Integer>> _clients,	Map<CharSequence,Map<CharSequence,Boolean>> _users){
		log("in sync; clients: "+ _clients + " users: " + _users);
		clientlist = new HashMap<>(_clients);
		//log("Preusersync");
		userlist = new HashMap<>(_users);
		//log("Prereturnsync: clients: " + clients + " users : " + users);
		
		for(CharSequence key: clientlist.keySet()){
			//log("Key reconversion, key "+ key);
			//log("Key reconversion, value "+ clientlist.get(key));
			clients.put(key.toString(), new HashSet<>(clientlist.get(key)) );
			
		}
		for(CharSequence key: userlist.keySet()){
			
			int newkey = Integer.parseInt(key.toString()); //(users.get(key).getKey().toString());
			CharSequence otherkey = null;
			for( CharSequence key2: userlist.get(key).keySet()){
				otherkey = key2;
			}
			Boolean bool = userlist.get(key).get(otherkey);
			SimpleEntry<CharSequence,Boolean> tempmap = new SimpleEntry<CharSequence,Boolean>(otherkey, bool);
			users.put(newkey, tempmap);
		}
		return null;
	}
	@Override
	public boolean election(int LastID){
		int OwnID = this.getID();

		int nextInChain = UPPERBOUND;
		int min = UPPERBOUND;
		for(String key: clients.keySet()){
			for(Integer ID: clients.get(key)){
				if(key == "fridges" || key == "users"){
					if(ID < nextInChain && ID > OwnID){
						nextInChain = ID;
						
					}
					if(ID<min){
						min = ID;
					}
				}
			}
		}
		if( nextInChain == UPPERBOUND){
			nextInChain = min;
		}
		if(nextInChain == UPPERBOUND){

			SetElected(OwnID);
			return true;
		}
		else if(LastID == OwnID){
			//Elect myself
			SetElected(OwnID);
			elected(OwnID, nextInChain);
			return true;
		}
		log("election: LastID: " + OwnID + " NextInChain: " + nextInChain );
		Transceiver client = null;
		try{
			client = new SaslSocketTransceiver(new InetSocketAddress(nextInChain));
			electable.Callback proxy = (electable.Callback) SpecificRequestor.getClient(electable.Callback.class, client);
			if(OwnID > LastID){
				proxy.election(OwnID);
			}
			else{
				proxy.election(LastID);
			}


		}
		catch(IOException e){
			log("THROWN ELECTION: " + e);
		}
		finally{
			try {
				if(client != null)
					client.close();
			}
			catch (IOException e){}
		}
		return true;
	}
	
	@Override
	public boolean elected(int _ElectedID, int nextInChain){
		log("Elected: LastID: " + _ElectedID + " NextInChain: " + nextInChain );
		Transceiver client = null;
		try{
			client = new SaslSocketTransceiver(new InetSocketAddress(nextInChain));
			electable.Callback proxy = (electable.Callback) SpecificRequestor.getClient(electable.Callback.class, client);
			SetElected(_ElectedID);

			proxy.elected(_ElectedID, nextInChain);


		}
		catch(IOException e){
			log("THROWN in elected");
		}
		finally{
			try {
				if(client != null)
					client.close();
			}
			catch (IOException e){}
		}
		return false;
	}
	//_________________________________________________DomoticsServer inherit methods_________________________________________________________\\
	public class pinginfo{
		public int missedpings = 0;
	}
	
	public class Synchronizer extends TimerTask{

		public void run(){
			log("preparing for syncing");
			Map<CharSequence,List<Integer>> clientlist = new HashMap<>();
			Map<CharSequence,Map<CharSequence,Boolean>> userlist = new HashMap<>();
			for(String key: clients.keySet()){
				clientlist.put((CharSequence)key, new ArrayList<>(clients.get(key)) );
			}
			for(int key: users.keySet()){
				
				CharSequence newkey = users.get(key).getKey();
				Boolean bool = users.get(key).getValue();
				HashMap<CharSequence,Boolean> tempmap = new HashMap<CharSequence,Boolean>();
				tempmap.put(newkey, bool);
				userlist.put(Integer.toString(key), tempmap);
			}
			
			
			log("syncing");
			for(String key: clients.keySet()){
				for(Integer ID: clients.get(key)){
					try{
						log("ID: " + ID);
						//client = new SaslSocketTransceiver(new InetSocketAddress(ID));
						Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
						switch (key) {
						case "users":
							log("pre request");
							User proxyU = (User) SpecificRequestor.getClient(User.class, client);
							log("pre client sync");
							proxyU._sync(clientlist, userlist);
							log("After clientsync clients: "+clientlist + " users: "+userlist);
							
							break;
						case "fridges":
							log("pre request");
							fridge proxyF = (fridge) SpecificRequestor.getClient(fridge.class,client);
							log("pre fridge sync");
							proxyF._sync(clientlist, userlist);
							//proxyF._sync(new HashMap<CharSequence,List<Integer> >(), new HashMap<CharSequence,Map<CharSequence,Boolean>>());
							log("After fridgesync clients: "+clientlist + " users: "+userlist);
							break;
							
						}
						client.close();
					}
					catch(IOException e){
						log("syncing IOException: "+e);
					}
				}
			}
			log("synched");
		}
		
	}
	public class pinger implements Runnable{
		Electable ptr;
		int sleeper= 3000;
		boolean _run = true;
		int threshold = 3;
		
		public pinger(Electable electable){
			ptr = electable;
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
								//proxyF._sync(new HashMap<CharSequence,List<Integer> >(), new HashMap<CharSequence,Map<CharSequence,Boolean>>());
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
								clients.get(key).remove(ID);
								if(clients.get(key).size() == 0){
									if (key != "server"){
										clients.remove(key);
									}
								}
								if(key == "users"){
									try{LeaveHouse(key);}
									catch(IOException woops){
										log("User not in users,... huh?");
									}
								}
								
							}
							
						}
					}
				}
				log("Pinging called: successes: " + successes + " fails: " + fails);
			}
		}
	}
	public void run(){
		int ID = this.getID();
		if (clients.get("server") == null){
			Set<Integer> values = new HashSet<Integer>();
			clients.put("server", values);
		}
		if(!clients.get("server").isEmpty() ){
			System.err.println("[error] Failed to start server");
			System.err.println("Tried to make a second instance");
			return;
		}
		clients.get("server").add(Integer.valueOf(ID));
		Server server = null;
		try{
			server = new SaslSocketServer(new SpecificResponder(electable.class, this),new InetSocketAddress(ID));
		} catch(IOException e){
			System.err.println("[error] Failed to start server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		server.start();
		PingingEveryone.start();
		java.util.Date now = new java.util.Date();
		syncTimer.schedule(sync, now , countdown);
		
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
		electiontimertask.cancel();
		electiontimertask = new StartElection();
		deadservertimer.schedule(electiontimertask, countdown);
		//log("Test electable lists: clients " + clients +  " users: " + users );
		return true;
	}
	//__________________________________________________________SmartFridge inherit_______________________________________________________________\\
	public class StartElection extends TimerTask{

		public void run(){
			//robert chang yey
			log("Server dead");
			clients.remove("server");
			election(0);

		}
	}
	public void standby(){
		this.deadservertimer.schedule(this.electiontimertask, this.countdown);
		log("Standby");
	}
}
