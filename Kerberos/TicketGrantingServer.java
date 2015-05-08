import java.io.IOException;
import java.util.*;


public class TicketGrantingServer 
{
	private String keyTGS = "MyAwesomeTripleDESKey"; //will be randomly generated
	private String ivTGS = "IP0M1S55"; //will be randomly generated
	
	private String keyServerTGS = "123456789ABCDEFGHIJKL";	//will be randomly generated
	private String ivServerTGS = "12345678"; //will be randomly generated
	
	private String blockCipherMode;
	private KerberosSystem kerberos;
	
	private LinkedList<String> timestamps = new LinkedList<String>();	//used for counter-attacking Replay Attack
	
	
	public TicketGrantingServer(String blockCipherMode, KerberosSystem kerberos) 
	{
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		System.out.println(toString());
	}
	
	
	
	public String toString()
	{
		String s = "Ticket Granting Server -------------------------------------------------------\n\n";
		s +=       " - Specializes in receiving Tickets submitted by Clients, and validating them." + "\n";
		s +=	   " - Creates session keys/IVs for Client/Server communication." + "\n\n";
		s +=       " - Configured with a Symmetric Encryption Key and Initialization Vector." + "\n";
		s +=	   "    > TGS-KEY:    " + keyTGS + "\n";
		s +=       "    > TGS-IV:     " + ivTGS + "\n\n";
		s +=	   " - Also configured with a Key and IV to use with the Resource Server." + "\n";
		s +=	   "    > TGS/S-KEY: " + keyServerTGS + "\n";
		s +=	   "    > TGS/S-IV:  " + ivServerTGS + "\n\n";

		return s;
	}
	
	
	
	public void receiveRequest(String request) throws IOException
	{
		kerberos.printStepEight();
		
		System.out.println("1. Message received by TGS: " + "\n\n" + request + "\n\n");
		
		System.out.println("2. TGS extracts Ticket, Server Name and encrypted Timestamp:" + "\n");

		String encryptedTimestamp = extractBetweenTags(request, "[START_TIMESTAMP]", "[END_TIMESTAMP]");
		String serverName =			extractBetweenTags(request, "[START_SERVER_NAME]", "[END_SERVER_NAME]");
		String ticket =				extractBetweenTags(request, "[START_TICKET]", "[END_TICKET]");
		
		String output = "Extracted Ticket:       -------------------------------------------- \n" + ticket + "\n";
		output += 	    "Extracted Server Name:  -------------------------------------------- \n" + " > " + serverName + "\n" + "\n";
		output += 	    "Extracted Timestamp:    -------------------------------------------- \n" + " > " + encryptedTimestamp + "\n";
		System.out.println(output + "\n\n");
				
		System.out.println("3. TGS decrypts Timestamp with its own TGS-key:" + "\n");
		String decryptedTimestamp = encryptOrDecrypt(encryptedTimestamp, keyTGS, ivTGS, "TGS_Decrypt_From_Client.txt", DES.processingMode.DECRYPT);
		System.out.println("Decrypted Timestamp:    -------------------------------------------- \n" + " > " + decryptedTimestamp + "\n");
		
		kerberos.pauseSimulation();
		
		validateRequestAndProceed(ticket, decryptedTimestamp, serverName);
	}
	
	
	
	private String extractBetweenTags(String m, String startTag, String endTag)
	{
		int startKeyIndex = m.indexOf(startTag) + startTag.length();
		int endKeyIndex = m.indexOf(endTag);
		if (!(startKeyIndex == -1 || endKeyIndex == -1))
			return m.substring(startKeyIndex, endKeyIndex);
		return null;
	}
	
	
	
	public String encryptOrDecrypt(String data, String key, String IV, String captureFilePath, DES.processingMode mode) throws IOException
	{
		if (blockCipherMode.equals("CBC"))
			return (new TripleDES(key, IV, captureFilePath).processData(data, DES.blockCipherMode.CBC, mode));
		else
			return (new TripleDES(key, null, captureFilePath).processData(data, DES.blockCipherMode.ECB, mode));
	}
	
	
	
