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
		String s = "[TICKET_START]:" + "\n";
		s += 	   " - Client Username: " + clientName + "\n";
		s +=       " - Validity Period: " + validityPeriod + "\n";
		s +=       " - Message:         " + message + "\n";
		s +=	   "[TICKET_END]";
		return s;
	}
}
