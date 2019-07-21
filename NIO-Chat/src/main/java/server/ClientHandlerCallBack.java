package server;

public interface ClientHandlerCallBack{
	void onCloseNotify(ClientHandler handler);
	
	void onReadNotify(ClientHandler handler,String msg);
}