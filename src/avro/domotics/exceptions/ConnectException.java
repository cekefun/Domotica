package avro.domotics.exceptions;

import org.apache.avro.AvroRemoteException;

public class ConnectException extends AvroRemoteException {
	private static final long serialVersionUID = 1L;
	private Integer port;
	public ConnectException(int ID){
		port = ID;
	}
	
	public String what(){
		return "Couldn't connect to port "+port.toString();
	}
}
