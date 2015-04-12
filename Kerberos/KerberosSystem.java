
public class KerberosSystem 
{
	private AuthenticationServer AS;
	private TicketGrantingServer TGS;
	private Server server;
	private Client client;
	
	public KerberosSystem() 
	{
		ShowWelcomeText();
		CreateAS();
		CreateTGS();
		CreateServer();
	}
	
	private void ShowWelcomeText()
	{
		String s = "Welcome to this simulation of a basic Kerberos system!";
		System.out.println(s);
	}
	
	private void CreateAS()
	{
		System.out.println("\n>>> STEP 1: CREATE AUTHENTICATION SERVER (AS) AND ADD CLIENTS TO DATABASE <<<");
		AS = new AuthenticationServer();
	}
	
	private void CreateTGS()
	{
		TGS = new TicketGrantingServer();
	}
	
	private void CreateServer()
	{
		server = new Server();
	}
}
