public class Login 
{
	private String username;
	private String password;
	
	public Login(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	/*	- Receives a username
	 *  - Returns TRUE if username matches this login's username
	 * 
	 */
	public boolean matches(String username)
	{
		return this.username.equals(username);
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public String toString()
	{
		String s = "  > Username: " + username + "\n";
		s += "  > Password: " + password;
		return s;
	}
}
