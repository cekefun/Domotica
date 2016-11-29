/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package avro.domotics.proto.server;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface DomServer {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"DomServer\",\"namespace\":\"avro.domotics.proto.server\",\"types\":[],\"messages\":{\"ConnectUser\":{\"request\":[{\"name\":\"username\",\"type\":\"string\"}],\"response\":\"int\"},\"ConnectLight\":{\"request\":[{\"name\":\"LightID\",\"type\":\"int\"}],\"response\":\"int\"},\"Switch\":{\"request\":[{\"name\":\"lightID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"GetClients\":{\"request\":[],\"response\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":\"int\"}}},\"GetLights\":{\"request\":[],\"response\":{\"type\":\"map\",\"values\":\"boolean\"}}}}");
  int ConnectUser(java.lang.CharSequence username) throws org.apache.avro.AvroRemoteException;
  int ConnectLight(int LightID) throws org.apache.avro.AvroRemoteException;
  boolean Switch(int lightID) throws org.apache.avro.AvroRemoteException;
  java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> GetClients() throws org.apache.avro.AvroRemoteException;
  java.util.Map<java.lang.CharSequence,java.lang.Boolean> GetLights() throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends DomServer {
    public static final org.apache.avro.Protocol PROTOCOL = avro.domotics.proto.server.DomServer.PROTOCOL;
    void ConnectUser(java.lang.CharSequence username, org.apache.avro.ipc.Callback<java.lang.Integer> callback) throws java.io.IOException;
    void ConnectLight(int LightID, org.apache.avro.ipc.Callback<java.lang.Integer> callback) throws java.io.IOException;
    void Switch(int lightID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void GetClients(org.apache.avro.ipc.Callback<java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>>> callback) throws java.io.IOException;
    void GetLights(org.apache.avro.ipc.Callback<java.util.Map<java.lang.CharSequence,java.lang.Boolean>> callback) throws java.io.IOException;
  }
}