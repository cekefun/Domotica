/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package avro.domotics.proto.smartFridge;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface fridge {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"fridge\",\"namespace\":\"avro.domotics.proto.smartFridge\",\"types\":[],\"messages\":{\"_sync\":{\"request\":[{\"name\":\"Clients\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":\"int\"}}},{\"name\":\"Users\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"map\",\"values\":\"boolean\"}}},{\"name\":\"addresses\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"savedLights\",\"type\":{\"type\":\"array\",\"items\":\"int\"}}],\"response\":\"null\"},\"election\":{\"request\":[{\"name\":\"OwnID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"elected\":{\"request\":[{\"name\":\"OwnID\",\"type\":\"int\"},{\"name\":\"NextID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"GetContents\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":\"string\"}},\"AddItem\":{\"request\":[{\"name\":\"UserID\",\"type\":\"int\"},{\"name\":\"item\",\"type\":\"string\"}],\"response\":\"null\"},\"RemoveItem\":{\"request\":[{\"name\":\"UserID\",\"type\":\"int\"},{\"name\":\"item\",\"type\":\"string\"}],\"response\":\"null\"},\"ConnectToClient\":{\"request\":[{\"name\":\"ClientID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"IsAlive\":{\"request\":[{\"name\":\"IPaddr\",\"type\":\"string\"},{\"name\":\"ID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"OpenFridge\":{\"request\":[{\"name\":\"ClientID\",\"type\":\"int\"},{\"name\":\"IP\",\"type\":\"string\"}],\"response\":\"boolean\"},\"CloseFridge\":{\"request\":[{\"name\":\"ClientID\",\"type\":\"int\"}],\"response\":\"null\"}}}");
  java.lang.Void _sync(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> Clients, java.util.Map<java.lang.CharSequence,java.util.Map<java.lang.CharSequence,java.lang.Boolean>> Users, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> addresses, java.util.List<java.lang.Integer> savedLights) throws org.apache.avro.AvroRemoteException;
  boolean election(int OwnID) throws org.apache.avro.AvroRemoteException;
  boolean elected(int OwnID, int NextID) throws org.apache.avro.AvroRemoteException;
  java.util.List<java.lang.CharSequence> GetContents() throws org.apache.avro.AvroRemoteException;
  java.lang.Void AddItem(int UserID, java.lang.CharSequence item) throws org.apache.avro.AvroRemoteException;
  java.lang.Void RemoveItem(int UserID, java.lang.CharSequence item) throws org.apache.avro.AvroRemoteException;
  boolean ConnectToClient(int ClientID) throws org.apache.avro.AvroRemoteException;
  boolean IsAlive(java.lang.CharSequence IPaddr, int ID) throws org.apache.avro.AvroRemoteException;
  boolean OpenFridge(int ClientID, java.lang.CharSequence IP) throws org.apache.avro.AvroRemoteException;
  java.lang.Void CloseFridge(int ClientID) throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends fridge {
    public static final org.apache.avro.Protocol PROTOCOL = avro.domotics.proto.smartFridge.fridge.PROTOCOL;
    void _sync(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> Clients, java.util.Map<java.lang.CharSequence,java.util.Map<java.lang.CharSequence,java.lang.Boolean>> Users, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> addresses, java.util.List<java.lang.Integer> savedLights, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void election(int OwnID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void elected(int OwnID, int NextID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void GetContents(org.apache.avro.ipc.Callback<java.util.List<java.lang.CharSequence>> callback) throws java.io.IOException;
    void AddItem(int UserID, java.lang.CharSequence item, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void RemoveItem(int UserID, java.lang.CharSequence item, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void ConnectToClient(int ClientID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void IsAlive(java.lang.CharSequence IPaddr, int ID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void OpenFridge(int ClientID, java.lang.CharSequence IP, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void CloseFridge(int ClientID, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
  }
}