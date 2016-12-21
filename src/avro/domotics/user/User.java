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

import avro.domotics.lights.client.LightClient;
import avro.domotics.proto.lights.Lights;
import avro.domotics.proto.server.DomServer;
import org.apache.avro.AvroRemoteException;
import asg.cliche.Command;
import asg.cliche.ShellFactory;


public class User {
	protected Server server = null;
	protected Integer ServerID = 6789;
	protected Integer SelfID = 6789;
	protected String Name = "Foo";
	protected RunServer serverThread = new RunServer();
	
	User(Integer server, String name){
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
		public void run(){
			try{
				server = new SaslSocketServer(new SpecificResponder(Lights.class, new LightClient()),new InetSocketAddress(SelfID));
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
	
	public void EnterHouse(){
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			SelfID = proxy.ConnectUser(Name);
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		
		Thread serverRunning = new Thread(serverThread);
		serverRunning.start();	
	}
	
	public void LeaveHouse(){
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			proxy.LeaveHouse(Name);
			client.close();
		} catch (IOException e){
			//Cannot connect to server so things should happen
		}
		serverThread.stop();
		
	}
	
	@Command
	public String Switch(int ID){
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
		Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			AllClients = proxy.GetClients();
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
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
		
		User myUser = new User(ServerID,Name);
		
		try{
			ShellFactory.createConsoleShell(myUser.Name, "Domotics User", myUser).commandLoop();
		} catch(IOException e){
			System.exit(1);
		}
	}
}
