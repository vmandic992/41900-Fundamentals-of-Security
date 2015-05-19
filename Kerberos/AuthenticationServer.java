import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

public class AuthenticationServer 
{
	private String keyTGS;				//TGS key
	private String ivTGS;				//TGS IV
		
	private String blockCipherMode;		//CBC or ECB
	private KerberosSystem kerberos;

	//Used to store the 4 hard-coded user accounts
	LinkedList<Login> database = new LinkedList<Login>();
	
	
	
	
	/*	- Constructor takes the block-cipher mode (CBC or ECB)	~ However, ECB will always be used between AS and Client
	 * 	  														  because there is no shared IV between the AS and Client, only
	 * 															  the Client's password
	 * 
	 *  - Constructor also takes a reference to the Kerberos object
	 *  - Finally, it takes the TGS KEY and TGS IV
	 */
	public AuthenticationServer(String blockCipherMode, KerberosSystem kerberos, String keyTGS, String ivTGS) 
	{
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		this.keyTGS = keyTGS;
		this.ivTGS = ivTGS;
		createDatabase();
		System.out.println(toString());
	}
	
	
	
	public String toString()
	{                                     
		String s = "AUTHENTICATION SERVER ___________________________________________________________________________\n\n";
		s +=       " - Authenticates users and generates Tickets for submission to the TGS." + "\n\n";
		s +=       " - Configured with the TGS Key and TGS IV {RANDOMLY GENERATED}" + "\n";
		s +=	   "    > TGS-KEY:    " + keyTGS + "\n";
		s +=       "    > TGS-IV:     " + ivTGS + "\n\n";
		s +=       " - Contains a database with 4 Users; each with a Username and Password." + "\n\n";
		s +=       " - User Database: " + printDatabase();
		return s;
	}
	
	
	/*	- Creates 4 users (group members)
	 *  - Each user has a username and password
	 */
	private void createDatabase()
	{
		database.add(new Login("Andrew Scott" , "Scotty_22_from_BOX-HQ"));
		database.add(new Login("Vedran Mandic", "xx_MiamiHotlinePro_xx"));	
		database.add(new Login("Jeremiah Cruz", "MyLaptopBatteryIsCrap"));	
		database.add(new Login("Jason D'Souza", "UTS_IT_SUPPORT_BEAST!"));	
	}
	
	
	
