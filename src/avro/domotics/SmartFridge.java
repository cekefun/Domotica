package avro.domotics.smartfridge;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import asg.cliche.Command;
import asg.cliche.ShellFactory;
import avro.domotics.lights.client.LightClient;
import avro.domotics.proto.lights.Lights;
import avro.domotics.proto.server.DomServer;


public class SmartFridge {
	protected Server server = null;
	protected Integer ServerID = 6789;
	protected Integer SelfID = 6789;
	protected String Name = "Frosty";
	protected Vector<String> contents;
	protected boolean Open;
	protected Integer CurrentuserID = null;
	//protected RunServer serverThread = new RunServer();
	
	SmartFridge(Integer server, String name){
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
	/*
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
	}*/
	@Command
	public Vector<String> getContents(){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		Vector<String> allContents = this.contents;
	
		return allContents;
	}
	@Command
	public Boolean OpenFridge(int UserID){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.Open == false){
			this.Open = true;
		}
		else{
			return false;
		}
		
		this.CurrentuserID = UserID;
		return true;
	}
	@Command
	public Void AddItem(int UserID, String item){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID == UserID){
			this.contents.add(item);
		}
		return null;
	}
	@Command
	public Boolean RemoveItem(int UserID, String item){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID == UserID){
			for(int i = 0;  i < this.contents.size(); i++){
				if(item == this.contents.get(i)){
					this.contents.remove(i);
						if(this.contents.isEmpty()){
							//send message to controller
							try{
								Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
								DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
								proxy.FridgeIsEmpty(SelfID);
								client.close();
							} catch(IOException e){
								System.err.println("Error connecting to server");
								e.printStackTrace(System.err);
								System.exit(1);
							}
						}
						return true;
				}
			}
		}
		return false;
	}
	@Command
	public Void CloseFridge(int UserID){
		//Map<CharSequence, List<Integer>> AllClients = new HashMap<CharSequence, List<Integer>>();
		//Vector<String> allContents = this.contents;
		if(this.CurrentuserID == UserID){
			this.Open= false;
			this.CurrentuserID = null;
		}
		return null;
	}

	public static void main(String[] args){
		System.setErr(new PrintStream(new NullOutputStream()));
		Integer ServerID = 6789;
		String Name = "Frosty";

		if (args.length>0){
			ServerID = Integer.valueOf(args[0]);
		}
		if (args.length>1){
			Name = args[1];
		}
		
		SmartFridge fridge = new SmartFridge(ServerID,Name);
		
		try{
			ShellFactory.createConsoleShell(fridge.Name, "Domotics User", fridge).commandLoop();
		} catch(IOException e){
			System.exit(1);
		}
	}
}