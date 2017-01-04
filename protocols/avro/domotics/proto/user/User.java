/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package avro.domotics.proto.user;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface User {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"User\",\"namespace\":\"avro.domotics.proto.user\",\"types\":[],\"messages\":{\"IsAlive\":{\"request\":[],\"response\":\"boolean\"},\"_sync\":{\"request\":[{\"name\":\"Clients\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":\"int\"}}},{\"name\":\"Users\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"map\",\"values\":\"boolean\"}}}],\"response\":\"null\"}}}");
  boolean IsAlive() throws org.apache.avro.AvroRemoteException;
  java.lang.Void _sync(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> Clients, java.util.Map<java.lang.CharSequence,java.util.Map<java.lang.CharSequence,java.lang.Boolean>> Users) throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends User {
    public static final org.apache.avro.Protocol PROTOCOL = avro.domotics.proto.user.User.PROTOCOL;
    void IsAlive(org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void _sync(java.util.Map<java.lang.CharSequence,java.util.List<java.lang.Integer>> Clients, java.util.Map<java.lang.CharSequence,java.util.Map<java.lang.CharSequence,java.lang.Boolean>> Users, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
  }
}