	public String printDatabase()
	{
		String s = "\n";
		for (Login l : database)
		{
			s += l.toString() + "\n\n";
		}
		return s;
	}
	
	
	/*	- Takes a username
	 *  - Looks up the username from the list of Logins
	 *  - Returns the correspongind password of the username
	 */
	private String findPassword(String username)
	{
		for (Login login : database)
			if (login.matches(username))
				return login.getPassword();
		return null;
	}
	
	
	/*	- AS receives the client's request, and a reference to the Client object
	 *  - AS then extracts the username from the message
	 *  - AS then uses the username to look up the client and find the correct password
	 *  - The username, password and client are passed into 'respondToClient()'
	 */
	public void receiveRequest(String request, Client client) throws IOException, ParseException
	{
		kerberos.printStepThree();
		
		System.out.println("1. Message received by AS: " + "\n\n" + request + "\n\n");
		
		String clientUsername = extractClientUsername(request);
		System.out.println("2. AS extracts the username from the request: \n\n" + " > USERNAME: " + clientUsername + "\n\n");
		
		String clientPassword = findPassword(clientUsername);
		System.out.println("3. AS then looks up username's corresponding password from the database: \n\n" + " > PASSWORD: " + clientPassword + "\n\n");
		
		kerberos.pauseSimulation();
		
		respondToClient(clientUsername, clientPassword, client);
	}
	
	
	/*	- Takes a string (the Client's request) and searches for the part containing the username
	 *  - It does this by finding the string "USERNAME: " and collecting the characters following this
	 */
	private String extractClientUsername(String request)
	{
		int start = request.indexOf("USERNAME: ") + 10;
		int end = request.length();
		return request.substring(start, end);
	}
	
	
	/*	- The AS uses 'generateResponse()' to make a message for the client
	 *  - The returned message is sent to the Client
	 */
	private void respondToClient(String clientUsername, String clientPassword, Client client) throws IOException, ParseException
	{
		kerberos.printStepFour();
		
		String message = generateResponse(clientUsername, clientPassword);
		
		System.out.println("2. AS then encrypts the message with the client's password ('" 
				+ clientPassword + "') - See 'AS_ENCRYPT_TO_CLIENT.txt' \n\n" + message + "\n\n");
		
		kerberos.pauseSimulation();
		
		client.receiveASResponse(message);
	}
	
	
	/*	- This method creates a message to send back to the client
	 *  - An Expiration Date for a new Ticket is generated using 'getTicketExpiryDate()'
	 *  - A new Ticket is created, which receives the client's username and expiration date
	 *  
	 *  - The message contains:
	 *  	1. The ticket details
	 *  	2. TGS Key and TGS IV (using tags to separate each part) 
	 *  
	 *  - The message is encrypted using 'encryptMessageToClient()'
	 */
	private String generateResponse(String clientUsername, String clientPassword) throws IOException
	{
		String expiration = getTicketExpiryDate();;
		Ticket ticket = createTicket(clientUsername, expiration);
		
		String messageToClient = ticket.toString() + "\n";
		messageToClient += "[START_TGS_KEY]" + keyTGS + "[END_TGS_KEY]" + "\n";
		messageToClient += "[START_TGS_IV]" + ivTGS + "[END_TGS_IV]";
		
		System.out.println("1. AS creates a plaintext message containing a new Ticket, "
						   + "TGS-Key & TGS-IV: \n\n" + messageToClient + "\n\n");
		
		return encryptMessageToClient(messageToClient, clientPassword);
	}
	
	
	/*	- To create an expiration date (for the ticket) we first make a Calendar object called 'cal'
	 * 	- We then ADD 1 hour to it (meaning, expiration is 1 HOUR from now)
	 * 	- We get the current date of our Calendar object using 'cal.getTime()'
	 *  - We convert the date into a "yyyy/mm/dd hh:mm:ss" format (human-readable)
	 *  - We return this date
	 *  
	 *  - E.g. if our current date is 5/15/2015 3:15:20, 
	 *  	   then the expiration date will be 5/15/2015 4:15:20 (1 hour ahead)
	 */
	private String getTicketExpiryDate()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);					//current time + 1 hour = expiration
		String date = dateFormat.format(cal.getTime());

		return date;
	}
	
	
	/*	- To create a ticket we make a new Ticket object
	 * 	- The ticket receives the client's username, ticket expiration date and a small note (for extra detail's sake)
	 */
	private Ticket createTicket(String clientUsername, String expirationDate)
	{
		String ticketNote = "NOTE: Present this ticket to TGS.";
		return new Ticket(clientUsername, expirationDate, ticketNote);
	}
	
	
	/*	- To encrypt a message to the client:
	 * 
	 * 		1. Make a new TripleDES object
	 * 			- Pass in the key
	 * 			- Pass in 'null' for the IV (because CBC will never be used between AS and Client)
	 * 			- Pass in the text file used for capturing the encryption's operations
	 * 
	 * 		2. Then call the processData() method of TripleDES, passing in:
	 * 			- The message to encrypt
	 * 			- The block-cipher mode (ECB)
	 * 			- The processing mode (encrypt)
	 * 
	 */
	private String encryptMessageToClient(String m, String key) throws IOException
	{		
		//No IV will be used between the AS and Client, only ECB can be used for this transmission
		return (new TripleDES(key, null, "AS_Encrypt_To_Client.txt").processData
				(m,  DES.blockCipherMode.ECB, DES.processingMode.ENCRYPT));
	}
}
