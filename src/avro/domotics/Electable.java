package avro.domotics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;

public class Electable {
	Map<String,Set<Integer> > clients = null;
	Map<Integer,SimpleEntry<CharSequence,Boolean>> users = null;
	
	public void _Sync(Map<String,Set<Integer> > _clients,Map<Integer,SimpleEntry<CharSequence,Boolean>> _users ){
		clients = new HashMap<String,Set<Integer> >(_clients);// _clients.clone();
		users = new HashMap<Integer,SimpleEntry<CharSequence,Boolean>>(_users);//_users.clone();
	}
	public void Chang_Roberts(){
		
	}
}
