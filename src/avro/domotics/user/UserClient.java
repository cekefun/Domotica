package avro.domotics.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import avro.domotics.proto.server.DomServer;
import avro.domotics.proto.user.User;
import avro.domotics.smartFridge.SmartFridge;

import org.apache.avro.AvroRemoteException;
import asg.cliche.Command;
import asg.cliche.ShellFactory;


public class UserClient  implements User{
	private Server server = null;
	private Integer ServerID = 6789;
	private Integer SelfID = 6789;
	private String Name = "Foo";
	private Integer OpenFridgeID = null;
	private Thread serverThread = null;
	
	UserClient(Integer server, String name){
		server = ServerID;
		Name = name;
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
	
	@Command
	public void EnterHouse(){
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
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
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
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
	public String Switch(int ID){
		if (server == null){
			return "You are not connected";
		}
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			proxy.Switch(ID);
			client.close();
		} catch(AvroRemoteException e){
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
		return result;
	}
	
	@Command
	public String getLights(){
		if (server == null){
			return "You are not connected";
		}
		Map<CharSequence, Boolean> lights = new HashMap<CharSequence,Boolean>();;
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			lights = proxy.GetLights();
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		String result = "";
		for (CharSequence Key: lights.keySet()){
			result  += Key.toString() + '\t';
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
	public List<Integer> getFridges(){
		if (server == null){
			System.out.println("You are not connected");
			return null;
		}
		List<Integer> fridges = new Vector<Integer>();// = new List<Integer>();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			fridges = proxy.GetFridges();
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		return fridges;
	}
	
	@Command
	public Void openFridge(int fridgeID){
		if (server == null){
			System.out.println("You are not connected");
			return null;
		}
		//List<Integer> fridges = new Vector<Integer>();// = new List<Integer>();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(fridgeID));
			SmartFridge proxy = (SmartFridge) SpecificRequestor.getClient(SmartFridge.class, client);
			proxy.OpenFridge(SelfID);
			client.close();
			this.OpenFridgeID = fridgeID;
		} catch(IOException e){
			System.err.println("Error connecting to Fridge");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		return null;
	}
	
	@Command
	public Void AdditemToFridge(String item){
		if (server == null){
			System.out.println("You are not connected");
			return null;
		}
		//List<Integer> fridges = new Vector<Integer>();// = new List<Integer>();
		if(this.OpenFridgeID != null){
			try{
				Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(this.OpenFridgeID));
				SmartFridge proxy = (SmartFridge) SpecificRequestor.getClient(SmartFridge.class, client);
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
				SmartFridge proxy = (SmartFridge) SpecificRequestor.getClient(SmartFridge.class, client);
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
				SmartFridge proxy = (SmartFridge) SpecificRequestor.getClient(SmartFridge.class, client);
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
		
		try{
			ShellFactory.createConsoleShell(myUser.Name, "Domotics User", myUser).commandLoop();
		} catch(IOException e){
			System.exit(1);
		}
	}

	@Override
	public boolean IsAlive() throws AvroRemoteException {
		return true;
	}
}
