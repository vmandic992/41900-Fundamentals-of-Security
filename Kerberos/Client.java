import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Timestamp;
import java.util.Date;

public class Client 
{
	private String username;
	private String password;
		
	private String keyTGS;		//TGS key
	private String ivTGS;		//TGS IV (for CBC)
	private String ticket;		//Ticket from AS
	
	private String keyClientServer;		//Session key (generated by TGS)
	private String ivClientServer;		//Session key (generated by TGS)
	
	private String blockCipherMode;		//CBC or ECB
	private KerberosSystem kerberos;
	
	private String destinationServerName = "RSA-KeyGen-Server";
	
	
	public Client(String username, String password, String blockCipherMode, KerberosSystem kerberos) 
	{
		this.username = username;
		this.password = password;
		this.blockCipherMode = blockCipherMode;
		this.kerberos = kerberos;
		System.out.println(toString());
	}
	
	
	
	public String toString()
	{
		String s = "\n\n SIMULATED CLIENT ___________________________________________________________________________\n\n";
		s +=	   " - Wants to access a Resource Server." + "\n\n";
		s +=       " - Username: " + username + "\n";
		s +=       " - Password: " + password + "\n";
		return s;
	}
	
	
	/*	- Client makes a request to send to the AS
	 *  - Request contains a small message + the client's username
	 *  - Then the AS receives the request, and a reference to the Client object
	 */
	public void sendRequestToAS() throws IOException, ParseException
	{
		String request = " > REQUEST: I want to authenticate and access network resources.\n";
		request +=       "   USERNAME: " + username;
		
		System.out.println("Client sends the following message to AS: " + "\n\n" + request + "\n");
		
		kerberos.pauseSimulation();
		kerberos.AS.receiveRequest(request, this);
	}
		
	
	/*
	 * 
	 */
	public void receiveASResponse(String message) throws IOException, ParseException
	{
		kerberos.printStepFive();
		
		System.out.println("1. Client receives encrypted message from AS: " + "\n\n" + message + "\n\n");
				
		String plaintext = encryptOrDecrypt(message, password, null, "Client_Decrypt_From_AS.txt", DES.processingMode.DECRYPT);
		
		System.out.println("2. Client decrypts response using their password: ('" + password + "') - See 'CLIENT_DECRYPT_FROM_AS.txt'"
				+ "\n\n" + plaintext + "\n\n");
		
		kerberos.pauseSimulation();
		
		dissectMessageFromAS(plaintext);
	}
	
	
	
	public String encryptOrDecrypt(String data, String key, String IV, String captureFilePath, DES.processingMode mode) throws IOException
	{
		if (blockCipherMode.equals("CBC") && (IV != null)) //use ECB for messages from AS (no IV is exchanged)
			return (new TripleDES(key, IV, captureFilePath).processData(data, DES.blockCipherMode.CBC, mode));
		else
			return (new TripleDES(key, null, captureFilePath).processData(data, DES.blockCipherMode.ECB, mode));
	}
	
	
	
	private void dissectMessageFromAS(String messageFromAS) throws IOException, ParseException
	{
		kerberos.printStepSix();
		
		extractDetails(messageFromAS);

		System.out.println("Client extracts Ticket, TGS-Key & TGS-IV from the decrypted message [from Step 5]:" + "\n");
		String output = "Extracted Ticket:  -------------------------------------------- \n" + ticket + "\n";
		output += 	    "Extracted TGS-key: -------------------------------------------- \n" + " > " + keyTGS + "\n" + "\n";
		output += 	    "Extracted TGS-IV:  -------------------------------------------- \n" + " > " + ivTGS + "\n";
		System.out.println(output + "\n");
		
		kerberos.pauseSimulation();
		
		transmitRequestToTGS();
	}
	
	
	private void extractDetails(String messageFromAS)
	{
		ticket = extractBetweenTags(messageFromAS, "[START_TICKET]" + "\n", "[END_TICKET]");
		keyTGS = extractBetweenTags(messageFromAS, "[START_TGS_KEY]", "[END_TGS_KEY]");
		ivTGS =  extractBetweenTags(messageFromAS, "[START_TGS_IV]", "[END_TGS_IV]");
	}
	
	
	private void transmitRequestToTGS() throws IOException, ParseException
	{
		kerberos.printStepSeven();
		
		System.out.println("1. Client gets the Resource Server name and generates an encrypted Timestamp:" + "\n");

		String timestamp = generateTimeStamp();
		String encryptedTimestamp = encryptOrDecrypt(timestamp, keyTGS, ivTGS, "Client_Encrypt_To_TGS.txt", DES.processingMode.ENCRYPT);

																		 
		String output = "Server Name:                                     \n" + " > " + destinationServerName + "\n\n";
		output += 		"Timestamp:                                       \n" + " > " + timestamp + "\n\n\n";
		output +=		"Timestamp is encrypted using TGS-Key ('" + keyTGS + "') & TGS-IV ('" + ivTGS + "') - See 'CLIENT_ENCRYPT_TO_TGS.txt' \n\n";
		output +=		"Encrypted Timestamp:                             \n" + " > " + encryptedTimestamp + "\n";
		System.out.println(output + "\n");
		
		
		System.out.println("2. Client creates a message containing the Ticket, Server Name, and encrypted Timestamp:" + "\n");
		String message = generateMessageToTGS(encryptedTimestamp);				
		System.out.println(message + "\n\n");
		
		kerberos.pauseSimulation();
		
		if (kerberos.includeAttackInSimulation)
			kerberos.hacker.copyTransmission(message);
			
		kerberos.TGS.receiveRequest(message);
	}
	
	
	private String generateMessageToTGS(String encryptedTimestamp) throws IOException
	{
		String message = "";
		message += 	"[START_TICKET]" + "\n" + ticket + "[END_TICKET]" + "\n";
		message +=  "[START_SERVER_NAME]" + destinationServerName + "[END_SERVER_NAME]" + "\n";
		message +=  "[START_TIMESTAMP]" + encryptedTimestamp + "[END_TIMESTAMP]";
		
		return message;
	}
	
	
	private String extractBetweenTags(String m, String startTag, String endTag)
	{
		int startKeyIndex = m.indexOf(startTag) + startTag.length();
		int endKeyIndex = m.indexOf(endTag);
		if (!(startKeyIndex == -1 || endKeyIndex == -1))
			return m.substring(startKeyIndex, endKeyIndex);
		return null;
	}
	
	
	
