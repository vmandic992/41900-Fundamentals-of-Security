import java.io.IOException;
import java.util.*;
import java.sql.Timestamp;
import java.util.Date;

public class Client 
{
	private String username;
	private String password;
	private String initializationVectorAS = "YTC45132";
	private String blockCipherMode;
	private KerberosSystem kerberos;
	
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
		String s = "Simulated Client \n\n";
		s +=       " - Username: " + username + "\n";
		s +=       " - Password: " + password + "\n";
		//include keys/IVs
		return s;
	}
	
	public void sendRequestToAS() throws IOException
	{
		String request = " > REQUEST: I want to authenticate and access network resources.\n";
		request +=       "   USERNAME: " + username;
		
		System.out.println("\n" + "Client message to AS: " + "\n\n" + request + "\n");
		
		kerberos.AS.receiveRequest(request, this);
	}
		
	public void receiveASResponse(String message) throws IOException
	{
		kerberos.printStepFive();
		
		System.out.println("Client receives encrypted message from AS: " + "\n\n" + message + "\n\n");
		String plaintext = encryptOrDecrypt(message, password, initializationVectorAS, "TripleDESCapture2.txt", DES.processingMode.DECRYPT);
		System.out.println("Client decrypts response with their password: " + "\n\n" + plaintext + "\n\n");
		sendTicketToTGS(plaintext);
	}
	
	public String encryptOrDecrypt(String data, String key, String IV, String captureFilePath, DES.processingMode mode) throws IOException
	{
		if (blockCipherMode.equals("CBC"))
			return (new TripleDES(key, IV, captureFilePath).processData(data, DES.blockCipherMode.CBC, mode));
		else
			return (new TripleDES(key, null, captureFilePath).processData(data, DES.blockCipherMode.ECB, mode));
	}
	
	private void sendTicketToTGS(String message)
	{
		kerberos.printStepSix();

		String ticket = 	extractBetweenTags(message, "[START_TICKET]", "[END_TICKET]");
		String keyTGS = 	extractBetweenTags(message, "[START_KEY]", "[END_KEY]");
		String ivTGS = 		extractBetweenTags(message, "[START_IV]", "[END_IV]");
		String timestamp = 	generateTimeStamp();
		String serverName = kerberos.server.getName();
		
		String messageToTGS = generateMessageToTGS(ticket, timestamp, serverName, keyTGS, ivTGS);
	}
	
	private String generateMessageToTGS(String ticket, String timestamp, String serverName, String keyTGS, String ivTGS)
	{
		String message = "";
		//encrypt the timestamp (with TGS key and IV)
		//send the rest in plaintext
		//use delimiters (tags) for everything in the message
		return "";
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
}
