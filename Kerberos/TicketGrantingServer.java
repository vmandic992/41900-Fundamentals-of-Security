import java.io.IOException;


public class TicketGrantingServer 
{
	private String keyTGS = "MyAwesomeTripleDESKey"; //will be randomly generated
	private String ivTGS = "IP0M1S55"; //will be randomly generated

	private String keyServerTGS;
	private String ivServerTGS;
	
	private String blockCipherMode;
	private KerberosSystem kerberos;
	
	
	public TicketGrantingServer(String blockCipherMode, KerberosSystem kerberos) 
	{
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		System.out.println(toString());
	}
	
	
	
	public String toString()
	{
		String s = "Ticket Granting Server ---------------------------------------------\n\n";
		//include keys/IVs
		return s;
	}
	
	
	
	public void receiveRequest(String message) throws IOException
	{
		kerberos.printStepEight();
		
		System.out.println("1. TGS extracts Ticket, Server Name and encrypted Timestamp:" + "\n");

		String encryptedTimestamp = extractBetweenTags(message, "[START_TIMESTAMP]", "[END_TIMESTAMP]");
		String serverName =			extractBetweenTags(message, "[START_SERVER_NAME]", "[END_SERVER_NAME]");
		String ticket =				extractBetweenTags(message, "[START_TICKET]", "[END_TICKET]");
		String decryptedTimestamp = encryptOrDecrypt(encryptedTimestamp, keyTGS, ivTGS, "TripleDESCapture2.txt", DES.processingMode.DECRYPT);

		
		String output = "Extracted Ticket:       -------------------------------------------- \n" + ticket + "\n";
		output += 	    "Extracted Server Name:  -------------------------------------------- \n" + " > " + serverName + "\n" + "\n";
		output += 	    "Extracted Timestamp:    -------------------------------------------- \n" + " > " + encryptedTimestamp + "\n";
		System.out.println(output + "\n\n");
				
		System.out.println("2. TGS decrypts Timestamp with its own key/IV:" + "\n");
		System.out.println("Decrypted Timestamp:    -------------------------------------------- \n" + " > " + decryptedTimestamp + "\n");
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
}
