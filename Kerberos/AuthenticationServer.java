import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

public class AuthenticationServer 
{
	private String keyTGS = "MyAwesomeTripleDESKey";
	private String ivTGS = "IP0M1S55";
		
	private String blockCipherMode;
	private KerberosSystem kerberos;

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
		database.add(new Login("Jeremiah Cruz", "MyLaptopBatteryIsShit"));	
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
		for (Login login : database)
			if (login.matches(username))
				return login.getPassword();
		return null;
	}
	
	
	
	public void receiveRequest(String request, Client client) throws IOException
	{
		kerberos.printStepThree();
		
		System.out.println("1. Message received by AS: " + "\n\n" + request + "\n\n");
		
		String clientUsername = request.substring(request.indexOf("USERNAME: ") + 10, request.length());
		String clientPassword = findPassword(clientUsername);
		
		System.out.println("2. AS extracts the username from the request: \n\n" + " > USERNAME: " + clientUsername + "\n\n");
		System.out.println("3. AS then looks up username's corresponding password from its database: \n\n" + " > PASSWORD: " + clientPassword + "\n\n");
		
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
	
	
	
	private String generateResponse(String clientUsername, String clientPassword) throws IOException
	{
		String date = getExpiryDate();;
		Ticket ticket = createTicket(clientUsername, date);
		
		String message = ticket.toString() + "\n";
		
		message += "[START_TGS_KEY]" + keyTGS + "[END_TGS_KEY]" + "\n";
		message += "[START_TGS_IV]" + ivTGS + "[END_TGS_IV]";
		
		System.out.println("1. AS generates a plaintext message containing the TGS encryption key & IV, plus a new TICKET: \n\n" + message + "\n\n");
		
		return encryptMessageToClient(message, clientPassword);
	}
	
	
	private String getExpiryDate()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 2);					//current time + 2 hours = expiration
		String date = dateFormat.format(cal.getTime());

		return date;
	}
	
	
	
	private Ticket createTicket(String clientUsername, String date)
	{
		String validity = date;
		String ticketNote = "NOTE: Present this ticket to TGS.";
		return new Ticket(clientUsername, validity, ticketNote);
	}
	
	
	
	private String encryptMessageToClient(String m, String key) throws IOException
	{		
		return (new TripleDES(key, null, "AS_Encrypt_To_Client.txt").processData(m,  DES.blockCipherMode.ECB, DES.processingMode.ENCRYPT));
	}
}
