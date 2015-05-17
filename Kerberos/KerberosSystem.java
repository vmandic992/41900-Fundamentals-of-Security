import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import javax.jws.WebParam.Mode;

public class KerberosSystem 
{
	private Scanner scanner = new Scanner(System.in);
	
	//Servers and Client
	public AuthenticationServer AS;
	public TicketGrantingServer TGS;
	public LinkedList<Server> servers = new LinkedList<Server>(); //will only hold 1 server (RSA key generator)
	public Client client;										  //used to hold the simulated client
	public Hacker hacker;
	public boolean includeAttackInSimulation = false;
	
	//Holds the chosen block-cipher mode (ECB or CBC)
	private String blockCipherMode;
	
	//If both are true, then the client and server can start communicating
	public boolean clientHasKey = false;
	public boolean serverHasKey = false;
	
	
	public KerberosSystem() throws IOException, ParseException 
	{
		showWelcomeText();				//introduce program
		displayCaptureFiles();			//display the text file names which will capture encryption/decryption output
		
		pauseSimulation();				//STOP, let the user read
		setBlockCipherMode();			//prompt the user to enter ECB or CBC for block-cipher mode
		
		setSimulationMode();			//ask if the user wants to see the simulation with an attack
		
		createServersAndClient();		//create entities
		startKerberos();				//everything's setup, now start Kerberos
	}
	
	//Introduces the program
	private void showWelcomeText()
	{
		String s = "Welcome to this simulation of a basic Kerberos system! \n\n";
		s += "By Vedran Mandic, Jason D'Souza, Andrew Scott & Jeremiah Cruz \n\n";
		System.out.println(s);
	}
	
	//Prints all the text files used for capturing all operations of encryption & decryption
	//Each time an encryption or decryption occurs, a separate file is used
	//This lets the viewer choose which specific encryption or decryption they want to analyze
	private void displayCaptureFiles()
	{
		String s = "All ENCRYPTION and DECRYPTION operations are captured to output text files: \n\n";
		s +=	   " 1.  'AS_Encrypt_To_Client'        ---   When AS encrypts Ticket and TGS Key \n";
		s +=	   " 2.  'Client_Decrypt_From_AS'      ---   When Client decrypts Ticket and TGS Key \n";
		s +=	   " 3.  'Client_Encrypt_To_TGS'       ---   When Client encrypts Timestamp \n";
		s += 	   " 4.  'TGS_Decrypt_From_Client'     ---   When TGS decrypts Timestamp \n";
		s +=	   " 5.  'TGS_Encrypt_To_Client'       ---   When TGS encrypts Client/Server Session Key (for Client) \n";
		s +=	   " 6.  'TGS_Encrypt_To_Server'       ---   When TGS encrypts Client/Server Session Key (for Server) \n";
		s +=	   " 7.  'Client_Decrypt_From_TGS'     ---   When Client decrypts Client/Server Session Key \n";
		s +=	   " 8.  'Server_Decrypt_From_TGS'     ---   When Server decrypts Client/Server Session Key \n";
		s +=       " 9.  'Client_Encrypt_To_Server'    ---   When Client encrypts RSA key-pair request \n";
		s +=	   " 10. 'Server_Decrypt_From_Client'  ---   When Server decrypts RSA key-pair request \n";
		s +=       " 11. 'Server_Encrypt_To_Client'    ---   When Server encrypts RSA key-pair response \n";
		s +=       " 12. 'Client_Decrypt_From Server'  ---   When Client decrypts RSA key-pair response \n";
		s +=       " 13. 'TGS_Encrypt_To_Hacker'       ---   When TGS encrypts random session key \n\n";

		System.out.println(s);
	}
	
	//Prompts the user to enter which block-cipher mode to use
	private void setBlockCipherMode()
	{
		String s = "\nThis code uses 168-Bit TripleDES encryption, using 64-Bit blocks. \n\n";
		s += "Please decide which 'Block-Cipher Mode' to use: \n";
		s += " - 'ECB': Each block is encrypted independently. \n";
		s += " - 'CBC': Each block is encrypted after being XORed with previous ciphertext block. \n\n";
		s += "NOTE: ECB will always be used between the CLIENT and AS. \n\n";
		s += "Block-Cipher Mode: ('ECB' or 'CBC'): ";
		
		blockCipherMode = getEncryptionMode(s);
	}
	
	//Prompts the user to enter which block-cipher mode to use
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
	
	//Prompts the user to enter 'yes' or 'no' for showing the MITM attack
	private void setSimulationMode()
	{
		System.out.print("\n\nShow MITM attack during simulation? ");
		String input = scanner.nextLine();
		while (!(input.equalsIgnoreCase("YES") || input.equalsIgnoreCase("NO")))
		{
			System.out.print("\n INVALID input, enter 'Yes' or 'No': ");
			input = scanner.nextLine();
		}
		includeAttackInSimulation = input.equalsIgnoreCase("Yes");
	}
	
