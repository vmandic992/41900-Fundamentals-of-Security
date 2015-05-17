public class Ticket 
{
	private String clientName;			//Name of the Client requesting this ticket
	private String validityPeriod;		//How long this ticket is valid for
	private String message;				//A small message contained in the ticket
	
	
	public Ticket(String clientName, String validityPeriod, String message)
	{
		this.clientName = clientName;
		this.validityPeriod = validityPeriod;	//fix validity period
		this.message = message;
	}
	
	
	/*	NOTE: Tags such as [START_TICKET], [/Name], etc... are all used for extracting elements from the Ticket string
	 * 	
	 * 	Example: If the TGS wants to validate the ticket's expiration date, it needs to extract the 'validityPeriod'
	 * 			 It does this by extracting whatever's between 'Expiration Date: ' and '[/Date]' 
	 * 
	 */
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
