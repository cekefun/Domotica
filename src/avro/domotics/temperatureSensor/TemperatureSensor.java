package avro.domotics.temperatureSensor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.util.Random;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import avro.domotics.SimpleClient;
import avro.domotics.proto.Electable.electable;
import avro.domotics.proto.thermostat.thermostat;
import avro.domotics.util.NetAddress;

	public class TemperatureSensor extends SimpleClient implements thermostat {
	public double temperature;
	private NetAddress SensorID;
	private Integer serverID;
	private String serverIP;
	private Thread PingingServer;
	private Random rand = new Random();
	
	public TemperatureSensor(NetAddress ServerAddr, NetAddress MyAddr){
		this.SensorID = MyAddr;
		this.serverID = ServerAddr.getPort();
		this.serverIP = ServerAddr.getIPStr();
		this.PingingServer = new Thread(new clientpinger(this));
		
	}

	public void run(NetAddress serverAddress){
		NetAddress ID = this.getAddress();
		try{
			Transceiver client = new SaslSocketTransceiver(new InetSocketAddress(serverAddress.getIP(),serverAddress.getPort()));
			electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
			ID.setPort(proxy.ConnectThermostat(ID.getPort(),ID.getIPStr()));
			client.close();
		} catch(IOException e){
			System.err.println("Error connecting to server");
			e.printStackTrace(System.err);
			System.exit(1);
		}

		System.out.println("You have ID: "+Integer.toString(this.getAddress().getPort()));
		serverRunning = new Thread(new ServerThread(this.getAddress(),this));
		serverRunning.start();
		
	}
	
	public class clientpinger implements Runnable{
		TemperatureSensor ptr;
		boolean stop  = false;
		long sleeper = 3000;

		
		public clientpinger(TemperatureSensor owner){
			ptr = owner;
			//log("hello");
			ptr.temperature = ((rand.nextDouble() + 0.5) * 10) + 10;
		}
		
		public void run(){
			//ptr.CurrentuserID;
			while(! stop){

				try{
					
				Thread.sleep(sleeper);
				
				}
				
				catch(InterruptedException e){}
				Transceiver client = null;
				try{
					//log("update: " + ptr.serverIP + ":" + ptr.serverID);
					client = new SaslSocketTransceiver(new InetSocketAddress(ptr.serverIP,ptr.serverID));
					electable proxy = (electable) SpecificRequestor.getClient(electable.class, client);
					if(rand.nextDouble()> 0.5){
						temperature = temperature + (rand.nextDouble());
					}
					else{
						temperature = temperature - (rand.nextDouble());
					}
					DecimalFormat df = new DecimalFormat("#.##");
					System.out.println(df.format(temperature));
					//log("temperature: " + temperature);
					proxy.UpdateTemperature(temperature);
				}
				catch(IOException e){
					//log("Ioexception thermostat pinging"+ e);
				}
				finally{
					try {
						client.close();
					} catch (IOException|NullPointerException e) {}
				}
			}
		}
		
	}
	
	@Override
	public double GetTemperature() throws AvroRemoteException{
		return temperature;
	}
	

	

	
	public static void main(String[] args){
		clientinfo info = mainstart("thermostat",args);
		
		TemperatureSensor ThisSensor = new TemperatureSensor(info.serverAddr, info.MyAddr);
		/*ThisSensor.SensorID = info.MyAddr;
		ThisSensor.serverID= info.serverAddr.getPort();
		ThisSensor.serverIP = info.serverAddr.getIPStr();*/
		ThisSensor.run(info.serverAddr);
		ThisSensor.PingingServer.start();
		while(true){
			int input = 0;
			try{
				input = System.in.read();
			} catch(Exception e){
				
			}
			if (input =='e'){ 
				ThisSensor.stop();
				break;
			}
		}
		
	}



	@Override
	public int getID() {
		return SensorID.getPort();
	}

	@Override
	public String getName() {
		return "thermostat";
	}

	@Override
	public NetAddress getAddress() {
		return SensorID;
	}

	@Override
	public boolean IsAlive(CharSequence IPaddr, int ID) throws AvroRemoteException {
		serverID = ID;
		serverIP = IPaddr.toString();
		//log("newID: " + ID + ":" + IPaddr.toString());
		return true;
	}

	@Override
	public Class getClientClass() {
		return thermostat.class;
	}
}
