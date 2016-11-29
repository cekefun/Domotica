package avro.domotics.user;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import avro.domotics.proto.server.DomServer;

public class User {
	
	private static void printMapStrInt(Map<CharSequence, List<Integer>> map){
		for (CharSequence Key: map.keySet()){
			for (Integer value: map.get(Key)){
				String line = Key.toString() + '\t' + value.toString();
				System.out.println(line);
			}
		}
	}
	
	private static void printMapStrBool(Map<CharSequence,Boolean> map){
		for (CharSequence Key: map.keySet()){
			String line = Key.toString() + '\t' + map.get(Key).toString();
			System.out.println(line);
		}
	}
	
	public static void main(String[] args){
		int ServerID = 6789;
		if (args.length>0){
			ServerID = Integer.valueOf(args[0]);
		}
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
		
		printMapStrInt(AllClients);
		
		
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
		
		printMapStrBool(lights);
		
		Integer LightID = 0;
		Scanner s = new Scanner(System.in);
		LightID = s.nextInt();
		s.close();
		
		System.out.println(LightID.toString());
		
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(ServerID));
			DomServer proxy = (DomServer) SpecificRequestor.getClient(DomServer.class, client);
			proxy.Switch(LightID);
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		
	}
}
