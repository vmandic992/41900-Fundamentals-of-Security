import java.util.*;

public class KerberosSystem 
{
	private AuthenticationServer AS;
	private TicketGrantingServer TGS;
	private Server server;
	private Client client;
	private Scanner scanner = new Scanner(System.in);
	
	public KerberosSystem() 
	{
		showWelcomeText();
		createAS();
		createTGS();
		createServer();
		startKerberos();
	}
	
	private void showWelcomeText()
	{
		System.out.println("Welcome to this simulation of a basic Kerberos system!");
	}
	
	private void createAS()
	{
		System.out.println("\n>>> STEP 1: CREATE AUTHENTICATION SERVER (AS) AND ADD CLIENTS TO DATABASE <<<");
		AS = new AuthenticationServer();
	}
	
	private void createTGS()
	{
		TGS = new TicketGrantingServer();
	}
	
	private void createServer()
	{
		server = new Server();
	}
	
	private void startKerberos()
	{
		//client = AS.lookUpClient(getClientName());
		/*while (client == null)
		{
			System.out.println("Error: Client does not exist \n");
			client = AS.lookUpClient(getClientName());
		}
		AS.generateTicket();*/
	}
	/*
	private String getClientName()
	{
		System.out.print("Enter client username: ");
		return scanner.nextLine();
	}*/
}
