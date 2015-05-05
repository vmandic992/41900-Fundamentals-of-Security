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
		String s = "Authentication Server ------------------------------------------------------------------\n\n";
		s +=       " - Specializes in authenticating users and generating Tickets for submission to the TGS." + "\n";
		s +=       " - Already knows the TGS Symmetric Encryption Key and Initialization Vector (IV)." + "\n";
		s +=       " - Contains a database with 4 Users; each with a Username and Password." + "\n\n";
		s +=       " - User Database: " + printDatabase();
		return s;
	}
	
	
	
	private void createDatabase()
	{
		database.add(new Login("Andrew Scott" , "Scotty_22_from_BOX-HQ"));
		database.add(new Login("Vedran Mandic", "xx_MiamiHotlinePro_xx"));	
		database.add(new Login("Jeremiah Cruz", "MyLaptopBatteryIsFked"));	
		database.add(new Login("Jason D'Souza", "UTS_IT_SUPPORT_BEAST!"));	
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
		
		System.out.println("1. Message received by AS: " + "\n\n" + request + "\n\n");
		
		String clientUsername = request.substring(request.indexOf("USERNAME: ") + 10, request.length());	//extract username in request
		String clientPassword = findPassword(clientUsername);
		
		System.out.println("2. AS extracts the username from the request: \n\n" + " > USERNAME: " + clientUsername + "\n\n");
		System.out.println("3. AS then looks up username's corresponding password: \n\n" + " > PASSWORD: " + clientPassword + "\n\n");
		
		kerberos.pauseSimulation();
		
		respondToClient(clientUsername, clientPassword, client);
	}
	
	
	
	private void respondToClient(String clientUsername, String clientPassword, Client client) throws IOException
	{
		kerberos.printStepFour();
		
		String message = generateResponse(clientUsername, clientPassword);
		System.out.println("2. AS then encrypts the message with the client's password ('" + clientPassword + "') \n\n" + message + "\n\n");
		
		kerberos.pauseSimulation();
		
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
		
		message += "[START_TGS_KEY]" + symmetricKeyTGS + "[END_TGS_KEY]" + "\n";
		message += "[START_TGS_IV]" + initializationVectorTGS + "[END_TGS_IV]";
		
		System.out.println("1. AS generates a plaintext message containing the TGS encryption key & IV, plus a new TICKET: \n\n" + message + "\n\n");
		
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
