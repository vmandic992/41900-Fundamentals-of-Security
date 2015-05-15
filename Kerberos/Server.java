import java.io.IOException;

public class Server 
{
	private String serverName;
	private String service;
	
	private String keyServerTGS;
	private String ivServerTGS;
	
	private String keyClientServer; //will be randomly generated
	private String ivClientServer; //will be randomly generated
	
	private String blockCipherMode;
	private KerberosSystem kerberos;

	
	public Server(String serverName, String service, String blockCipherMode, KerberosSystem kerberos,
				  String keyServerTGS, String ivServerTGS) 
	{
		this.serverName = serverName;
		this.service = service;
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		this.keyServerTGS = keyServerTGS;
		this.ivServerTGS = ivServerTGS;
		System.out.println(toString());
	}
	
	
	public String toString()
	{
		String s = "\n\n RESOURCE SERVER ___________________________________________________________________________\n\n";
		s +=       " - Provides a service for Clients." + "\n\n";
		s +=       " - Name:    " + serverName + "\n";
		s +=       " - Service: " + service + "\n\n";
		s +=	   " - Configured with the TGS-Server Key and TGS-Server IV (to use with the TGS)" + "\n";
		s +=	   "    > Server/TGS-KEY: " + keyServerTGS + "\n";
		s +=	   "    > Server/TGS-IV:  " + ivServerTGS + "\n\n";
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
		System.out.println("Server receives encrypted Client/Server-Key and Client/Server-IV: " + "\n");
		System.out.println("   > Ciphertext:  " + encryptedKey + "\n\n");

		String plaintext = encryptOrDecrypt(encryptedKey, keyServerTGS, ivServerTGS, "Server_Decrypt_From_TGS.txt", DES.processingMode.DECRYPT);
		
		System.out.println("Server decrypts message with the Server/TGS-Key ('" + keyServerTGS + "') & Server/TGS-IV ('" + ivServerTGS + "'): "
						    + " - See 'SERVER_DECRYPT_FROM_TGS.txt' \n");
		
		System.out.println("   > Plaintext:   " + plaintext + "\n\n");
		
		keyClientServer = extractBetweenTags(plaintext, "[START_KEY]", "[END_KEY]");
		ivClientServer =  extractBetweenTags(plaintext, "[START_IV]", "[END_IV]");
		
		System.out.println("Server extracts the Client/Server-Key & Client/Server-IV: " + "\n");
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
	
		System.out.println("4. Server decrypts message with the Client/Server-Key ('" + keyClientServer + "')"
				+ " & Client/Server-IV ('" + ivClientServer + "') - See 'SERVER_DECRYPT_FROM_CLIENT.txt' \n");
		System.out.println("   > Plaintext:    " + plaintext + "\n\n");
		
		kerberos.pauseSimulation();
		
		transmitRSAKeys(client);
	}
	
	
	private void transmitRSAKeys(Client client) throws IOException
	{
		kerberos.printStepThirteen();
		
		RSA rsa = new RSA();
		String message = rsa.toString();
		
		System.out.println("1. Server generates a new RSA key pair: " + "\n\n" + rsa.toString() + "\n\n");
		
		System.out.println("2. Server encrypts key pair with Client/Server-Key & Client/Server-IV and sends it to Client: "
				+ "- See 'SERVER_ENCRYPT_TO_CLIENT.txt'" + "\n");
		String encryptedMessage = encryptOrDecrypt(message, keyClientServer, ivClientServer, "Server_Encrypt_To_Client.txt", DES.processingMode.ENCRYPT);
		System.out.println(encryptedMessage + "\n\n");		
		
		kerberos.pauseSimulation();
		
		client.receiveRSAKeys(encryptedMessage);
	}
}
