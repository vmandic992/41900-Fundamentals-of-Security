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
		String s = "Server [RSA Key Generator] \n\n";
		//include keys/IVs
		return s;
	}
	
	public String getName()
	{
		return serverName;
	}
	
}
