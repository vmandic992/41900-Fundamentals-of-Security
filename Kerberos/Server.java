public class Server 
{
	private String serverName;
	
	private String symmetricKeyServerTGS;
	private String initializationVectorServerTGS;
	
	private String symmetricKeyClientServer;
	private String initializationVectorClientServer;
	
	private String blockCipherMode;
	private KerberosSystem kerberos;

	
	public Server(String serverName, String blockCipherMode, KerberosSystem kerberos) 
	{
		this.serverName = serverName;
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		System.out.println(toString());
	}
	
	
	public String toString()
	{
		String s = "Server ---------------------------------------------\n\n";
		s += " - Name:    " + serverName + "\n";
		s += " - Service: " + "RSA 512-bit key generator" + "\n\n";
		//include keys/IVs
		return s;
	}
	
	
	public String getName()
	{
		return serverName;
	}
	
	
	public void receiveSessionKey(String encryptedKey)
	{
		
	}
	
	
	public String receiveMessage(String cipher)
	{
		return "";
	}
}
