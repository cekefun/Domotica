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
import avro.domotics.smartfridge.SmartFridge;
import avro.domotics.exceptions.ConnectException;
import avro.domotics.exceptions.ExistException;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class DomoticsServer implements DomServer{
	static HashMap<String,Set<Integer> > clients = new HashMap<String,Set<Integer> >();
	static HashMap<String,SimpleEntry<Integer,Boolean>> users = new HashMap<String,SimpleEntry<Integer,Boolean> >();
	
	public static void main(String[] args){
		if (clients.get("server") == null){
			Set<Integer> values = new HashSet<Integer>();
			clients.put("server", values);
		}
		if(!clients.get("server").isEmpty() ){
			System.err.println("[error] Failed to start server");
			System.err.println("Tried to make a second instace");
			return;
		}
		
		Server server = null;
		int ID = 6789;
		
		if ( args.length > 0 ){
			ID = Integer.valueOf(args[0]);
		}
		
		clients.get("server").add(Integer.valueOf(ID));
			
		try{
			server = new SaslSocketServer(new SpecificResponder(DomServer.class, new DomoticsServer()),new InetSocketAddress(ID));
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
				System.out.println(entry);
				if (entry.intValue() >= result){
					result = entry.intValue()+1;
				}
			}
		}	
		return result;
	}

    //@Override
	public int ConnectLight(int LightID) throws AvroRemoteException{
		if (clients.get("lights") == null){
			Set<Integer> values = new HashSet<Integer>();
			clients.put("lights", values);
		}
		if( find(LightID) ){
			LightID = getFreeID();
		}
		System.out.println(String.valueOf(LightID));
		clients.get("lights").add(LightID);
		return LightID;
	}
	
	//@Override
	public int ConnectUser(CharSequence username) throws AvroRemoteException {
		if (users.containsKey(username)){
			users.get(username).setValue(true);
			return users.get(username).getKey();
		}
		int ID = getFreeID();
		String name = username.toString();
		SimpleEntry<Integer,Boolean> tuple = new SimpleEntry<Integer,Boolean>(ID,true);
		users.put(name, tuple);
		if(clients.get("users") == null){
			Set<Integer> values = new HashSet<Integer>();
			clients.put("users", values);
		}
		clients.get("users").add(ID);
		return ID;
	}

	//@Override
	public boolean Switch(int lightID) throws AvroRemoteException {
		if (clients.get("lights")==null || !clients.get("lights").contains(lightID)){
			throw new ExistException("light",lightID);
		}
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(lightID));
			Lights proxy = (Lights) SpecificRequestor.getClient(Lights.class, client);
			proxy.LightSwitch();
			client.close();
		} catch(IOException e){
			throw new ConnectException(lightID);
		}
		return true;
	}

	//@Override
	public Map<CharSequence, List<Integer>> GetClients() throws AvroRemoteException {
		Map<CharSequence, List<Integer>> result = new HashMap<CharSequence, List<Integer>>();
		for(String key: clients.keySet()){
			List<Integer> ValueList = new Vector<Integer>();
			result.put(key, ValueList);
			for(Integer ID: clients.get(key)){
				try{
					Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
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


	//@Override
	public Map<CharSequence, Boolean> GetLights() throws AvroRemoteException {
		Map<CharSequence, Boolean> result = new HashMap<CharSequence, Boolean>();
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

	//@Override
	public Void LeaveHouse(CharSequence username) throws AvroRemoteException {
		users.get(username).setValue(false);
		return null;
	}
	//@Override
	public List<String> GetContents() throws AvroRemoteException {
		List<String> result = new Vector<String>();
		for(Integer ID: clients.get("SmartFridge")){
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
				SmartFridge proxy = (SmartFridge) SpecificRequestor.getClient(SmartFridge.class, client);
				result = proxy.getContents();
				client.close();
			}
			catch(IOException e){
				continue;
			}
		}
		return result;
	}
	
	//@Override
	public Void ConnectUserToFridge(int userID, int fridgeID) throws AvroRemoteException {
		//Vector<String> result = new Vector<String>();
		for(Integer ID: clients.get("SmartFridge")){
			if(ID == fridgeID){
				try{
					Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
					SmartFridge proxy = (SmartFridge) SpecificRequestor.getClient(SmartFridge.class, client);
					//result = proxy.getContents();
					client.close();
				}
				catch(IOException e){
					continue;
				}
			}
		}
		return null;
	}
	//@Override
	public List<Integer> GetFridges() throws AvroRemoteException {
		// TODO Auto-generated method stub
		List<Integer> result = new Vector<Integer>();
		for(Integer ID: clients.get("SmartFridge")){

			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ID));
				//SmartFridge proxy = (SmartFridge) SpecificRequestor.getClient(SmartFridge.class, client);
				//result = proxy.getContents();
				client.close();
				result.add(ID);
			}
			catch(IOException e){
				continue;
			}
		}
		return result;
	}
	
	//@Override
	public Void FridgeIsEmpty(int fridgeID) throws AvroRemoteException {
		// TODO Auto-generated method stub
		//needs to inform users somehow.
		return null;
	}
}
