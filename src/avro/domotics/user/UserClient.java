package avro.domotics.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import avro.domotics.Electable;
import avro.domotics.proto.Electable.electable;
import avro.domotics.proto.smartFridge.fridge;
import avro.domotics.proto.user.User;

import org.apache.avro.AvroRemoteException;
import asg.cliche.Command;
import asg.cliche.ShellFactory;


public class UserClient extends Electable implements User{
	private Server server = null;
	private Integer ServerID = 6789;
	private Integer SelfID = 6789;
	private String Name = "Foo";
	private Integer OpenFridgeID = null;
	private Thread serverThread = null;
	//Map<String,Set<Integer> > clients = null;
	//Map<Integer,SimpleEntry<CharSequence,Boolean>> users = null;
	
	UserClient(Integer server, String name){
		server = ServerID;
		Name = name;
	}

	/*public void _Sync(Map<String,Set<Integer> > _clients,Map<Integer,SimpleEntry<CharSequence,Boolean>> _users ){
		clients = new HashMap<String,Set<Integer> >(_clients);// _clients.clone();
		users = new HashMap<Integer,SimpleEntry<CharSequence,Boolean>>(_users);//_users.clone();
	}*/
	public int getID(){
		return SelfID;
	}
	public String getName(){
		return "UserClient";
	}
	
	private static class NullOutputStream extends OutputStream {
		public void write(int b){
			return;
		}
		public void write(byte[]b){
			return;
		}
		public void write(byte[]b, int off, int len){
			return;
		}
		
		public NullOutputStream(){
			
		}
	}
	
	public class RunServer implements Runnable{
		UserClient ptr = null;
		public RunServer(UserClient above){
			ptr= above;
		}
		public void run(){
			try{
				server = new SaslSocketServer(new SpecificResponder(User.class, ptr),new InetSocketAddress(SelfID));
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
	
	public void stop(){
		log("stop");
		server.close();
	}
	
	@Command
	public void EnterHouse(){
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			SelfID = proxy.ConnectUser(Name);
			client.close();
		} catch(Exception e){
			System.out.println("Could not connect to the server");
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			return;
		} 
		serverThread = new Thread(new RunServer(this));
		serverThread.start();	
	}
	
	@Command
	public void LeaveHouse(){
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			proxy.LeaveHouse(Name);
			client.close();
		} catch (IOException e){
			//Cannot connect to server so things should happen
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		serverThread.interrupt();
		serverThread = null;
		server = null;
		
	}
	
	@Command
	public String SwitchLight(int ID){
		if (server == null){
			return "You are not connected";
		}
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			proxy.Switch(ID);
			client.close();
		} catch(AvroRemoteException e){
			if(e.getValue() == "Exist"){
				System.out.println("[error]: This light does not exist");
			}
			if(e.getValue() == "Connect"){
				System.out.println("[error]: Could not connect to that light");
			}
			System.out.println("Something");
			System.err.println(e.getMessage());
			
		} catch(IOException e){
			System.err.println("Error connecting to server");
		} catch(Exception e){
			return "Problem connecting to that light.";
		}
		return null;		
	}
	
	@Command
	public String getClients(){
		
		if (server == null){
			return "You are not connected";
		}
		String result = "";
		result += getServers();
		result += getUsers();
		result += getLights();
		result += getFridges();
		
		
		/*
		Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			AllClients = proxy.GetClients();
			client.close();
		} catch(IOException e){
			System.out.println("Could not connect to the server");
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
		}
		String result = "";
		for (CharSequence Key: AllClients.keySet()){
			for (Integer value: AllClients.get(Key)){
				result += Key.toString() + '\t' + value.toString() + '\n';
			}
		}
		*/
		return result;
	}
	
	@Command
	public String getServers(){
		if (server == null){
			return "You are not connected";
		}
		String result = "";
		Map<CharSequence, Boolean> Servers = new HashMap<CharSequence,Boolean>();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			Servers = proxy.GetServers();
			client.close();
		} catch(IOException e){
			System.out.println("Could not connect to the server");
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
		}
		for(CharSequence ID: Servers.keySet()){
			result +="Server"+'\t'+  ID.toString()+'\t';
			if (Servers.get(ID)){
				result += "CONNECTED";
			}
			else{
				result += "DISCONNECTED";
			}
			result += '\n';
		}
		return result;
	}
	