	private void validateRequestAndProceed(String ticket, String timestamp, String serverName) throws IOException
	{
		boolean timestampValid = validateTimestamp(timestamp);
		boolean ticketExpired = validateTicket(ticket);
		
		if (timestampValid && ticketExpired)
		{
			generateSessionKey(serverName);
		}
		else if (!timestampValid)
		{
			kerberos.abortWithError("ERROR!!! Timestamp already exists, potential replay-attack detected.");
		}
		else if (!ticketExpired)
		{
			kerberos.abortWithError("ERROR!!! Invalid ticket; passed expiration date");
		}
		//generateSessionKey(serverName);
	}
	
	
	private boolean validateTimestamp(String timestamp)
	{
		for (String s : timestamps)
		{
			if (s.equals(timestamp))
				return false;
		}
		timestamps.add(timestamp);
		return true;
	}
	
	
	private boolean validateTicket(String ticket)
	{
		/*
		 * CODE HERE FOR VALIDATING TICKET EXPIRATION DATE (basically a way we can use the ticket...)
		 * 
		int dateStart = ticket.indexOf("Expiration Date: ") + 17;
		int dateEnd = ticket.indexOf("[/Date]");
		String expirationDate = ticket.substring(dateStart, dateEnd);
		
		*/
		return true;
	}
	
	
	public void generateSessionKey(String serverName) throws IOException
	{
		kerberos.printStepNine();
		
		//code here for constructing key and IV:
		String clientServerKey = "newClientServerKey123";		//hard-coded for now
		String clientServerIV =  "12345678";					//hard-coded for now
		
		String plaintextKey = "[START_KEY]" + clientServerKey + "[END_KEY]";
		plaintextKey +=		  "[START_IV]" + clientServerIV + "[END_IV]";
		
		System.out.println("1. TGS creates a random 168-bit (21 character) Triple-DES key: " + "\n");
		System.out.println("   > Key:      " + clientServerKey + "\n\n");

		System.out.println("2. TGS creates a random 64-bit (8 character) Initialization Vector: " + "\n");
		System.out.println("   > IV:       " + clientServerIV + "\n\n");
		
		Server server = findServer(serverName);
		
		System.out.println("3. TGS uses extracted Server Name to find correct server: " + "\n");
		System.out.println("   > Server:   " + serverName + "\n\n");
		
		kerberos.pauseSimulation();
		
		sendKeyToClientAndServer(plaintextKey, server, kerberos.client);	
	}
	
	
	private void sendKeyToClientAndServer(String key, Server server, Client client) throws IOException
	{
		kerberos.printStepTen();
		
		System.out.println("1. TGS creates TWO copies of a message containing the key and IV: " + "\n");
		System.out.println("   > Message:  " + key + "\n\n");
		
		String encryptedKeyToServer = encryptOrDecrypt(key, keyServerTGS, ivServerTGS, "TGS_Encrypt_To_Server.txt", DES.processingMode.ENCRYPT);
		String encryptedKeyToClient = encryptOrDecrypt(key, keyTGS, ivTGS, "TGS_Encrypt_To_Client.txt", DES.processingMode.ENCRYPT);
		
		System.out.println("2. TGS encrypts the 1st copy with the Server/TGS key, and sends it to Server: " + "\n");
		System.out.println("   > Message to Server:  " + encryptedKeyToServer + "\n\n");
		
		System.out.println("3. TGS encrypts the 2nd copy with the TGS key, and sends it to Client: " + "\n");
		System.out.println("   > Message to Client:  " + encryptedKeyToClient + "\n\n");

		kerberos.pauseSimulation();
		kerberos.printStepEleven();
		
		server.receiveSessionKey(encryptedKeyToServer);
		client.receiveSessionKey(encryptedKeyToClient);
	}
	
	
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
