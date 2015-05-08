import java.io.IOException;

public class Server 
{
	private String serverName;
	private String service;
	
	private String keyServerTGS = "123456789ABCDEFGHIJKL";	//will be randomly generated
	private String ivServerTGS = "12345678"; //will be randomly generated
	
	private String keyClientServer; //will be randomly generated
	private String ivClientServer; //will be randomly generated
	
	private String blockCipherMode;
	private KerberosSystem kerberos;

	
	public Server(String serverName, String service, String blockCipherMode, KerberosSystem kerberos) 
	{
		this.serverName = serverName;
		this.service = service;
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		System.out.println(toString());
	}
	
	
	public String toString()
	{
		String s = "Server ---------------------------------------------\n\n";
		s +=       " - Acts as a network resource to Clients." + "\n\n";
		s +=       " - Name:    " + serverName + "\n";
		s +=       " - Service: " + service + "\n\n";
		s +=	   " - Also configured with a Key and IV to use with the TGS." + "\n";
		s +=	   "    > S/TGS-KEY: " + keyServerTGS + "\n";
		s +=	   "    > S/TGS-IV:  " + ivServerTGS + "\n\n";
		return s;
	}
	
	
	public boolean hasName(String serverName)
	{
		return this.serverName.equals(serverName);
	}
	
	
	public String getName()
	{
		return serverName;
	}
	
	
	public String encryptOrDecrypt(String data, String key, String IV, String captureFilePath, DES.processingMode mode) throws IOException
	{
		if (blockCipherMode.equals("CBC"))
			return (new TripleDES(key, IV, captureFilePath).processData(data, DES.blockCipherMode.CBC, mode));
		else
			return (new TripleDES(key, null, captureFilePath).processData(data, DES.blockCipherMode.ECB, mode));
	}
	
	
	public void receiveSessionKey(String encryptedKey) throws IOException
	{
		System.out.println("Server receives encrypted session key: " + "\n");
		System.out.println("   > Ciphertext:  " + encryptedKey + "\n\n");

		String plaintext = encryptOrDecrypt(encryptedKey, keyServerTGS, ivServerTGS, "Server_Decrypt_From_TGS.txt", DES.processingMode.DECRYPT);
		
		System.out.println("Server decrypts message with the Server/TGS-key: " + "\n");
		System.out.println("   > Plaintext:   " + plaintext + "\n\n");
		
		keyClientServer = extractBetweenTags(plaintext, "[START_KEY]", "[END_KEY]");
		ivClientServer =  extractBetweenTags(plaintext, "[START_IV]", "[END_IV]");
		
		System.out.println("Server extracts the Session key & IV: " + "\n");
		System.out.println("   > Key:         " + keyClientServer + "\n");
		System.out.println("   > IV:          " + ivClientServer + "\n\n");
		
		kerberos.serverHasKey = true;
	}
	
	
	private String extractBetweenTags(String m, String startTag, String endTag)
	{
		int startKeyIndex = m.indexOf(startTag) + startTag.length();
		int endKeyIndex = m.indexOf(endTag);
		if (!(startKeyIndex == -1 || endKeyIndex == -1))
			return m.substring(startKeyIndex, endKeyIndex);
		return null;
	}
	
	
	public void receiveClientRequest(String request, Client client) throws IOException
	{
		System.out.println("\n3. Server receives message from Client: " + "\n");
		System.out.println("   > Ciphertext:   " + request + "\n\n");

		String plaintext = encryptOrDecrypt(request, keyClientServer, ivClientServer, "Server_Decrypt_From_Client.txt", DES.processingMode.DECRYPT);
	
		System.out.println("4. Server decrypts message from Client: " + "\n");
		System.out.println("   > Plaintext:    " + plaintext + "\n\n");
		
		kerberos.pauseSimulation();
	}
	
	
	private void transmitRSAKeys(Client client)
	{
		
	}
	
}
