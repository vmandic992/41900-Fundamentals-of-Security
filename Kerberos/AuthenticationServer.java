import java.util.*;

public class AuthenticationServer 
{
	private LinkedList<Client> clients = new LinkedList<Client>();
	private Scanner scanner = new Scanner(System.in);
	
	public AuthenticationServer()
	{
		CreateClients();
		DisplayAddedClients();
	}
	
	private void CreateClients()
	{
		CreateMandatoryClient();
		
		String prompt = "\nAdd a client? [Hit (Enter) for 'Yes', type 'No' to stop]: ";
		String input = GetStringInput(prompt);
		while (!input.equalsIgnoreCase("No"))
		{
			clients.add(new Client());
			input = GetStringInput(prompt);
		}
	}
	
	private void CreateMandatoryClient()
	{
		System.out.println("\nCreate first client");
		clients.add(new Client());
	}
	
	private String GetStringInput(String prompt)
	{
		System.out.print(prompt);
		return scanner.nextLine();
	}
	
	private void DisplayAddedClients()
	{
		System.out.println("\n\n Clients added to AS database: \n");
		for (Client client: clients)
		{
			System.out.print(client.toString() + "\n");
		}
	}
}
