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
		if (blockCipherMode.equals("CBC") && !key.equals(password)) //use ECB for messages from AS (no IV is exchanged)
			return (new TripleDES(key, IV, captureFilePath).processData(data, DES.blockCipherMode.CBC, mode));
		else
			return (new TripleDES(key, null, captureFilePath).processData(data, DES.blockCipherMode.ECB, mode));
	}
	
	
	
	private void dissectMessageFromAS(String messageFromAS) throws IOException
	{
		kerberos.printStepSix();

		System.out.println("1. Client extracts Ticket and TGS Key/IV from the decrypted response:" + "\n");
		
		String ticket = 	extractBetweenTags(messageFromAS, "[START_TICKET]" + "\n", "[END_TICKET]");
		keyTGS = 			extractBetweenTags(messageFromAS, "[START_TGS_KEY]", "[END_TGS_KEY]");
		ivTGS = 			extractBetweenTags(messageFromAS, "[START_TGS_IV]", "[END_TGS_IV]");
		
		String output = "Extracted Ticket:  -------------------------------------------- \n" + ticket + "\n";
		output += 	    "Extracted TGS key: -------------------------------------------- \n" + " > " + keyTGS + "\n" + "\n";
		output += 	    "Extracted TGS IV:  -------------------------------------------- \n" + " > " + ivTGS + "\n";
		System.out.println(output + "\n\n");
				
		System.out.println("2. Client constructs a timestamp and gets the resource server name:" + "\n");

		String timestamp = 	generateTimeStamp();
		String serverName = destinationServerName;
		
		String output2 = "Timestamp:   --------------------------- \n" + " > " + timestamp + "\n\n";
		output2 += 		 "Server Name: --------------------------- \n" + " > " + serverName + "\n";
		
		System.out.println(output2 + "\n\n");
		
		kerberos.pauseSimulation();
		
		transmitRequestToTGS(ticket, timestamp, serverName, keyTGS, ivTGS);
	}
	
	
	
	private void transmitRequestToTGS(String ticket, String timestamp, String serverName, String keyTGS, String ivTGS) throws IOException
	{
		kerberos.printStepSeven();
		
		System.out.println("1. Client creates a message containing the Ticket, Server Name, and encrypted Timestamp (using TGS-Key/IV)" + "\n");

		String encryptedTimestamp = encryptOrDecrypt(timestamp, keyTGS, ivTGS, "Client_Encrypt_To_TGS.txt", DES.processingMode.ENCRYPT);
		
		String message = "";
		message += 	"[START_TICKET]" + "\n" + ticket + "[END_TICKET]" + "\n";
		message +=  "[START_SERVER_NAME]" + serverName + "[END_SERVER_NAME]" + "\n";
		message +=  "[START_TIMESTAMP]" + encryptedTimestamp + "[END_TIMESTAMP]";
				
		System.out.println(message + "\n\n");
		
		kerberos.pauseSimulation();
		
		kerberos.TGS.receiveRequest(message);
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
		String plaintext = encryptOrDecrypt(encryptedKey, keyTGS, ivTGS, "Client_Decrypt_From_TGS.txt", DES.processingMode.DECRYPT);
		
		keyClientServer = extractBetweenTags(plaintext, "[START_KEY]", "[END_KEY]");
		ivClientServer =  extractBetweenTags(plaintext, "[START_IV]", "[END_IV]");
		
		kerberos.clientHasKey = true;
	}
	
	
	public void requestRSAKeys() throws IOException
	{
		Server server = findServer(destinationServerName);
		String message = "Hello server, I'd like a new RSA key pair";
		String encryptedMessage = encryptOrDecrypt(message, keyClientServer, ivClientServer, "Client_Encrypt_To_Server.txt", DES.processingMode.ENCRYPT);
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