	private void createServersAndClient()
	{
		printStepOne();
		
		//Create random TGS key and TGS IV
		String keyTGS = new KeyGenerator(21).getKey();
		String ivTGS = new KeyGenerator(8).getKey();
		
		//Create random Server-TGS Key and Server-TGS IV
		String keyServerTGS = new KeyGenerator(21).getKey();
		String ivServerTGS = new KeyGenerator(8).getKey();
		
		//Make the AS, passing in the block-cipher mode, this kerberos object, TGS key and TGS IV
		AS = new AuthenticationServer(blockCipherMode, this, keyTGS, ivTGS);
		pauseSimulation();
		
		//Make the TGS, passing in the block-cipher mode, this kerberos object, TGS key, TGS IV, TGS/Server key and TGS/Server IV
		TGS = new TicketGrantingServer(blockCipherMode, this, keyTGS, ivTGS, keyServerTGS, ivServerTGS);
		pauseSimulation();

		//Make the Server, passing in the block-cipher mode, this kerberos object, TGS/Server key and TGS/Server IV
		servers.add(new Server("RSA-KeyGen-Server", "512-bit RSA key-pair generator", blockCipherMode, this, keyServerTGS, ivServerTGS));
		pauseSimulation();

		//Make a Client, passing in the block-cipher mode, this Kerberos object, the username and password
		client = new Client("Vedran Mandic", "xx_MiamiHotlinePro_xx", blockCipherMode, this);	
		pauseSimulation();	
		
		//If we'e including a man-in-the-middle attack, make a Hacker, passing in a name and this Kerberos object
		if (includeAttackInSimulation)
		{
			hacker = new Hacker("Bob Hack", this);
			pauseSimulation();	
		}
	}
	
	public void pauseSimulation()
	{
		System.out.print("<<< PRESS ENTER TO CONTINUE >>>");
		scanner.nextLine();
	}
	
	
	/*	- This method starts Kerberos by instructing the Client to send a request to the AS
	 */
	private void startKerberos() throws IOException, ParseException
	{
		printStepTwo();
		client.sendRequestToAS();
		
		/* - Once the Client and Server have received a shared session key, instruct the Client to
		 *   send a request to the Server
		 */
		if (sessionIsEstablished())
		{
			printStepTwelve();
			client.requestRSAKeys();
		}
	}
	
	private boolean sessionIsEstablished()
	{
		return (clientHasKey && serverHasKey);
	}
	
	public void abortWithError(String error)
	{
		System.out.println("\n :ALERT > KERBEROS ABORTED \n\n" + error);
	}
	
	
	/*	- Once the normal Kerberos flow is over, show the man-in-the-middle attack (replay attack)
	 */
	public void endSimulation() throws IOException, ParseException
	{
		printEnd();		
		
		if (includeAttackInSimulation)
		{
			pauseSimulation();
			hacker.doReplayAttack();
		}
	}
	
	private void printStepOne()
	{
		String s = "\n=======================================================================================================";
		s += 	   "\n>>> STEP 1: CREATE THE 4 ENTITIES: (1) AS, (2) TGS, (3) SERVER, (4) CLIENT, & (5) HACKER [optional] <<<";
		s +=       "\n=======================================================================================================\n";
		System.out.println(s);
	}
	
	private void printStepTwo()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 2: CLIENT TRANSMITS PLAINTEXT REQUEST TO AS, TO ACCESS THE SERVER <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepThree()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 3: AS RECEIVES REQUEST AND LOOKS UP THE CLIENT'S PASSWORD <<<";
		s +=       "\n===============================================================================================\n";
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
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 5: CLIENT RECEIVES RESPONSE AND DECRYPTS IT WITH THEIR PASSWORD <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepSix()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 6: CLIENT EXTRACTS TICKET AND TGS-KEY & TGS-IV <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepSeven()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 7: CLIENT CREATES MESSAGE TO SEND TO THE TGS <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepEightA()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 8(A): TGS RECEIVES MESSAGE, EXTRACTS THE ELEMENTS, AND DECRYPTS TIMESTAMP <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepEightB()
	{
		String s = "\n==================================================================================================";
		s += 	   "\n>>> STEP 8(B): TGS CHECKS IF TICKET HAS NOT EXPIRED, AND ALSO VALIDATES THE INCLUDED TIMESTAMP <<<";
		s +=       "\n==================================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepNine()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 9: TGS GENERATES CLIENT/SERVER SESSION KEY <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepTen()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 10: TGS ENCRYPTS SESSION KEY AND SENDS IT TO BOTH CLIENT AND SERVER <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepEleven()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 11: CLIENT AND SERVER RECEIVE ENCRYPTED SESSION KEY AND DECRYPT IT INDEPENDENTLY <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepTwelve()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 12: CLIENT SENDS A REQUEST TO THE SERVER FOR A NEW RSA KEY PAIR <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepThirteen()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 13: SERVER GENERATES NEW RSA KEY-PAIR AND SENDS IT TO THE CLIENT <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printStepFourteen()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> STEP 14: CLIENT RECEIVES RSA KEY-PAIR AND DECRYPTS IT <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printEnd()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> END OF KERBEROS SIMULATION - CLIENT AND SERVER CAN NOW COMMUNICATE SECURELY <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
	
	public void printAttack()
	{
		String s = "\n===============================================================================================";
		s += 	   "\n>>> MAN-IN-THE-MIDDLE-ATTACK - HACKER WILL ATTEMPT TO EXECUTE A REPLAY ATTACK <<<";
		s +=       "\n===============================================================================================\n";
		System.out.println(s);
	}
}
