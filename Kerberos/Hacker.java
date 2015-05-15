
public class Hacker 
{
	KerberosSystem kerberos;
	private String name;
	
	public Hacker(String name, KerberosSystem kerberos)
	{
		this.name = name;
		this.kerberos = kerberos;
	}
	
	public String toString()
	{
		String s = "\n\n MAN-IN-THE-MIDDLE ___________________________________________________________________________\n\n";
		s +=	   " - Wants to perform a Replay Attack" + "\n\n";
		s +=       " - Name: " + name + "\n";
		return s;
	}
}
