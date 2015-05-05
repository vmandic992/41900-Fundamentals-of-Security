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
		
		/*  BEFORE creating AS and TGS
		 *    - Create random TGS-key and TGS-IV
		 *    - Create AS and TGS, passing the TGS-key and TGS-IV into both of them
		 *    
		 *  BEFORE creating TGS and Server
		 *    - Create random TGS/Server-key and TGS/Server-IV
		 *    - Create TGS and Server, passing the TGS/Server-key and TGS/Server-IV into both of them
		 */
		
		AS = new AuthenticationServer(blockCipherMode, this);
		TGS = new TicketGrantingServer(blockCipherMode, this);
		server = new Server("RSA-KeyGen-Server", blockCipherMode, this);
		
		client = new Client("Andrew Scott", "Scotty_22_from_BOX-HQ", blockCipherMode, this);
		
		pauseSimulation();
	}
	
	public void pauseSimulation()
	{
		System.out.print("<<< PRESS ENTER TO CONTINUE >>>");
		String cont = scanner.nextLine();
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
	
	public void printStepThree()
	{
		String s = "\n======================================================================";
		s += 	   "\n>>> STEP 3: AS RECEIVES REQUEST AND LOOKS UP THE CLIENT'S PASSWORD <<<";
		s +=       "\n======================================================================\n";
		System.out.println(s);
	}
	
	public void printStepFour()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 4: AS GENERATES A RESPONSE TO SEND TO THE CLIENT, ENCRYPTED WITH CLIENT'S PASSWORD <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepFive()
	{
		String s = "\n============================================================================";
		s += 	   "\n>>> STEP 5: CLIENT RECEIVES RESPONSE AND DECRYPTS IT WITH THEIR PASSWORD <<<";
		s +=       "\n============================================================================\n";
		System.out.println(s);
	}
	
	public void printStepSix()
	{
		String s = "\n=====================================================";
		s += 	   "\n>>> STEP 6: CLIENT EXTRACTS TICKET AND TGS KEY/IV <<<";
		s +=       "\n=====================================================\n";
		System.out.println(s);
	}
	
	public void printStepSeven()
	{
		String s = "\n=========================================================";
		s += 	   "\n>>> STEP 7: CLIENT CREATES MESSAGE TO SEND TO THE TGS <<<";
		s +=       "\n=========================================================\n";
		System.out.println(s);
	}
	
	public void printStepEight()
	{
		String s = "\n======================================================================================";
		s += 	   "\n>>> STEP 8: TGS RECEIVES MESSAGE, DECRYPTS TIMESTAMP AND VALIDATES THE INFORMATION <<<";
		s +=       "\n======================================================================================\n";
		System.out.println(s);
	}
}
