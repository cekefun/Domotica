package avro.domotics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.AbstractMap.SimpleEntry;

import avro.domotics.server.DomoticsServer;


public class Electable {
	public Map<CharSequence,List<Integer>> clientlist = null;
	public Map<CharSequence, Map<CharSequence, Boolean>> userlist = null;
	public Map<String,Set<Integer> > clients = new ConcurrentHashMap<String,Set<Integer> >();
	public Map<Integer,SimpleEntry<CharSequence,Boolean>> users = new ConcurrentHashMap<Integer,SimpleEntry<CharSequence,Boolean>>();
	
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
	public void Chang_Roberts(){
		
	}
}
