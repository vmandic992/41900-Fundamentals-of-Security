import java.util.*;

public class AuthenticationServer 
{
	private LinkedList<Client> clients = new LinkedList<Client>();
	private Scanner scanner = new Scanner(System.in);
	
	public AuthenticationServer()
	{
		createClients();
		displayAddedClients();
	}
	
	private void createClients()
	{
		createMandatoryClient();
		
		String prompt = "\nAdd a client? [Hit (Enter) for 'Yes', type 'No' to stop]: ";
		String input = getStringInput(prompt);
		while (!input.equalsIgnoreCase("No"))
		{
			clients.add(new Client());
			input = getStringInput(prompt);
		}
	}
	
	private void createMandatoryClient()
	{
		System.out.println("\nCreate first client");
		clients.add(new Client());
	}
	
	private String getStringInput(String prompt)
	{
		System.out.print(prompt);
		return scanner.nextLine();
	}
	
	private void displayAddedClients()
	{
		System.out.println("\n\n Clients added to AS database: \n");
		for (Client client: clients)
		{
			System.out.print(client.toString() + "\n");
		}
	}
}