	private String generateTimeStamp()
	{
		Date date = new Date();
		return (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(date);
		//return new Timestamp(date.getTime()).toString();
	}
	
	
	
	public void receiveSessionKey(String encryptedKey) throws IOException
	{
		System.out.println("Client receives encrypted Client/Server-Key and Client/Server-IV: " + "\n");
		System.out.println("   > Ciphertext:  " + encryptedKey + "\n\n");
		
		String plaintext = encryptOrDecrypt(encryptedKey, keyTGS, ivTGS, "Client_Decrypt_From_TGS.txt", DES.processingMode.DECRYPT);
		
		System.out.println("Client decrypts message with the TGS-Key ('" + keyTGS + "') & TGS-IV ('" + ivTGS + "'): "
			    + " - See 'CLIENT_DECRYPT_FROM_TGS.txt' \n");
		
		System.out.println("   > Plaintext:   " + plaintext + "\n\n");
		
		keyClientServer = extractBetweenTags(plaintext, "[START_KEY]", "[END_KEY]");
		ivClientServer =  extractBetweenTags(plaintext, "[START_IV]", "[END_IV]");
		
		System.out.println("Client extracts the Session key & IV: " + "\n");
		System.out.println("   > Key:         " + keyClientServer + "\n");
		System.out.println("   > IV:          " + ivClientServer + "\n\n");
		
		kerberos.clientHasKey = true;
		kerberos.pauseSimulation();
	}
	
	
	public void requestRSAKeys() throws IOException, ParseException
	{	
		Server server = findServer(destinationServerName);
		String message = "Hello server, I would like a new RSA key pair";
		
		System.out.println("1. Client creates a plaintext message to send to the Server: " + "\n");
		System.out.println("   > Plaintext:    " + message + "\n\n");
		
		String encryptedMessage = encryptOrDecrypt(message, keyClientServer, ivClientServer, "Client_Encrypt_To_Server.txt", DES.processingMode.ENCRYPT);
		
		System.out.println("2. Client encrypts message with the Client/Server-Key ('" + keyClientServer + "')"
				+ " & Client/Server-IV ('" + ivClientServer + "') - See 'CLIENT_ENCRYPT_TO_SERVER.txt' \n");
		System.out.println("   > Ciphertext:   " + encryptedMessage + "\n\n");
		
		kerberos.pauseSimulation();
		server.receiveClientRequest(encryptedMessage, this);
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
	
	
	public void receiveRSAKeys(String message) throws IOException, ParseException
	{	
		kerberos.printStepFourteen();
		System.out.println("1. Client receives encrypted key-pair from Server: " + "\n\n" + message + "\n");

		String decryptedMessage = encryptOrDecrypt(message, keyClientServer, ivClientServer, "Client_Decrypt_From_Server.txt", DES.processingMode.DECRYPT);
		
		System.out.println("2. Client decrypts key pair with Client/Server-Key & Client/Server-IV: "
				+ "- See 'CLIENT_DECRYPT_FROM_SERVER.txt'" + "\n");
		
		System.out.println(decryptedMessage + "\n");
		
		kerberos.endSimulation();
	}
}
