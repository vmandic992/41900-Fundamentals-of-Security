import java.util.*;

public class Client 
{
	private String username;
	private String password;
	private Scanner scanner = new Scanner(System.in);
	
	public Client() 
	{
		username = SetLogin("Username");
		password = SetLogin("Password");
	}
	
	private String SetLogin(String attribute)
	{
		String input = GetStringInput("\n > " + attribute + ": ");
		while (input.equals(""))
		{
			System.out.print(" Error: You entered an empty " + attribute + "\n");
			input = GetStringInput("\n > " + attribute + ": ");
		}
		return input;
	}
	
	private String GetStringInput(String prompt)
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
