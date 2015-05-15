import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class TicketGrantingServer 
{
	private String keyTGS;			//TGS key
	private String ivTGS;			//TGS IV (for CBC)
	
	private String keyServerTGS;	//TGS-Server Key
	private String ivServerTGS;		//TGS-Server IV (for CBC)
	
	private String blockCipherMode;	//ECB or CBC
	private KerberosSystem kerberos;
		
	
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
		s +=	   " - Creates new session keys & IVs for Client/Server communication." + "\n\n";
		s +=       " - Configured with the TGS Key and TGS IV." + "\n";
		s +=	   "    > TGS-KEY:        " + keyTGS + "\n";
		s +=       "    > TGS-IV:         " + ivTGS + "\n\n";
		s +=	   " - Configured with the TGS-Server Key and TGS-Server IV (to use with the Resource Server)" + "\n";
		s +=	   "    > TGS/Server-KEY: " + keyServerTGS + "\n";
		s +=	   "    > TGS/Server-IV:  " + ivServerTGS + "\n\n";

		return s;
	}
	
	
	
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
		//generateSessionKey(serverName);
	}
	
	
	private boolean validateTimestamp(String timestamp) throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		Date currentTimestamp = new Date();
		Date receivedTimestamp = dateFormat.parse(timestamp);	
		
		long differenceBetweenTimestamps = compareTimestamps(currentTimestamp, receivedTimestamp);
		boolean timestampValid = (differenceBetweenTimestamps < 1);
		
		String output = "2. TGS checks if timestamp is within 2 minutes of System Time: \n\n";
		output +=	    "    > Received Timestamp:          " + dateFormat.format(receivedTimestamp) + "\n";
		output +=		"    > Current Timestamp:           " + dateFormat.format(currentTimestamp) + "\n";
		output +=		"    > Difference in Minutes:       " + differenceBetweenTimestamps + "\n";
		output +=	    "    > Valid Timestamp?             " + timestampValid + "\n";
		
		System.out.println(output);

		return timestampValid;
	}
	
	
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
	
	
	private boolean validateTicketExpiration(String ticket) throws ParseException
	{		
		int dateStart = ticket.indexOf("Expiration Date: ") + 17;
		int dateEnd = ticket.indexOf("[/Date]");
		String expirationDateInTicket = ticket.substring(dateStart, dateEnd);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date expiryDateFormatted = format.parse(expirationDateInTicket);
				
		Date currentDate = new Date();
		
		boolean ticketNotExpired = currentDate.before(expiryDateFormatted);
		
		String output = "1. TGS checks if Ticket has not expired: \n\n";
		output +=	    "    > Ticket Expiration Time:      " + format.format(expiryDateFormatted) + "\n";
		output +=       "    > Current Time:                " + format.format(currentDate) + "\n";
		output +=       "    > Current Date < Expiry Date?  " + ticketNotExpired + "\n";
		
		System.out.println(output + "\n");
		
		return ticketNotExpired;
	}
	
	
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
		
		System.out.println("3. TGS uses extracted Server Name to find correct server: " + "\n");
		System.out.println("   > Server:   " + serverName + "\n\n");
		
		kerberos.pauseSimulation();
		
		sendKeyToClientAndServer(plaintextKey, server, kerberos.client);	
	}
	
	
	private void sendKeyToClientAndServer(String key, Server server, Client client) throws IOException
	{
		kerberos.printStepTen();
		
		System.out.println("1. TGS creates TWO copies of a message containing the Client/Server-Key and Client/Server-IV: " + "\n");
		System.out.println("   > Message:  " + key + "\n\n");
		
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
