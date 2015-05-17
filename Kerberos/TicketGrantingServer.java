import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class TicketGrantingServer 
{
	private String keyTGS;				//TGS key
	private String ivTGS;				//TGS IV (for CBC)
	
	private String keyServerTGS;		//TGS-Server Key
	private String ivServerTGS;			//TGS-Server IV (for CBC)
	
	private String blockCipherMode;		//Block-cipher mode (ECB or CBC)
	private KerberosSystem kerberos; 	//Reference to main KerberosSystem object
		
	
	/*	- Receives block cipher mode, KerberosSystem object, TGS key, TGS IV, TGS-Server key, and TGS-Server IV
	 */
	public TicketGrantingServer(String blockCipherMode, KerberosSystem kerberos, 
			String keyTGS, String ivTGS, String keyServerTGS, String ivServerTGS) 
	{
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		this.keyTGS = keyTGS;
		this.ivTGS = ivTGS;
		this.keyServerTGS = keyServerTGS;
		this.ivServerTGS = ivServerTGS;
		System.out.println(toString());
	}
	
	
	
	public String toString()
	{
		String s = "\n\n TICKET GRANTING SERVER ___________________________________________________________________________\n\n";
		s +=       " - Receives Tickets submitted by Clients, and validates them using an expiration date." + "\n";
		s +=	   " - Detects Replay Attacks by validating Timestamps of their freshness (1 minute).\n";
		s +=	   " - Creates new session keys & IVs for Client/Server communication." + "\n\n";
		s +=       " - Configured with the TGS Key and TGS IV." + "\n";
		s +=	   "    > TGS-KEY:        " + keyTGS + "\n";
		s +=       "    > TGS-IV:         " + ivTGS + "\n\n";
		s +=	   " - Configured with the TGS-Server Key and TGS-Server IV (to use with the Resource Server)" + "\n";
		s +=	   "    > TGS/Server-KEY: " + keyServerTGS + "\n";
		s +=	   "    > TGS/Server-IV:  " + ivServerTGS + "\n\n";

		return s;
	}
	
	
	/*	- TGS receives the Client's request
	 *  - It extracts the Ticket, Server Name, and Timestamp from the request (using method 'extractBetweenTags()')
	 *  - It then decrypts the Timestamp using the TGS Key
	 *  
	 *  - Using these extracted components, it validates the Client's request using method 'validateRequestAndProceed()'
	 */
	public void receiveRequest(String request) throws IOException, ParseException
	{
		kerberos.printStepEightA();
		
		System.out.println("1. Message received by TGS: " + "\n\n" + request + "\n\n");
		
		System.out.println("2. TGS extracts Ticket, Server Name and encrypted Timestamp:" + "\n");

		String encryptedTimestamp = extractBetweenTags(request, "[START_TIMESTAMP]", "[END_TIMESTAMP]");
		String serverName =			extractBetweenTags(request, "[START_SERVER_NAME]", "[END_SERVER_NAME]");
		String ticket =				extractBetweenTags(request, "[START_TICKET]", "[END_TICKET]");
		
		String output = "Extracted Ticket:       -------------------------------------------- \n" + ticket + "\n";
		output += 	    "Extracted Server Name:  -------------------------------------------- \n" + " > " + serverName + "\n" + "\n";
		output += 	    "Extracted Timestamp:    -------------------------------------------- \n" + " > " + encryptedTimestamp + "\n";
		System.out.println(output + "\n\n");
				
		System.out.println("3. TGS decrypts Timestamp with its TGS-key ('" + keyTGS + "') & TGS-IV ('" + ivTGS + "') - See 'TGS_DECRYPT_FROM_CLIENT.txt' \n");
		
		String decryptedTimestamp = encryptOrDecrypt(encryptedTimestamp, keyTGS, ivTGS, "TGS_Decrypt_From_Client.txt", DES.processingMode.DECRYPT);
		
		System.out.println("Decrypted Timestamp:    -------------------------------------------- \n" + " > " + decryptedTimestamp + "\n\n");
				
		kerberos.pauseSimulation();
		
		validateRequestAndProceed(ticket, decryptedTimestamp, serverName);
	}
	

	public void receiveReplayedRequest(String request, Hacker hacker) throws IOException, ParseException
	{
		String encryptedTimestamp = extractBetweenTags(request, "[START_TIMESTAMP]", "[END_TIMESTAMP]");
		String decryptedTimestamp = encryptOrDecrypt(encryptedTimestamp, keyTGS, ivTGS, "TGS_Decrypt_From_Client.txt", DES.processingMode.DECRYPT);
		
		System.out.println("\n > Just like before (Step 8), the TGS will validate the received Timestamp.\n");
		
		boolean timestampValid = validateTimestamp(decryptedTimestamp);
		
		if (!timestampValid)
			kerberos.abortWithError("ERROR!!! Timestamp too old, potential Replay Attack detected.");
		else
		{
			kerberos.pauseSimulation();
			hacker.receiveSessionKey(generateKeyForHacker());
		}
	}
	
	
	private String generateKeyForHacker() throws IOException
	{
		String key = "[START_KEY]" + (new KeyGenerator(21).getKey()) + "[END_KEY]";
		key +=		 "[START_IV]" + (new KeyGenerator(8).getKey()) + "[END_IV]";
		
		String encryptedKeyToHacker = encryptOrDecrypt(key, keyTGS, ivTGS, "TGS_Encrypt_To_Hacker.txt", DES.processingMode.ENCRYPT);
	   
		String s = "\n > Uh-Oh!... The hacker was quick enough to beat the Timestamp validation test! \n\n";
		s +=       " > The TGS generates a session key for the Hacker and encrypts it with the TGS-Key - See 'TGS_ENCRYPT_TO_HACKER.txt'. \n\n";
		System.out.println(s);
		
		kerberos.pauseSimulation();
		
		return encryptedKeyToHacker;
	}
	
	
	
	/*	- Method takes a message and 2 tags (delimiters)
	 *  - The string-data BETWEEN these 2 tags is returned
	 *  
	 *  - E.g.
	 *  	> m 	   = [START]Hello[END]
	 *  	> startTag = [START]
	 *  	> endTag   = [END]
	 *  	> RETURN   = Hello
	 */
	private String extractBetweenTags(String m, String startTag, String endTag)
	{
		int startKeyIndex = m.indexOf(startTag) + startTag.length();
		int endKeyIndex = m.indexOf(endTag);
		if (!(startKeyIndex == -1 || endKeyIndex == -1))
			return m.substring(startKeyIndex, endKeyIndex);
		return null;
	}
	
	
	/*	- Method receives:
	 * 		1. Data
	 * 	    2. 168-bit 3DES Key
	 *      3. 64-bit IV
	 *      4. Capture file name
	 *      5. Mode (encrypt or decrypt)
	 *      
	 *  - Make a new TripleDES object and call 'processData()' to encrypt or decrypt the data
	 * 
	 *  - If the 'blockCipherMode' is CBC, then use CBC, otherwise use ECB
	 * 
	 */
	public String encryptOrDecrypt(String data, String key, String IV, String captureFilePath, DES.processingMode mode) throws IOException
	{
		if (blockCipherMode.equals("CBC"))
			return (new TripleDES(key, IV, captureFilePath).processData(data, DES.blockCipherMode.CBC, mode));
		else
			return (new TripleDES(key, null, captureFilePath).processData(data, DES.blockCipherMode.ECB, mode));
	}
	
	
	
	/*	- Method receives the Client's Ticket, Timestamp and Server Name
	 * 
	 * 	- To validate the request we need to:
	 * 		1. Make sure the Ticket hasn't expired in time
	 * 		2. Make sure the Timetstamp is valid (to detect a replay attack)
	 * 
	 *  - If both conditions are valid, then proceed to making a new session key for the Client
	 *  
	 *  - Otherwise, abort the program with the appropriate error
	 * 
	 */
	private void validateRequestAndProceed(String ticket, String timestamp, String serverName) throws IOException, ParseException
	{
		kerberos.printStepEightB();
		
		boolean ticketExpired = validateTicketExpiration(ticket);
		boolean timestampValid = validateTimestamp(timestamp);
		
		if (timestampValid && ticketExpired)
		{
			kerberos.pauseSimulation();
			generateSessionKey(serverName);
		}
		else if (!timestampValid)
		{
			kerberos.abortWithError("ERROR!!! Timestamp too old, potential Replay Attack detected.");
		}
		else if (!ticketExpired)
		{
			kerberos.abortWithError("ERROR!!! Invalid ticket; passed expiration date.");
		}
	}
	
	
	
	/* 	- Method receives the Timestamp and validates it
	 * 
	 *	- The timestamp is validated by:
	 * 		1. Making a new Java 'Date' object to get the current system time
	 * 		2. Converting the received Timestamp string into a 'Date' object (using parsing)
	 * 		3. Gets the difference between the 2 dates using the 'compareTimestamps()' method
	 * 		4. If the difference is < 1, then return true, otherwise return false
	 * 
	 * 	- NOTE: If the difference > 1, then this could indicate an old timestamp is being replayed by a hacker.
	 * -		If the difference < 1, then we say the timestamp is very recent and still fresh
	 */
	private boolean validateTimestamp(String timestamp) throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		Date currentTimestamp = new Date();
		Date receivedTimestamp = dateFormat.parse(timestamp);	
		
		long differenceBetweenTimestamps = compareTimestamps(currentTimestamp, receivedTimestamp);
		boolean timestampValid = (differenceBetweenTimestamps < 1);
		
		String output = "\n > The TGS checks if Timestamp is no more than 1 MINUTE before System Time. \n";
		output +=       "   (A Timestamp > 1 minute in age indicates a Replay Attack for this Simulation) \n\n";
		output +=	    "    > Received Timestamp:          " + dateFormat.format(receivedTimestamp) + "\n";
		output +=		"    > Current Timestamp:           " + dateFormat.format(currentTimestamp) + "\n";
		output +=		"    > Difference in Minutes:       " + differenceBetweenTimestamps + "\n";
		output +=	    "    > Valid Timestamp?             " + timestampValid + "\n";
		
		System.out.println(output);

		return timestampValid;
	}
	
	
	/*	- Method receives 2 Date objects and returns the difference (in minutes) between the 2 Dates
	 * 	- It does this by using the 'getTime()' method of Date, and using subtraction to find the difference
	 */
	private long compareTimestamps(Date currentTimestamp, Date receivedTimestamp) throws ParseException
	{		
		SimpleDateFormat f = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss");
		
		String date1Formatted = f.format(currentTimestamp);
		Date date1Truncated = f.parse(date1Formatted);
		
		String date2Formatted = f.format(receivedTimestamp);
		Date date2Truncated = f.parse(date2Formatted);
		
		long timeDiff = Math.abs(date2Truncated.getTime() - date1Truncated.getTime());
		
		return TimeUnit.MILLISECONDS.toMinutes(timeDiff);

	}
	
	
	/*	- Method receives the Client's Ticket and checks that it hasn't expired
	 *  - To do this:
	 *  	1. Extract the date included in the ticket
	 * 		2. Convert the extracted date string into a Java 'Date' object - call this 'expirationDateInTicket'
	 * 		3. Make a new 'Date' object to get the current system time	   - call this 'currentDate'
	 * 		4. Check if the current date is BEFORE the expiry date
	 */
	private boolean validateTicketExpiration(String ticket) throws ParseException
	{		
		int dateStart = ticket.indexOf("Expiration Date: ") + 17;	//tag sitting before expiration date
		int dateEnd = ticket.indexOf("[/Date]");					//tag sitting right after expiration date
		String expirationDateInTicket = ticket.substring(dateStart, dateEnd);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date expiryDateFormatted = format.parse(expirationDateInTicket);
				
		Date currentDate = new Date();
		
		boolean ticketNotExpired = currentDate.before(expiryDateFormatted);
		
		String output = " > The TGS checks if Ticket has not expired: \n\n";
		output +=	    "    > Ticket Expiration Time:      " + format.format(expiryDateFormatted) + "\n";
		output +=       "    > Current Time:                " + format.format(currentDate) + "\n";
		output +=       "    > Current Date < Expiry Date?  " + ticketNotExpired + "\n";
		
		System.out.println(output + "\n");
		
		return ticketNotExpired;
	}
	
	
	
	
	/*	- Method creates a random 21-character (168-bit Triple-DES key)
	 * 	- Also creates a random 8-character (64-bit IV)
	 * 
	 *	- It then places the 2 elements in a single message (surrounded by tags [START_KEY], [END_KEY], [START_IV] and [END_IV]
	 *  - The TGS then passes the message into the method 'sendKeyToClientAndServer()'
	 */
	public void generateSessionKey(String serverName) throws IOException
	{
		kerberos.printStepNine();
		
		//code here for constructing key and IV:
		String clientServerKey = new KeyGenerator(21).getKey();
		String clientServerIV =  new KeyGenerator(8).getKey();
		
		String plaintextKey = "[START_KEY]" + clientServerKey + "[END_KEY]";
		plaintextKey +=		  "[START_IV]" + clientServerIV + "[END_IV]";
		
		System.out.println("1. TGS creates a random 168-bit (21 character) Triple-DES key: " + "\n");
		System.out.println("   > Key:      " + clientServerKey + "\n\n");

		System.out.println("2. TGS creates a random 64-bit (8 character) Initialization Vector: " + "\n");
		System.out.println("   > IV:       " + clientServerIV + "\n\n");
		
		Server server = findServer(serverName);
		
		System.out.println("3. TGS uses extracted Server Name (from Step 8(A)) to find correct server: " + "\n");
		System.out.println("   > Server:   " + serverName + "\n\n");
		
		kerberos.pauseSimulation();
		
		sendKeyToClientAndServer(plaintextKey, server, kerberos.client);	
	}
	
	
	
	/*	- The TGS does 5 things here:
	 * 		
	 * 		1. copy the message containing the new session key
	 * 
	 * 		2. encrypt the 1st copy with the TGS-SERVER KEY (known to the Server)
	 * 
	 * 		3. encrypt the 2nd copy with the TGS KEY 		(known to the Client)
	 * 
	 *  	4. send the encrypted 1st copy to the Server
	 *  
	 *   	5. send the encrypted 2nd copy to the Client
	 * 
	 */
	private void sendKeyToClientAndServer(String key, Server server, Client client) throws IOException
	{
		kerberos.printStepTen();
		
		System.out.println("1. TGS creates TWO copies of a message containing the Client/Server-Key and Client/Server-IV: " + "\n");
		System.out.println("   > Message:  " + key + "\n\n");
		
		//Encryption of the 2 copies of the session key
		String encryptedKeyToServer = encryptOrDecrypt(key, keyServerTGS, ivServerTGS, "TGS_Encrypt_To_Server.txt", DES.processingMode.ENCRYPT);
		String encryptedKeyToClient = encryptOrDecrypt(key, keyTGS, ivTGS, "TGS_Encrypt_To_Client.txt", DES.processingMode.ENCRYPT);
		
		System.out.println("2. TGS encrypts the 1st copy with the Server/TGS-Key & Server/TGS-IV, "
				+ "and sends it to Server: - 'See 'TGS_ENCRYPT_TO_SERVER.txt'" + "\n");
		
		System.out.println("   > Message to Server:  " + encryptedKeyToServer + "\n\n");
		
		System.out.println("3. TGS encrypts the 2nd copy with the TGS-Key & TGS-IV, "
				+ "and sends it to Client: - 'See 'TGS_ENCRYPT_TO_CLIENT.txt'" + "\n");
		
		System.out.println("   > Message to Client:  " + encryptedKeyToClient + "\n\n");

		kerberos.pauseSimulation();
		kerberos.printStepEleven();
		
		//Send the session key to the Client and Server
		server.receiveSessionKey(encryptedKeyToServer);
		client.receiveSessionKey(encryptedKeyToClient);
	}
	
	
	/*	- Passes the KerberosSystem the name of the Server in the Client's request
	 *  - The KerberosSystem returns the correspondin Server with the matching name
	 */
	private Server findServer(String serverName)
	{
		for (Server server: kerberos.servers)
		{
			if (server.hasName(serverName))
				return server;
		}
		return null;
	}
}
