
public class TicketGrantingServer 
{
	private String symmetricKeyTGS;
	private String initializationVectorTGS;

	private String symmetricKeyServerTGS;
	private String initializationVectorServerTGS;
	
	private String blockCipherMode;
	private KerberosSystem kerberos;
	
	public TicketGrantingServer(String blockCipherMode, KerberosSystem kerberos) 
	{
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		System.out.println(toString());
	}
	
	public String toString()
	{
		String s = "Ticket Granting Server \n\n";
		//include keys/IVs
		return s;
	}
}
