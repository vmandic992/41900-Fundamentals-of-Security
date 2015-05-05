public class Server 
{
	private String serverName;
	
	private String keyServerTGS = "123456789ABCDEFGHIJKL";	//will be randomly generated
	private String ivServerTGS = "12345678"; //will be randomly generated
	
	private String keyClientServer; //will be randomly generated
	private String ivClientServer; //will be randomly generated
	
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
		s +=       " - Acts as a network resource to Clients." + "\n\n";
		s +=       " - Name:    " + serverName + "\n";
		s +=       " - Service: " + "RSA 512-bit key generator" + "\n\n";
		s +=	   " - Also configured with a Key and IV to use with the TGS." + "\n";
		s +=	   "    > S/TGS-KEY: " + keyServerTGS + "\n";
		s +=	   "    > S/TGS-IV:  " + ivServerTGS + "\n\n";
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