	@Command
	public String getUsers(){
		if (server == null){
			return "You are not connected";
		}
		String result = "";
		Map<CharSequence, Boolean> Users = new HashMap<CharSequence,Boolean>();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			Users = proxy.GetUsers();
			client.close();
		} catch(IOException e){
			System.out.println("Could not connect to the server");
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
		}
		for(CharSequence ID: Users.keySet()){
			result +="User"+'\t'+  ID.toString()+'\t';
			if (Users.get(ID)){
				result += "INSIDE";
			}
			else{
				result += "OUTSIDE";
			}
			result += '\n';
		}
		return result;
	}
	
	@Command
	public String getLights(){
		if (server == null){
			return "You are not connected";
		}
		Map<CharSequence, Boolean> lights = new HashMap<CharSequence,Boolean>();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			lights = proxy.GetLights();
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		String result = "";
		for (CharSequence Key: lights.keySet()){
			result  +="Light"+'\t'+ Key.toString() + '\t';
			if (lights.get(Key)){
				result += "ON\n";
			}
			else{
				result += "OFF\n";
			}
		}
		return result;
	}
	
	@Command
	public String hello(){
		return "Hello World";
	}
	
	@Command
	public String getFridges(){
		if (server == null){
			System.out.println("You are not connected");
			return null;
		}
		log("Getting fridges");
		String result = "";
		Map<CharSequence, List<CharSequence>> fridges = new HashMap<CharSequence, List<CharSequence>>();// = new List<Integer>();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			fridges = proxy.GetFridges();
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		
		for (CharSequence ID: fridges.keySet()){
			result += "Fridge"+'\t'+ID+'\t';
			if(fridges.get(ID) == null || fridges.get(ID).isEmpty()){
				result += "EMTPY";
			}
			else{
				result += fridges.get(ID).toString();
			}
			result += '\n';
		}
		return result;
	}
	
	@Command
	public Void openFridge(int fridgeID){
		if (server == null){
			System.out.println("You are not connected");
			return null;
		}
		//List<Integer> fridges = new Vector<Integer>();// = new List<Integer>();
		boolean success = false;
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			success = proxy.ConnectUserToFridge(SelfID, fridgeID);
			client.close();
			this.OpenFridgeID = fridgeID;
		} catch(IOException e){
			System.err.println("Error connecting to Fridge");
			e.printStackTrace(System.err);
		}
		if (!success){
			System.out.println("Could not open the fridge");
		}
		return null;
	}
	
	@Command
	public Void AddItemToFridge(String item){
		if (server == null){
			System.out.println("You are not connected");
			return null;
		}
		//List<Integer> fridges = new Vector<Integer>();// = new List<Integer>();
		if(this.OpenFridgeID != null){
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(this.OpenFridgeID));
				fridge proxy = (fridge) SpecificRequestor.getClient(fridge.class, client);
				proxy.AddItem(SelfID, item);
				client.close();
			} catch(IOException e){
				System.err.println("Error connecting to Fridge");
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}
		return null;
	}
	
	@Command
	public Void RemoveItemFromFridge(String item){
		if (server == null){
			System.out.println("You are not connected");
			return null;
		}
		//List<Integer> fridges = new Vector<Integer>();// = new List<Integer>();
		if(this.OpenFridgeID != null){
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(this.OpenFridgeID));
				fridge proxy = (fridge) SpecificRequestor.getClient(fridge.class, client);
				proxy.RemoveItem(SelfID, item);
				client.close();
			} catch(IOException e){
				System.err.println("Error connecting to Fridge");
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}
		return null;
	}
	
	@Command
	public Void CloseFridge(){
		if (server == null){
			System.out.println("You are not connected");
			return null;
		}
		//List<Integer> fridges = new Vector<Integer>();// = new List<Integer>();
		if(this.OpenFridgeID != null){
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(this.OpenFridgeID));
				fridge proxy = (fridge) SpecificRequestor.getClient(fridge.class, client);
				proxy.CloseFridge(SelfID);
				client.close();
				this.OpenFridgeID = null;
			} catch(IOException e){
				System.err.println("Error connecting to Fridge");
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}
		return null;
	}
	
	public static void main(String[] args){
		System.setErr(new PrintStream(new NullOutputStream()));
		Integer ServerID = 6789;
		String Name = "Bob";

		if (args.length>0){
			ServerID = Integer.valueOf(args[0]);
		}
		if (args.length>1){
			Name = args[1];
		}
		
		UserClient myUser = new UserClient(ServerID,Name);
		myUser.standby();
		
		try{
			ShellFactory.createConsoleShell(myUser.Name, "Domotics User", myUser).commandLoop();
		} catch(IOException e){
			System.exit(1);
		}
	}

}
