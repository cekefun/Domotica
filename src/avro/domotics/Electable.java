package avro.domotics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.AbstractMap.SimpleEntry;

import avro.domotics.proto.Electable.electable;
import avro.domotics.server.DomoticsServer;
import avro.domotics.user.UserClient;


public abstract class Electable implements electable {
	public Map<CharSequence,List<Integer>> clientlist = null;
	public Map<CharSequence, Map<CharSequence, Boolean>> userlist = null;
	public Map<String,Set<Integer> > clients = new ConcurrentHashMap<String,Set<Integer> >();
	public Map<Integer,SimpleEntry<CharSequence,Boolean>> users = new ConcurrentHashMap<Integer,SimpleEntry<CharSequence,Boolean>>();
	public int UPPERBOUND = 64*1024 + 1;
	private Integer SelfID = null;
	int electedID = 0;
	
	public abstract int getID();
	public abstract String getName();
	
	public void SetElected(int _electedID){

		electedID = _electedID;

		int OwnID = this.getID();
		DomoticsServer.log("SetElected, ElectedID: " + _electedID + " Own ID: " + OwnID );
		if(OwnID == _electedID){
			//startserver
		}

	}
	
	public Void _sync(Map<CharSequence,List<Integer>> _clients,	Map<CharSequence,Map<CharSequence,Boolean>> _users){
		DomoticsServer.log("in sync; clients: "+ _clients + " users: " + _users);
		clientlist = new HashMap<>(_clients);
		//DomoticsServer.log("Preusersync");
		userlist = new HashMap<>(_users);
		//DomoticsServer.log("Prereturnsync: clients: " + clients + " users : " + users);
		
		for(CharSequence key: clientlist.keySet()){
			DomoticsServer.log("Key reconversion, key "+ key);
			DomoticsServer.log("Key reconversion, value "+ clientlist.get(key));
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
		DomoticsServer.log("election: LastID: " + OwnID + " NextInChain: " + nextInChain );
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
			DomoticsServer.log("THROWN ELECTION: " + e);
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
		DomoticsServer.log("Elected: LastID: " + _ElectedID + " NextInChain: " + nextInChain );
		Transceiver client = null;
		try{
			client = new SaslSocketTransceiver(new InetSocketAddress(nextInChain));
			electable.Callback proxy = (electable.Callback) SpecificRequestor.getClient(electable.Callback.class, client);
			SetElected(_ElectedID);

			proxy.elected(_ElectedID, nextInChain);


		}
		catch(IOException e){
			DomoticsServer.log("THROWN in elected");
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

}
