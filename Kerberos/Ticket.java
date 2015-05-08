public class Ticket 
{
	private String clientName;
	private String validityPeriod;
	private String message;
	
	public Ticket(String clientName, String validityPeriod, String message)
	{
		this.clientName = clientName;
		this.validityPeriod = validityPeriod;	//fix validity period
		this.message = message;
	}
	
	public String toString()
	{
		String s = "[START_TICKET]" + "\n";
		s +=	   " Ticket Details" + "\n";
		s += 	   " - Client Username: " + clientName + "[/Name]" + "\n";
		s +=       " - Issuer Message:  " + message + "[/Message]" + "\n";
		s +=       " - Expiration Date: " + validityPeriod + "[/Date]" + "\n";
		s +=	   "[END_TICKET]";
		return s;
	}
}
