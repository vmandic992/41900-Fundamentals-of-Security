import java.util.*;
import java.io.IOException;

public class AuthenticationServer 
{
	private String symmetricKeyTGS = "MyAwesomeTripleDESKey";
	private String initializationVectorTGS = "IP0M1S55";
	
	private String initializationVectorAS = "YTC45132";
	
	private String blockCipherMode;
	private KerberosSystem kerberos;

	private static Calendar c = Calendar.getInstance();
	
	LinkedList<Login> database = new LinkedList<Login>();
	
	
	
	public AuthenticationServer(String blockCipherMode, KerberosSystem kerberos) 
	{
		this.kerberos = kerberos;
		this.blockCipherMode = blockCipherMode;
		createDatabase();
		System.out.println(toString());
	}
	
	public String toString()
	{
		String s = "Authentication Server \n\n";
		s +=       " - Database: " + printDatabase();
		//include keys/IVs
		return s;
	}
	
	private void createDatabase()
	{
		database.add(new Login("Andrew Scott", "Yippee ki-yay mthrfkr"));
		database.add(new Login("Vedran Mandic", "VedranMandicPassword1"));		
	}
	
	public String printDatabase()
	{
		String s = "\n";
		for (Login l : database)
		{
			s += l.toString() + "\n\n";
		}
		return s;
	}
	
	private String findPassword(String username)
	{
		for (Login l : database)
			if (l.matches(username))
				return l.getPassword();
		return null;
	}
	
	public void receiveRequest(String request, Client client) throws IOException
	{
		kerberos.printStepThree();
		
		System.out.println("Message received by AS: " + "\n\n" + request + "\n\n");
		
		String clientUsername = request.substring(request.indexOf("USERNAME: ") + 10, request.length());	//extract username in request
		String clientPassword = findPassword(clientUsername);
		
		System.out.println("AS extracts the username from the request: \n\n" + " > USERNAME: " + clientUsername + "\n\n");
		System.out.println("AS then looks up username's corresponding password: \n\n" + " > PASSWORD: " + clientPassword);
		
		respondToClient(clientUsername, clientPassword, client);
	}
	
	private void respondToClient(String clientUsername, String clientPassword, Client client) throws IOException
	{
		kerberos.printStepFour();
		
		String message = generateResponse(clientUsername, clientPassword);
		System.out.println("AS then encrypts the message with the client's password ('" + clientPassword + "') \n\n" + message + "\n\n");
		client.receiveASResponse(message);
	}
	
	private String getDate()
	{
		String date = "" + c.get(Calendar.DATE) + "/";
		date += + (c.get(Calendar.MONTH) + 1) + "/";
		date += + c.get(Calendar.YEAR);
		return date;
	}
	
	private Ticket createTicket(String clientUsername, String date)
	{
		String validity = "Valid until end of: " + date;
		String ticketNote = "NOTE: Created by AS on " + date;
		return new Ticket(clientUsername, validity, ticketNote);
	}

	private String generateResponse(String clientUsername, String clientPassword) throws IOException
	{
		String date = getDate();;
		Ticket ticket = createTicket(clientUsername, date);
		
		String message = ticket.toString() + "\n";
		
		message += "[TGS KEY AND CBC-IV]:" + "\n";
		message += " - [START_KEY]" + symmetricKeyTGS;
		message += "[END_KEY][START_IV]" + initializationVectorTGS + "[END_IV]";
		
		System.out.println("AS generates a plaintext message containing the TGS encryption key & IV, plus a new TICKET: \n\n" + message + "\n\n");
		
		return encryptMessage(message, clientPassword);
	}
	
	private String encryptMessage(String m, String key) throws IOException
	{
		if (blockCipherMode.equals("CBC"))
			return (new TripleDES(key, initializationVectorAS, "TripleDESCapture.txt").processData(m, DES.blockCipherMode.CBC, DES.processingMode.ENCRYPT));
		else
			return (new TripleDES(key, null, "TripleDESCapture.txt").processData(m,  DES.blockCipherMode.ECB, DES.processingMode.ENCRYPT));
	}
}
