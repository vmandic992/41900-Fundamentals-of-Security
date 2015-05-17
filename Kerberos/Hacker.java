import java.io.IOException;
import java.text.ParseException;


public class Hacker 
{
	KerberosSystem kerberos;
	private String name;
	
	private String copiedMessage; //Holds the message sent from Client to TGS, which the hacker will copy (to replay later on)
	
	public Hacker(String name, KerberosSystem kerberos)
	{
		this.name = name;
		this.kerberos = kerberos;
		System.out.println(toString());
	}
	
	public String toString()
	{
		String s = "\n\n MAN-IN-THE-MIDDLE ___________________________________________________________________________\n\n";
		s +=	   " - Wants to perform a Replay Attack" + "\n\n";
		s +=       " - Name: " + name + "\n";
		return s;
	}
	
	
	/*	- Hacker takes a copy of the message between Client and TGS
	 */
	public void copyTransmission(String copiedMessage)
	{
		this.copiedMessage = copiedMessage;
		
		String s = "\n********************************************************************************************************\n";
		s += 	     "******************* (((ATTACK))): HACKER HAS COPIED THE CLIENT'S MESSAGE TO THE TGS: *******************\n";
		s +=         "********************************************************************************************************\n";
		
		System.out.println(s);
		
		kerberos.pauseSimulation();
	}
	
	
	/*	- Hacker forwards the copied message to the TGS, pretending to be the Client
	 */
	public void doReplayAttack() throws IOException, ParseException
	{
		kerberos.printAttack();
		
		String s = " > " + name + " (the MITM), will now replay the Client's message to the TGS. \n\n";
		s +=	   " > His aim is to fool the TGS to send him a Client/Server session key. \n\n";
		s +=	   " > The message he copied from earlier: \n\n" + copiedMessage;
		
		System.out.println(s + "\n\n");
		
		kerberos.pauseSimulation();
		
		//TGS receives the copied message and a reference to the Hacker object
		kerberos.TGS.receiveReplayedRequest(copiedMessage, this);
	}
	
	
	public void receiveSessionKey(String key)
	{
		String s = "\n\n > " + name + " receives the encrypted session key: \n\n" + key + "\n\n";
		s +=	   " > HOWEVER, he cannot decrypt it because he does not know the TGS-Key \n\n";
		s +=	   " > Therefore, the attack was ultimately unsuccessful.";
		System.out.println(s);
	}
}
