import java.util.*;

public class Client 
{
	private String username;
	private String password;
	private Scanner scanner = new Scanner(System.in);
	
	public Client() 
	{
		username = setLogin("Username");
		password = setLogin("Password");
	}
	
	private String setLogin(String attribute)
	{
		String input = getStringInput("\n > " + attribute + ": ");
		while (input.equals(""))
		{
			System.out.print(" Error: You entered an empty " + attribute + "\n");
			input = getStringInput("\n > " + attribute + ": ");
		}
		return input;
	}
	
	private String getStringInput(String prompt)
	{
		System.out.print(prompt);
		return scanner.nextLine();
	}
	
	public String toString()
	{
		String s = " - Username: " + username + "\n";
		s += " - Password: " + password + "\n";
		return s;
	}
}
