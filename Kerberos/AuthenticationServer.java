import java.util.*;

public class AuthenticationServer 
{
	private LinkedList<Client> clients = new LinkedList<Client>();
	private Scanner scanner = new Scanner(System.in);
	
	private Client currentClient;	//represents the new logged-in client who wants to access the Server
	private String symmetricKeyTGS;
	
	public AuthenticationServer()
	{
		clients.add(new Client("Joe Client", "joePass"));
		displayClient();
		//createClients();
		//displayAddedClients();
	}
	/*
	private void createClients()
	{
		int clientID = 1;
		createMandatoryClient(clientID);
		
		String prompt = "\nAdd a client? [Hit (Enter) for 'Yes', type 'No' to stop]: ";
		String input = getStringInput(prompt);
		while (!input.equalsIgnoreCase("No"))
		{
			clients.add(new Client(++clientID));
			input = getStringInput(prompt);
		}
	}
	
	private void createMandatoryClient(int clientID)
	{
		System.out.println("\nCreate first client");
		clients.add(new Client(clientID));
	}
	
	private String getStringInput(String prompt)
	{
		System.out.print(prompt);
		return scanner.nextLine();
	}*/
	
	private void displayClient()
	{
		System.out.println("\n\n Clients added to AS database: \n");
		for (Client client: clients)
		{
			System.out.print(client.toString() + "\n");
		}
	}
	/*
	public Client lookUpClient(String username)
	{
		for(Client client: clients)
			if (client.matches(username))
			{
				currentClient = client;
				return client;
			}
		return null;
	}
	*/
	public void generateTicket()
	{
		//Ticket ticket = new Ticket();
	}
}
