
public class KerberosSystem 
{
	private AuthenticationServer as;
	private TicketGrantingServer tgs;
	private Server server;
	private Client client;
	
	public KerberosSystem() 
	{
		showWelcomeText();
		createAS();
		createTGS();
		createServer();
	}
	
	private void showWelcomeText()
	{
		System.out.println("Welcome to this simulation of a basic Kerberos system!");
	}
	
	private void createAS()
	{
		System.out.println("\n>>> STEP 1: CREATE AUTHENTICATION SERVER (AS) AND ADD CLIENTS TO DATABASE <<<");
		as = new AuthenticationServer();
	}
	
	private void createTGS()
	{
		tgs = new TicketGrantingServer();
	}
	
	private void createServer()
	{
		server = new Server();
	}
}
