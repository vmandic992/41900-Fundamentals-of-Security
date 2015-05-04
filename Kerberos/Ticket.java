public class Ticket 
{
	private String clientName;
	private String validityPeriod;
	private String message;
	
	public Ticket(String clientName, String validityPeriod, String message)
	{
		this.clientName = clientName;
		this.validityPeriod = validityPeriod;
		this.message = message;
	}
	
	public String toString()
	{
		String s = "[START_TICKET]" + "\n";
		s +=	   " Ticket Details" + "\n";
		s += 	   " - Client Username: " + clientName + "\n";
		s +=       " - Validity Period: " + validityPeriod + "\n";
		s +=       " - Message:         " + message + "\n";
		s +=	   "[END_TICKET]";
		return s;
	}
}
