import java.io.IOException;
import java.util.*;
import java.sql.Timestamp;

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
		System.out.println("Client receives encrypted message from AS: " + "\n\n" + message + "\n\n");
		String plaintext = decryptASResponse(message);
		System.out.println("Client decrypts response with their password: " + "\n\n" + plaintext + "\n\n");
		sendTicketToTGS(plaintext);
	}
	
	private String decryptASResponse(String c) throws IOException
	{
		if (blockCipherMode.equals("CBC"))
			return (new TripleDES(password, initializationVectorAS, "TripleDESCapture2.txt").processData(c, DES.blockCipherMode.CBC, DES.processingMode.DECRYPT));
		else
			return (new TripleDES(password, null, "TripleDESCapture2.txt").processData(c, DES.blockCipherMode.ECB, DES.processingMode.DECRYPT));
	}
	
	private void sendTicketToTGS(String m)
	{
		String symmetricKeyTGS = "";
		String initializationVectorTGS = "";
		String timestamp = "";
		String serverName = "";
		String ticket = "";
	}
}
