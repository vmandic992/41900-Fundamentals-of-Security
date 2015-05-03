import java.io.IOException;
import java.util.*;

public class KerberosSystem 
{
	public AuthenticationServer AS;
	public TicketGrantingServer TGS;
	public Server server;
	public Client client;
	
	private String blockCipherMode;
	
	private Scanner scanner = new Scanner(System.in);
	
	
	public KerberosSystem() throws IOException 
	{
		showWelcomeText();
		
		setBlockCipherMode();
		createServersAndClient();
		
		startKerberos();
	}
	
	private void showWelcomeText()
	{
		String s = "Welcome to this simulation of a basic Kerberos system! \n\n";
		s += "By Vedran Mandic, Jason D'Souza, Andrew Scott & Jeremiah Cruz \n\n";
		System.out.println(s);
	}
	
	private void setBlockCipherMode()
	{
		String s = "This code uses 168-bit TripleDES encryption, with 64-bit blocks. \n";
		s += "Please enter the block-cipher mode to be used (ECB[default] or CBC): ";
		blockCipherMode = getEncryptionMode(s);
	}
	
	private String getEncryptionMode(String prompt)
	{
		System.out.print(prompt);
		String mode = scanner.nextLine();
		while (!(mode.equalsIgnoreCase("ECB") || mode.equalsIgnoreCase("CBC")))
		{
			System.out.print("\n INVALID mode, enter 'CBC' or 'ECB': ");
			mode = scanner.nextLine();
		}
		return mode.toUpperCase();
	}
	
	private void createServersAndClient()
	{
		printStepOne();
		
		AS = new AuthenticationServer(blockCipherMode, this);
		TGS = new TicketGrantingServer(blockCipherMode, this);
		server = new Server(blockCipherMode, this);
		
		client = new Client("Andrew Scott", "Yippee ki-yay mthrfkr", blockCipherMode, this);
	}
	
	private void startKerberos() throws IOException
	{
		printStepTwo();
		client.sendRequestToAS();
	}
	
	private void printStepOne()
	{
		String s = "\n=================================================================";
		s += 	   "\n>>> STEP 1: CREATE THE 4 ENTITIES: Client, TGS, AS and Server <<<";
		s +=       "\n=================================================================\n";
		System.out.println(s);
	}
	
	private void printStepTwo()
	{
		String s = "\n==============================================================================";
		s += 	   "\n>>> STEP 2: CLIENT TRANSMITS PLAINTEXT REQUEST TO AS, TO ACCESS THE SERVER <<<";
		s +=       "\n==============================================================================";
		System.out.println(s);
	}
}
