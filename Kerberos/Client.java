import java.io.IOException;
import java.util.*;
import java.sql.Timestamp;
import java.util.Date;

public class Client 
{
	private String username;
	private String password;
		
	private String keyTGS;
	private String ivTGS;
	private String ticket;
	
	private String keyClientServer;
	private String ivClientServer;
	
	private String blockCipherMode;
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
		String s = "Simulated Client ---------------------------------------------\n\n";
		s +=	   " - Wants to access a Resource Server." + "\n\n";
		s +=       " - Username: " + username + "\n";
		s +=       " - Password: " + password + "\n";
		return s;
	}
	
	
	
	public void sendRequestToAS() throws IOException
	{
		String request = " > REQUEST: I want to authenticate and access network resources.\n";
		request +=       "   USERNAME: " + username;
		
		System.out.println("\n" + "Client sends the following message to AS: " + "\n\n" + request + "\n");
		
		kerberos.pauseSimulation();
		kerberos.AS.receiveRequest(request, this);
	}
		
	
	
	public void receiveASResponse(String message) throws IOException
	{
		kerberos.printStepFive();
		
		System.out.println("1. Client receives encrypted message from AS: " + "\n\n" + message + "\n\n");
				
		String plaintext = encryptOrDecrypt(message, password, null, "Client_Decrypt_From_AS.txt", DES.processingMode.DECRYPT);
		
		System.out.println("2. Client decrypts response using their password: ('" + password + "') \n\n" + plaintext + "\n\n");
		
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
	
	
	
	private void dissectMessageFromAS(String messageFromAS) throws IOException
	{
		kerberos.printStepSix();
		
		extractDetails(messageFromAS);

		System.out.println("Client extracts Ticket and TGS Key/IV from the decrypted response:" + "\n");
		String output = "Extracted Ticket:  -------------------------------------------- \n" + ticket + "\n";
		output += 	    "Extracted TGS key: -------------------------------------------- \n" + " > " + keyTGS + "\n" + "\n";
		output += 	    "Extracted TGS IV:  -------------------------------------------- \n" + " > " + ivTGS + "\n";
		System.out.println(output + "\n\n");
		
		kerberos.pauseSimulation();
		
		transmitRequestToTGS();
	}
	
	
	private void extractDetails(String messageFromAS)
	{
		ticket = extractBetweenTags(messageFromAS, "[START_TICKET]" + "\n", "[END_TICKET]");
		keyTGS = extractBetweenTags(messageFromAS, "[START_TGS_KEY]", "[END_TGS_KEY]");
		ivTGS =  extractBetweenTags(messageFromAS, "[START_TGS_IV]", "[END_TGS_IV]");
	}
	
	
	private void transmitRequestToTGS() throws IOException
	{
		kerberos.printStepSeven();
		
		System.out.println("1. Client constructs a timestamp and gets the resource Server name:" + "\n");

		String timestamp = generateTimeStamp();
		String encryptedTimestamp = encryptOrDecrypt(timestamp, keyTGS, ivTGS, "Client_Encrypt_To_TGS.txt", DES.processingMode.ENCRYPT);

		
		String output = "Server Name:         --------------------------- \n" + " > " + destinationServerName + "\n\n";
		output += 		"Timestamp:           --------------------------- \n" + " > " + timestamp + "\n\n\n";
		output +=		"Timestamp is encrypted using TGS-key: ('" + keyTGS + "')" + "\n\n";
		output +=		"Encrypted Timestamp: --------------------------- \n" + " > " + encryptedTimestamp + "\n";
		System.out.println(output + "\n");
		
		
		System.out.println("2. Client creates a message containing the Ticket, Server Name, and encrypted Timestamp:" + "\n");
		String message = generateMessageToTGS(encryptedTimestamp);				
		System.out.println(message + "\n\n");
		
		kerberos.pauseSimulation();
		
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
		return new Timestamp(date.getTime()).toString();
	}
	
	
	
	public void receiveSessionKey(String encryptedKey) throws IOException
	{
		System.out.println("Client receives encrypted session key: " + "\n");
		System.out.println("   > Ciphertext:  " + encryptedKey + "\n\n");
		
		String plaintext = encryptOrDecrypt(encryptedKey, keyTGS, ivTGS, "Client_Decrypt_From_TGS.txt", DES.processingMode.DECRYPT);
		
		System.out.println("Client decrypts message with the TGS-key: " + "\n");
		System.out.println("   > Plaintext:   " + plaintext + "\n\n");
		
		keyClientServer = extractBetweenTags(plaintext, "[START_KEY]", "[END_KEY]");
		ivClientServer =  extractBetweenTags(plaintext, "[START_IV]", "[END_IV]");
		
		System.out.println("Client extracts the Session key & IV: " + "\n");
		System.out.println("   > Key:         " + keyClientServer + "\n");
		System.out.println("   > IV:          " + ivClientServer + "\n\n");
		
		kerberos.clientHasKey = true;
	}
	
	
	public void requestRSAKeys() throws IOException
	{	
		Server server = findServer(destinationServerName);
		String message = "Hello server, I'd like a new RSA key pair";
		
		System.out.println("1. Client creates a plaintext message to send to the Server: " + "\n");
		System.out.println("   > Plaintext:    " + message + "\n\n");
		
		String encryptedMessage = encryptOrDecrypt(message, keyClientServer, ivClientServer, "Client_Encrypt_To_Server.txt", DES.processingMode.ENCRYPT);
		
		System.out.println("2. Client encrypts message with the Client/Server session key: " + "\n");
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
	
	
	public void receiveRSAKeys() throws IOException
	{	
		
	}
}
