/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package avro.domotics.proto.Electable;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface electable {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"electable\",\"namespace\":\"avro.domotics.proto.Electable\",\"types\":[],\"messages\":{\"_sync\":{\"request\":[{\"name\":\"Clients\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":\"int\"}}},{\"name\":\"Users\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"map\",\"values\":\"boolean\"}}},{\"name\":\"addresses\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"savedLights\",\"type\":{\"type\":\"array\",\"items\":\"int\"}}],\"response\":\"null\"},\"election\":{\"request\":[{\"name\":\"OwnID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"elected\":{\"request\":[{\"name\":\"OwnID\",\"type\":\"int\"},{\"name\":\"NextID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"ConnectUser\":{\"request\":[{\"name\":\"username\",\"type\":\"string\"},{\"name\":\"IP\",\"type\":\"string\"}],\"response\":\"int\"},\"ConnectLight\":{\"request\":[{\"name\":\"LightID\",\"type\":\"int\"},{\"name\":\"IP\",\"type\":\"string\"}],\"response\":\"int\"},\"ConnectFridge\":{\"request\":[{\"name\":\"FridgeID\",\"type\":\"int\"},{\"name\":\"IP\",\"type\":\"string\"}],\"response\":\"int\"},\"Switch\":{\"request\":[{\"name\":\"lightID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"GetClients\":{\"request\":[],\"response\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":\"int\"}}},\"GetLights\":{\"request\":[],\"response\":{\"type\":\"map\",\"values\":\"boolean\"}},\"GetServers\":{\"request\":[],\"response\":{\"type\":\"map\",\"values\":\"boolean\"}},\"GetUsers\":{\"request\":[],\"response\":{\"type\":\"map\",\"values\":\"boolean\"}},\"LeaveHouse\":{\"request\":[{\"name\":\"userID\",\"type\":\"int\"}],\"response\":\"null\"},\"GetFridges\":{\"request\":[],\"response\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":\"string\"}}},\"ConnectUserToFridge\":{\"request\":[{\"name\":\"username\",\"type\":\"int\"},{\"name\":\"fridgeID\",\"type\":\"int\"}],\"response\":\"string\"},\"FridgeIsEmpty\":{\"request\":[{\"name\":\"fridgeID\",\"type\":\"int\"}],\"response\":\"null\"},\"IsAlive\":{\"request\":[],\"response\":\"boolean\"}}}");
  java.lang.Void _sync(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> Clients, java.util.Map<java.lang.CharSequence,java.util.Map<java.lang.CharSequence,java.lang.Boolean>> Users, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> addresses, java.util.List<java.lang.Integer> savedLights) throws org.apache.avro.AvroRemoteException;
  boolean election(int OwnID) throws org.apache.avro.AvroRemoteException;
  boolean elected(int OwnID, int NextID) throws org.apache.avro.AvroRemoteException;
  int ConnectUser(java.lang.CharSequence username, java.lang.CharSequence IP) throws org.apache.avro.AvroRemoteException;
  int ConnectLight(int LightID, java.lang.CharSequence IP) throws org.apache.avro.AvroRemoteException;
  int ConnectFridge(int FridgeID, java.lang.CharSequence IP) throws org.apache.avro.AvroRemoteException;
  boolean Switch(int lightID) throws org.apache.avro.AvroRemoteException;
  java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> GetClients() throws org.apache.avro.AvroRemoteException;
  java.util.Map<java.lang.CharSequence,java.lang.Boolean> GetLights() throws org.apache.avro.AvroRemoteException;
  java.util.Map<java.lang.CharSequence,java.lang.Boolean> GetServers() throws org.apache.avro.AvroRemoteException;
  java.util.Map<java.lang.CharSequence,java.lang.Boolean> GetUsers() throws org.apache.avro.AvroRemoteException;
  java.lang.Void LeaveHouse(int userID) throws org.apache.avro.AvroRemoteException;
  java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>> GetFridges() throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence ConnectUserToFridge(int username, int fridgeID) throws org.apache.avro.AvroRemoteException;
  java.lang.Void FridgeIsEmpty(int fridgeID) throws org.apache.avro.AvroRemoteException;
  boolean IsAlive() throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends electable {
    public static final org.apache.avro.Protocol PROTOCOL = avro.domotics.proto.Electable.electable.PROTOCOL;
    void _sync(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> Clients, java.util.Map<java.lang.CharSequence,java.util.Map<java.lang.CharSequence,java.lang.Boolean>> Users, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> addresses, java.util.List<java.lang.Integer> savedLights, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void election(int OwnID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void elected(int OwnID, int NextID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void ConnectUser(java.lang.CharSequence username, java.lang.CharSequence IP, org.apache.avro.ipc.Callback<java.lang.Integer> callback) throws java.io.IOException;
    void ConnectLight(int LightID, java.lang.CharSequence IP, org.apache.avro.ipc.Callback<java.lang.Integer> callback) throws java.io.IOException;
    void ConnectFridge(int FridgeID, java.lang.CharSequence IP, org.apache.avro.ipc.Callback<java.lang.Integer> callback) throws java.io.IOException;
    void Switch(int lightID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void GetClients(org.apache.avro.ipc.Callback<java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>>> callback) throws java.io.IOException;
    void GetLights(org.apache.avro.ipc.Callback<java.util.Map<java.lang.CharSequence,java.lang.Boolean>> callback) throws java.io.IOException;
    void GetServers(org.apache.avro.ipc.Callback<java.util.Map<java.lang.CharSequence,java.lang.Boolean>> callback) throws java.io.IOException;
    void GetUsers(org.apache.avro.ipc.Callback<java.util.Map<java.lang.CharSequence,java.lang.Boolean>> callback) throws java.io.IOException;
    void LeaveHouse(int userID, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void GetFridges(org.apache.avro.ipc.Callback<java.util.Map<java.lang.CharSequence,java.util.List<java.lang.CharSequence>>> callback) throws java.io.IOException;
    void ConnectUserToFridge(int username, int fridgeID, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void FridgeIsEmpty(int fridgeID, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void IsAlive(org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
  }
}