package client;

public class ServerInfo {
	private String address;
	private String sn;
	private int port;
	
	public ServerInfo(String address, String sn, int port) {
		super();
		this.address = address;
		this.sn = sn;
		this.port = port;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "ServerInfo [sn=" + sn + ", port=" + port + ", address=" + address + "]";
	}

}
