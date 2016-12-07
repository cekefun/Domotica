package avro.domotics.exceptions;

import org.apache.avro.AvroRemoteException;

public class ExistException extends AvroRemoteException {
	private static final long serialVersionUID = 1L;
	private Integer port;
	private String type;
	public ExistException(String input,int ID){
		type = input;
		port = ID;
	}
	
	public String getMessage(){
		return "Couldn't find a "+type+" with ID "+port.toString();
	}
}
