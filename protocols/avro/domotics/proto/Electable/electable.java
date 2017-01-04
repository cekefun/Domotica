/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package avro.domotics.proto.Electable;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface electable {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"electable\",\"namespace\":\"avro.domotics.proto.Electable\",\"types\":[],\"messages\":{\"_sync\":{\"request\":[{\"name\":\"Clients\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":\"int\"}}},{\"name\":\"Users\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"map\",\"values\":\"boolean\"}}}],\"response\":\"null\"},\"election\":{\"request\":[{\"name\":\"OwnID\",\"type\":\"int\"}],\"response\":\"boolean\"},\"elected\":{\"request\":[{\"name\":\"OwnID\",\"type\":\"int\"},{\"name\":\"NextID\",\"type\":\"int\"}],\"response\":\"boolean\"}}}");
  java.lang.Void _sync(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> Clients, java.util.Map<java.lang.CharSequence,java.util.Map<java.lang.CharSequence,java.lang.Boolean>> Users) throws org.apache.avro.AvroRemoteException;
  boolean election(int OwnID) throws org.apache.avro.AvroRemoteException;
  boolean elected(int OwnID, int NextID) throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends electable {
    public static final org.apache.avro.Protocol PROTOCOL = avro.domotics.proto.Electable.electable.PROTOCOL;
    void _sync(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> Clients, java.util.Map<java.lang.CharSequence,java.util.Map<java.lang.CharSequence,java.lang.Boolean>> Users, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void election(int OwnID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void elected(int OwnID, int NextID, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
  }
}