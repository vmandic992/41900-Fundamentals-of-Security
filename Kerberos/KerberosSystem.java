import java.io.IOException;
import java.util.*;

public class KerberosSystem 
{
	public AuthenticationServer AS;
	public TicketGrantingServer TGS;
	public LinkedList<Server> servers = new LinkedList<Server>();
	public Client client;
	
	private String blockCipherMode;
	private Scanner scanner = new Scanner(System.in);
	
	public boolean clientHasKey = false;
	public boolean serverHasKey = false;
	
	
	public KerberosSystem() throws IOException 
	{
		showWelcomeText();
		
		displayCaptureFiles();
		
		pauseSimulation();
		
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
	
	private void displayCaptureFiles()
	{
		String s = "All ENCRYPTION and DECRYPTION operations are captured to output text files: \n\n";
		s +=	   " 1.  'AS_Encrypt_To_Client'        ---   When AS sends ticket & TGS key to Client \n";
		s +=	   " 2.  'Client_Decrypt_From_AS'      ---   When Client decrypts ticket and TGS key from AS \n";
		s +=	   " 3.  'Client_Encrypt_To_TGS'       ---   When Client sends timestamp to TGS \n";
		s += 	   " 4.  'TGS_Decrypt_From_Client'     ---   When TGS decrypts timestamp from Client \n";
		s +=	   " 5.  'TGS_Encrypt_To_Client'       ---   When TGS sends Client/Server key to Client \n";
		s +=	   " 6.  'TGS_Encrypt_To_Server'       ---   When TGS sends Client/Server key to Server \n";
		s +=	   " 7.  'Client_Decrypt_From_TGS'     ---   When Client decrypts Client/Server key from TGS \n";
		s +=	   " 8.  'Server_Decrypt_From_TGS'     ---   When Server decrypts Client/Server key from TGS \n";
		s +=       " 9.  'Client_Encrypt_To_Server'    ---   When Client sends request to Server \n";
		s +=	   " 10.  'Server_Decrypt_From_Client'  ---   When Server decrypts Client request \n";
		s +=       " 11. 'Server_Encrypt_To_Client'    ---   When Server sends RSA keys to Client \n";
		s +=       " 12. 'Client_Decrypt_From Server'  ---   When Client decrypts RSA keys from Server \n\n";
		System.out.println(s);
	}
	
	private void setBlockCipherMode()
	{
		String s = "\nThis code uses 168-bit TripleDES encryption, using 64-bit blocks. \n\n";
		s += "Please decide which 'Block-Cipher Mode' to use: \n";
		s += " - 'ECB': Each block is encrypted independently. \n";
		s += " - 'CBC': Each block is encrypted after being XORed with previous ciphertext block.\n\n";
		s += "NOTE: ECB will always be used between the CLIENT and AS. \n\n";
		s += "Block-Cipher Mode: ('ECB' or 'CBC'): ";
		
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
		servers.add(new Server("RSA-KeyGen-Server", "512-bit RSA key-pair generator", blockCipherMode, this));
		client = new Client("Andrew Scott", "Scotty_22_from_BOX-HQ", blockCipherMode, this);
		
		pauseSimulation();
	}
	
	public void pauseSimulation()
	{
		System.out.print("<<< PRESS ENTER TO CONTINUE >>>");
		scanner.nextLine();
	}
	
	private void startKerberos() throws IOException
	{
		printStepTwo();
		client.sendRequestToAS();
		/*
		if (sessionIsEstablished())
		{
			client.requestRSAKeys();
		}*/
	}
	
	private boolean sessionIsEstablished()
	{
		return (clientHasKey && serverHasKey);
	}
	
	public void abortWithError(String error)
	{
		
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
