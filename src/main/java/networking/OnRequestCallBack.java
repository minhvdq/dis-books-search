package networking;

public interface OnRequestCallBack {
    byte[] handleRequest (byte[] inputByte);
    String getEndpoint();

}
